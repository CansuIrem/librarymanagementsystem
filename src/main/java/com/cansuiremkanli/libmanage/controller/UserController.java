package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.data.dto.UserDTO;
import com.cansuiremkanli.libmanage.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations related to user management")
public class UserController {

    private final UserService userService;

    //  Sadece LIBRARIAN kullanıcı oluşturabilir
    @PreAuthorize("hasRole('LIBRARIAN')")
    @PostMapping
    @Operation(summary = "Create a new user", description = "Allows librarians to create a new user.")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createUser(userDTO));
    }

    //  LIBRARIAN herkesin verisini görebilir, PATRON sadece kendi verisini görebilir
    @PreAuthorize("hasRole('LIBRARIAN') or #id == principal.id")
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves user details by ID.")
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    //  Tüm kullanıcıları listelemek sadece LIBRARIAN yetkisinde
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all users.")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //  Kullanıcı sadece kendi verisini güncelleyebilir, LIBRARIAN herkesi güncelleyebilir
    @PreAuthorize("hasRole('LIBRARIAN') or #id == principal.id")
    @PutMapping("/{id}")
    @Operation(summary = "Update user information", description = "Allows librarians to update user details.")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    //  Sadece LIBRARIAN kullanıcı silebilir
    @PreAuthorize("hasRole('LIBRARIAN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Allows librarians to delete a user.")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
