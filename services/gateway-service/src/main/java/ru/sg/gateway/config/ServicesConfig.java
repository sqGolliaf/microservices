package ru.sg.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "services")
public class ServicesConfig {

    private Map<String, ServiceInfo> userServices = new HashMap<>();

    @Setter
    @Getter
    public static class ServiceInfo {
        private String url;
        private String path;
    }
}
