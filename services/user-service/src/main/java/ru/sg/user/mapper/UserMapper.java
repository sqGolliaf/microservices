package ru.sg.user.mapper;

import org.springframework.stereotype.Component;
import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.entity.User;

@Component
public class UserMapper {

    public UserResponse toResponse(User user, Boolean isEnabled, String email, String username) {
        return new UserResponse(email, username, isEnabled, user.getBalance(), user.getTier(), user.getPreferences());
    }
}
