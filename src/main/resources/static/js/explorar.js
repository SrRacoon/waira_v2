(function(){
  const state = {
    categorias: [],
    servicios: [],
    filtroTexto: '',
    categoriaId: null,
    subcategoriaId: null,
    orden: 'relevancia',
    precioMin: null,
    precioMax: null,
    pagina: 1,
    porPagina: 10
  };

  const COP_FORMATTER = new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0
  });

  const el = {
    grid: null,
    empty: null,
    kpiResultados: null,
    kpiCategoria: null,
    kpiSubcategoria: null,
    filtroContexto: null,
    inputBusqueda: null,
    formBusqueda: null,
    selectOrden: null,
    statServicios: null,
    statCategorias: null,
    statSubcategorias: null,
    btnReset: null,
    btnResetEmpty: null,
    filterCategoria: null,
    filterSubcategoria: null,
    filterPrecioMin: null,
    filterPrecioMax: null,
    pagination: null,
    paginationPages: null,
    paginationPrev: null,
    paginationNext: null,
    btnCrearAnuncio: null
  };

  document.addEventListener('DOMContentLoaded', () => {
    cacheElements();
    wireEvents();
    init();
  });

  function cacheElements(){
    el.grid = document.getElementById('grid-servicios');
    el.empty = document.getElementById('sin-resultados');
    el.kpiResultados = document.getElementById('kpi-resultados');
    el.kpiCategoria = document.getElementById('kpi-categoria');
    el.kpiSubcategoria = document.getElementById('kpi-subcategoria');
    el.filtroContexto = document.getElementById('filtro-contexto');
    el.inputBusqueda = document.getElementById('input-busqueda');
    el.formBusqueda = document.getElementById('busqueda-form');
    el.selectOrden = document.getElementById('select-orden');
    el.statServicios = document.getElementById('stat-servicios');
    el.statCategorias = document.getElementById('stat-categorias');
    el.statSubcategorias = document.getElementById('stat-subcategorias');
    el.btnReset = document.getElementById('btn-reset-filtros');
    el.btnResetEmpty = document.getElementById('btn-sin-resultados');
    el.filterCategoria = document.getElementById('filter-categoria');
    el.filterSubcategoria = document.getElementById('filter-subcategoria');
    el.filterPrecioMin = document.getElementById('filter-precio-min');
    el.filterPrecioMax = document.getElementById('filter-precio-max');
    el.pagination = document.getElementById('paginacion-servicios');
    el.paginationPages = el.pagination?.querySelector('.paginacion__pages') ?? null;
    el.paginationPrev = el.pagination?.querySelector('[data-action="prev"]') ?? null;
    el.paginationNext = el.pagination?.querySelector('[data-action="next"]') ?? null;
    el.btnCrearAnuncio = document.getElementById('btn-crear-anuncio');
  }

  function wireEvents(){
    el.formBusqueda?.addEventListener('submit', (event) => {
      event.preventDefault();
      state.filtroTexto = normalizarTexto(el.inputBusqueda?.value);
      state.pagina = 1;
      render();
    });

    el.inputBusqueda?.addEventListener('input', () => {
      state.filtroTexto = normalizarTexto(el.inputBusqueda?.value);
      state.pagina = 1;
      render();
    });

    el.selectOrden?.addEventListener('change', () => {
      state.orden = el.selectOrden.value;
      render();
    });

    el.filterCategoria?.addEventListener('change', () => {
      const selected = parseId(el.filterCategoria.value);
      state.categoriaId = selected;
      state.subcategoriaId = null;
      state.pagina = 1;
      poblarSubcategorias(selected, null);
      syncSelectFilters();
      render();
    });

    el.filterSubcategoria?.addEventListener('change', () => {
      const selected = parseId(el.filterSubcategoria.value);
      state.subcategoriaId = selected;
      state.pagina = 1;
      render();
    });

    attachCurrencyInput(el.filterPrecioMin, 'precioMin');
    attachCurrencyInput(el.filterPrecioMax, 'precioMax');

    el.paginationPrev?.addEventListener('click', () => cambiarPagina(state.pagina - 1));
    el.paginationNext?.addEventListener('click', () => cambiarPagina(state.pagina + 1));
    el.paginationPages?.addEventListener('click', (event) => {
      const button = event.target.closest('button');
      if (!button || !el.paginationPages?.contains(button)) return;
      const page = Number(button.dataset.page);
      if (Number.isFinite(page)) {
        cambiarPagina(page);
      }
    });

    el.btnCrearAnuncio?.addEventListener('click', manejarCrearAnuncio);

    const resetFiltros = () => {
      state.categoriaId = null;
      state.subcategoriaId = null;
      state.filtroTexto = '';
      state.precioMin = null;
      state.precioMax = null;
      state.pagina = 1;
      if (el.inputBusqueda) el.inputBusqueda.value = '';
      if (el.filterCategoria) el.filterCategoria.value = '';
      if (el.filterSubcategoria) {
        el.filterSubcategoria.value = '';
        el.filterSubcategoria.disabled = true;
      }
      if (el.filterPrecioMin) el.filterPrecioMin.value = '';
      if (el.filterPrecioMax) el.filterPrecioMax.value = '';
      renderSelectLists();
      render();
      actualizarResetButtons();
    };

    el.btnReset?.addEventListener('click', resetFiltros);
    el.btnResetEmpty?.addEventListener('click', resetFiltros);
  }

  async function init(){
    mostrarCargando(true);
    try {
      const respuesta = await fetch('/api/explorar');
      if (!respuesta.ok) throw new Error('No se pudo cargar el catálogo de explorar');
      const data = await respuesta.json();
      state.categorias = Array.isArray(data.categorias) ? data.categorias.map(normalizarCategoria) : [];
      state.servicios = Array.isArray(data.servicios) ? data.servicios.map(normalizarServicio) : [];
      renderSelectLists();
      renderStats();
      render();
    } catch (error) {
      console.error('Error cargando explorar', error);
      if (el.grid) {
        el.grid.innerHTML = '<p class="tree-empty">Sin datos disponibles por el momento.</p>';
      }
    } finally {
      mostrarCargando(false);
    }
  }

  function mostrarCargando(activo){
    if (!el.grid) return;
    if (activo) {
      el.grid.innerHTML = '<p class="tree-empty">Cargando catálogos...</p>';
      el.empty?.setAttribute('hidden', 'hidden');
    }
  }

  function normalizarCategoria(categoria = {}){
    return {
      id: categoria.id ?? categoria.idCategoria ?? null,
      nombre: categoria.nombre ?? categoria.nombreCategoria ?? 'Sin nombre',
      totalServicios: Number(categoria.totalServicios) || 0,
      subcategorias: Array.isArray(categoria.subcategorias)
        ? categoria.subcategorias.map(sub => ({
            id: sub.id ?? sub.idSubcategoria ?? null,
            categoriaId: sub.categoriaId ?? categoria.id ?? null,
            nombre: sub.nombre ?? sub.nombreSubcategoria ?? 'Sin nombre',
            totalServicios: Number(sub.totalServicios) || 0
          }))
        : []
    };
  }

  function normalizarServicio(servicio = {}){
    return {
      id: servicio.id ?? servicio.idServicio ?? null,
      nombre: servicio.nombre ?? servicio.nombreServicio ?? 'Servicio',
      descripcion: servicio.descripcion || '',
      precio: typeof servicio.precio === 'number' ? servicio.precio : Number(servicio.precio) || null,
      vistas: servicio.vistas || 0,
      fecha: servicio.fechaPublicacion ? new Date(servicio.fechaPublicacion) : null,
      ciudad: servicio.ciudad || 'Colombia',
      proveedor: servicio.proveedor || 'Proveedor certificado',
      estado: (servicio.estado || servicio.nombreEstado || '').toString().trim().toUpperCase(),
      categorias: Array.isArray(servicio.categorias) ? servicio.categorias : [],
      categoriasIds: Array.isArray(servicio.categoriasIds) ? servicio.categoriasIds : [],
      subcategorias: Array.isArray(servicio.subcategorias) ? servicio.subcategorias : [],
      subcategoriasIds: Array.isArray(servicio.subcategoriasIds) ? servicio.subcategoriasIds : [],
      imagen: servicio.imagenDestacada || '/imgs/placeholder.png',
      calificacion: extraerCalificacionPromedio(servicio),
      totalResenas: extraerTotalResenas(servicio)
    };
  }

  function attachCurrencyInput(element, key){
    if (!element) return;
    element.addEventListener('input', () => {
      const numericValue = parsePrecio(element.value);
      state[key] = numericValue;
      element.value = formatCurrencyDisplay(numericValue);
      state.pagina = 1;
      render();
    });
    element.addEventListener('blur', () => {
      element.value = formatCurrencyDisplay(state[key]);
    });
  }

  function cambiarPagina(nuevaPagina){
    if (!Number.isFinite(nuevaPagina)) return;
    state.pagina = Math.max(1, nuevaPagina);
    render();
  }

  function actualizarResetButtons(){
    const filtrosActivos = filtrosAplicados();
    toggleResetVisibility(el.btnReset, filtrosActivos);
    toggleResetVisibility(el.btnResetEmpty, filtrosActivos);
  }

  function toggleResetVisibility(button, visible){
    if (!button) return;
    if (visible) {
      button.removeAttribute('hidden');
    } else {
      button.setAttribute('hidden', 'hidden');
    }
  }

  function filtrosAplicados(){
    return Boolean(
      state.categoriaId ||
      state.subcategoriaId ||
      state.filtroTexto ||
      state.precioMin != null ||
      state.precioMax != null
    );
  }

  function render(){
    if (!el.grid) return;
    const resultados = aplicarFiltros();
    actualizarResetButtons();
    if (el.kpiResultados) {
      el.kpiResultados.textContent = resultados.length;
    }
    actualizarContexto();

    if (!resultados.length) {
      el.grid.innerHTML = '';
      actualizarEmptyState(filtrosAplicados());
      renderPagination(0);
      return;
    }

    actualizarEmptyState(false);
    const { items, totalPaginas } = obtenerPagina(resultados);
    const fragment = document.createDocumentFragment();
    items.forEach(servicio => fragment.appendChild(crearCard(servicio)));
    el.grid.innerHTML = '';
      el.grid.appendChild(fragment);
    renderPagination(totalPaginas);
  }

  function obtenerPagina(resultados){
    const totalPaginas = Math.max(1, Math.ceil(resultados.length / state.porPagina));
    if (state.pagina > totalPaginas) {
      state.pagina = totalPaginas;
    }
    if (state.pagina < 1) {
      state.pagina = 1;
    }
    const inicio = (state.pagina - 1) * state.porPagina;
    const fin = inicio + state.porPagina;
    return { items: resultados.slice(inicio, fin), totalPaginas };
  }

  function renderPagination(totalPaginas){
    if (!el.pagination) return;
    if (totalPaginas <= 1) {
      el.pagination.setAttribute('hidden', 'hidden');
      return;
    }
    el.pagination.removeAttribute('hidden');
    if (el.paginationPrev) {
      el.paginationPrev.disabled = state.pagina <= 1;
    }
    if (el.paginationNext) {
      el.paginationNext.disabled = state.pagina >= totalPaginas;
    }
    if (!el.paginationPages) return;
    const fragment = document.createDocumentFragment();
    for (let page = 1; page <= totalPaginas; page += 1) {
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'paginacion__page' + (page === state.pagina ? ' is-active' : '');
      button.dataset.page = String(page);
      button.textContent = page;
      fragment.appendChild(button);
    }
    el.paginationPages.innerHTML = '';
    el.paginationPages.appendChild(fragment);
  }

  function extraerCalificacionPromedio(servicio){
    const candidatos = [
      servicio.calificacion,
      servicio.calificacionPromedio,
      servicio.promedioCalificacion,
      servicio.rating,
      servicio.ratingPromedio,
      servicio.puntuacion,
      servicio.puntuacionPromedio
    ];
    for (const valor of candidatos) {
      const numero = Number(valor);
      if (Number.isFinite(numero)) {
        return Math.max(0, Math.min(5, numero));
      }
    }
    return null;
  }

  function extraerTotalResenas(servicio){
    const candidatos = [
      servicio.totalResenas,
      servicio.totalReseñas,
      servicio.reseñasTotales,
      servicio.cantidadResenas,
      servicio.cantidadReseñas
    ];
    for (const valor of candidatos) {
      const numero = Number(valor);
      if (Number.isFinite(numero)) {
        return Math.max(0, numero);
      }
    }
    return 0;
  }

  function renderRatingStars(valor){
    const rating = Math.max(0, Math.min(5, Number(valor) || 0));
    const filled = Math.round(rating);
    let html = '';
    for (let i = 1; i <= 5; i += 1) {
      const clase = i <= filled ? 'rating-star is-filled' : 'rating-star';
      html += `<span class="${clase}" aria-hidden="true">★</span>`;
    }
    return html;
  }

  async function manejarCrearAnuncio(){
    try {
      const estado = await obtenerEstadoActual();
      if (!estado?.autenticado) {
        abrirModal('login');
        return;
      }
      if (!estado?.esProveedor) {
        if (typeof abrirModalSolicitud === 'function') {
          await abrirModalSolicitud();
        } else {
          abrirModal('solicitud-proveedor');
        }
        return;
      }
      window.location.href = estado?.urlCrearServicio || '/proveedor/servicios/nuevo';
    } catch (error) {
      console.error('No se pudo resolver el estado del usuario', error);
      if (typeof abrirModal === 'function') {
        abrirModal('login');
      }
      mostrarAlerta('Ups', error.message || 'No pudimos validar tu sesión, intenta iniciar sesión.');
    }
  }

  async function obtenerEstadoActual(){
    try {
      const response = await fetch('/api/auth/estado', { credentials: 'include' });
      if (!response.ok) {
        throw new Error('No fue posible consultar tu estado.');
      }
      const data = await response.json().catch(() => ({}));
      return data;
    } catch (error) {
      console.error('Error consultando /api/auth/estado', error);
      throw error;
    }
  }

  function aplicarFiltros(){
    const texto = state.filtroTexto;
    const categoriaId = state.categoriaId;
    const subcategoriaId = state.subcategoriaId;
    const precioMin = state.precioMin;
    const precioMax = state.precioMax;

    const filtrados = state.servicios.filter(servicio => {
      const coincideEstado = servicio.estado === 'DISPONIBLE';
      const coincideTexto = !texto ||
        servicio.nombre.toLowerCase().includes(texto) ||
        servicio.descripcion.toLowerCase().includes(texto) ||
        servicio.categorias.join(' ').toLowerCase().includes(texto) ||
        servicio.subcategorias.join(' ').toLowerCase().includes(texto);

      const coincideCategoria = !categoriaId || servicio.categoriasIds.includes(categoriaId);
      const coincideSubcategoria = !subcategoriaId || servicio.subcategoriasIds.includes(subcategoriaId);
      const coincidePrecio = filtrarPorPrecio(servicio.precio, precioMin, precioMax);
      return coincideEstado && coincideTexto && coincideCategoria && coincideSubcategoria && coincidePrecio;
    });

    return ordenarResultados(filtrados);
  }

  function ordenarResultados(lista){
    switch(state.orden){
      case 'recientes':
        return lista.sort((a,b) => (b.fecha?.getTime() || 0) - (a.fecha?.getTime() || 0));
      case 'precio_asc':
        return lista.sort((a,b) => (a.precio || 0) - (b.precio || 0));
      case 'precio_desc':
        return lista.sort((a,b) => (b.precio || 0) - (a.precio || 0));
      case 'vistas_desc':
        return lista.sort((a,b) => (b.vistas || 0) - (a.vistas || 0));
      default:
        return lista;
    }
  }

  function crearCard(servicio){
    const card = document.createElement('article');
    card.className = 'card';
    const categoriaPrincipal = servicio.categorias[0] || 'General';
    const precio = typeof servicio.precio === 'number'
      ? new Intl.NumberFormat('es-CO',{style:'currency',currency:'COP',maximumFractionDigits:0}).format(servicio.precio)
      : 'Consultar';
    const rating = servicio.calificacion ?? 0;
    const ratingTexto = rating ? rating.toFixed(1) : '0.0';
    const ratingEstrellas = renderRatingStars(rating);
    const reseñasTexto = servicio.totalResenas ? `${servicio.totalResenas} reseñas` : 'Sin reseñas';

    card.innerHTML = `
      <div class="thumb">
        <img src="${servicio.imagen}" alt="${escapeHtml(servicio.nombre)}" onerror="this.src='/imgs/placeholder.png'" />
        <span class="badge">${escapeHtml(categoriaPrincipal)}</span>
      </div>
      <div class="body">
        <h3 class="title">${escapeHtml(servicio.nombre)}</h3>
        <div class="meta rating-meta">
          <div class="rating-block" aria-label="Calificación promedio ${ratingTexto} de 5">
            <div class="rating-stars">${ratingEstrellas}</div>
            <div class="rating-details">
              <span class="rating-score">${ratingTexto}</span>
              <span class="rating-count">${reseñasTexto}</span>
            </div>
          </div>
          <span>${servicio.vistas || 0} vistas</span>
        </div>
        <div class="price">${precio}</div>
        <div class="chips">
          ${servicio.subcategorias.slice(0,3).map(nombre => `<span class="chip">${escapeHtml(nombre)}</span>`).join('')}
        </div>
        <div class="actions">
          <a class="btn btn-primary" href="/servicios/${servicio.id}">Ver detalle</a>
        </div>
      </div>
    `;
    return card;
  }

  function actualizarContexto(){
    const categoria = state.categorias.find(cat => cat.id === state.categoriaId) || null;
    const subcategoria = categoria?.subcategorias?.find(sub => sub.id === state.subcategoriaId) || null;

    if (el.kpiCategoria) {
      el.kpiCategoria.textContent = categoria ? categoria.nombre : 'Todas';
    }
    if (el.kpiSubcategoria) {
      el.kpiSubcategoria.textContent = subcategoria ? subcategoria.nombre : 'Todas';
    }
    if (el.filtroContexto) {
      el.filtroContexto.textContent = subcategoria
        ? `${categoria?.nombre || 'Categoría'} / ${subcategoria.nombre}`
        : (categoria ? categoria.nombre : 'Todas las categorías');
    }
  }

  function renderStats(){
    const totalSubcategorias = state.categorias.reduce((acc, cat) => acc + (cat.subcategorias?.length || 0), 0);
    if (el.statServicios) el.statServicios.textContent = state.servicios.length;
    if (el.statCategorias) el.statCategorias.textContent = state.categorias.length;
    if (el.statSubcategorias) el.statSubcategorias.textContent = totalSubcategorias;
  }

  function normalizarTexto(texto = ''){
    return texto.trim().toLowerCase();
  }

  function renderSelectLists(){
    if (!el.filterCategoria) return;
    const options = ['<option value="">Todas</option>'];
    state.categorias.forEach(cat => {
      options.push(`<option value="${cat.id}">${escapeHtml(cat.nombre)}</option>`);
    });
    el.filterCategoria.innerHTML = options.join('');
    syncSelectFilters();
  }

  function poblarSubcategorias(categoriaId, selectedSubId){
    if (!el.filterSubcategoria) return;
    el.filterSubcategoria.innerHTML = '<option value="">Todas</option>';
    if (!categoriaId) {
      el.filterSubcategoria.disabled = true;
      return;
    }
    const categoria = state.categorias.find(cat => cat.id === categoriaId);
    if (!categoria || !categoria.subcategorias.length) {
      el.filterSubcategoria.disabled = true;
      return;
    }
    categoria.subcategorias.forEach(sub => {
      const option = document.createElement('option');
      option.value = String(sub.id);
      option.textContent = sub.nombre;
      el.filterSubcategoria.appendChild(option);
    });
    el.filterSubcategoria.disabled = false;
    if (selectedSubId !== null && selectedSubId !== undefined && selectedSubId !== '') {
      el.filterSubcategoria.value = String(selectedSubId);
    } else {
      el.filterSubcategoria.value = '';
    }
  }

  function syncSelectFilters(){
    if (el.filterCategoria) {
      el.filterCategoria.value = state.categoriaId ?? '';
    }
    poblarSubcategorias(state.categoriaId, state.subcategoriaId ?? '');
    if (el.filterSubcategoria) {
      el.filterSubcategoria.value = state.subcategoriaId ?? '';
    }
    if (el.filterPrecioMin) {
      el.filterPrecioMin.value = formatCurrencyDisplay(state.precioMin);
    }
    if (el.filterPrecioMax) {
      el.filterPrecioMax.value = formatCurrencyDisplay(state.precioMax);
    }
  }

  function parseId(value){
    if (value === '' || value === null || value === undefined) {
      return null;
    }
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }

  function parsePrecio(value){
    if (value === '' || value === null || value === undefined) {
      return null;
    }
    const cleaned = value.toString().replace(/[^0-9]/g, '');
    if (!cleaned.length) {
      return null;
    }
    const parsed = Number(cleaned);
    return Number.isFinite(parsed) ? parsed : null;
  }

  function formatCurrencyDisplay(value){
    if (value === null || value === undefined) {
      return '';
    }
    return COP_FORMATTER.format(value);
  }

  function filtrarPorPrecio(precio, minimo, maximo){
    if (minimo == null && maximo == null) {
      return true;
    }
    if (precio == null) {
      return false;
    }
    if (minimo != null && precio < minimo) {
      return false;
    }
    if (maximo != null && precio > maximo) {
      return false;
    }
    return true;
  }

  function actualizarEmptyState(mostrar){
    if (!el.empty) return;
    if (mostrar) {
      el.empty.removeAttribute('hidden');
      el.empty.style.display = 'flex';
    } else {
      el.empty.setAttribute('hidden', 'hidden');
      el.empty.style.display = 'none';
    }
  }

  function escapeHtml(str){
    return String(str || '').replace(/[&<>"']/g, (char) => {
      switch (char) {
        case '&': return '&amp;';
        case '<': return '&lt;';
        case '>': return '&gt;';
        case '"': return '&quot;';
        case "'": return '&#39;';
        default: return char;
      }
    });
  }
})();

