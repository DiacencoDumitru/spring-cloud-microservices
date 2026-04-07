package course.psvmchannel.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import course.psvmchannel.gateway.security.AuthUsersProperties;
import course.psvmchannel.gateway.security.JwtProperties;

@SpringBootApplication
@EnableZuulProxy
@EnableConfigurationProperties({JwtProperties.class, AuthUsersProperties.class})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
