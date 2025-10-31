package com.waira.waira_v2.service;

import com.waira.waira_v2.dto.AdministradorRegistroDto;
import com.waira.waira_v2.dto.ClienteRegistroDto;
import com.waira.waira_v2.dto.LoginDto;
import com.waira.waira_v2.dto.RegistroUsuarioDto;
import com.waira.waira_v2.dto.AuthResponse;
import com.waira.waira_v2.dto.ProveedorRegistroDto;

public interface UsuarioService {
    AuthResponse registrarUsuario(RegistroUsuarioDto dto);
    AuthResponse registrarCliente(ClienteRegistroDto dto);
    AuthResponse registrarAdministrador(AdministradorRegistroDto dto);
    AuthResponse login(LoginDto dto);
    AuthResponse registrarProveedor(ProveedorRegistroDto dto);
}
