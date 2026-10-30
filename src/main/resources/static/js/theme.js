// Light / dark color mode with Bootstrap 5.3 color modes (data-bs-theme on <html>).
// Loaded without "defer" in <head> so the theme is applied before the first paint.
// The choice is kept in localStorage; without a choice the operating system preference is used.
(() => {
    const STORAGE_KEY = 'tensio-theme';

    const stored = () => {
        try {
            return localStorage.getItem(STORAGE_KEY);
        } catch {
            return null; // storage blocked (private mode, policies)
        }
    };
    const preferred = () => stored() ?? (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');

    const apply = theme => {
        document.documentElement.setAttribute('data-bs-theme', theme);
        document.dispatchEvent(new CustomEvent('tensio:themechange', {detail: {theme}}));
    };

    apply(preferred());

    // Follow the operating system while the user has not chosen a theme
    matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
        if (!stored()) {
            apply(preferred());
        }
    });

    // Any element with data-theme-toggle switches between light and dark
    document.addEventListener('click', event => {
        if (!event.target.closest('[data-theme-toggle]')) {
            return;
        }
        const next = document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
        try {
            localStorage.setItem(STORAGE_KEY, next);
        } catch {
            // the theme still changes for this page
        }
        apply(next);
    });
})();
