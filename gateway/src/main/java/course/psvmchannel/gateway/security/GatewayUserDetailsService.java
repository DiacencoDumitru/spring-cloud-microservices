package course.psvmchannel.gateway.security;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GatewayUserDetailsService implements UserDetailsService {

    private final Map<String, UserDetails> users;

    public GatewayUserDetailsService(AuthUsersProperties properties) {
        this.users = properties.getUsers().stream()
                .collect(Collectors.toUnmodifiableMap(
                        AuthUsersProperties.UserRecord::getUsername,
                        GatewayUserDetailsService::buildUser
                ));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("Unknown username: " + username);
        }
        return user;
    }

    private static UserDetails buildUser(AuthUsersProperties.UserRecord record) {
        List<SimpleGrantedAuthority> authorities = record.getRoles().stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return User.withUsername(record.getUsername())
                .password(record.getPassword())
                .authorities(authorities)
                .build();
    }
}
