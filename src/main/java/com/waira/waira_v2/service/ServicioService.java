package com.waira.waira_v2.service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.waira.waira_v2.dto.CategoriaTreeDTO;
import com.waira.waira_v2.dto.CrearServicioDTO;
import com.waira.waira_v2.dto.ExplorarPayloadDTO;
import com.waira.waira_v2.dto.ProveedorStatsDTO;
import com.waira.waira_v2.dto.ResenaDTO;
import com.waira.waira_v2.dto.ServicioDetalleDTO;
import com.waira.waira_v2.dto.ServicioExplorarDTO;
import com.waira.waira_v2.dto.SubcategoriaTreeDTO;
import com.waira.waira_v2.entity.Categoria;
import com.waira.waira_v2.entity.Direccion;
import com.waira.waira_v2.entity.Estado;
import com.waira.waira_v2.entity.Imagen;
import com.waira.waira_v2.entity.Reseña;
import com.waira.waira_v2.entity.Servicio;
import com.waira.waira_v2.entity.Subcategoria;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.repository.CategoriaRepository;
import com.waira.waira_v2.repository.DireccionRepository;
import com.waira.waira_v2.repository.EstadoRepository;
import com.waira.waira_v2.repository.ImagenRepository;
import com.waira.waira_v2.repository.ResenaRepository;
import com.waira.waira_v2.repository.ServicioRepository;
import com.waira.waira_v2.repository.SubcategoriaRepository;

