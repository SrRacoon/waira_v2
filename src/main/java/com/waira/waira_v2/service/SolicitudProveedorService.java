package com.waira.waira_v2.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.waira.waira_v2.dto.SolicitudProveedorDTO;
import com.waira.waira_v2.entity.Direccion;
import com.waira.waira_v2.entity.Estado;
import com.waira.waira_v2.entity.SolicitudProveedor;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.repository.DireccionRepository;
import com.waira.waira_v2.repository.EstadoRepository;
import com.waira.waira_v2.repository.SolicitudProveedorRepository;

@Service
public class SolicitudProveedorService {
    
    @Autowired
    private SolicitudProveedorRepository solicitudProveedorRepository;
    
    @Autowired
    private EstadoRepository estadoRepository;
    
    @Autowired
    private DireccionRepository direccionRepository;
    
    public SolicitudProveedor crearSolicitud(Usuario usuario, SolicitudProveedorDTO dto) {
        // Permitir nueva solicitud solo si no existe o si la última fue DENEGADA
        // Buscar la última solicitud por usuario (id más alto)
        solicitudProveedorRepository.findAll().stream()
            .filter(s -> s.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            .sorted((a, b) -> b.getIdSolicitud().compareTo(a.getIdSolicitud()))
            .findFirst()
            .ifPresent(s -> {
                String estado = s.getEstado() != null ? s.getEstado().getNombreEstado() : "";
                if (!"DENEGADA".equalsIgnoreCase(estado)) {
                    throw new IllegalArgumentException("Ya tienes una solicitud de proveedor pendiente o aprobada");
                } else {
                    // Si es DENEGADA, eliminar la anterior para permitir crear una nueva
                    solicitudProveedorRepository.delete(s);
                }
            });
        
        // Crear y guardar dirección
        Direccion direccion = new Direccion();
        direccion.setTipoVia(dto.getTipoVia());
        direccion.setNumero(dto.getNumero());
        direccion.setComplemento(dto.getComplemento());
        direccion.setBarrio(dto.getBarrio());
        direccion.setCiudad(dto.getCiudad());
        direccion = direccionRepository.save(direccion);
        
        // Obtener estado "PENDIENTE" de tipo "SOLICITUD"
        Estado estadoPendiente = estadoRepository.findByNombreEstadoAndTipoEstado("PENDIENTE", "SOLICITUD")
            .orElseThrow(() -> new IllegalArgumentException("Estado PENDIENTE de tipo SOLICITUD no existe"));
        
        // Crear solicitud
        SolicitudProveedor solicitud = new SolicitudProveedor();
        solicitud.setUsuario(usuario);
        solicitud.setDireccion(direccion);
        solicitud.setNit(dto.getNit());
        solicitud.setRazonSocial(dto.getRazonSocial());
        solicitud.setEstado(estadoPendiente);
        solicitud.setFechaSolicitud(new Date());
        
        return solicitudProveedorRepository.save(solicitud);
    }

    public boolean usuarioTieneSolicitud(Usuario usuario) {
        return solicitudProveedorRepository.findByUsuario(usuario).isPresent();
    }

    public String obtenerEstadoSolicitud(Usuario usuario) {
        return solicitudProveedorRepository.findByUsuario(usuario)
                .map(s -> s.getEstado() != null ? s.getEstado().getNombreEstado() : null)
                .orElse(null);
    }
}
