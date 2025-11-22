package com.waira.waira_v2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.Servicio;
import com.waira.waira_v2.entity.Usuario;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
    List<Servicio> findByUsuario(Usuario usuario);

    @Query("SELECT c.idCategoria, COUNT(s) FROM Servicio s JOIN s.categorias c WHERE UPPER(s.estado.nombreEstado) = 'DISPONIBLE' GROUP BY c.idCategoria")
    List<Object[]> countServiciosPorCategoria();

    @Query("SELECT sc.idSubcategoria, COUNT(s) FROM Servicio s JOIN s.subcategorias sc WHERE UPPER(s.estado.nombreEstado) = 'DISPONIBLE' GROUP BY sc.idSubcategoria")
    List<Object[]> countServiciosPorSubcategoria();
}
