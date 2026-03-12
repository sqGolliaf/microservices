package ru.sg.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class MdcFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String sagaId = UUID.randomUUID().toString();
        String keycloakId = request.getHeader("X-USER-ID");
        String email = request.getHeader("X-EMAIL");

        MDC.put("sagaId", sagaId);
        MDC.put("keycloakId", keycloakId != null ? keycloakId : "ANONYMOUS");
        MDC.put("email", email != null ? email : "UNKNOWN");

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
