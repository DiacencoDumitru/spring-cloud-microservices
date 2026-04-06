package course.psvmchannel.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class UsersApplication {

    private final List<String> users = List.of("John", "Mike", "Anna");

    public static void main(String[] args) {
        SpringApplication.run(UsersApplication.class, args);
    }

    @GetMapping("/users")
    public List<String> getUsers(
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) Integer limit
    ) {
        List<String> filteredUsers = users;

        if (prefix != null && !prefix.isBlank()) {
            String normalizedPrefix = prefix.toLowerCase();
            filteredUsers = users.stream()
                    .filter(user -> user.toLowerCase().startsWith(normalizedPrefix))
                    .collect(Collectors.toList());
        }

        if (limit == null || limit < 1) {
            return filteredUsers;
        }

        return filteredUsers.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{id}")
    public String userById(@PathVariable int id) {
        try {
            return users.get(id - 1);
        } catch (RuntimeException e) {
            return "User not found";
        }
    }
}