@Service
public class ServicioService {

    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024; // 5 MB
    private static final int MAX_IMAGES = 10;
    private static final int MIN_IMAGES = 2;
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg");

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private ImagenRepository imagenRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Transactional
    public Servicio crearServicio(Usuario usuario, CrearServicioDTO dto, List<MultipartFile> imagenes) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario requerido");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Datos del servicio requeridos");
        }
        if (isBlank(dto.getNombreServicio())) {
            throw new IllegalArgumentException("Nombre de servicio requerido");
        }
        if (isBlank(dto.getDescripcion())) {
            throw new IllegalArgumentException("Descripción requerida");
        }
        if (dto.getPrecio() == null || dto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (dto.getDiasDuracion() == null || dto.getDiasDuracion() <= 0) {
            throw new IllegalArgumentException("La duración mínima es de un día");
        }

        Direccion direccionServicio = construirDireccion(dto);

        Estado estado = estadoRepository
            .findByNombreEstadoAndTipoEstado("DISPONIBLE", "SERVICIO")
            .orElseThrow(() -> new IllegalArgumentException("Estado DISPONIBLE SERVICIO no definido"));

        List<MultipartFile> imagenesValidas = imagenes == null ? Collections.emptyList() : imagenes.stream()
            .filter(file -> file != null && !file.isEmpty())
            .collect(Collectors.toList());

        if (imagenesValidas.size() < MIN_IMAGES) {
            throw new IllegalArgumentException("Debes subir al menos " + MIN_IMAGES + " imágenes por servicio");
        }
        if (imagenesValidas.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Solo puedes subir hasta " + MAX_IMAGES + " imágenes por servicio");
        }

        for (MultipartFile file : imagenesValidas) {
            if (file.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("Cada imagen debe pesar máximo 5MB");
            }
            String contentType = file.getContentType();
            boolean tipoPermitido = contentType != null && ALLOWED_CONTENT_TYPES.stream()
                    .anyMatch(permitido -> permitido.equalsIgnoreCase(contentType));
            if (!tipoPermitido) {
                throw new IllegalArgumentException("Solo se permiten imágenes JPG o PNG");
            }
        }

        List<Integer> categoriasIds = dto.getCategoriasIds() != null ? dto.getCategoriasIds() : Collections.emptyList();
        if (categoriasIds.isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos una categoría para tu servicio");
        }
        List<Categoria> categoriasSeleccionadas = categoriaRepository.findAllById(categoriasIds);
        if (categoriasSeleccionadas.size() != new HashSet<>(categoriasIds).size()) {
            throw new IllegalArgumentException("Alguna de las categorías seleccionadas no existe");
        }

        List<Integer> subcategoriasIds = dto.getSubcategoriasIds() != null ? dto.getSubcategoriasIds() : Collections.emptyList();
        List<Subcategoria> subcategoriasSeleccionadas = subcategoriasIds.isEmpty()
                ? Collections.emptyList()
                : subcategoriaRepository.findAllById(subcategoriasIds);

        if (!subcategoriasIds.isEmpty() && subcategoriasSeleccionadas.size() != new HashSet<>(subcategoriasIds).size()) {
            throw new IllegalArgumentException("Alguna de las subcategorías seleccionadas no existe");
        }

        if (!subcategoriasSeleccionadas.isEmpty()) {
            Set<Integer> categoriasPermitidas = categoriasSeleccionadas.stream()
                    .map(Categoria::getIdCategoria)
                    .collect(Collectors.toSet());
            boolean todasValidas = subcategoriasSeleccionadas.stream()
                    .allMatch(sub -> sub.getCategoria() != null && categoriasPermitidas.contains(sub.getCategoria().getIdCategoria()));
            if (!todasValidas) {
                throw new IllegalArgumentException("Las subcategorías deben pertenecer a las categorías seleccionadas");
            }
        }

        Servicio servicio = new Servicio();
        servicio.setUsuario(usuario);
        servicio.setEstado(estado);
        servicio.setNombreServicio(dto.getNombreServicio().trim());
        servicio.setDescripcion(dto.getDescripcion().trim());
        servicio.setPrecio(dto.getPrecio());
        servicio.setDireccion(direccionServicio);
        servicio.setDiasDuracion(dto.getDiasDuracion());
        servicio.setFechaPublicacion(new Date());
        servicio.setVistas(0);
        servicio.setCategorias(new ArrayList<>(categoriasSeleccionadas));
        servicio.setSubcategorias(new ArrayList<>(subcategoriasSeleccionadas));
        Servicio guardado = servicioRepository.save(servicio);

        List<Imagen> imagenesEntidad = imagenesValidas.stream()
                .map(file -> {
                    String url = fileStorageService.saveServicioImage(file);
                    Imagen img = new Imagen();
                    img.setServicio(guardado);
                    img.setUrl(url);
                    return img;
                })
                .collect(Collectors.toList());

        if (!imagenesEntidad.isEmpty()) {
            imagenRepository.saveAll(imagenesEntidad);
            guardado.setImagenes(imagenesEntidad);
        }

        return guardado;
    }

    private Direccion construirDireccion(CrearServicioDTO dto) {
        if (isBlank(dto.getTipoVia())) {
            throw new IllegalArgumentException("Ingresa un tipo de vía (ej. Carrera, Calle, Avenida)");
        }
        if (isBlank(dto.getNumero())) {
            throw new IllegalArgumentException("Ingresa el número principal de la dirección");
        }
        if (isBlank(dto.getBarrio())) {
            throw new IllegalArgumentException("Ingresa el barrio o sector de la dirección");
        }
        if (isBlank(dto.getCiudad())) {
            throw new IllegalArgumentException("Ingresa la ciudad de prestación del servicio");
        }

        Direccion direccion = new Direccion();
        direccion.setTipoVia(dto.getTipoVia().trim());
        direccion.setNumero(dto.getNumero().trim());
        direccion.setComplemento(isBlank(dto.getComplemento()) ? null : dto.getComplemento().trim());
        direccion.setBarrio(dto.getBarrio().trim());
        direccion.setCiudad(dto.getCiudad().trim());
        return direccionRepository.save(direccion);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public List<Servicio> listarServiciosProveedor(Usuario usuario) {
        return servicioRepository.findByUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public ExplorarPayloadDTO construirExplorarPayload() {
        ExplorarPayloadDTO payload = new ExplorarPayloadDTO();
        payload.setCategorias(construirArbolCategorias());
        payload.setServicios(construirCatalogoServicios());
        return payload;
    }

    @Transactional(readOnly = true)
    public List<CategoriaTreeDTO> construirArbolCategorias() {
        List<Categoria> categorias = categoriaRepository.findAllWithSubcategorias();
        Map<Integer, Long> totalPorCategoria = convertirConteo(servicioRepository.countServiciosPorCategoria());
        Map<Integer, Long> totalPorSubcategoria = convertirConteo(servicioRepository.countServiciosPorSubcategoria());

        return categorias.stream()
                .map(categoria -> {
                    CategoriaTreeDTO dto = new CategoriaTreeDTO();
                    dto.setId(categoria.getIdCategoria());
                    dto.setNombre(categoria.getNombreCategoria());
                    dto.setTotalServicios(totalPorCategoria.getOrDefault(categoria.getIdCategoria(), 0L));

                    List<SubcategoriaTreeDTO> subcategorias = categoria.getSubcategorias() == null
                            ? Collections.emptyList()
                            : categoria.getSubcategorias().stream()
                                    .map(sub -> {
                                        SubcategoriaTreeDTO subDTO = new SubcategoriaTreeDTO();
                                        subDTO.setId(sub.getIdSubcategoria());
                                        subDTO.setCategoriaId(categoria.getIdCategoria());
                                        subDTO.setNombre(sub.getNombreSubcategoria());
                                        subDTO.setTotalServicios(totalPorSubcategoria.getOrDefault(sub.getIdSubcategoria(), 0L));
                                        return subDTO;
                                    })
                                    .collect(Collectors.toList());

                    dto.setSubcategorias(new ArrayList<>(subcategorias));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServicioExplorarDTO> construirCatalogoServicios() {
        List<Servicio> servicios = servicioRepository.findAll();
        Map<Integer, RatingStats> ratingStats = construirMapaRatings();
        return servicios.stream()
                .filter(this::esServicioDisponible)
                .map(servicio -> mapearServicioExplorar(servicio, ratingStats))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ServicioDetalleDTO> obtenerDetalleServicio(Integer idServicio) {
        if (idServicio == null) {
            return Optional.empty();
        }
        return servicioRepository.findById(idServicio)
                .filter(this::esServicioDisponible)
                .map(this::construirDetalleServicio);
    }

    private ServicioExplorarDTO mapearServicioExplorar(Servicio servicio, Map<Integer, RatingStats> ratingStats) {
        ServicioExplorarDTO dto = new ServicioExplorarDTO();
        dto.setId(servicio.getIdServicio());
        dto.setNombre(servicio.getNombreServicio());
        dto.setDescripcion(servicio.getDescripcion());
        dto.setPrecio(servicio.getPrecio());
        dto.setVistas(servicio.getVistas());
        dto.setFechaPublicacion(servicio.getFechaPublicacion());
        dto.setCiudad(servicio.getDireccion() != null ? servicio.getDireccion().getCiudad() : null);
        dto.setEstado(servicio.getEstado() != null ? servicio.getEstado().getNombreEstado() : null);
        if (servicio.getUsuario() != null) {
            String proveedor = (servicio.getUsuario().getNombres() != null ? servicio.getUsuario().getNombres() : "").concat(
                    servicio.getUsuario().getApellidos() != null ? " " + servicio.getUsuario().getApellidos() : "");
            dto.setProveedor(proveedor.trim().isEmpty() ? null : proveedor.trim());
        }

        List<Categoria> categorias = servicio.getCategorias() == null ? Collections.emptyList() : servicio.getCategorias();
        dto.getCategoriasIds().addAll(categorias.stream()
                .map(Categoria::getIdCategoria)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        dto.getCategorias().addAll(categorias.stream()
                .map(Categoria::getNombreCategoria)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        List<Subcategoria> subcategorias = servicio.getSubcategorias() == null ? Collections.emptyList() : servicio.getSubcategorias();
        dto.getSubcategoriasIds().addAll(subcategorias.stream()
                .map(Subcategoria::getIdSubcategoria)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        dto.getSubcategorias().addAll(subcategorias.stream()
                .map(Subcategoria::getNombreSubcategoria)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        List<Imagen> imagenes = servicio.getImagenes() == null ? Collections.emptyList() : servicio.getImagenes();
        dto.setImagenDestacada(imagenes.isEmpty() ? null : imagenes.get(0).getUrl());

        RatingStats stats = ratingStats.get(servicio.getIdServicio());
        if (stats != null) {
            dto.setCalificacionPromedio(stats.promedio);
            dto.setTotalResenas(stats.total);
        }
        return dto;
    }

    private ServicioDetalleDTO construirDetalleServicio(Servicio servicio) {
        ServicioDetalleDTO dto = new ServicioDetalleDTO();
        dto.setId(servicio.getIdServicio());
        dto.setNombre(servicio.getNombreServicio());
        dto.setDescripcion(servicio.getDescripcion());
        dto.setPrecio(servicio.getPrecio());
        dto.setCiudad(servicio.getDireccion() != null ? servicio.getDireccion().getCiudad() : null);
        dto.setProveedor(obtenerNombreProveedor(servicio));
        dto.setDiasDuracion(servicio.getDiasDuracion());
        dto.setVistas(servicio.getVistas());

        NumberFormat currency = NumberFormat.getCurrencyInstance(LOCALE_CO);
        currency.setMaximumFractionDigits(0);
        dto.setPrecioTexto(servicio.getPrecio() != null ? currency.format(servicio.getPrecio()) : "$0");

        List<Imagen> imagenes = servicio.getImagenes() == null ? Collections.emptyList() : servicio.getImagenes();
        dto.getImagenes().addAll(imagenes.stream()
                .map(Imagen::getUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        List<Categoria> categorias = servicio.getCategorias() == null ? Collections.emptyList() : servicio.getCategorias();
        dto.getCategorias().addAll(categorias.stream()
                .map(Categoria::getNombreCategoria)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        List<Subcategoria> subcategorias = servicio.getSubcategorias() == null ? Collections.emptyList() : servicio.getSubcategorias();
        dto.getSubcategorias().addAll(subcategorias.stream()
                .map(Subcategoria::getNombreSubcategoria)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        Object[] resumen = resenaRepository.resumenPorServicio(servicio.getIdServicio());
        if (resumen != null && resumen.length >= 2) {
            Double promedio = resumen[0] != null ? ((Number) resumen[0]).doubleValue() : null;
            Long total = resumen[1] != null ? ((Number) resumen[1]).longValue() : 0L;
            dto.setCalificacionPromedio(promedio);
            dto.setTotalResenas(total);
        } else {
            dto.setCalificacionPromedio(null);
            dto.setTotalResenas(0L);
        }

        dto.setResenas(mapearResenas(servicio));
        return dto;
    }

    private String obtenerNombreProveedor(Servicio servicio) {
        if (servicio == null || servicio.getUsuario() == null) {
            return "Proveedor verificado";
        }
        String nombres = servicio.getUsuario().getNombres() != null ? servicio.getUsuario().getNombres() : "";
        String apellidos = servicio.getUsuario().getApellidos() != null ? servicio.getUsuario().getApellidos() : "";
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? "Proveedor verificado" : full;
    }

    private List<ResenaDTO> mapearResenas(Servicio servicio) {
        List<Reseña> reseñas = resenaRepository.findByServicioOrderByFechaCreacionDesc(servicio);
        if (reseñas == null || reseñas.isEmpty()) {
            return Collections.emptyList();
        }
        return reseñas.stream()
                .map(this::mapearResena)
                .collect(Collectors.toList());
    }

    private ResenaDTO mapearResena(Reseña reseña) {
        ResenaDTO dto = new ResenaDTO();
        dto.setAutor(extraerNombreCompleto(reseña));
        dto.setCalificacion(reseña.getCalificacion());
        dto.setFecha(reseña.getFechaCreacion());
        dto.setComentario(reseña.getComentario());
        return dto;
    }

    private String extraerNombreCompleto(Reseña reseña) {
        if (reseña == null || reseña.getUsuario() == null) {
            return "Usuario anónimo";
        }
        String nombres = reseña.getUsuario().getNombres() != null ? reseña.getUsuario().getNombres() : "";
        String apellidos = reseña.getUsuario().getApellidos() != null ? reseña.getUsuario().getApellidos() : "";
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? "Usuario anónimo" : full;
    }

    private boolean esServicioDisponible(Servicio servicio) {
        if (servicio == null || servicio.getEstado() == null || servicio.getEstado().getNombreEstado() == null) {
            return false;
        }
        return "DISPONIBLE".equalsIgnoreCase(servicio.getEstado().getNombreEstado().trim());
    }

    private Map<Integer, Long> convertirConteo(List<Object[]> rows) {
        Map<Integer, Long> conteo = new HashMap<>();
        if (rows == null) {
            return conteo;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }
            Integer id = ((Number) row[0]).intValue();
            Long total = ((Number) row[1]).longValue();
            conteo.put(id, total);
        }
        return conteo;
    }

    private Map<Integer, RatingStats> construirMapaRatings() {
        Map<Integer, RatingStats> mapa = new HashMap<>();
        List<Object[]> filas = resenaRepository.promedioYTotalPorServicio();
        if (filas == null) {
            return mapa;
        }
        for (Object[] fila : filas) {
            if (fila == null || fila.length < 3 || fila[0] == null) {
                continue;
            }
            Integer idServicio = ((Number) fila[0]).intValue();
            double promedio = fila[1] != null ? ((Number) fila[1]).doubleValue() : 0d;
            long total = fila[2] != null ? ((Number) fila[2]).longValue() : 0L;
            mapa.put(idServicio, new RatingStats(promedio, total));
        }
        return mapa;
    }

    private static final class RatingStats {
        private final double promedio;
        private final long total;

        private RatingStats(double promedio, long total) {
            this.promedio = promedio;
            this.total = total;
        }
    }

    public ProveedorStatsDTO calcularStatsProveedor(List<Servicio> servicios) {
        ProveedorStatsDTO stats = new ProveedorStatsDTO();
        if (servicios == null || servicios.isEmpty()) {
            stats.setPrecioPromedioTexto("$0");
            return stats;
        }

        int totalServicios = servicios.size();
        int serviciosActivos = (int) servicios.stream()
                .filter(s -> s.getEstado() != null && "DISPONIBLE".equalsIgnoreCase(s.getEstado().getNombreEstado()))
                .count();
        int serviciosSinImagen = (int) servicios.stream()
                .filter(s -> s.getImagenes() == null || s.getImagenes().isEmpty())
                .count();
        long totalVistas = servicios.stream()
                .map(Servicio::getVistas)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        double promedioPrecio = servicios.stream()
                .map(Servicio::getPrecio)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        NumberFormat currency = NumberFormat.getCurrencyInstance(LOCALE_CO);
        currency.setMaximumFractionDigits(0);

        stats.setTotalServicios(totalServicios);
        stats.setServiciosActivos(serviciosActivos);
        stats.setServiciosSinImagen(serviciosSinImagen);
        stats.setTotalVistas(totalVistas);
        stats.setPrecioPromedio(promedioPrecio);
        stats.setPrecioPromedioTexto(currency.format(promedioPrecio));
        return stats;
    }
}
