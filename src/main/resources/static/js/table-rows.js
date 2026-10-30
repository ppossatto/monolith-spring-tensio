// Makes every <tr data-href="..."> clickable as a whole. Delegated on the document, so it also works
// for rows swapped in by HTMX. The first cell keeps a real link for keyboard and screen-reader users.
document.addEventListener('click', event => {
    const row = event.target.closest('tr[data-href]');
    if (!row || event.target.closest('a, button, input, select, textarea, label')) {
        return;
    }
    if (window.getSelection().toString()) {
        return; // the user is selecting text, not navigating
    }
    if (event.ctrlKey || event.metaKey) {
        window.open(row.dataset.href, '_blank', 'noopener');
    } else {
        window.location.assign(row.dataset.href);
    }
});
