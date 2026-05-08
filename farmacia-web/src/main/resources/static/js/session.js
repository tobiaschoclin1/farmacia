// Gestión de sesión de usuario
(function() {
    'use strict';

    let currentUser = null;

    // Obtener usuario de la sesión (primero intenta sessionStorage, luego servidor)
    async function getCurrentUser() {
        // Si ya tenemos el usuario en memoria, devolverlo
        if (currentUser) {
            return currentUser;
        }

        // Intentar obtener de sessionStorage
        const userJson = sessionStorage.getItem('user');
        if (userJson) {
            currentUser = JSON.parse(userJson);
            return currentUser;
        }

        // Si no está en sessionStorage, verificar en el servidor
        try {
            const response = await fetch('/api/auth/me');
            const data = await response.json();

            if (data.authenticated && data.user) {
                // Guardar en sessionStorage para futuras consultas
                sessionStorage.setItem('user', JSON.stringify(data.user));
                currentUser = data.user;
                return currentUser;
            }
        } catch (error) {
            console.error('Error al verificar autenticación:', error);
        }

        // Si no hay sesión, redirigir al login (excepto si ya estamos en login/registro)
        if (!window.location.pathname.includes('/login') &&
            !window.location.pathname.includes('/registro') &&
            !window.location.pathname.includes('/')) {
            window.location.href = '/login';
        }
        return null;
    }

    // Actualizar información del usuario en el sidebar
    async function updateUserProfile() {
        const user = await getCurrentUser();
        if (!user) return;

        // Actualizar nombre
        const nameElements = document.querySelectorAll('.sidebar-profile-name');
        nameElements.forEach(el => {
            el.textContent = user.nombre || 'Usuario';
        });

        // Actualizar rol
        const roleElements = document.querySelectorAll('.sidebar-profile-role');
        roleElements.forEach(el => {
            el.textContent = user.rol || 'Usuario';
        });

        // Actualizar avatar con iniciales
        const avatarElements = document.querySelectorAll('.sidebar-avatar');
        avatarElements.forEach(el => {
            const iniciales = user.nombre
                ? user.nombre.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()
                : 'U';
            el.textContent = iniciales;
        });
    }

    // Cerrar sesión
    window.logout = function() {
        sessionStorage.removeItem('user');
        currentUser = null;
        window.location.href = '/logout';
    };

    // Ejecutar al cargar la página
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', updateUserProfile);
    } else {
        updateUserProfile();
    }

    // Exponer función globalmente
    window.getCurrentUser = getCurrentUser;
})();
