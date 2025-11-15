document.addEventListener("DOMContentLoaded", () => {
    const registroForm = document.querySelector("#registro-usuario form");
    const loginForm = document.querySelector("#login form");
    const solicitudForm = document.querySelector("#solicitud-proveedor form");
    
    if (registroForm) {
        registroForm.addEventListener("submit", (e) => {
            e.preventDefault();
            
            const nombres = document.getElementById("input-nombres").value;
            const apellidos = document.getElementById("input-apellidos").value;
            const tipoDocumento = document.getElementById("input-tipo-documento").value;
            const documento = document.getElementById("input-documento").value;
            const telefono = document.getElementById("input-telefono").value;
            const email = document.getElementById("input-email").value;
            const contrasena = document.getElementById("input-contraseña").value;
            const confirmContrasena = document.getElementById("input-confirm_contraseña").value;
            
            const datos = {
                nombres,
                apellidos,
                tipoDocumento,
                documento,
                telefono,
                email,
                contraseña: contrasena,
                confirmContraseña: confirmContrasena
            };
            
            fetch("/api/auth/registrar", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(datos)
            })
            .then(async response => {
                const data = await response.json().catch(() => null);
                if (!response.ok) {
                    throw new Error(data?.error || data?.message || 'Error en el servidor');
                }
                return data;
            })
            .then(data => {
                document.getElementById("error-registro").innerHTML = "";
                if (data?.redirect) {
                    window.location.href = data.redirect;
                } else {
                    cerrarModal("registro-usuario");
                    registroForm.reset();
                    location.reload();
                }
            })
            .catch(error => {
                document.getElementById("error-registro").innerHTML = error.message;
            });
        });
    }
    
    if (loginForm) {
        loginForm.addEventListener("submit", (e) => {
            e.preventDefault();
            
            const email = document.getElementById("login-email").value;
            const contrasena = document.getElementById("login-contrasena").value;
            
            const datos = {
                email,
                contrasena
            };
            
            fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(datos)
            })
            .then(async response => {
                const data = await response.json().catch(() => null);
                if (!response.ok) {
                    throw new Error(data?.error || data?.message || 'Error en el servidor');
                }
                return data;
            })
            .then(data => {
                document.getElementById("error-login").innerHTML = "";
                if (data?.redirect) {
                    window.location.href = data.redirect;
                } else {
                    cerrarModal("login");
                    loginForm.reset();
                    location.reload();
                }
            })
            .catch(error => {
                document.getElementById("error-login").innerHTML = error.message;
            });
        });
    }

    if (solicitudForm) {
        solicitudForm.addEventListener("submit", (e) => {
            e.preventDefault();
            
            const nit = document.getElementById("prov-nit").value;
            const razonSocial = document.getElementById("prov-razon-social").value;
            const tipoVia = document.getElementById("prov-tipo-via").value;
            const numero = document.getElementById("prov-numero").value;
            const complemento = document.getElementById("prov-complemento").value;
            const barrio = document.getElementById("prov-barrio").value;
            const ciudad = document.getElementById("prov-ciudad").value;
            
            const datos = {
                nit,
                razonSocial,
                tipoVia,
                numero,
                complemento,
                barrio,
                ciudad
            };
            
            fetch("/api/auth/solicitud-proveedor", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(datos)
            })
            .then(async response => {
                const data = await response.json().catch(() => null);
                if (!response.ok) {
                    throw new Error(data?.error || 'Error en el servidor');
                }
                return data;
            })
            .then(data => {
                document.getElementById("error-solicitud-proveedor").innerHTML = "";
                mostrarAlerta("¡Solicitud Enviada!", data?.message || "Solicitud enviada exitosamente. Plazo de respuesta: 15 días");
                cerrarModal("solicitud-proveedor");
                solicitudForm.reset();
                setTimeout(() => location.reload(), 1500);
            })
            .catch(error => {
                mostrarAlerta("Error", error.message);
            });
        });
    }
});
