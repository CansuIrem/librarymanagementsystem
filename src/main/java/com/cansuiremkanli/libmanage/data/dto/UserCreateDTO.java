package com.cansuiremkanli.libmanage.data.dto;

import com.cansuiremkanli.libmanage.core.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserCreateDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[+]?[0-9]{10,15}$",
            message = "Phone number must be valid (e.g., +905551112233 or 05551112233)"
    )
    private String phoneNumber;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Password is required")
    private String password;
}
