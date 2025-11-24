(function(){
    document.addEventListener('DOMContentLoaded', () => {
        const form = document.querySelector('.crear-servicio-form');
        if (!form) return;
        const requiresImages = form.dataset.requiresImages !== 'false';

        const precioHidden = document.getElementById('precio');
        const precioDisplay = document.getElementById('precioDisplay');
        const imagenesInput = document.getElementById('imagenes');
        const imagenesHelp = document.querySelector('[data-imagenes-help]');
        const categoriaHelp = document.querySelector('[data-categoria-help]');
        const categoriaInputs = Array.from(document.querySelectorAll('[data-categoria-checkbox]'));
        const subcategoriaInputs = Array.from(document.querySelectorAll('[data-subcategoria-checkbox]'));
        const modal = document.getElementById('confirmModal');
        const modalCancel = document.getElementById('modalCancel');
        const modalConfirm = document.getElementById('modalConfirm');
        const cancelBtn = document.querySelector('[data-cancel-btn]');
        const fileTrigger = document.querySelector('[data-file-trigger]');
        let confirmReady = false;

        const formatter = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 });

        const parseCurrency = (value) => {
            if (!value) return NaN;
            const cleaned = value
                .replace(/[^0-9,\.]/g, '')
                .replace(/\.(?=\d{3})/g, '')
                .replace(',', '.');
            return cleaned ? parseFloat(cleaned) : NaN;
        };

        const formatDisplay = () => {
            if (!precioHidden || !precioDisplay) return;
            const currentValue = parseFloat(precioHidden.value);
            if (!isNaN(currentValue)) {
                precioDisplay.value = formatter.format(currentValue);
            }
        };

        const actualizarLeyendaImagenes = () => {
            if (!imagenesHelp || !imagenesInput) return;
            const count = imagenesInput.files?.length || 0;
            if (!requiresImages && count === 0) {
                imagenesHelp.textContent = 'Mantendrás tus imágenes actuales. Si deseas reemplazarlas, carga entre 2 y 10 archivos (máx. 5 MB c/u).';
                return;
            }
            imagenesHelp.textContent = `Selecciona entre 2 y 10 imágenes JPG o PNG (máx. 5 MB c/u). Actualmente: ${count} seleccionadas.`;
        };

        const validarImagenes = () => {
            if (!imagenesInput) return true;
            const count = imagenesInput.files?.length || 0;
            if (!requiresImages && count === 0) {
                imagenesInput.setCustomValidity('');
                return true;
            }
            if (count < 2 || count > 10) {
                imagenesInput.setCustomValidity('Selecciona entre 2 y 10 imágenes para continuar.');
                return false;
            }
            imagenesInput.setCustomValidity('');
            return true;
        };

        fileTrigger?.addEventListener('click', () => imagenesInput?.click());

        cancelBtn?.addEventListener('click', () => {
            window.location.href = '/dashboard';
        });

        if (imagenesInput) {
            imagenesInput.addEventListener('change', () => {
                validarImagenes();
                actualizarLeyendaImagenes();
            });
            actualizarLeyendaImagenes();
        }

        if (precioHidden && precioDisplay) {
            if (precioHidden.value) {
                formatDisplay();
            }

            precioDisplay.addEventListener('focus', () => {
                const value = parseFloat(precioHidden.value);
                precioDisplay.value = !isNaN(value) ? value.toString() : '';
            });

            precioDisplay.addEventListener('blur', () => {
                const parsed = parseCurrency(precioDisplay.value);
                if (!isNaN(parsed)) {
                    precioHidden.value = parsed.toFixed(2);
                    precioDisplay.value = formatter.format(parsed);
                } else {
                    precioHidden.value = '';
                    precioDisplay.value = '';
                }
            });

            precioDisplay.addEventListener('input', () => {
                precioDisplay.setCustomValidity('');
                const parsed = parseCurrency(precioDisplay.value);
                if (!isNaN(parsed)) {
                    precioHidden.value = parsed.toFixed(2);
                }
            });
        }

        const actualizaMensajeCategorias = () => {
            if (!categoriaHelp || !categoriaInputs.length) return;
            const total = categoriaInputs.filter(input => input.checked).length;
            if (total === 0) {
                categoriaHelp.textContent = 'Selecciona al menos una categoría para describir tu servicio.';
            } else {
                categoriaHelp.textContent = `${total} categoría${total > 1 ? 's' : ''} seleccionada${total > 1 ? 's' : ''}. Puedes activar subcategorías específicas.`;
                categoriaHelp.removeAttribute('data-error');
            }
        };

        const sincronizarSubcategorias = (categoriaId, habilitar) => {
            subcategoriaInputs
                .filter(sub => sub.dataset.parentId === String(categoriaId))
                .forEach(sub => {
                    sub.disabled = !habilitar;
                    if (!habilitar) {
                        sub.checked = false;
                    }
                });
        };

        const validarCategorias = () => {
            if (!categoriaInputs.length) {
                return true;
            }
            const tieneSeleccion = categoriaInputs.some(input => input.checked);
            if (!tieneSeleccion) {
                categoriaHelp?.setAttribute('data-error', 'true');
            }
            return tieneSeleccion;
        };

        const openModal = () => {
            if (!modal) return;
            modal.classList.add('is-visible');
            modal.setAttribute('aria-hidden', 'false');
        };

        const closeModal = () => {
            if (!modal) return;
            modal.classList.remove('is-visible');
            modal.setAttribute('aria-hidden', 'true');
        };

        modalCancel?.addEventListener('click', closeModal);
        modal?.addEventListener('click', (event) => {
            if (event.target === modal) {
                closeModal();
            }
        });

        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && modal?.classList.contains('is-visible')) {
                closeModal();
            }
        });

        modalConfirm?.addEventListener('click', () => {
            confirmReady = true;
            closeModal();
            form.submit();
        });

        if (categoriaInputs.length) {
            categoriaInputs.forEach(input => {
                sincronizarSubcategorias(input.value, input.checked);
                input.addEventListener('change', () => {
                    sincronizarSubcategorias(input.value, input.checked);
                    actualizaMensajeCategorias();
                });
            });
            actualizaMensajeCategorias();
        }

        form.addEventListener('submit', (event) => {
            if (confirmReady) {
                return;
            }
            if (!form.checkValidity()) {
                form.reportValidity();
                event.preventDefault();
                return;
            }
            if (!validarImagenes()) {
                event.preventDefault();
                imagenesInput?.reportValidity();
                return;
            }
            if (!validarCategorias()) {
                event.preventDefault();
                categoriaInputs[0]?.focus();
                return;
            }
            event.preventDefault();
            precioDisplay?.setCustomValidity('');
            const parsed = parseCurrency(precioDisplay?.value || '');
            if (isNaN(parsed) || parsed <= 0) {
                if (precioDisplay) {
                    precioDisplay.setCustomValidity('Ingresa un precio mayor a 0');
                    precioDisplay.focus();
                    precioDisplay.reportValidity();
                }
                return;
            }
            openModal();
        });
    });
})();
