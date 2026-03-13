package ru.sg.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.function.*;

import java.util.Map;
import java.util.function.Function;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {

    private final ServicesConfig servicesConfig;

    @Bean
    RouterFunction<ServerResponse> gatewayRoutes() {
        var builder = route("dynamic-services");

        for (Map.Entry<String, ServicesConfig.ServiceInfo> entry : servicesConfig.getUserServices().entrySet()) {
            String path = entry.getValue().getPath();
            String url = entry.getValue().getUrl();

            builder = builder.route(RequestPredicates.path(path), http())
                    .before(uri(url))
                    .before(addUserHeaders());
        }

        return builder.build();
    }

    private Function<ServerRequest, ServerRequest> addUserHeaders() {
        return request -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
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
