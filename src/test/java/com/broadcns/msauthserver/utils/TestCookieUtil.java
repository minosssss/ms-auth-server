package com.broadcns.msauthserver.utils;

import jakarta.servlet.http.Cookie;

public class TestCookieUtil {

    public static Cookie createTestCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }
}
