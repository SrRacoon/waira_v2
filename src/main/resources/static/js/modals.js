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
