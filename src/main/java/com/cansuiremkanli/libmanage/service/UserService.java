package com.cansuiremkanli.libmanage.service;
import com.cansuiremkanli.libmanage.data.dto.UserCreateDTO;
import com.cansuiremkanli.libmanage.data.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserCreateDTO userCreateDTO);
    UserDTO getUserById(UUID id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(UUID id, UserDTO userDTO);
    void deleteUser(UUID id);
}

