(function(){
  const state = {
    categorias: [],
    servicios: [],
    filtroTexto: '',
    categoriaActiva: 'todas',
    orden: 'relevancia'
  };

  const el = {
    chips: null,
    grid: null,
    empty: null,
    kpiResultados: null,
    kpiCategoria: null,
    inputBusqueda: null,
    formBusqueda: null,
    selectOrden: null
  };

  document.addEventListener('DOMContentLoaded', () => {
    el.chips = document.getElementById('categorias-scroll');
    el.grid = document.getElementById('grid-servicios');
    el.empty = document.getElementById('sin-resultados');
    el.kpiResultados = document.getElementById('kpi-resultados');
    el.kpiCategoria = document.getElementById('kpi-categoria');
    el.inputBusqueda = document.getElementById('input-busqueda');
    el.formBusqueda = document.getElementById('busqueda-form');
    el.selectOrden = document.getElementById('select-orden');

    wireEvents();
    init();
  });

  function wireEvents(){
    el.formBusqueda.addEventListener('submit', (e) => {
      e.preventDefault();
      state.filtroTexto = (el.inputBusqueda.value || '').trim().toLowerCase();
      render();
    });

    el.inputBusqueda.addEventListener('input', () => {
      state.filtroTexto = (el.inputBusqueda.value || '').trim().toLowerCase();
      render();
    });

    el.selectOrden.addEventListener('change', () => {
      state.orden = el.selectOrden.value;
      render();
    });
  }

  async function init(){
    await Promise.all([cargarCategorias(), cargarServicios()]);
    render();
  }

  async function cargarCategorias(){
    const endpoints = ['/api/categorias', '/api/public/categorias'];
    let data = [];
    for (const url of endpoints){
      try {
        const r = await fetch(url);
        if (r.ok){ data = await r.json(); break; }
      } catch(_) { /* ignore */ }
    }
    // normalizar
    state.categorias = (Array.isArray(data) ? data : []).map(c => ({
      id: c.idCategoria || c.id || c.idcategoria || c.codigo || String(c),
      nombre: c.nombre || c.nombreCategoria || c.titulo || String(c)
    })).filter(c => !!c.nombre);

    dibujarChips();
  }

  async function cargarServicios(){
    const endpoints = ['/api/servicios', '/api/public/servicios'];
    let data = [];
    for (const url of endpoints){
      try {
        const r = await fetch(url);
        if (r.ok){ data = await r.json(); break; }
      } catch(_) { /* ignore */ }
    }
    state.servicios = (Array.isArray(data) ? data : []).map(s => normalizarServicio(s));
  }

  function normalizarServicio(s){
    const categorias = s.categorias || s.cats || [];
    const imagenes = s.imagenes || s.images || [];
    const primeraImagen = imagenes.length ? (imagenes[0].url || imagenes[0].imagenUrl || imagenes[0].ruta || null) : null;
    return {
      id: s.idServicio || s.id || s.codigo || null,
      nombre: s.nombreServicio || s.nombre || 'Servicio',
      descripcion: s.descripcion || '',
      precio: typeof s.precio === 'number' ? s.precio : (Number(s.precio) || null),
      categorias: categorias.map(c => (c.nombre || c.nombreCategoria || c.titulo || '').toString()).filter(Boolean),
      imagen: primeraImagen,
      vistas: s.vistas || 0,
      fecha: s.fechaPublicacion || s.fecha || null
    };
  }

  function dibujarChips(){
    if (!el.chips) return;
    el.chips.innerHTML = '';

    const todas = document.createElement('button');
    todas.className = 'chip' + (state.categoriaActiva === 'todas' ? ' active' : '');
    todas.textContent = 'Todas';
    todas.addEventListener('click', () => { state.categoriaActiva = 'todas'; el.kpiCategoria.textContent = 'Todas'; activarChip(todas); render(); });
    el.chips.appendChild(todas);

    state.categorias.forEach(c => {
      const b = document.createElement('button');
      b.className = 'chip' + (state.categoriaActiva === c.nombre ? ' active' : '');
      b.textContent = c.nombre;
      b.addEventListener('click', () => { state.categoriaActiva = c.nombre; el.kpiCategoria.textContent = c.nombre; activarChip(b); render(); });
      el.chips.appendChild(b);
    });
  }

  function activarChip(btn){
    [...el.chips.querySelectorAll('.chip')].forEach(ch => ch.classList.remove('active'));
    btn.classList.add('active');
  }

  function applyFilters(list){
    const t = state.filtroTexto;
    const cat = state.categoriaActiva;
    let results = list.filter(s => {
      const textoOk = !t || (
        (s.nombre || '').toLowerCase().includes(t) ||
        (s.descripcion || '').toLowerCase().includes(t) ||
        (s.categorias || []).join(' ').toLowerCase().includes(t)
      );
      const catOk = cat === 'todas' || (s.categorias || []).some(n => n === cat);
      return textoOk && catOk;
    });

    switch(state.orden){
      case 'recientes':
        results.sort((a,b) => new Date(b.fecha||0) - new Date(a.fecha||0));
        break;
      case 'precio_asc':
        results.sort((a,b) => (a.precio||0) - (b.precio||0));
        break;
      case 'precio_desc':
        results.sort((a,b) => (b.precio||0) - (a.precio||0));
        break;
      case 'vistas_desc':
        results.sort((a,b) => (b.vistas||0) - (a.vistas||0));
        break;
      case 'relevancia':
      default:
        // Mantener orden original
        break;
    }

    return results;
  }

  function render(){
    if (!el.grid) return;
    const data = applyFilters(state.servicios);

    el.kpiResultados.textContent = data.length;

    el.grid.innerHTML = '';
    if (!data.length){
      el.empty.hidden = false;
      return;
    }
    el.empty.hidden = true;

    data.forEach(s => el.grid.appendChild(cardServicio(s)));
  }

  function cardServicio(s){
    const div = document.createElement('div');
    div.className = 'card';
    const imgSrc = s.imagen || '/imgs/placeholder.png';
    const precioTxt = (typeof s.precio === 'number') ? new Intl.NumberFormat('es-CO',{style:'currency',currency:'COP',maximumFractionDigits:0}).format(s.precio) : 'Consultar';
    const categoriaTxt = (s.categorias && s.categorias[0]) ? s.categorias[0] : 'General';

    div.innerHTML = `
      <div class="thumb">
        <img src="${imgSrc}" alt="${escapeHtml(s.nombre)}" onerror="this.src='/imgs/placeholder.png'" />
        <span class="badge">${escapeHtml(categoriaTxt)}</span>
      </div>
      <div class="body">
        <div class="title">${escapeHtml(s.nombre)}</div>
        <div class="meta">
          <span>${s.vistas || 0} vistas</span>
        </div>
        <div class="price">${precioTxt}</div>
        <div class="actions">
          <button class="btn btn-primary">Ver detalle</button>
          <button class="btn btn-outline">Guardar</button>
        </div>
      </div>
    `;
    return div;
  }

  function escapeHtml(str){
    return String(str || '').replace(/[&<>'"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','\'':'&#39;','"':'&quot;'}[c]));
  }
})();
