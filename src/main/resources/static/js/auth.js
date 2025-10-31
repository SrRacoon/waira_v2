document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.querySelector("#login-modal form");
    const registerClienteForm = document.querySelector("#registerCliente-modal form");
    const registerAdminForm = document.querySelector("#registerAdmin-modal form");
    const registerProveedorForm = document.querySelector("#registerProveedor-modal form");

    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const payload = {
            email: document.getElementById('login-email').value,
            contrasena: document.getElementById('login-contrasena').value
        };

        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: 'same-origin',
            body: JSON.stringify(payload)
        });

        const loginError = document.getElementById("login-error");

        if (response.ok) {
            const data = await response.json();
            window.location.href = data.redirect;
        } else {
            const data = await response.json();
            loginError.textContent = data.error;
        }
    });

    registerClienteForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const payload = {
            nombre: document.getElementById('reg-nombre').value,
            apellido: document.getElementById('reg-apellido').value,
            telefono: document.getElementById('reg-telefono').value,
            email: document.getElementById('reg-email').value,
            contrasena: document.getElementById('reg-contrasena').value,
            // por defecto, registramos como ROLE_CLIENTE
            nombreRol: 'ROLE_CLIENTE'
        };

        const response = await fetch("/api/auth/register/cliente", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: 'same-origin',
            body: JSON.stringify(payload)
        });

        const registerError = document.getElementById("register-error");

        if (response.ok) {
            const data = await response.json();
            window.location.href = data.redirect;
        } else {
            const data = await response.json();
            registerError.textContent = data.error;
        }
    });

    registerAdminForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const permisos = Array.from(registerAdminForm.querySelectorAll('input[name="permisos"]:checked')).map(i => i.value);

        const payload = {
            nombre: document.getElementById('admin-nombre').value,
            apellido: document.getElementById('admin-apellido').value,
            telefono: document.getElementById('admin-telefono').value,
            email: document.getElementById('admin-email').value,
            contrasena: document.getElementById('admin-contrasena').value,
            permisos: permisos,
            nombreRol: 'ROLE_ADMIN'
        };

        const response = await fetch("/api/auth/register/admin", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: 'same-origin',
            body: JSON.stringify(payload)
        });

        const registerError = document.getElementById("register-admin-error");

        if (response.ok) {
            const data = await response.json();
            window.location.href = data.redirect;
        } else {
            const data = await response.json();
            registerError.textContent = data.error;
        }
    });

    registerProveedorForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const payload = {
            nombre: document.getElementById('prov-nombre').value,
            apellido: document.getElementById('prov-apellido').value,
            telefono: document.getElementById('prov-telefono').value,
            email: document.getElementById('prov-email').value,
            razonSocial: document.getElementById('prov-company').value,
            nit: document.getElementById('prov-nit').value,
            direccion: document.getElementById('prov-address').value,
            contrasena: document.getElementById('prov-contrasena').value,
            nombreRol: 'ROLE_PROVEEDOR'
        };

        const response = await fetch("/api/auth/register/proveedor", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: 'same-origin',
            body: JSON.stringify(payload)
        });

        const registerError = document.getElementById("register-prov-error");

        if (response.ok) {
            const data = await response.json();
            window.location.href = data.redirect;
        } else {
            const data = await response.json();
            registerError.textContent = data.error;
        }
    });
});
