package com.waira.waira_v2.util;

import org.springframework.stereotype.Component;

import com.waira.waira_v2.entity.Rol;
import com.waira.waira_v2.entity.Usuario;

@Component
public class RoleHelper {

    public String determinarRolPrincipal(Usuario usuario) {
        if (usuario == null || usuario.getRoles() == null) {
            return "CLIENTE";
        }
        boolean admin = false;
        boolean proveedor = false;
        for (Rol r : usuario.getRoles()) {
            if (r == null || r.getNombreRol() == null) continue;
            String upper = r.getNombreRol().trim().toUpperCase();
            if (upper.contains("ADMIN")) {
                admin = true;
            } else if (upper.contains("PROVEEDOR")) {
                proveedor = true;
            }
        }
        if (admin) return "ADMINISTRADOR";
        if (proveedor) return "PROVEEDOR";
        Rol primero = usuario.getRoles().stream()
            .filter(r -> r != null && r.getNombreRol() != null)
            .findFirst()
            .orElse(null);
        return primero != null ? primero.getNombreRol() : "CLIENTE";
    }

    public String listarRoles(Usuario usuario) {
        if (usuario == null || usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            return "Sin roles";
        }
        StringBuilder sb = new StringBuilder();
        for (Rol r : usuario.getRoles()) {
            if (r == null || r.getNombreRol() == null) continue;
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(r.getNombreRol().trim());
        }
        return sb.length() > 0 ? sb.toString() : "Sin roles";
    }
}
