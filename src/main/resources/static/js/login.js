/**
 * Login page JavaScript
 * Handles form submission, password visibility toggle, and error display
 */

document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('login-form');
    const passwordInput = document.getElementById('password');
    const togglePasswordBtn = document.getElementById('toggle-password');
    const toggleIcon = document.getElementById('toggle-icon');
    const loginBtn = document.getElementById('login-btn');
    const loadingSpinner = document.getElementById('loading-spinner');
    const errorAlert = document.getElementById('error-alert');
    const successAlert = document.getElementById('success-alert');

    // Check URL parameters for error or logout messages
    const urlParams = new URLSearchParams(window.location.search);
    const hasError = urlParams.get('error');
    const hasLogout = urlParams.get('logout');

    if (hasError) {
        showError('用户名或密码错误');
    }

    if (hasLogout) {
        showSuccess('您已成功退出登录');
    }

    // Password visibility toggle
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

    // Form submission
    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // Clear previous alerts
        hideAlerts();

        // Validate form
        const username = document.getElementById('username').value.trim();
        const password = passwordInput.value;

        if (!username || !password) {
            showError('请输入用户名和密码');
            return;
        }

        // Show loading spinner
        loginBtn.disabled = true;
        loginBtn.classList.add('d-none');
        loadingSpinner.classList.remove('d-none');

        // Submit form normally (Spring Security handles authentication)
        loginForm.submit();
    });

    // Helper functions
    function showError(message) {
        document.getElementById('error-message').textContent = message;
        errorAlert.classList.remove('d-none');
        successAlert.classList.add('d-none');

        // Scroll to top to show error
        window.scrollTo({ top: 0, behavior: 'smooth' });

        // Auto-hide after 5 seconds
        setTimeout(() => {
            errorAlert.classList.add('d-none');
        }, 5000);
    }

    function showSuccess(message) {
        document.getElementById('success-message').textContent = message;
        successAlert.classList.remove('d-none');
        errorAlert.classList.add('d-none');

        // Scroll to top to show message
        window.scrollTo({ top: 0, behavior: 'smooth' });

        // Auto-hide after 3 seconds
        setTimeout(() => {
            successAlert.classList.add('d-none');
        }, 3000);
    }

    function hideAlerts() {
        errorAlert.classList.add('d-none');
        successAlert.classList.add('d-none');
    }

    // Add enter key support for password field
    passwordInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            loginForm.dispatchEvent(new Event('submit'));
        }
    });

    // Focus username field on page load
    document.getElementById('username').focus();
});
