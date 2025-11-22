package com.waira.waira_v2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByNumeroIdentificacion(String numeroIdentificacion);
    
    Optional<Usuario> findByTelefono(String telefono);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE COALESCE(u.estadoCuenta, true) = true")
    long countUsuariosActivos();

    @Query("SELECT COUNT(DISTINCT u) FROM Usuario u JOIN u.roles r WHERE UPPER(r.nombreRol) IN :roles")
    long countUsuariosPorRoles(@Param("roles") List<String> roles);
}
