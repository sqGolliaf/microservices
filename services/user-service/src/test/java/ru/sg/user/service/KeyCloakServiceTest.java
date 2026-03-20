package ru.sg.user.service;

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class KeyCloakServiceTest {

    @Autowired
    KeycloakService service;

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    private UsersResource users() {
        return keycloak.realm(realm).users();
    }


    @Test
    public void success_delete_all() {
        var list = users().list();
        list.forEach(it -> {
            System.out.println(it.getUsername() + " " + it.getId() + " " + it.getEmail());
        });
    }
}
