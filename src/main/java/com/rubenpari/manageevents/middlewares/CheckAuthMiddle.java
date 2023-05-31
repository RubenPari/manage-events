package com.rubenpari.manageevents.middlewares;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@SuppressWarnings("NullableProblems")
public class CheckAuthMiddle implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(request.getRequestURI().equals("/auth/login") || request.getRequestURI().equals("/auth/login") || request.getRequestURI().equals("/auth/login"))) {
            if (request.getSession().getAttribute("accessToken") == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthenticated");
                return false;
            }
        }
        return true;
    }
}