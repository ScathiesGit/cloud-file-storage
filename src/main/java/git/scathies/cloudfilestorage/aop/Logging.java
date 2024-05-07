package git.scathies.cloudfilestorage.aop;

import git.scathies.cloudfilestorage.model.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class Logging {

    @Pointcut("@within(org.springframework.stereotype.Controller)")
    public void isController() {
    }

    @Pointcut("within(git.scathies.cloudfilestorage.service.UserServiceImpl)")
    public void isUserService() {
    }

    @Before("isController() " +
            "&& within(git.scathies.cloudfilestorage.controller.AuthenticationController)" +
            "&& execution(public * processRegistration(*))" +
            "&& args(user)")
    public void logBeforeProcessRegistration(JoinPoint joinPoint, User user) {
        log.info("{}. Start process registration username: {}",
                joinPoint.getTarget().getClass().getSimpleName(), user.getUsername());
    }

    @AfterReturning("isController() " +
            "&& within(git.scathies.cloudfilestorage.controller.AuthenticationController)" +
            "&& execution(public * processRegistration(*))" +
            "&& args(user)")
    public void logAfterProcessRegistration(JoinPoint joinPoint, User user) {
        log.info("{}. Registration successful for username: {}, id: {}.",
                joinPoint.getTarget().getClass().getSimpleName(), user.getUsername(), user.getId());
    }

    @AfterThrowing(value = "isController()" +
            "&& within (git.scathies.cloudfilestorage.controller.AuthenticationController)" +
            "&& execution(public * processRegistration(*))" +
            "&& args(user)",
            throwing = "e")
    public void logThrowingProcessRegistration(JoinPoint joinPoint, User user, Throwable e) {
        log.error("{}. Failed registration process for username: {}",
                joinPoint.getTarget().getClass().getSimpleName(), user.getUsername(), e);
    }

    @AfterThrowing(value = "isUserService()" +
            "&& execution(public * loadUserByUsername(*))" +
            "&& args(username)",
            throwing = "e")
    public void logUserNotFound(JoinPoint joinPoint, String username, Throwable e) {
        log.error("{}. Username: {} not found.",
                joinPoint.getTarget().getClass().getSimpleName(), username, e);
    }
}
