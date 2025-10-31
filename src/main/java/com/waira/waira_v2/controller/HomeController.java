package com.waira.waira_v2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.repository.PermisoRepository;
import com.waira.waira_v2.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;

    public HomeController(PermisoRepository permisoRepository, UsuarioRepository usuarioRepository) {
        this.permisoRepository = permisoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String index() {
        return "index"; 
    }

    @GetMapping("/temporal")
    public String temporal(HttpSession session, Model model) {
        Object usuarioObj = session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuarioObj);

        if (usuarioObj instanceof Usuario) {
            Usuario u = (Usuario) usuarioObj;
            if (u.getRol() != null && "ADMIN".equalsIgnoreCase(u.getRol().getNombreRol())) {
                // Intentar cargar permisos si el usuario es Administrador
                try {
                    // Permiso.administrador apunta a Administrador entidad con idUsuario
                    model.addAttribute("permisos", permisoRepository.findByAdministradorIdUsuario(u.getIdUsuario()));
                } catch (Exception ex) {
                    // No fallar la vista por problemas al cargar permisos; registrar en consola
                    System.err.println("No se pudieron cargar permisos: " + ex.getMessage());
                }
            }
        }

        return "temporal";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    @GetMapping("/delete-account")
    public String deleteAccount(Integer id, HttpSession session, Model model) {
        Object usuarioObj = session != null ? session.getAttribute("usuarioLogueado") : null;
        if (usuarioObj == null || !(usuarioObj instanceof Usuario)) {
            return "redirect:/";
        }

        Usuario sessionUser = (Usuario) usuarioObj;

        boolean allowed = false;
        if (sessionUser.getIdUsuario() != null && sessionUser.getIdUsuario().equals(id)) {
            allowed = true;
        } else if (sessionUser.getRol() != null && "ADMIN".equalsIgnoreCase(sessionUser.getRol().getNombreRol())) {
            allowed = true;
        }

        if (!allowed) {
            return "redirect:/";
        }

        try {
            usuarioRepository.deleteById(id);
            if (session != null) session.invalidate();
            return "redirect:/";
        } catch (Exception ex) {
            model.addAttribute("deleteError", "No se pudo eliminar la cuenta: " + ex.getMessage());
            model.addAttribute("usuario", sessionUser);
            return "temporal";
        }
    }

    @GetMapping("/registro-usuario-temp")
    public String registroUsuarioTemp() {
        return "registro-usuario-temp";
    }
}
