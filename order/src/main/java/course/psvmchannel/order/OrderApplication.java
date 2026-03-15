package course.psvmchannel.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableFeignClients
public class OrderApplication {

    private final OrderService orderService;

    public OrderApplication(OrderService orderService) {
        this.orderService = orderService;
    }

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @GetMapping("/doOrder")
    public String doOrder(@RequestParam String orderName) {
        return orderService.placeOrder(orderName);
    }
}

