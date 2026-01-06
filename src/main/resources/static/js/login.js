document.addEventListener('DOMContentLoaded', function() {
    const passwordInput = document.getElementById('password');
    const togglePasswordBtn = document.getElementById('toggle-password');
    const toggleIcon = document.getElementById('toggle-icon');
    const errorAlert = document.getElementById('error-alert');
    const successAlert = document.getElementById('success-alert');

    // Check URL parameters for logout messages
    const urlParams = new URLSearchParams(window.location.search);
    const hasLogout = urlParams.has('logout');

    // Initial display of error/success messages is now handled by Thymeleaf server-side.
    // The showError/showSuccess functions are kept for potential future client-side use.

    if (hasLogout) {
        showSuccess('您已成功退出登录');
    }

    // Password visibility toggle
    if(togglePasswordBtn) {
        togglePasswordBtn.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);

            // Toggle icon
            if (type === 'text') {
                toggleIcon.classList.remove('bi-eye');
                toggleIcon.classList.add('bi-eye-slash');
            } else {
                toggleIcon.classList.remove('bi-eye-slash');
                toggleIcon.classList.add('bi-eye');
            }
        });
    }


    // Helper functions
    function showError(message) {
        const errorMessage = document.getElementById('error-message');
        if(errorMessage) {
            errorMessage.textContent = message;
        }
        if(errorAlert) {
            errorAlert.classList.remove('d-none');
        }
        if(successAlert) {
            successAlert.classList.add('d-none');
        }

        // Auto-hide after 5 seconds
        setTimeout(() => {
            if(errorAlert) {
                errorAlert.classList.add('d-none');
            }
        }, 5000);
    }

    function showSuccess(message) {
        const successMessage = document.getElementById('success-message');
        if(successMessage) {
            successMessage.textContent = message;
        }
        if(successAlert) {
            successAlert.classList.remove('d-none');
        }
        if(errorAlert) {
            errorAlert.classList.add('d-none');
        }

        // Auto-hide after 3 seconds
        setTimeout(() => {
            if(successAlert) {
                successAlert.classList.add('d-none');
            }
        }, 3000);
    }

    // Focus username field on page load
    const usernameInput = document.getElementById('username');
    if (usernameInput) {
        usernameInput.focus();
    }
});
