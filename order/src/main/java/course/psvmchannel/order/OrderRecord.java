package course.psvmchannel.order;

import java.util.UUID;

public class OrderRecord {

    private final UUID id;
    private final String orderName;
    private OrderStatus status;

    public OrderRecord(UUID id, String orderName, OrderStatus status) {
        this.id = id;
        this.orderName = orderName;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getOrderName() {
        return orderName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
