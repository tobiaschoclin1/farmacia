// Gestión de sesión de usuario
(function() {
    'use strict';

    // Obtener usuario de la sesión
    function getCurrentUser() {
        const userJson = sessionStorage.getItem('user');
        if (!userJson) {
            // Si no hay sesión, redirigir al login (excepto si ya estamos en login/registro)
            if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/registro')) {
                window.location.href = '/login';
            }
            return null;
        }
        return JSON.parse(userJson);
    }

    // Actualizar información del usuario en el sidebar
    function updateUserProfile() {
        const user = getCurrentUser();
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
        window.location.href = '/login';
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
