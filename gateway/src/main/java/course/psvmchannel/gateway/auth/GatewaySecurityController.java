package course.psvmchannel.gateway.auth;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gateway")
public class GatewaySecurityController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", authentication.getName());
        result.put("roles", extractRoles(authentication));
        return result;
    }

    @GetMapping("/admin/ping")
    public Map<String, Object> adminPing(Authentication authentication) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("username", authentication.getName());
        result.put("roles", extractRoles(authentication));
        return result;
    }

    private List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
