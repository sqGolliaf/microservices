package ru.sg.user.mapper;

import org.springframework.stereotype.Component;
import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.entity.User;

@Component
public class UserMapper {

    public UserResponse toResponse(
            User user,
            String email,
            String username,
            String keycloakId
    ) {
        return new UserResponse(
                user.getId(),
                keycloakId,
                email,
                username,
                user.getBalance(),
                user.getTier(),
                user.getPreferences()
        );
    }
}
