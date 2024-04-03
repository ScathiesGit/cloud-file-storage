package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    void createUser(User user);
}
