// Script simplificado para nueva interfaz de administración
window.addEventListener('DOMContentLoaded', () => {
    configurarSidebar();
    cargarRoles().then(() => cargarUsuarios());
    cargarSolicitudes();
    cargarMetricasDashboard();
    prepararBusquedaUsuarios();
    prepararLogout();
    inicializarCopiasSolicitudes();
});

async function cargarSolicitudes() {
    try {
        const res = await fetch('/api/admin/solicitudes');
        const solicitudes = await res.json();
        solicitudesData = Array.isArray(solicitudes) ? solicitudes : [];
        solicitudesPagina = 1;
        renderSolicitudes();
        actualizarControlesSolicitudes();
    } catch(e){ console.error('Error solicitudes', e); }
}

let ROLES = [];
let confirmacionPendiente = null;
// Estado de paginación
let usuariosData = [];
let solicitudesData = [];
let usuariosPagina = 1;
let solicitudesPagina = 1;
let usuariosPageSize = 10;
let solicitudesPageSize = 10;

async function cargarRoles(){
    try {
        const r = await fetch('/api/admin/roles');
        if (r.ok) ROLES = await r.json();
    } catch(e){ console.warn('No roles', e); }
}

async function cargarUsuarios() {
    try {
        const res = await fetch('/api/admin/usuarios');
        const usuarios = await res.json();
        usuariosData = Array.isArray(usuarios)? usuarios : [];
        usuariosPagina = 1;
        renderUsuarios();
        actualizarControlesUsuarios();
    } catch(e){ console.error('Error usuarios', e); }
}

function renderUsuarios(){
    const tbody = document.getElementById('tbody-usuarios');
    if (!tbody) return;
    const filtro = (document.getElementById('buscar-usuario')?.value || '').toLowerCase();
    let listaFiltrada = usuariosData.filter(u => {
        if (!filtro) return true;
        return ((u.nombres||'') + ' ' + (u.apellidos||'')).toLowerCase().includes(filtro) || (u.email||'').toLowerCase().includes(filtro);
    });
    const total = listaFiltrada.length;
    const maxPage = Math.max(1, Math.ceil(total / usuariosPageSize));
    if (usuariosPagina > maxPage) usuariosPagina = maxPage;
    const start = (usuariosPagina - 1) * usuariosPageSize;
    const slice = listaFiltrada.slice(start, start + usuariosPageSize);
    tbody.innerHTML='';
    slice.forEach(u => {
        const tr = document.createElement('tr');
        const rolesSelect = crearSelectRoles(u);
        const estado = u.estadoCuenta ? 'Activo' : 'Inactivo';
        tr.innerHTML = `
            <td class="id-col">${u.id}</td>
            <td class="nombre-col">${(u.nombres||'') + ' ' + (u.apellidos||'')}</td>
            <td class="email-col">${u.email||''}</td>
            <td class="estado-col"><span class="badge ${u.estadoCuenta? 'badge-confirmada':'badge-denegada'}">${estado}</span></td>
            <td class="roles-cell roles-col"></td>
            <td class="acciones-col">
                <button class="accion-btn toggle" onclick="pedirConfirmacionToggleEstado(${u.id}, ${!u.estadoCuenta})">${u.estadoCuenta? 'Desactivar':'Activar'}</button>
                <button class="accion-btn roles" onclick="pedirConfirmacionRoles(${u.id})" data-id="${u.id}">Guardar roles</button>
            </td>`;
        tbody.appendChild(tr);
        tr.querySelector('.roles-cell').appendChild(rolesSelect);
    });
    const wrapper = tbody.closest('.table-wrapper');
    if (wrapper) wrapper.scrollLeft = 0;
}

function crearSelectRoles(u){
    const select = document.createElement('select');
    select.multiple = true;
    select.size = Math.min(ROLES.length, 4);
    ROLES.forEach(r => {
        const opt = document.createElement('option');
        opt.value = r.nombre;
        opt.textContent = r.nombre;
        if (Array.isArray(u.roles) && u.roles.includes(r.nombre)) opt.selected = true;
        select.appendChild(opt);
    });
    return select;
}

async function toggleEstadoCuenta(id, nuevo){
    try {
        const r = await fetch(`/api/admin/usuarios/${id}/estado`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({estadoCuenta:nuevo})});
        if (!r.ok){ mostrarAlerta('Error','No se pudo actualizar'); return; }
        const idx = usuariosData.findIndex(u => u.id === id);
        if (idx >= 0) usuariosData[idx].estadoCuenta = nuevo;
        renderUsuarios();
        actualizarControlesUsuarios();
    } catch(e){ console.error(e); mostrarAlerta('Error','Error de red'); }
}

