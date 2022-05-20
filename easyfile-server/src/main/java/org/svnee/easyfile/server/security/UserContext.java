package org.svnee.easyfile.server.security;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.NamedThreadLocal;

/**
 * User context (Transition scheme).
 *
 * @author svnee
 */
public final class UserContext {

    private UserContext() {
    }

    private static final ThreadLocal<User> USER_THREAD_LOCAL = new NamedThreadLocal<>("User");

    public static void setUserInfo(String username, String userRole) {
        USER_THREAD_LOCAL.set(new User(username, userRole));
    }

    public static String getUserName() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get()).map(User::getUsername).orElse("");
    }

    public static String getUserRole() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get()).map(User::getUserRole).orElse("");
    }

    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class User {

        private String username;

        private String userRole;

    }

}
