package course.psvmchannel.order;

import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class OrderService {

    private final OrderPlacementSaga orderPlacementSaga;

    public OrderService(OrderPlacementSaga orderPlacementSaga) {
        this.orderPlacementSaga = orderPlacementSaga;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "placeOrderFallback")
    public String placeOrder(String orderName) {
        return orderPlacementSaga.placeOrder(orderName);
    }

    @SuppressWarnings("unused")
    private String placeOrderFallback(String orderName, Throwable ex) {
        return "Temporary failure: notification path not available. Try again. (" + orderName + ")";
    }
}
