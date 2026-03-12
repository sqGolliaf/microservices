package ru.sg.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.sg.order.dto.response.UserResponse;

@FeignClient(name = "user-service", url = "http://localhost:8081")
public interface UserServiceClient {

    @PostMapping("/users/me")
    UserResponse getOrCreateUser(
            @RequestHeader("X-USER-ID") String keycloakId,
            @RequestHeader("X-EMAIL") String email,
            @RequestHeader("X-USERNAME") String username
    );
}