function pedirConfirmacionToggleEstado(id, nuevo){
    confirmacionPendiente = () => toggleEstadoCuenta(id, nuevo);
    mostrarAlertaConConfirmacion('Confirmar Cambio de Estado', '¿Seguro que deseas ' + (nuevo? 'activar':'desactivar') + ' esta cuenta?');
}

async function actualizarRolesUsuario(id){
    try {
        const fila = [...document.querySelectorAll('#tbody-usuarios tr')].find(tr => tr.querySelector('button.roles')?.dataset.id == id);
        const select = fila?.querySelector('select');
        if (!select){ mostrarAlerta('Error','No se encontró select'); return; }
        const roles = [...select.selectedOptions].map(o => o.value);
        const r = await fetch(`/api/admin/usuarios/${id}/roles`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({roles})});
        if (!r.ok){ mostrarAlerta('Error','No se pudo guardar roles'); return; }
        cargarUsuarios();
    } catch(e){ console.error(e); mostrarAlerta('Error','Error de red'); }
}

function pedirConfirmacionRoles(id){
    confirmacionPendiente = () => actualizarRolesUsuario(id);
    mostrarAlertaConConfirmacion('Confirmar Cambio de Roles', '¿Seguro que deseas actualizar los roles de este usuario?');
}

function confirmarAccion(){
    if (typeof confirmacionPendiente === 'function'){
        const fn = confirmacionPendiente;
        confirmacionPendiente = null;
        cerrarModalAlerta();
        fn();
    }
}

function prepararBusquedaUsuarios(){
    const input = document.getElementById('buscar-usuario');
    if (input){ input.addEventListener('input', () => { renderUsuarios(); actualizarControlesUsuarios(); }); }
    // Paginación usuarios
    const uprev = document.getElementById('usuarios-prev');
    const unext = document.getElementById('usuarios-next');
    const usize = document.getElementById('usuarios-page-size');
    if (uprev) uprev.addEventListener('click', () => { if (usuariosPagina>1){ usuariosPagina--; renderUsuarios(); actualizarControlesUsuarios(); } });
    if (unext) unext.addEventListener('click', () => { usuariosPagina++; renderUsuarios(); actualizarControlesUsuarios(); });
    if (usize) usize.addEventListener('change', () => { 
        const val = usize.value;
        if (val === 'all'){ usuariosPageSize = usuariosData.length || 1; usuariosPagina = 1; }
        else { usuariosPageSize = parseInt(val,10)||10; usuariosPagina=1; }
        renderUsuarios(); actualizarControlesUsuarios(); 
    });
    // Paginación solicitudes
    const sprev = document.getElementById('solicitudes-prev');
    const snext = document.getElementById('solicitudes-next');
    const ssize = document.getElementById('solicitudes-page-size');
    if (sprev) sprev.addEventListener('click', () => { if (solicitudesPagina>1){ solicitudesPagina--; renderSolicitudes(); actualizarControlesSolicitudes(); } });
    if (snext) snext.addEventListener('click', () => { solicitudesPagina++; renderSolicitudes(); actualizarControlesSolicitudes(); });
    if (ssize) ssize.addEventListener('change', () => { 
        const val = ssize.value;
        if (val === 'all'){ solicitudesPageSize = solicitudesData.length || 1; solicitudesPagina = 1; }
        else { solicitudesPageSize = parseInt(val,10)||10; solicitudesPagina=1; }
        renderSolicitudes(); actualizarControlesSolicitudes(); 
    });
    // Filtro estado solicitudes
    const sfilter = document.getElementById('filtro-estado-solicitudes');
    if (sfilter) sfilter.addEventListener('change', () => { solicitudesPagina = 1; renderSolicitudes(); actualizarControlesSolicitudes(); });
}

