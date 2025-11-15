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
    abrirModal("modal-alerta");
}

function cerrarModalAlerta() {
    cerrarModal("modal-alerta");
}

async function abrirModalSolicitud() {
    try {
        const response = await fetch("/api/auth/verificar-solicitud");
        const data = await response.json();
        
        if (data.tieneSolicitud) {
            mostrarAlerta("Solicitud Existente", "Ya tienes una solicitud de proveedor pendiente o aprobada");
        } else {
            abrirModal("solicitud-proveedor");
        }
    } catch (error) {
        console.error("Error verificando solicitud:", error);
        abrirModal("solicitud-proveedor");
    }
}