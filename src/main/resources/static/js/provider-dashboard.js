(function () {
    const normalize = (value) => {
        if (!value) {
            return '';
        }
        const text = value.toString();
        return typeof text.normalize === 'function'
            ? text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
            : text.toLowerCase();
    };

    const initSearchFilter = () => {
        const searchInput = document.querySelector('[data-servicios-search]');
        const serviciosWrapper = document.querySelector('.provider-servicios .servicios-wrapper');
        const emptyState = document.querySelector('[data-empty-search]');

        if (!searchInput || !serviciosWrapper) {
            return;
        }

        const cards = Array.from(serviciosWrapper.querySelectorAll('[data-servicio-card]'));
        if (!cards.length) {
            return;
        }

        const cardCache = cards.map((card) => {
            const titleEl = card.querySelector('h2');
            const descEl = card.querySelector('.servicio-desc');
            const title = titleEl ? titleEl.textContent : '';
            const description = descEl ? descEl.textContent : '';
            return { card, tokens: normalize(`${title} ${description}`) };
        });

        const applyFilter = (query) => {
            const needle = normalize(query);
            let visible = 0;

            cardCache.forEach(({ card, tokens }) => {
                const matches = !needle || tokens.includes(needle);
                card.hidden = !matches;
                if (matches) {
                    visible += 1;
                }
            });

            if (emptyState) {
                emptyState.hidden = visible !== 0;
            }
        };

        searchInput.addEventListener('input', (event) => {
            applyFilter(event.target.value);
        });

        searchInput.addEventListener('search', (event) => {
            if (!event.target.value) {
                applyFilter('');
            }
        });

        applyFilter(searchInput.value || '');
    };

    const initPreviewModal = () => {
        const modal = document.querySelector('[data-preview-modal]');
        if (!modal) {
            return;
        }

        const imageEl = modal.querySelector('[data-preview-image]');
        const placeholder = modal.querySelector('[data-preview-placeholder]');
        const titleEl = modal.querySelector('[data-preview-title]');
        const cityEl = modal.querySelector('[data-preview-city]');
        const stateEl = modal.querySelector('[data-preview-state]');
        const priceEl = modal.querySelector('[data-preview-price]');
        const durationEl = modal.querySelector('[data-preview-duration]');
        const viewsEl = modal.querySelector('[data-preview-views]');
        const addressEl = modal.querySelector('[data-preview-address]');
        const descriptionEl = modal.querySelector('[data-preview-description]');
        const closeButtons = modal.querySelectorAll('[data-preview-close]');

        const parseImages = (value) => {
            if (!value) {
                return [];
            }
            return value.split('|').map((url) => url.trim()).filter(Boolean);
        };

        const openModal = (dataset) => {
            if (titleEl) titleEl.textContent = dataset.nombre || 'Servicio sin nombre';
            if (cityEl) cityEl.textContent = dataset.ciudad || 'Ubicación pendiente';
            if (stateEl) stateEl.textContent = dataset.estado || 'Sin estado';
            if (priceEl) priceEl.textContent = dataset.precio || 'Precio no definido';
            if (durationEl) durationEl.textContent = dataset.duracion || 'Duración no definida';
            if (viewsEl) viewsEl.textContent = dataset.vistas || '0 vistas';
            if (addressEl) addressEl.textContent = dataset.direccion || 'Dirección no disponible';
            if (descriptionEl) descriptionEl.textContent = dataset.descripcion || 'Sin descripción disponible.';

            const urls = parseImages(dataset.imagenes);
            if (urls.length && imageEl) {
                imageEl.style.backgroundImage = `url(${urls[0]})`;
                if (placeholder) {
                    placeholder.hidden = true;
                }
            } else if (imageEl) {
                imageEl.style.removeProperty('background-image');
                if (placeholder) {
                    placeholder.hidden = false;
                }
            }

            modal.classList.add('is-visible');
            modal.setAttribute('aria-hidden', 'false');
            document.body.style.overflow = 'hidden';
        };

        const closeModal = () => {
            modal.classList.remove('is-visible');
            modal.setAttribute('aria-hidden', 'true');
            document.body.style.removeProperty('overflow');
        };

        closeButtons.forEach((button) => {
            button.addEventListener('click', closeModal);
        });

        modal.addEventListener('click', (event) => {
            if (event.target === modal) {
                closeModal();
            }
        });

        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && modal.classList.contains('is-visible')) {
                closeModal();
            }
        });

        document.addEventListener('click', (event) => {
            const trigger = event.target.closest('[data-preview-trigger]');
            if (!trigger) {
                return;
            }
            event.preventDefault();
            openModal(trigger.dataset || {});
        });
    };

    const onReady = () => {
        initSearchFilter();
        initPreviewModal();
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', onReady);
    } else {
        onReady();
    }
})();
