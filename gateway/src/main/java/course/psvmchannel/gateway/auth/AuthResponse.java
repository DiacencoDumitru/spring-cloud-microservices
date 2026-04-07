package course.psvmchannel.gateway.auth;

import java.util.List;

public class AuthResponse {

    private final String tokenType;
    private final String accessToken;
    private final long expiresIn;
    private final List<String> roles;

    public AuthResponse(String tokenType, String accessToken, long expiresIn, List<String> roles) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.roles = roles;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public List<String> getRoles() {
        return roles;
    }
}
