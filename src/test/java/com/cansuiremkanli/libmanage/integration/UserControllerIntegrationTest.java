package com.cansuiremkanli.libmanage.integration;

import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.data.dto.RegisterRequest;
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

        UserDTO userDTO = new UserDTO();
        userDTO.setName("New Patron");
        userDTO.setEmail("newpatron@example.com");
        userDTO.setPhoneNumber("05551112233");
        userDTO.setRole(Role.PATRON);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newpatron@example.com"))
                .andExpect(jsonPath("$.role").value("PATRON"));
    }

    @Test
    void createUser_WithPatronRole_ShouldReturn403() throws Exception {
        String token = registerAndGetToken("patron@example.com", "PATRON");

        UserDTO userDTO = new UserDTO();
        userDTO.setName("Unauthorized Create");
        userDTO.setEmail("unauthorized@example.com");
        userDTO.setPhoneNumber("05551112233");
        userDTO.setRole(Role.PATRON);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied. Access Denied"));
    }

    @Test
    void getUser_WithLibrarianRole_ShouldSucceed() throws Exception {
        String token = registerAndGetToken("librarian@example.com", "LIBRARIAN");

        // Önce başka bir kullanıcı oluştur
        UserDTO newUser = new UserDTO();
        newUser.setName("Target User");
        newUser.setEmail("target@example.com");
        newUser.setPhoneNumber("05551112233");
        newUser.setRole(Role.PATRON);

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

        UserDTO target = new UserDTO();
        target.setName("Secret Patron");
        target.setEmail("secret@example.com");
        target.setPhoneNumber("05551112233");
        target.setRole(Role.PATRON);

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
        // 1 kullanıcı daha ekleyelim görünür olsun diye
        String token = registerAndGetToken("admin@example.com", "LIBRARIAN");

        UserDTO newUser = new UserDTO();
        newUser.setName("Second User");
        newUser.setEmail("second@example.com");
        newUser.setPhoneNumber("05551110000");
        newUser.setRole(Role.PATRON);

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

        // 1. Yeni kullanıcı oluştur
        UserDTO user = new UserDTO();
        user.setName("UserToUpdate");
        user.setEmail("updatable@example.com");
        user.setPhoneNumber("05551112233");
        user.setRole(Role.PATRON);

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        // 2. Güncelleme isteği
        user.setName("Updated Name");

        mockMvc.perform(put("/api/users/" + id)
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
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

        // Başka kullanıcı oluştur
        UserDTO target = new UserDTO();
        target.setName("TargetUser");
        target.setEmail("targetupdate@example.com");
        target.setPhoneNumber("05551113333");
        target.setRole(Role.PATRON);

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(target)))
                .andExpect(status().isOk())
                .andReturn();

        UUID targetId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        // Farklı PATRON login olur
        String patronToken = registerAndGetToken("intruder@example.com", "PATRON");

        // Güncelleme denemesi
        target.setName("HACKED");

        mockMvc.perform(put("/api/users/" + targetId)
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(target)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied. Access Denied"));
    }

    @Test
    void deleteUser_WithLibrarianRole_ShouldSucceed() throws Exception {
        String librarianToken = registerAndGetToken("librarian@delete.com", "LIBRARIAN");

        // Silinecek kullanıcıyı oluştur
        UserDTO userToDelete = new UserDTO();
        userToDelete.setName("To Be Deleted");
        userToDelete.setEmail("delete@example.com");
        userToDelete.setPhoneNumber("05551114444");
        userToDelete.setRole(Role.PATRON);

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToDelete)))
                .andExpect(status().isOk())
                .andReturn();

        UUID userId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        // Silme işlemi
        mockMvc.perform(delete("/api/users/" + userId)
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_WithPatronRole_ShouldReturn403() throws Exception {
        String librarianToken = registerAndGetToken("creator@delete.com", "LIBRARIAN");

        // Hedef kullanıcıyı oluştur
        UserDTO target = new UserDTO();
        target.setName("No Rights");
        target.setEmail("nopriv@example.com");
        target.setPhoneNumber("05551115555");
        target.setRole(Role.PATRON);

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(target)))
                .andExpect(status().isOk())
                .andReturn();

        UUID targetId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        // PATRON kullanıcı silmeye çalışır
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

}

