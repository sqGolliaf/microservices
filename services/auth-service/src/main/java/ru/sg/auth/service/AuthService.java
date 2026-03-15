package ru.sg.auth.service;

import ru.sg.auth.dto.UserDTO;

public interface AuthService {
    void registration(UserDTO userDTO);
    void authorization();
}
