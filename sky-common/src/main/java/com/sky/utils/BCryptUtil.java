package com.sky.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptUtil {// 加密密码
    private static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public static String encode(String password) {
        return encoder.encode(password);
    }
    public static boolean matches(String password, String encodedPassword) {
        return encoder.matches(password, encodedPassword);
    }

}

