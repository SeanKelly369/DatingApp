package org.meeboo.constant;

import java.util.List;

public class Authority {

    private Authority() {

    }
    public static final List<String> USER_AUTHORITIES =  List.of("user:read");

    public static final List<String> HR_AUTHORITIES = List.of("user:read", "user:update");
    public static final List<String> MANAGER_AUTHORITIES = List.of("user:read", "user:update");
    public static final List<String> ADMIN_AUTHORITIES = List.of("user:read", "user:create", "user:update");
    public static final List<String> SUPER_ADMIN_AUTHORITIES = List.of("user:read", "user:create", "user:update", "user:delete");
}
