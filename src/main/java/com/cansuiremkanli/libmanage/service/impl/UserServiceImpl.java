package com.cansuiremkanli.libmanage.service.impl;

import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.data.repository.UserRepository;
import com.cansuiremkanli.libmanage.data.dto.UserDTO;
import com.cansuiremkanli.libmanage.data.mapper.UserMapper;
import com.cansuiremkanli.libmanage.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating new user with email: {}", userDTO.getEmail());

        User user = userMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode("defaultPassword")); // Öneri: DTO ayrıştırılabilir
        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDTO getUserById(UUID id) {
        log.info("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new EntityNotFoundException("User not found");
                });

        return userMapper.toDto(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Override
    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for update. ID: {}", id);
                    return new EntityNotFoundException("User not found");
                });

        user.setName(userDTO.getName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(userDTO.getRole());

        User updated = userRepository.save(user);
        log.info("User updated successfully. ID: {}", updated.getId());

        return userMapper.toDto(updated);
    }

    @Override
    public void deleteUser(UUID id) {
        log.warn("Request to delete user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            log.warn("Delete failed. User not found: {}", id);
            throw new EntityNotFoundException("User not found");
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }
}
