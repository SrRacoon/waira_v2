package com.waira.waira_v2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.waira.waira_v2.entity.Rol;
import com.waira.waira_v2.repository.RolRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository rolRepository;

    public DataInitializer(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            seedRole("CLIENTE");
            seedRole("ADMIN");
            seedRole("PROVEEDOR");
        } catch (DataAccessException dae) {
            logger.warn("No se pudo inicializar datos por error en la base de datos: {}. Se omite el seed.", dae.getMessage());
        } catch (Exception ex) {
            logger.warn("Error inesperado al inicializar datos: {}. Se omite el seed.", ex.getMessage());
        }
    }

    private void seedRole(String nombre) {
        rolRepository.findByNombreRol(nombre).orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol(nombre);
            return rolRepository.save(r);
        });
    }
}
