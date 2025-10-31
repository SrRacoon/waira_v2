package com.waira.waira_v2.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waira.waira_v2.dto.AdministradorRegistroDto;
import com.waira.waira_v2.dto.AuthResponse;
import com.waira.waira_v2.dto.ClienteRegistroDto;
import com.waira.waira_v2.dto.LoginDto;
import com.waira.waira_v2.dto.ProveedorRegistroDto;
import com.waira.waira_v2.dto.RegistroUsuarioDto;
import com.waira.waira_v2.entity.Administrador;
import com.waira.waira_v2.entity.Cliente;
import com.waira.waira_v2.entity.Direccion;
import com.waira.waira_v2.entity.Proveedor;
import com.waira.waira_v2.entity.Rol;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.service.UsuarioService;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final com.waira.waira_v2.dao.UsuarioDao usuarioDao;
    private final com.waira.waira_v2.dao.ClienteDao clienteDao;
    private final com.waira.waira_v2.dao.AdministradorDao administradorDao;
    private final com.waira.waira_v2.dao.RolDao rolDao;
    private final com.waira.waira_v2.dao.DireccionDao direccionDao;

    public UsuarioServiceImpl(com.waira.waira_v2.dao.UsuarioDao usuarioDao,
            com.waira.waira_v2.dao.ClienteDao clienteDao,
            com.waira.waira_v2.dao.AdministradorDao administradorDao,
            com.waira.waira_v2.dao.RolDao rolDao,
            com.waira.waira_v2.dao.DireccionDao direccionDao) {
        this.usuarioDao = usuarioDao;
        this.clienteDao = clienteDao;
        this.administradorDao = administradorDao;
        this.rolDao = rolDao;
        this.direccionDao = direccionDao;
    }

    @Override
    public AuthResponse registrarUsuario(RegistroUsuarioDto dto) {
        if (usuarioDao.existsByEmail(dto.getEmail())) {
            return new AuthResponse(null, dto.getEmail(), "Email ya registrado");
        }

        java.util.Optional<Rol> r = rolDao.findByNombreRol(dto.getNombreRol());
        if (r.isEmpty()) {
            return new AuthResponse(null, dto.getEmail(), "Rol no encontrado");
        }

        if (dto.getContraseña() == null || dto.getConfirmContraseña() == null || !dto.getContraseña().equals(dto.getConfirmContraseña())) {
            return new AuthResponse(null, dto.getEmail(), "Las contraseñas no coinciden");
        }

        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setApellido(dto.getApellido());
        u.setTelefono(dto.getTelefono());
        u.setEmail(dto.getEmail());
        u.setContrasena(dto.getContraseña());
        u.setRol(r.get());

        Usuario saved = usuarioDao.save(u);
        return new AuthResponse(saved.getIdUsuario(), saved.getEmail(), "Registrado");
    }

    @Override
    public AuthResponse registrarCliente(ClienteRegistroDto dto) {
        if (usuarioDao.existsByEmail(dto.getEmail())) {
            return new AuthResponse(null, dto.getEmail(), "Email ya registrado");
        }
        Rol rol = rolDao.findByNombreRol("CLIENTE").orElse(null);
        if (rol == null) {
            return new AuthResponse(null, dto.getEmail(), "Rol no encontrado");
        }
        
        String lengthErr = validateUserFields(dto.getNombre(), dto.getApellido(), dto.getTelefono(), dto.getEmail(), dto.getContrasena());
        if (lengthErr != null) {
            return new AuthResponse(null, dto.getEmail(), lengthErr);
        }

        if (dto.getContrasena() == null || dto.getConfirmContrasena() == null || !dto.getContrasena().equals(dto.getConfirmContrasena())) {
            return new AuthResponse(null, dto.getEmail(), "Las contraseñas no coinciden");
        }
        

        Cliente c = new Cliente();
        c.setNombre(dto.getNombre());
        c.setApellido(dto.getApellido());
        c.setTelefono(dto.getTelefono());
        c.setEmail(dto.getEmail());
        c.setContrasena(dto.getContrasena());
        c.setRol(rol);

        Cliente saved = clienteDao.save(c);
        return new AuthResponse(saved.getIdUsuario(), saved.getEmail(), "Cliente registrado");
    }

    // Por terminar
    @Override
    public AuthResponse registrarAdministrador(AdministradorRegistroDto dto) {
        if (usuarioDao.existsByEmail(dto.getEmail())) {
            return new AuthResponse(null, dto.getEmail(), "Email ya registrado");
        }
        Rol rol = rolDao.findByNombreRol("ADMIN").orElse(null);
        if (rol == null) {
            return new AuthResponse(null, dto.getEmail(), "Rol no encontrado");
        }

        String lengthErr = validateUserFields(dto.getNombre(), dto.getApellido(), dto.getTelefono(), dto.getEmail(), dto.getContrasena());
        if (lengthErr != null) {
            return new AuthResponse(null, dto.getEmail(), lengthErr);
        }

        // validar que contraseña y confirmación coincidan
        if (dto.getContrasena() == null || dto.getConfirmContrasena() == null || !dto.getContrasena().equals(dto.getConfirmContrasena())) {
            return new AuthResponse(null, dto.getEmail(), "Las contraseñas no coinciden");
        }

        Administrador a = new Administrador();
        a.setNombre(dto.getNombre());
        a.setApellido(dto.getApellido());
        a.setTelefono(dto.getTelefono());
        a.setEmail(dto.getEmail());
        a.setContrasena(dto.getContrasena());
        a.setRol(rol);

        Administrador saved = administradorDao.save(a);
        return new AuthResponse(saved.getIdUsuario(), saved.getEmail(), "Administrador registrado");
    }

    @Override
    public AuthResponse login(LoginDto dto) {
        java.util.Optional<Usuario> u = usuarioDao.findByEmail(dto.getEmail());
        if (u.isEmpty()) {
            return new AuthResponse(null, dto.getEmail(), "Credenciales inválidas");
        }

        Usuario user = u.get();
        if (!user.getContrasena().equals(dto.getContrasena())) {
            return new AuthResponse(null, dto.getEmail(), "Credenciales inválidas");
        }

        return new AuthResponse(user.getIdUsuario(), user.getEmail(), "Login OK");
    }

    // Por terminar
    @Override
    public AuthResponse registrarProveedor(ProveedorRegistroDto dto) {
        if (usuarioDao.existsByEmail(dto.getEmail())) {
            return new AuthResponse(null, dto.getEmail(), "Email ya registrado");
        }
        Rol rol = rolDao.findByNombreRol("PROVEEDOR").orElse(null);
        if (rol == null) {
            return new AuthResponse(null, dto.getEmail(), "Rol no encontrado");
        }

        String lengthErr = validateUserFields(dto.getNombre(), dto.getApellido(), dto.getTelefono(), dto.getEmail(), dto.getContrasena());
        if (lengthErr != null) {
            return new AuthResponse(null, dto.getEmail(), lengthErr);
        }
        
        if (dto.getContrasena() == null || dto.getConfirmContrasena() == null || !dto.getContrasena().equals(dto.getConfirmContrasena())) {
            return new AuthResponse(null, dto.getEmail(), "Las contraseñas no coinciden");
        }

    Direccion direccion = new Direccion();
    direccion.setTipoVia("");
    direccion.setNumero("");
    direccion.setComplemento("");
    direccion.setBarrio(dto.getDireccion());
    direccion.setCiudad("");

    Direccion savedDir = direccionDao.save(direccion);

        Proveedor p = new Proveedor();
        p.setNombre(dto.getNombre());
        p.setApellido(dto.getApellido());
        p.setTelefono(dto.getTelefono());
        p.setEmail(dto.getEmail());
        p.setContrasena(dto.getContrasena());
        p.setRazonSocial(dto.getRazonSocial());
        Integer nitValue;
        try {
            nitValue = Integer.valueOf(dto.getNit());
        } catch (NumberFormatException ex) {
            return new AuthResponse(null, dto.getEmail(), "NIT inválido");
        }
        p.setNit(nitValue);
    p.setDireccion(savedDir);
    p.setRol(rol);

        Usuario saved = usuarioDao.save(p);
        return new AuthResponse(saved.getIdUsuario(), saved.getEmail(), "Proveedor registrado");
    }

    private String validateUserFields(String nombre, String apellido, String telefono, String email, String contrasena) {
        if (nombre == null || nombre.length() == 0) return "Nombre es requerido";
        if (apellido == null || apellido.length() == 0) return "Apellido es requerido";
        if (telefono == null || telefono.length() == 0) return "Teléfono es requerido";
        if (email == null || email.length() == 0) return "Email es requerido";
        if (contrasena == null || contrasena.length() == 0) return "Contraseña es requerida";

        if (nombre.length() > 15) return "Nombre demasiado largo (máx 15)";
        if (apellido.length() > 15) return "Apellido demasiado largo (máx 15)";
        if (telefono.length() > 15) return "Teléfono demasiado largo (máx 15)";
        if (email.length() > 100) return "Email demasiado largo (máx 100)";
        if (contrasena.length() > 100) return "Contraseña demasiado larga (máx 100)";

        return null;
    }
}
