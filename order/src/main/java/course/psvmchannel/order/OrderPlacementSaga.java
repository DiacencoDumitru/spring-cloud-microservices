package course.psvmchannel.order;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import course.psvmchannel.order.dto.ReservationRequest;
import course.psvmchannel.order.dto.ReservationResponse;

import feign.FeignException;

@Service
public class OrderPlacementSaga {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacementSaga.class);

    private final InMemoryOrderStore orderStore;
    private final InventoryFeignClient inventoryClient;
    private final NotificationServiceFeignClient notificationClient;

    public OrderPlacementSaga(
            InMemoryOrderStore orderStore,
            InventoryFeignClient inventoryClient,
            NotificationServiceFeignClient notificationClient) {
        this.orderStore = orderStore;
        this.inventoryClient = inventoryClient;
        this.notificationClient = notificationClient;
    }

    public String placeOrder(String orderName) {
        UUID orderId = UUID.randomUUID();
        orderStore.createPending(orderId, orderName);

        String reservationId = null;
        try {
            ReservationResponse reservation = inventoryClient.reserve(new ReservationRequest(orderName));
            reservationId = reservation.getReservationId();
        } catch (FeignException e) {
            orderStore.cancel(orderId);
            if (e.status() == 409) {
                return "Out of stock for SKU: " + orderName;
            }
            log.warn("Inventory reserve failed: {}", e.getMessage());
            return "Order failed: inventory unavailable (" + e.status() + ")";
        } catch (Exception e) {
            orderStore.cancel(orderId);
            log.warn("Inventory reserve failed", e);
            return "Order failed: " + e.getMessage();
        }

        try {
            notificationClient.sendNotification("Order " + orderId + " placed: " + orderName);
        } catch (Exception e) {
            log.warn("Notification step failed, compensating inventory", e);
            safeReleaseInventory(reservationId);
            orderStore.cancel(orderId);
            return "Order rolled back: notification failed (" + e.getMessage() + ")";
        }

        try {
            orderStore.complete(orderId);
        } catch (Exception e) {
            log.error("Local complete failed, running compensations", e);
            safeCompensateNotification(orderId);
            safeReleaseInventory(reservationId);
            orderStore.cancel(orderId);
            return "Order rolled back: could not finalize locally";
        }

        return "Order completed: " + orderName + " (id=" + orderId + ", reservation=" + reservationId + ")";
    }

    private void safeReleaseInventory(String reservationId) {
        if (reservationId == null) {
            return;
        }
        try {
            inventoryClient.release(reservationId);
        } catch (Exception e) {
            log.error("Compensation: failed to release inventory {}", reservationId, e);
        }
    }

    private void safeCompensateNotification(UUID orderId) {
        try {
            notificationClient.compensateNotification("Rollback order " + orderId);
        } catch (Exception e) {
            log.error("Compensation: failed to notify rollback for {}", orderId, e);
        }
    }
}
