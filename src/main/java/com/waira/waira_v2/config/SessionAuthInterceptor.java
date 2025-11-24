package com.waira.waira_v2.config;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.waira.waira_v2.entity.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionAuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SessionAuthInterceptor.class);
    private static final List<String> PROTECTED_PREFIXES = List.of("/dashboard", "/provider-dashboard");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;

        if (!requiresProtection(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Usuario usuario = session != null ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (usuario == null) {
            logger.debug("Intento de acceso sin sesión a {}", path);
            response.sendRedirect(contextPath + "/");
            return false;
        }

        return true;
    }

    private boolean requiresProtection(String path) {
        return PROTECTED_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
