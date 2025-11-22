package com.waira.waira_v2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    @EntityGraph(attributePaths = {"subcategorias"})
    @Query("SELECT DISTINCT c FROM Categoria c")
    List<Categoria> findAllWithSubcategorias();
}