function renderSolicitudes(){
    const tbody = document.getElementById('tbody-solicitudes');
    if (!tbody) return;
    const filtroSel = document.getElementById('filtro-estado-solicitudes');
    const filtroEstado = (filtroSel?.value || 'ALL').toUpperCase();
    const normalizar = (txt) => {
        const up = (txt || '').toString().toUpperCase();
        if (['CONFIRMADA','APROBADA','APROBADO'].includes(up)) return 'CONFIRMADA';
        if (up === 'DENEGADA') return 'DENEGADA';
        return 'PENDIENTE';
    };
    const filtradas = solicitudesData.filter(s => {
        if (filtroEstado === 'ALL') return true;
        return normalizar(s.estado) === filtroEstado;
    });
    const total = filtradas.length;
    const maxPage = Math.max(1, Math.ceil(total / solicitudesPageSize));
    if (solicitudesPagina > maxPage) solicitudesPagina = maxPage;
    const start = (solicitudesPagina - 1) * solicitudesPageSize;
    const slice = filtradas.slice(start, start + solicitudesPageSize);
    tbody.innerHTML='';
    slice.forEach(s => {
        const tr = document.createElement('tr');
        const estadoTxt = s.estado || '';
        const estadoUp = estadoTxt.toUpperCase();
        let badge = 'badge';
        if (estadoUp === 'DENEGADA') badge += ' badge-denegada';
        else if (['CONFIRMADA','APROBADA','APROBADO'].includes(estadoUp)) badge += ' badge-confirmada';
        else badge += ' badge-pendiente';
        tr.innerHTML = `
            <td class="id-col">${s.id ?? ''}</td>
            <td class="nit-col">${s.nit ?? ''}</td>
            <td class="razon-col">${s.razonSocial ?? ''}</td>
            <td class="usuario-col">${s.usuarioNombre ?? ''}</td>
            <td class="email-col">${s.usuarioEmail ?? ''}</td>
            <td class="estado-col"><span class="${badge}">${estadoTxt}</span></td>
            <td class="fecha-col">${s.fecha ? new Date(s.fecha).toLocaleDateString('es-CO') : ''}</td>
            <td class="acciones-col">
                <button class="accion-btn confirm" onclick="cambiarEstadoSolicitud(${s.id}, 'CONFIRMADA')">Confirmar</button>
                <button class="accion-btn deny" onclick="cambiarEstadoSolicitud(${s.id}, 'DENEGADA')">Denegar</button>
            </td>`;
        tbody.appendChild(tr);
    });
    const wrapper = tbody.closest('.table-wrapper');
    if (wrapper) wrapper.scrollLeft = 0;
}

function actualizarControlesUsuarios(){
    const info = document.getElementById('usuarios-page-info');
    const prev = document.getElementById('usuarios-prev');
    const next = document.getElementById('usuarios-next');
    const filtro = (document.getElementById('buscar-usuario')?.value || '').toLowerCase();
    const totalFiltrado = usuariosData.filter(u => {
        if (!filtro) return true;
        return ((u.nombres||'') + ' ' + (u.apellidos||'')).toLowerCase().includes(filtro) || (u.email||'').toLowerCase().includes(filtro);
    }).length;
    const maxPage = Math.max(1, Math.ceil(totalFiltrado / usuariosPageSize));
    if (info) info.textContent = `Página ${usuariosPagina} de ${maxPage}`;
    const usize = document.getElementById('usuarios-page-size');
    const allSelected = usize && usize.value === 'all';
    if (prev) prev.disabled = allSelected || usuariosPagina <= 1;
    if (next) next.disabled = allSelected || usuariosPagina >= maxPage;
}

function actualizarControlesSolicitudes(){
    const info = document.getElementById('solicitudes-page-info');
    const prev = document.getElementById('solicitudes-prev');
    const next = document.getElementById('solicitudes-next');
    const total = solicitudesData.length;
    const maxPage = Math.max(1, Math.ceil(total / solicitudesPageSize));
    if (info) info.textContent = `Página ${solicitudesPagina} de ${maxPage}`;
    const ssize = document.getElementById('solicitudes-page-size');
    const allSelected = ssize && ssize.value === 'all';
    if (prev) prev.disabled = allSelected || solicitudesPagina <= 1;
    if (next) next.disabled = allSelected || solicitudesPagina >= maxPage;
}

function configurarSidebar(){
    const links = document.querySelectorAll('.sidebar .menu a[data-target]');
    const sections = document.querySelectorAll('.content .section');
    const title = document.getElementById('section-title');
    links.forEach(a => a.addEventListener('click', e => {
        e.preventDefault();
        links.forEach(l => l.classList.remove('active'));
        a.classList.add('active');
        const target = a.getAttribute('data-target');
        sections.forEach(s => s.classList.remove('visible'));
        const sectionEl = document.getElementById(`section-${target}`);
        if (sectionEl) sectionEl.classList.add('visible');
        if (title) {
            if (target === 'dashboard') title.textContent = 'Panel de Administración';
            else title.textContent = a.textContent.trim();
        }
        if (target === 'usuarios') cargarUsuarios();
        if (target === 'solicitudes') cargarSolicitudes();
        if (target === 'dashboard') cargarMetricasDashboard();
    }));
}

function prepararLogout(){
    const link = document.getElementById('logout-link');
    if (!link) return;
    link.addEventListener('click', async (e) => {
        e.preventDefault();
        try {
            const r = await fetch('/api/auth/logout');
            if (r.ok){
                const data = await r.json().catch(()=>({redirect:'/'}));
                window.location.href = data.redirect || '/';
            } else {
                mostrarAlerta('Error','No se pudo cerrar sesión');
            }
        } catch(err){
            console.error(err);
            mostrarAlerta('Error','Error de red al cerrar sesión');
        }
    });
}

