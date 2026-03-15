package ru.sg.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.sg.paymentservice.dto.request.CreditRequest;
import ru.sg.paymentservice.dto.request.DebitRequest;

@FeignClient(
        name = "user-service",
        url = "${users.url}")
public interface UserServiceClient {

    @PostMapping("/users/{keycloakId}/debit")
    void debit(
            @PathVariable String keycloakId,
            @RequestBody DebitRequest request
    );

    @PostMapping("/users/{keycloakId}/credit")
    void credit(
            @PathVariable String keycloakId,
            @RequestBody CreditRequest request
    );
}
