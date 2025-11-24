(function(){
  const fetchEstado = async () => {
    try {
      const response = await fetch('/api/auth/estado', { credentials: 'include' });
      if (!response.ok) {
        throw new Error('No se pudo validar la sesión');
      }
      return await response.json();
    } catch (error) {
      console.error('Estado de sesión no disponible', error);
      return { autenticado: false };
    }
  };

  const initGallery = () => {
    const mainImage = document.querySelector('[data-main-image]');
    const thumbs = document.querySelectorAll('[data-thumb]');
    if (!mainImage || !thumbs.length) {
      return;
    }

    const activateThumb = (thumb) => {
      const src = thumb?.dataset?.thumb;
      if (!src) {
        return;
      }
      mainImage.src = src;
      thumbs.forEach((btn) => btn.classList.remove('is-active'));
      thumb.classList.add('is-active');
    };

    thumbs.forEach((thumb) => {
      thumb.addEventListener('click', () => activateThumb(thumb));
    });

    activateThumb(thumbs[0]);
  };

  const initReservaButton = () => {
    const button = document.querySelector('[data-action="reservar"]');
    if (!button) {
      return;
    }
    button.addEventListener('click', async () => {
      const estado = await fetchEstado();
      if (!estado.autenticado) {
        if (typeof abrirModal === 'function') {
          abrirModal('login');
        } else {
          window.location.href = '/explorar';
        }
        return;
      }
      alert('Gracias por tu interés. Muy pronto podrás reservar este servicio en línea.');
    });
  };

  document.addEventListener('DOMContentLoaded', () => {
    initGallery();
    initReservaButton();
  });
})();