async function cambiarEstadoSolicitud(id, estado){
    try {
        const r = await fetch(`/api/admin/solicitudes/${id}/cambiar-estado`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({estado})});
        if (!r.ok){ mostrarAlerta('Error','No se pudo cambiar estado'); return; }
        cargarSolicitudes();
    } catch(e){ console.error(e); mostrarAlerta('Error','Error de red'); }
}

async function cargarMetricasDashboard(){
    try {
        const res = await fetch('/api/admin/metrics');
        if (!res.ok) throw new Error('metrics response not ok');
        const data = await res.json();
        const formato = new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 });
        setText('kpi-total-usuarios', data.totalUsuarios ?? '0');
        setText('kpi-total-operadores', data.totalOperadores ?? '0');
        setText('kpi-total-paquetes', data.totalPaquetes ?? '0');
        setText('kpi-reservas-activas', data.reservasActivas ?? '0');
        setText('kpi-ingresos-mes', formato.format(data.ingresosMes ?? 0));
        return;
    } catch (err) {
        console.warn('Fallo API /api/admin/metrics, usando fallback', err);
    }
    await cargarMetricasDashboardFallback();
}

async function cargarMetricasDashboardFallback(){
    try {
        const usuarios = await fetch('/api/admin/usuarios').then(r => r.ok ? r.json() : []).catch(() => []);
        const solicitudes = await fetch('/api/admin/solicitudes').then(r => r.ok ? r.json() : []).catch(() => []);

        const activos = usuarios.filter(u => {
            const v = u && 'estadoCuenta' in u ? u.estadoCuenta : undefined;
            if (typeof v === 'boolean') return v;
            if (typeof v === 'string') return v.toLowerCase() === 'true';
            return false;
        });
        const totalUsuarios = (activos.length > 0 ? activos.length : usuarios.length);
        const baseOperadores = (activos.length > 0 ? activos : usuarios);
        const totalOperadores = baseOperadores.filter(u => incluyeRol(u, ['OPERADOR','PROVEEDOR','OPERADOR_TURISTICO'])).length;

        const totalPaquetes = solicitudes.length;
        const reservasActivas = solicitudes.filter(s => {
            const estado = (s.estado || '').toString().toUpperCase();
            return estado.includes('CONFIRM') || estado.includes('APROB');
        }).length;

        setText('kpi-total-usuarios', totalUsuarios);
        setText('kpi-total-operadores', totalOperadores);
        setText('kpi-total-paquetes', totalPaquetes);
        setText('kpi-reservas-activas', reservasActivas);

        try{
            const ri = await fetch('/api/admin/ingresos-mes');
            const ji = ri.ok ? await ri.json() : { ingresosMes: 0 };
            const ingresos = Number(ji.ingresosMes || 0);
            const formato = new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 });
            setText('kpi-ingresos-mes', formato.format(ingresos));
        }catch(_){
            const formato = new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 });
            setText('kpi-ingresos-mes', formato.format(0));
        }
    } catch (e) {
        console.error('Error cargando métricas', e);
    }
}

function incluyeRol(u, posibles){
    const roles = Array.isArray(u?.roles) ? u.roles : [];
    return roles.some(r => posibles.includes(String(r).toUpperCase()));
}

function setText(id, value){
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}
// Reutilizamos mostrarAlerta de activar-modales.js para feedback

// --- Copiar valores de columnas (NIT, Razón Social, Email) en Solicitudes ---
function inicializarCopiasSolicitudes(){
    const tbody = document.getElementById('tbody-solicitudes');
    if (!tbody) return;
    tbody.addEventListener('click', (e) => {
        const td = e.target.closest('td');
        if (!td) return;
        if (td.classList.contains('nit-col') || td.classList.contains('razon-col') || td.classList.contains('email-col')){
            const texto = td.textContent.trim();
            if (texto) copiarTextoConToast(texto);
        }
    });
}

function copiarTextoConToast(texto){
    // Copiar al portapapeles
    if (navigator.clipboard && navigator.clipboard.writeText){
        navigator.clipboard.writeText(texto).catch(()=>fallbackCopy(texto));
    } else {
        fallbackCopy(texto);
    }
    mostrarToastCopiado(texto);
}

function fallbackCopy(texto){
    try {
        const ta = document.createElement('textarea');
        ta.value = texto; ta.style.position='fixed'; ta.style.top='-1000px';
        document.body.appendChild(ta); ta.select(); document.execCommand('copy'); document.body.removeChild(ta);
    } catch(_){/* ignore */}
}

function mostrarToastCopiado(texto){
    const ya = document.querySelector('.toast-copiado');
    if (ya) ya.remove();
    const div = document.createElement('div');
    div.className = 'toast-copiado';
    div.textContent = 'Copiado: ' + texto;
    document.body.appendChild(div);
    setTimeout(() => { div.classList.add('fade-out'); }, 1200);
    setTimeout(() => { div.remove(); }, 1800);
}
