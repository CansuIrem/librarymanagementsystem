
package com.cansuiremkanli.libmanage.data.repository;

import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.core.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test");
        user.setRole(Role.PATRON);
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("test@example.com");
        assertThat(result).isPresent();
    }
}
