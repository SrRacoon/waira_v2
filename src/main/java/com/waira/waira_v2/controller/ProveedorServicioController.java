package com.waira.waira_v2.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.waira.waira_v2.dto.CrearServicioDTO;
import com.waira.waira_v2.entity.Categoria;
import com.waira.waira_v2.entity.Direccion;
import com.waira.waira_v2.entity.Servicio;
import com.waira.waira_v2.entity.Subcategoria;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.repository.CategoriaRepository;
import com.waira.waira_v2.service.ServicioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/proveedor/servicios")
public class ProveedorServicioController {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorServicioController.class);

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping("/nuevo")
    public String nuevoServicio(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/";
        }
        if (!tieneRolProveedor(usuario)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("crearServicioDTO", new CrearServicioDTO());
        model.addAttribute("categoriasDisponibles", categoriaRepository.findAllWithSubcategorias());
        model.addAttribute("editMode", false);
        return "provider-servicio-form";
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String crearServicio(@ModelAttribute CrearServicioDTO crearServicioDTO,
                                @RequestParam(name = "imagenes", required = false) MultipartFile[] imagenes,
                                HttpSession session,
                                Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/";
        }
        if (!tieneRolProveedor(usuario)) {
            return "redirect:/dashboard";
        }
        List<MultipartFile> imagenesList = imagenes != null ? Arrays.asList(imagenes) : Collections.emptyList();
        logger.info("Proveedor {} intenta crear servicio con {} imágenes adjuntas", usuario.getEmail(), imagenesList.size());

        try {
            servicioService.crearServicio(usuario, crearServicioDTO, imagenesList);
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Error al crear servicio", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categoriasDisponibles", categoriaRepository.findAllWithSubcategorias());
            model.addAttribute("editMode", false);
            return "provider-servicio-form";
        }
    }

    @GetMapping("/{idServicio}/editar")
    public String editarServicio(@PathVariable("idServicio") Integer idServicio, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/";
        }
        if (!tieneRolProveedor(usuario)) {
            return "redirect:/dashboard";
        }
        try {
            Servicio servicio = servicioService.obtenerServicioProveedorPorId(usuario, idServicio);
            CrearServicioDTO dto = mapearDtoDesdeServicio(servicio);
            model.addAttribute("crearServicioDTO", dto);
            model.addAttribute("categoriasDisponibles", categoriaRepository.findAllWithSubcategorias());
            model.addAttribute("editMode", true);
            model.addAttribute("servicioId", idServicio);
            return "provider-servicio-form";
        } catch (Exception e) {
            logger.error("Error al cargar servicio {} para edición", idServicio, e);
            return "redirect:/dashboard?section=servicios";
        }
    }

    @PostMapping(value = "/{idServicio}/actualizar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String actualizarServicio(@PathVariable("idServicio") Integer idServicio,
                                     @ModelAttribute CrearServicioDTO crearServicioDTO,
                                     @RequestParam(name = "imagenes", required = false) MultipartFile[] imagenes,
                                     HttpSession session,
                                     Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/";
        }
        if (!tieneRolProveedor(usuario)) {
            return "redirect:/dashboard";
        }
        List<MultipartFile> imagenesList = imagenes != null ? Arrays.asList(imagenes) : Collections.emptyList();
        try {
            servicioService.actualizarServicio(usuario, idServicio, crearServicioDTO, imagenesList);
            return "redirect:/dashboard?section=servicios";
        } catch (Exception e) {
            logger.error("Error al actualizar servicio {}", idServicio, e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categoriasDisponibles", categoriaRepository.findAllWithSubcategorias());
            model.addAttribute("editMode", true);
            model.addAttribute("servicioId", idServicio);
            return "provider-servicio-form";
        }
    }

    @DeleteMapping("/{idServicio}")
    @ResponseBody
    public ResponseEntity<?> eliminarServicio(@PathVariable("idServicio") Integer idServicio, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión");
        }
        if (!tieneRolProveedor(usuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para esta acción");
        }
        try {
            servicioService.eliminarServicio(usuario, idServicio);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar servicio {}", idServicio, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo eliminar el servicio");
        }
    }

    private boolean tieneRolProveedor(Usuario usuario) {
        if (usuario.getRoles() == null) return false;
        return usuario.getRoles().stream().anyMatch(r -> {
            if (r == null || r.getNombreRol() == null) return false;
            String nombre = r.getNombreRol().trim().toUpperCase();
            return nombre.contains("PROVEEDOR") || nombre.contains("OPERADOR");
        });
    }

    private CrearServicioDTO mapearDtoDesdeServicio(Servicio servicio) {
        CrearServicioDTO dto = new CrearServicioDTO();
        dto.setNombreServicio(servicio.getNombreServicio());
        dto.setDescripcion(servicio.getDescripcion());
        dto.setPrecio(servicio.getPrecio());
        dto.setDiasDuracion(servicio.getDiasDuracion());

        Direccion direccion = servicio.getDireccion();
        if (direccion != null) {
            dto.setTipoVia(direccion.getTipoVia());
            dto.setNumero(direccion.getNumero());
            dto.setComplemento(direccion.getComplemento());
            dto.setBarrio(direccion.getBarrio());
            dto.setCiudad(direccion.getCiudad());
        }

        if (servicio.getCategorias() != null) {
            dto.getCategoriasIds().addAll(servicio.getCategorias().stream()
                    .map(Categoria::getIdCategoria)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        if (servicio.getSubcategorias() != null) {
            dto.getSubcategoriasIds().addAll(servicio.getSubcategorias().stream()
                    .map(Subcategoria::getIdSubcategoria)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
