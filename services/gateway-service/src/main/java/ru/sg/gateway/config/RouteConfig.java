package ru.sg.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {

    private final ServicesConfig servicesConfig;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, GatewayFilter addUserHeadersFilter) {
        RouteLocatorBuilder.Builder routes = builder.routes();

        servicesConfig.getUserServices().forEach((serviceId, info) ->
                routes.route(serviceId, r -> r
                        .path(info.getPath())
                        .filters(f -> f.filter(addUserHeadersFilter))
                        .uri(info.getUrl()))
        );

        return routes.build();
    }

    @Bean
    public GatewayFilter addUserHeadersFilter() {
        return (exchange, chain) -> exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(auth -> mutateExchangeWithUserHeaders(exchange, auth)
                        .flatMap(chain::filter))
                .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<ServerWebExchange> mutateExchangeWithUserHeaders(ServerWebExchange exchange, Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            String userId = jwt.getSubject();
            String username = jwt.getClaimAsString("preferred_username");
            String email = jwt.getClaimAsString("email");

            ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

            if (userId != null) {
                requestBuilder.header("X-USER-ID", userId);
            }
            if (username != null) {
                requestBuilder.header("X-USERNAME", username);
            }
            if (email != null) {
                requestBuilder.header("X-EMAIL", email);
            }

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(requestBuilder.build())
                    .build();

            return Mono.just(mutatedExchange);
        }

        return Mono.just(exchange);
    }
}
