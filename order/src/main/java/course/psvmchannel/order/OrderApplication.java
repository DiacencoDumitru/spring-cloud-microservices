package course.psvmchannel.order;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private NotificationServiceFeignClient feignClient;

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }


    // сделать заказ (передаём поле orderName)
    @GetMapping("/doOrder")
    public String doOrder(@RequestParam String orderName) {

        // в случае если возникнет исключение отправим сообщение
        try {
            feignClient.sendNotification("Вы сделали заказ " + orderName);
            return "Был сделан заказ " + orderName;
        } catch (Exception e) {
            return "Исключение. Оповещение не было отправлено";
        }
    }
}
