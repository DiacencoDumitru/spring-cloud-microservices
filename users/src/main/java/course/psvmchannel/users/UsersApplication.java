package course.psvmchannel.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class UsersApplication {

    // проинициализируем список пользователей
    private final List<String> users = List.of("Дима", "Виталик", "Денис");

    public static void main(String[] args) {
        SpringApplication.run(UsersApplication.class, args);
    }

    // верни список пользователей
    @GetMapping("/users")
    public List<String> getUsers() {
        return this.users;
    }

    // верни пользователя по id
    @GetMapping("/users/{id}")
    public String userById(@PathVariable int id) {
        //обработаем исключение если id выйдет за пределы коллекции
        try {
            return users.get(id - 1);
        } catch (RuntimeException e) {
            return "Нет такого пользователя";
        }
    }
}
