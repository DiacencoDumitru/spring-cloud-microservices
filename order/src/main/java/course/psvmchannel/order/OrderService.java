package course.psvmchannel.order;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final NotificationServiceFeignClient feignClient;

    public OrderService(NotificationServiceFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyFallback")
    public String placeOrder(String orderName) {
        feignClient.sendNotification("You created order: " + orderName);
        return "Order was created: " + orderName;
    }

    @SuppressWarnings("unused")
    private String notifyFallback(String orderName, Throwable ex) {
        return "Notification service is unavailable. Order " + orderName + " was created without notification";
    }
}

