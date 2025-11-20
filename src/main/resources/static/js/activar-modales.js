function abrirModal(idModal) {
    document.getElementById(idModal).style.display = 'flex';
}

function cerrarModal(idModal) {
    document.getElementById(idModal).style.display = 'none';
}

function cambiarModal(idCerrarModal, idAbrirModal) {
    cerrarModal(idCerrarModal);
    abrirModal(idAbrirModal);
}

function mostrarAlerta(titulo, mensaje) {
    document.getElementById("alerta-titulo").textContent = titulo;
    document.getElementById("alerta-mensaje").textContent = mensaje;
    const botonesDiv = document.getElementById("alerta-botones");
    botonesDiv.innerHTML = '<button type="button" class="boton" onclick="cerrarModalAlerta()">Aceptar</button>';
    abrirModal("modal-alerta");
}

function mostrarAlertaConConfirmacion(titulo, mensaje) {
    document.getElementById("alerta-titulo").textContent = titulo;
    document.getElementById("alerta-mensaje").textContent = mensaje;
    const botonesDiv = document.getElementById("alerta-botones");
    botonesDiv.innerHTML = `
        <button type="button" class="boton" onclick="confirmarAccion()">Confirmar</button>
        <button type="button" class="boton" style="background-color: #ccc; color: #333;" onclick="cerrarModalAlerta()">Cancelar</button>
    `;
    abrirModal("modal-alerta");
}

function cerrarModalAlerta() {
    cerrarModal("modal-alerta");
}

function aceptarAlertaYAbrir(idModal){
    cerrarModalAlerta();
    abrirModal(idModal);
}

function mostrarAlertaLuegoAbrir(titulo, mensaje, idModal){
    document.getElementById("alerta-titulo").textContent = titulo;
    document.getElementById("alerta-mensaje").textContent = mensaje;
    const botonesDiv = document.getElementById("alerta-botones");
    botonesDiv.innerHTML = `<button type="button" class="boton" onclick="aceptarAlertaYAbrir('${idModal}')">Aceptar</button>`;
    abrirModal("modal-alerta");
}

async function abrirModalSolicitud() {
    try {
        const response = await fetch("/api/auth/verificar-solicitud");
        const data = await response.json();
        
        if (data.tieneSolicitud) {
            const estado = (data.estado || '').toString().toUpperCase();
            // Treat CONFIRMADA (or Aprobado variants) as approved
            if (estado === 'CONFIRMADA' || estado === 'APROBADA' || estado === 'APROBADO') {
                mostrarAlerta("¡Ya eres proveedor!", "Tu solicitud fue aprobada. Ahora eres proveedor en la plataforma.");
            } else if (estado === 'DENEGADA') {
                mostrarAlertaLuegoAbrir("Solicitud Denegada", "Tu solicitud anterior fue denegada. Puedes volver a solicitar.", "solicitud-proveedor");
            } else {
                mostrarAlerta("Solicitud Existente", `Ya tienes una solicitud en estado: ${data.estado || 'PENDIENTE'}`);
            }
        } else {
            abrirModal("solicitud-proveedor");
        }
    } catch (error) {
        console.error("Error verificando solicitud:", error);
        abrirModal("solicitud-proveedor");
    }
}