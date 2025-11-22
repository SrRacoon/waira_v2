package com.waira.waira_v2.controller;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waira.waira_v2.dto.AdminMetricsDTO;
import com.waira.waira_v2.entity.Estado;
import com.waira.waira_v2.entity.SolicitudProveedor;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.repository.CompraRepository;
import com.waira.waira_v2.repository.EstadoRepository;
import com.waira.waira_v2.repository.ReservaRepository;
import com.waira.waira_v2.repository.RolRepository;
import com.waira.waira_v2.repository.ServicioRepository;
import com.waira.waira_v2.repository.SolicitudProveedorRepository;
import com.waira.waira_v2.repository.TipoIdentificacionRepository;
import com.waira.waira_v2.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    @Autowired
    private SolicitudProveedorRepository solicitudRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ServicioRepository servicioRepo;

    @Autowired
    private ReservaRepository reservaRepo;

    @Autowired
    private EstadoRepository estadoRepo;

    @Autowired
    private TipoIdentificacionRepository tipoIdentRepo;

    @Autowired
    private RolRepository rolRepo;

    @Autowired
    private CompraRepository compraRepo;

    private static final List<String> ROLES_OPERADORES = List.of("PROVEEDOR", "OPERADOR", "OPERADOR_TURISTICO");
    private static final List<String> ESTADOS_RESERVA_ACTIVOS = List.of("CONFIRMADA", "CONFIRMADO", "APROBADA", "APROBADO", "ACTIVA", "ACTIVO");

    @GetMapping("/solicitudes")
    public ResponseEntity<?> listarSolicitudes() {
        List<Map<String, Object>> list = solicitudRepo.findAll().stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getIdSolicitud());
            m.put("nit", s.getNit());
            m.put("razonSocial", s.getRazonSocial());
            m.put("estado", s.getEstado() != null ? s.getEstado().getNombreEstado() : null);
            m.put("usuarioId", s.getUsuario() != null ? s.getUsuario().getIdUsuario() : null);
            m.put("usuarioNombre", s.getUsuario() != null ? s.getUsuario().getNombres() + " " + s.getUsuario().getApellidos() : null);
            m.put("usuarioEmail", s.getUsuario() != null ? s.getUsuario().getEmail() : null);
            m.put("fecha", s.getFechaSolicitud());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios() {
        List<Map<String, Object>> list = usuarioRepo.findAll().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();

            m.put("id", u.getIdUsuario());
            m.put("nombres", u.getNombres());
            m.put("apellidos", u.getApellidos());
            m.put("email", u.getEmail());
            m.put("numeroIdentificacion", u.getNumeroIdentificacion());
            m.put("tipoIdentificacion", u.getTipoIdentificacion() != null ? u.getTipoIdentificacion().getNombreTipoDocumento() : null);
            m.put("razonSocial", u.getRazonSocial());
            m.put("estadoCuenta", u.getEstadoCuenta());
            m.put("roles", u.getRoles() != null ? u.getRoles().stream().map(r -> r.getNombreRol()).collect(Collectors.toList()) : Collections.emptyList());
            
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/roles")
    public ResponseEntity<?> listarRoles() {
        var roles = rolRepo.findAll().stream().map(r -> Map.of(
            "id", r.getIdRol(),
            "nombre", r.getNombreRol()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/usuarios/{id}/estado")
    public ResponseEntity<?> actualizarEstadoCuenta(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Usuario u = usuarioRepo.findById(id).orElse(null);
        if (u == null) return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
        Object val = body.get("estadoCuenta");
        if (val == null) return ResponseEntity.badRequest().body(Map.of("error", "estadoCuenta requerido"));
        boolean nuevo = Boolean.parseBoolean(val.toString());
        u.setEstadoCuenta(nuevo);
        usuarioRepo.save(u);
        return ResponseEntity.ok(Map.of("success", true, "estadoCuenta", nuevo));
    }

    @PostMapping("/usuarios/{id}/roles")
    public ResponseEntity<?> actualizarRoles(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Usuario u = usuarioRepo.findById(id).orElse(null);
        if (u == null) return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
        Object rolesObj = body.get("roles");
        if (!(rolesObj instanceof List<?> lista)) return ResponseEntity.badRequest().body(Map.of("error", "roles debe ser lista"));
        var nuevos = lista.stream().map(Object::toString).collect(Collectors.toList());
        var entidades = rolRepo.findAll().stream()
            .filter(r -> nuevos.contains(r.getNombreRol()))
            .collect(Collectors.toList());
        u.setRoles(entidades);
        usuarioRepo.save(u);
        return ResponseEntity.ok(Map.of("success", true, "roles", nuevos));
    }

    @PostMapping("/solicitudes/{id}/cambiar-estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String nuevo = body.getOrDefault("estado", "").toUpperCase();
        SolicitudProveedor sol = solicitudRepo.findById(id).orElse(null);
        if (sol == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Solicitud no encontrada"));
        }

        Estado estado = estadoRepo.findByNombreEstadoAndTipoEstado(nuevo, "SOLICITUD")
                .orElse(null);
        if (estado == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Estado inválido para solicitudes"));
        }

        sol.setEstado(estado);
        solicitudRepo.save(sol);

        if ("CONFIRMADA".equalsIgnoreCase(nuevo) || "APROBADA".equalsIgnoreCase(nuevo) || "APROBADO".equalsIgnoreCase(nuevo)) {
            Usuario u = sol.getUsuario();
            if (u != null) {

                u.setDireccion(sol.getDireccion());

                tipoIdentRepo.findByNombreTipoDocumento("NIT").ifPresent(u::setTipoIdentificacion);
                u.setNumeroIdentificacion(sol.getNit());

                u.setRazonSocial(sol.getRazonSocial());

                rolRepo.findByNombreRol("PROVEEDOR").ifPresent(r -> {
                    if (u.getRoles() == null || u.getRoles().stream().noneMatch(rr -> rr.getNombreRol().equals(r.getNombreRol()))) {
                        var roles = u.getRoles();
                        if (roles == null) roles = new java.util.ArrayList<>();
                        roles.add(r);
                        u.setRoles(roles);
                    }
                });

                usuarioRepo.save(u);
            }
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/ingresos-mes")
    public ResponseEntity<?> ingresosDelMes() {
        double total = calcularIngresosMesActual();
        return ResponseEntity.ok(Map.of("ingresosMes", total));
    }

    @GetMapping("/metrics")
    public ResponseEntity<AdminMetricsDTO> obtenerMetricasDashboard() {
        long totalUsuarios = usuarioRepo.count();
        long totalOperadores = usuarioRepo.countUsuariosPorRoles(ROLES_OPERADORES);
        long totalPaquetes = servicioRepo.count();
        long reservasActivas = reservaRepo.countReservasActivas(ESTADOS_RESERVA_ACTIVOS);
        double ingresosMes = calcularIngresosMesActual();

        AdminMetricsDTO dto = new AdminMetricsDTO(
            totalUsuarios,
            totalOperadores,
            totalPaquetes,
            reservasActivas,
            ingresosMes
        );
        return ResponseEntity.ok(dto);
    }

    private double calcularIngresosMesActual() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date inicio = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date fin = cal.getTime();
        Double total = compraRepo.sumaIngresosConfirmadosEntre(inicio, fin);
        return total != null ? total : 0.0;
    }
}
