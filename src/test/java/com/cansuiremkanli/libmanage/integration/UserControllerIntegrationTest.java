package com.cansuiremkanli.libmanage.integration;

import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.data.dto.RegisterRequest;
import com.cansuiremkanli.libmanage.data.dto.UserCreateDTO;
import com.cansuiremkanli.libmanage.data.dto.UserDTO;
import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.data.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private String registerAndGetToken(String email, String role) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Admin User");
        request.setEmail(email);
        request.setPassword("password123");
        request.setPhoneNumber("05551112233");
        request.setRole(Role.valueOf(role));

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void createUser_WithLibrarianRole_ShouldSucceed() throws Exception {
        String token = registerAndGetToken("librarian@example.com", "LIBRARIAN");

        UserCreateDTO userCreateDTO = toUserCreateDTO("New User", "newpatron@example.com", "05551112233", Role.PATRON, "password123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newpatron@example.com"))
                .andExpect(jsonPath("$.role").value("PATRON"));
    }

    @Test
    void createUser_WithPatronRole_ShouldReturn403() throws Exception {
        String token = registerAndGetToken("patron@example.com", "PATRON");

        UserCreateDTO userCreateDTO = toUserCreateDTO("New User", "newpatron@example.com", "05551112233", Role.PATRON, "password123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied. Access Denied"));
    }

    @Test
    void getUser_WithLibrarianRole_ShouldSucceed() throws Exception {
        String token = registerAndGetToken("librarian@example.com", "LIBRARIAN");

        UserCreateDTO newUser = toUserCreateDTO("New User", "target@example.com", "05551112233", Role.PATRON, "password123");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(get("/api/users/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("target@example.com"));
    }

    @Test
    void getOwnUser_WithPatronRole_ShouldSucceed() throws Exception {
        String token = registerAndGetToken("self@example.com", "PATRON");

        UUID id = userRepository.findByEmail("self@example.com").orElseThrow().getId();

        mockMvc.perform(get("/api/users/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("self@example.com"));
    }

    @Test
    void getOtherUser_WithPatronRole_ShouldReturn403() throws Exception {
        // LIBRARIAN ile bir kullanıcı oluştur
        String librarianToken = registerAndGetToken("librarian2@example.com", "LIBRARIAN");

        UserCreateDTO target = toUserCreateDTO("Secret Patron", "secret@example.com", "05551112233", Role.PATRON, "password");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + librarianToken)
                        .content(objectMapper.writeValueAsString(target)))
                .andExpect(status().isOk())
                .andReturn();

        UUID targetId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        // PATRON login oluyor
        String patronToken = registerAndGetToken("nospy@example.com", "PATRON");

        mockMvc.perform(get("/api/users/" + targetId)
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied. Access Denied"));
    }

    @Test
    void getAllUsers_WithLibrarianRole_ShouldSucceed() throws Exception {
        String token = registerAndGetToken("admin@example.com", "LIBRARIAN");

        UserCreateDTO newUser = toUserCreateDTO("Second User", "second@example.com", "05551110000", Role.PATRON, "password");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)); // admin + second user
    }

    @Test
    void getAllUsers_WithPatronRole_ShouldReturn403() throws Exception {
        String token = registerAndGetToken("simple@example.com", "PATRON");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied. Access Denied"));
    }

    @Test
    void updateUser_WithLibrarianRole_ShouldSucceed() throws Exception {
        String librarianToken = registerAndGetToken("librarian@example.com", "LIBRARIAN");

        // 1. Yeni kullanıcı oluştur - UserCreateDTO kullanmalıyız!
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setName("UserToUpdate");
        createDTO.setEmail("updatable@example.com");
        createDTO.setPhoneNumber("05551112233");
        createDTO.setRole(Role.PATRON);
        createDTO.setPassword("password123"); // zorunlu alan

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        // 2. Güncelleme isteği - artık UserDTO ile yapabiliriz
        UserDTO updateDTO = new UserDTO();
        updateDTO.setId(id);
        updateDTO.setName("Updated Name");
        updateDTO.setEmail("updatable@example.com");
        updateDTO.setPhoneNumber("05551112233");
        updateDTO.setRole(Role.PATRON);

        mockMvc.perform(put("/api/users/" + id)
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }


    @Test
    void updateOwnUser_WithPatronRole_ShouldSucceed() throws Exception {
        String token = registerAndGetToken("patronupdate@example.com", "PATRON");

        User user = userRepository.findByEmail("patronupdate@example.com").orElseThrow();
        UUID id = user.getId();

        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setName("Self Updated");
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber("05550000000");
        dto.setRole(Role.PATRON);

        mockMvc.perform(put("/api/users/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Self Updated"));
    }

    @Test
    void updateOtherUser_WithPatronRole_ShouldReturn403() throws Exception {
        String librarianToken = registerAndGetToken("creator@example.com", "LIBRARIAN");

        // 1. Hedef kullanıcıyı oluştur - UserCreateDTO
        UserCreateDTO createTarget = new UserCreateDTO();
        createTarget.setName("TargetUser");
        createTarget.setEmail("targetupdate@example.com");
        createTarget.setPhoneNumber("05551113333");
        createTarget.setRole(Role.PATRON);
        createTarget.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTarget)))
                .andExpect(status().isOk())
                .andReturn();

        UUID targetId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        // 2. Giriş yapan farklı PATRON
        String patronToken = registerAndGetToken("intruder@example.com", "PATRON");

        // 3. Hedef kullanıcıya ait UserDTO güncelleme payload
        UserDTO targetUpdate = new UserDTO();
        targetUpdate.setId(targetId);
        targetUpdate.setName("HACKED");
        targetUpdate.setEmail("targetupdate@example.com");
        targetUpdate.setPhoneNumber("05551113333");
        targetUpdate.setRole(Role.PATRON);

        mockMvc.perform(put("/api/users/" + targetId)
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(targetUpdate)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied. Access Denied"));
    }


    @Test
    void deleteUser_WithLibrarianRole_ShouldSucceed() throws Exception {
        String librarianToken = registerAndGetToken("librarian@delete.com", "LIBRARIAN");

        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setName("To Be Deleted");
        createDTO.setEmail("delete@example.com");
        createDTO.setPhoneNumber("05551114444");
        createDTO.setRole(Role.PATRON);
        createDTO.setPassword("123456");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn();

        UUID userId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(delete("/api/users/" + userId)
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNoContent());
    }


    @Test
    void deleteUser_WithPatronRole_ShouldReturn403() throws Exception {
        String librarianToken = registerAndGetToken("creator@delete.com", "LIBRARIAN");

        UserCreateDTO createTarget = new UserCreateDTO();
        createTarget.setName("No Rights");
        createTarget.setEmail("nopriv@example.com");
        createTarget.setPhoneNumber("05551115555");
        createTarget.setRole(Role.PATRON);
        createTarget.setPassword("secretpass");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTarget)))
                .andExpect(status().isOk())
                .andReturn();

        UUID targetId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        String patronToken = registerAndGetToken("unauthorized@delete.com", "PATRON");

        mockMvc.perform(delete("/api/users/" + targetId)
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied. Access Denied"));
    }


    @Test
    void getUser_NonExistent_ShouldReturn404() throws Exception {
        String token = registerAndGetToken("librarian@404.com", "LIBRARIAN");

        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(get("/api/users/" + fakeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void updateUser_NonExistent_ShouldReturn404() throws Exception {
        String token = registerAndGetToken("librarian@404update.com", "LIBRARIAN");

        UUID fakeId = UUID.randomUUID();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(fakeId);
        userDTO.setName("Ghost");
        userDTO.setEmail("ghost@example.com");
        userDTO.setPhoneNumber("05551112233");
        userDTO.setRole(Role.PATRON);

        mockMvc.perform(put("/api/users/" + fakeId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void deleteUser_NonExistent_ShouldReturn404() throws Exception {
        String token = registerAndGetToken("librarian@404delete.com", "LIBRARIAN");

        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/" + fakeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }


    @Test
    void createUser_WithInvalidEmail_ShouldReturn400() throws Exception {
        String token = registerAndGetToken("librarian@valid.com", "LIBRARIAN");

        UserDTO invalidUser = new UserDTO();
        invalidUser.setName("Invalid Email");
        invalidUser.setEmail("not-an-email");
        invalidUser.setPhoneNumber("05551112233");
        invalidUser.setRole(Role.PATRON);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    private UserCreateDTO toUserCreateDTO(String name, String email, String phone, Role role, String password) {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName(name);
        dto.setEmail(email);
        dto.setPhoneNumber(phone);
        dto.setRole(role);
        dto.setPassword(password);
        return dto;
    }


}

