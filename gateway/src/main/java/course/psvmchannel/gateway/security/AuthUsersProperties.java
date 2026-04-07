package course.psvmchannel.gateway.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.auth")
public class AuthUsersProperties {

    private List<UserRecord> users = new ArrayList<>();

    public List<UserRecord> getUsers() {
        return users;
    }

    public void setUsers(List<UserRecord> users) {
        this.users = users;
    }

    public static class UserRecord {
        private String username;
        private String password;
        private List<String> roles = new ArrayList<>();

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
