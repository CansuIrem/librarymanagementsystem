package com.cansuiremkanli.libmanage.data.dto;

import com.cansuiremkanli.libmanage.core.enums.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
}

