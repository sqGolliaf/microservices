package ru.sg.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.function.Function;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class RouteConfig {

    @Value("${services.order-service.url:http://order-service:8080}")
    private String orderServiceUrl;

    @Bean
    RouterFunction<ServerResponse> gatewayRoutes() {
        return route("order-service")
                .route(RequestPredicates.path("/api/v1/orders/**"), http())
                .before(uri(orderServiceUrl))
                .before(addUserHeaders())
                .build();
    }

    private Function<ServerRequest, ServerRequest> addUserHeaders() {
        return request -> {
            Authentication auth =
                    SecurityContextHolder.getContext().getAuthentication();

            if (auth instanceof JwtAuthenticationToken jwtAuth) {

                String userId = jwtAuth.getToken().getSubject();
                String username = jwtAuth.getToken().getClaim("preferred_username");
                String email = jwtAuth.getToken().getClaim("email");

                return ServerRequest.from(request)
                        .header("X-USER-ID", userId)
                        .header("X-USERNAME", username)
                        .header("X-EMAIL", email)
                        .build();
            }

            return request;
        };
    }
}
