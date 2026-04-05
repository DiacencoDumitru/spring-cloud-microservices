package course.psvmchannel.order;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class InMemoryOrderStore {

    private final Map<UUID, OrderRecord> orders = new ConcurrentHashMap<>();

    public OrderRecord createPending(UUID id, String orderName) {
        OrderRecord record = new OrderRecord(id, orderName, OrderStatus.PENDING_SAGA);
        orders.put(id, record);
        return record;
    }

    public void complete(UUID id) {
        OrderRecord record = orders.get(id);
        if (record != null) {
            record.setStatus(OrderStatus.COMPLETED);
        }
    }

    public void cancel(UUID id) {
        OrderRecord record = orders.get(id);
        if (record != null) {
            record.setStatus(OrderStatus.CANCELLED);
        }
        orders.remove(id);
    }

    public Optional<OrderRecord> find(UUID id) {
        return Optional.ofNullable(orders.get(id));
    }
}
