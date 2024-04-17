package git.scathies.cloudfilestorage.integration;

import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.repository.UserRepository;
import git.scathies.cloudfilestorage.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Disabled
public class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User user = User.builder()
            .username("test_valid_username")
            .password("test_valid_pass")
            .build();

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll(userRepository.findAll());
    }

    @Test
    void givenUniqueUsernameWhenCreateUserThenFindById() {
        userService.createUser(user);

        var actualUser = userRepository.findById(user.getId());
        assertAll(
                () -> assertThat(actualUser).isPresent(),
                () -> assertThat(actualUser.get()).isEqualTo(user)
        );
    }

    @Test
    void givenNotUniqueUserNameWhenCreateUser() {
        userService.createUser(user);
        var userSameName = User.builder()
                .username(user.getUsername())
                .password("password")
                .build();

        assertThatThrownBy(
                () -> userService.createUser(userSameName)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void givenExistUsernameWhenLoadUserByUsernameThenReturnUser() {
        userService.createUser(user);

        var actualUser = (User) userService.loadUserByUsername(user.getUsername());

        assertThat(actualUser).isEqualTo(user);
    }

    @Test
    void givenNotExistUserNameWhenLoadUserByUserNameThenNotFoundException() {
        assertThatThrownBy(
                () -> userService.loadUserByUsername("not_exist_username")
        ).isInstanceOf(UsernameNotFoundException.class);
    }
}
