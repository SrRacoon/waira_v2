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
    nodosAbiertos: new Set()
  };

  const el = {
    tree: null,
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
    filterPrecioMax: null
  };

  document.addEventListener('DOMContentLoaded', () => {
    cacheElements();
    wireEvents();
    init();
  });

  function cacheElements(){
    el.tree = document.getElementById('explorar-tree');
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
  }

  function wireEvents(){
    el.formBusqueda?.addEventListener('submit', (event) => {
      event.preventDefault();
      state.filtroTexto = normalizarTexto(el.inputBusqueda?.value);
      render();
    });

    el.inputBusqueda?.addEventListener('input', () => {
      state.filtroTexto = normalizarTexto(el.inputBusqueda?.value);
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
      if (selected) {
        state.nodosAbiertos.add(selected);
      }
      poblarSubcategorias(selected, null);
      syncSelectFilters();
      renderTree();
      render();
    });

    el.filterSubcategoria?.addEventListener('change', () => {
      const selected = parseId(el.filterSubcategoria.value);
      state.subcategoriaId = selected;
      if (selected) {
        state.nodosAbiertos.add(state.categoriaId);
      }
      renderTree();
      render();
    });

    el.filterPrecioMin?.addEventListener('input', () => {
      state.precioMin = parsePrecio(el.filterPrecioMin.value);
      render();
    });

    el.filterPrecioMax?.addEventListener('input', () => {
      state.precioMax = parsePrecio(el.filterPrecioMax.value);
      render();
    });

    const resetFiltros = () => {
      state.categoriaId = null;
      state.subcategoriaId = null;
      state.filtroTexto = '';
      state.precioMin = null;
      state.precioMax = null;
      if (el.inputBusqueda) el.inputBusqueda.value = '';
      if (el.filterCategoria) el.filterCategoria.value = '';
      if (el.filterSubcategoria) {
        el.filterSubcategoria.value = '';
        el.filterSubcategoria.disabled = true;
      }
      if (el.filterPrecioMin) el.filterPrecioMin.value = '';
      if (el.filterPrecioMax) el.filterPrecioMax.value = '';
      renderTree();
      renderSelectLists();
      render();
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
      state.nodosAbiertos = new Set(state.categorias.slice(0, 3).map(cat => cat.id));
      renderSelectLists();
      renderStats();
      renderTree();
      render();
    } catch (error) {
      console.error('Error cargando explorar', error);
      if (el.tree) {
        el.tree.innerHTML = '<p class="tree-empty">No fue posible cargar las categorías.</p>';
      }
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
      imagen: servicio.imagenDestacada || '/imgs/placeholder.png'
    };
  }

  function renderTree(){
    if (!el.tree) return;
    if (!state.categorias.length) {
      el.tree.innerHTML = '<p class="tree-empty">Aún no hay categorías configuradas.</p>';
      return;
    }
    const fragment = document.createDocumentFragment();
    state.categorias.forEach(cat => fragment.appendChild(crearNodoCategoria(cat)));
    el.tree.innerHTML = '';
    el.tree.appendChild(fragment);
  }

  function crearNodoCategoria(categoria){
    const nodo = document.createElement('details');
    const activo = state.categoriaId === categoria.id && state.subcategoriaId === null;
    nodo.className = 'tree-node' + (activo ? ' is-active' : '');
    nodo.open = state.nodosAbiertos.has(categoria.id) || activo;

    const summary = document.createElement('summary');
    const title = document.createElement('span');
    title.className = 'tree-node__title';
    title.textContent = categoria.nombre;
    const badge = document.createElement('span');
    badge.className = 'tree-node__badge';
    badge.textContent = `${categoria.totalServicios} srv`;
    summary.appendChild(title);
    summary.appendChild(badge);
    summary.addEventListener('click', () => {
      state.categoriaId = categoria.id;
      state.subcategoriaId = null;
      state.nodosAbiertos.add(categoria.id);
      syncSelectFilters();
      render();
      renderTree();
    });
    nodo.appendChild(summary);

    nodo.addEventListener('toggle', () => {
      if (nodo.open) {
        state.nodosAbiertos.add(categoria.id);
      } else {
        state.nodosAbiertos.delete(categoria.id);
      }
    });

    if (categoria.subcategorias?.length) {
      const contenedor = document.createElement('div');
      contenedor.className = 'tree-children';
      categoria.subcategorias.forEach(sub => contenedor.appendChild(crearNodoSubcategoria(sub, categoria)));
      nodo.appendChild(contenedor);
    }
    return nodo;
  }

  function crearNodoSubcategoria(subcategoria, categoria){
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'tree-child' + (state.subcategoriaId === subcategoria.id ? ' is-selected' : '');
    btn.textContent = `${subcategoria.nombre} (${subcategoria.totalServicios})`;
    btn.addEventListener('click', (event) => {
      event.preventDefault();
      state.categoriaId = subcategoria.categoriaId || categoria.id;
      state.subcategoriaId = subcategoria.id;
      state.nodosAbiertos.add(state.categoriaId);
      syncSelectFilters();
      render();
      renderTree();
    });
    return btn;
  }

  function render(){
    if (!el.grid) return;
    const resultados = aplicarFiltros();
    if (el.kpiResultados) {
      el.kpiResultados.textContent = resultados.length;
    }
    actualizarContexto();

    if (!resultados.length) {
      el.grid.innerHTML = '';
      el.empty?.removeAttribute('hidden');
      return;
    }

    el.empty?.setAttribute('hidden', 'hidden');
    const fragment = document.createDocumentFragment();
    resultados.forEach(servicio => fragment.appendChild(crearCard(servicio)));
    el.grid.innerHTML = '';
    el.grid.appendChild(fragment);
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

    card.innerHTML = `
      <div class="thumb">
        <img src="${servicio.imagen}" alt="${escapeHtml(servicio.nombre)}" onerror="this.src='/imgs/placeholder.png'" />
        <span class="badge">${escapeHtml(categoriaPrincipal)}</span>
      </div>
      <div class="body">
        <h3 class="title">${escapeHtml(servicio.nombre)}</h3>
        <div class="meta">
          <span>${escapeHtml(servicio.ciudad)}</span>
          <span>${servicio.vistas || 0} vistas</span>
        </div>
        <div class="price">${precio}</div>
        <div class="chips">
          ${servicio.subcategorias.slice(0,3).map(nombre => `<span class="chip">${escapeHtml(nombre)}</span>`).join('')}
        </div>
        <div class="actions">
          <button class="btn btn-primary">Ver detalle</button>
          <button class="btn btn-outline">Guardar</button>
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
      el.filterPrecioMin.value = state.precioMin ?? '';
    }
    if (el.filterPrecioMax) {
      el.filterPrecioMax.value = state.precioMax ?? '';
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
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
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
