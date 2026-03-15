package ru.sg.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.*;
import org.springframework.security.web.server.header.ClearSiteDataServerHttpHeadersWriter;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@EnableRedisWebSession
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity,
                                                  ServerOAuth2AuthorizationRequestResolver requestResolver,
                                                  ServerOAuth2AuthorizedClientRepository auth2AuthorizedClientRepository,
                                                  ServerLogoutSuccessHandler logoutSuccessHandler,
                                                  ServerLogoutHandler logoutHandler,
                                                  CorsConfigurationSource corsConfigurationSource) {
        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeExchange(
                        authorizeExchangeSpec ->
                                authorizeExchangeSpec.pathMatchers(
                                                "/actuator/**",
                                                "/access-token/**",
                                                "/id-token")
                                        .permitAll()
                                        .anyExchange()
                                        .authenticated()
                ).oauth2Login(oAuth2Login ->
                        oAuth2Login.authorizationRequestResolver(requestResolver)
                                .authorizedClientRepository(auth2AuthorizedClientRepository)
                ).oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler)
                        .logoutHandler(logoutHandler))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    ServerOAuth2AuthorizationRequestResolver requestResolver(ReactiveClientRegistrationRepository repository) {
        var resolver = new DefaultServerOAuth2AuthorizationRequestResolver(repository);
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        return resolver;
    }

    @Bean
    ServerOAuth2AuthorizedClientRepository auth2AuthorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    @Bean
    ServerLogoutSuccessHandler logoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/test");
        return oidcLogoutSuccessHandler;
    }

    @Bean
    ServerLogoutHandler logoutHandler() {
        return new DelegatingServerLogoutHandler(
                new SecurityContextServerLogoutHandler(),
                new WebSessionServerLogoutHandler(),
                new HeaderWriterServerLogoutHandler(
                        new ClearSiteDataServerHttpHeadersWriter(ClearSiteDataServerHttpHeadersWriter.Directive.COOKIES)
                )
        );
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:8000");
        configuration.addAllowedHeader(CorsConfiguration.ALL);
        configuration.addAllowedMethod(CorsConfiguration.ALL);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
