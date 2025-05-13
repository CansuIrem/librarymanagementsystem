package com.cansuiremkanli.libmanage.data.dto;

import com.cansuiremkanli.libmanage.core.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private Role role;
}
