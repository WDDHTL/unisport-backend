package com.unisport.common;

public class UserContext {

    public record UserSession(Long userId, Long schoolId) {}

    private static final ThreadLocal<UserSession> USER_HOLDER = new ThreadLocal<>();

    public static void setCurrentUser(Long userId, Long schoolId) {
        USER_HOLDER.set(new UserSession(userId, schoolId));
    }

    public static UserSession getCurrentUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        UserSession session = USER_HOLDER.get();
        return session != null ? session.userId() : null;
    }

    public static Long getSchoolId() {
        UserSession session = USER_HOLDER.get();
        return session != null ? session.schoolId() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
