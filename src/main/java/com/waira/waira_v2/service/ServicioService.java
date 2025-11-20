package com.waira.waira_v2.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.waira.waira_v2.dto.CrearServicioDTO;
import com.waira.waira_v2.entity.Estado;
import com.waira.waira_v2.entity.Servicio;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.repository.EstadoRepository;
import com.waira.waira_v2.repository.ServicioRepository;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    public Servicio crearServicio(Usuario usuario, CrearServicioDTO dto) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario requerido");
        }
        if (dto.getNombreServicio() == null || dto.getNombreServicio().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre de servicio requerido");
        }
        if (usuario.getDireccion() == null) {
            throw new IllegalStateException("El usuario no tiene dirección asociada");
        }

        // Estados válidos para servicio: DISPONIBLE / NO DISPONIBLE
        Estado estado = estadoRepository
            .findByNombreEstadoAndTipoEstado("DISPONIBLE", "SERVICIO")
            .orElseThrow(() -> new IllegalArgumentException("Estado DISPONIBLE SERVICIO no definido"));

        Servicio servicio = new Servicio();
        servicio.setUsuario(usuario);
        servicio.setEstado(estado);
        servicio.setNombreServicio(dto.getNombreServicio());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setPrecio(dto.getPrecio());
        servicio.setDireccion(usuario.getDireccion());
        servicio.setDiasDuracion(dto.getDiasDuracion());
        servicio.setFechaPublicacion(new Date());
        servicio.setVistas(0);
        // imagenes, metodosPago, categorias quedan vacíos inicialmente
        return servicioRepository.save(servicio);
    }

    public List<Servicio> listarServiciosProveedor(Usuario usuario) {
        return servicioRepository.findByUsuario(usuario);
    }
}
