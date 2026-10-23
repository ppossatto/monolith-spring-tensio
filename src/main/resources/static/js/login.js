document.querySelectorAll('[data-password-toggle]').forEach(button => {
    const input = document.getElementById(button.dataset.passwordToggle);
    const icon = button.querySelector('.bi');

    button.addEventListener('click', () => {
        const visible = input.type === 'password';
        input.type = visible ? 'text' : 'password';
        icon.classList.toggle('bi-eye', !visible);
        icon.classList.toggle('bi-eye-slash', visible);
        button.setAttribute('aria-pressed', String(visible));
        button.setAttribute('aria-label', visible ? 'Hide password' : 'Show password');
        input.focus();
    });
});
