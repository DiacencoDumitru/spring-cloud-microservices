package course.psvmchannel.gateway.auth;

import java.util.List;
import java.util.stream.Collectors;

import course.psvmchannel.gateway.security.GatewayUserDetailsService;
import course.psvmchannel.gateway.security.JwtProperties;
import course.psvmchannel.gateway.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final GatewayUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthController(GatewayUserDetailsService userDetailsService,
                          JwtService jwtService,
                          JwtProperties jwtProperties) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/token")
    public AuthResponse token(@RequestBody AuthRequest request) {
        try {
            UserDetails principal = userDetailsService.loadUserByUsername(request.getUsername());
            if (!principal.getPassword().equals(request.getPassword())) {
                throw new BadCredentialsException("Invalid credentials");
            }
            String jwt = jwtService.generateToken(principal);
            List<String> roles = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return new AuthResponse("Bearer", jwt, jwtProperties.getExpirationSeconds(), roles);
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException();
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    private static class UnauthorizedException extends RuntimeException {
    }
}
