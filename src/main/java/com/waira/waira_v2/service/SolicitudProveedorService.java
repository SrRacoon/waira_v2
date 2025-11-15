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
        // Verificar si el usuario ya tiene una solicitud pendiente o aprobada
        solicitudProveedorRepository.findByUsuario(usuario).ifPresent(s -> {
            throw new IllegalArgumentException("Ya tienes una solicitud de proveedor pendiente o aprobada");
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
}
