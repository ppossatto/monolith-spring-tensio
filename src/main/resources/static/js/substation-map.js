// Maps of substations with Leaflet and OpenStreetMap tiles.
//
// <div data-substation-map> shows one marker per element carrying data-lat, data-lng and data-label
// (data-href, when present, becomes a link in the popup):
// - with data-markers="<css selector>", the markers come from the matching elements (for example the rows
//   of a table) and are read again after every HTMX swap;
// - without it, the map element itself carries the coordinates.
(() => {
    const TILES = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png';
    // Required by the OpenStreetMap tile usage policy
    const ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';
    // Shown when there is nothing to mark: Brazil
    const DEFAULT_CENTER = [-14.2, -51.9];
    const DEFAULT_ZOOM = 4;
    const SINGLE_ZOOM = 14;
    const maps = new Map();

    function points(element) {
        const sources = element.dataset.markers
            ? [...document.querySelectorAll(element.dataset.markers)]
            : [element];
        return sources
            .map(source => ({
                lat: Number.parseFloat(source.dataset.lat),
                lng: Number.parseFloat(source.dataset.lng),
                label: source.dataset.label || '',
                href: source.dataset.href
            }))
            .filter(point => Number.isFinite(point.lat) && Number.isFinite(point.lng));
    }

    // Popup content built with DOM nodes: labels come from user data and are never parsed as HTML
    function popup(point) {
        const content = document.createElement(point.href ? 'a' : 'strong');
        content.textContent = point.label;
        if (point.href) {
            content.href = point.href;
        }
        return content;
    }

    function update(element) {
        const {map, layer} = maps.get(element);
        layer.clearLayers();
        const found = points(element);
        found.forEach(point => L.marker([point.lat, point.lng], {title: point.label})
            .bindPopup(popup(point))
            .addTo(layer));
        if (found.length === 0) {
            map.setView(DEFAULT_CENTER, DEFAULT_ZOOM);
        } else if (found.length === 1) {
            map.setView([found[0].lat, found[0].lng], SINGLE_ZOOM);
        } else {
            map.fitBounds(layer.getBounds(), {padding: [32, 32], maxZoom: SINGLE_ZOOM});
        }
    }

    function init(element) {
        const map = L.map(element, {scrollWheelZoom: false});
        L.tileLayer(TILES, {maxZoom: 19, attribution: ATTRIBUTION}).addTo(map);
        maps.set(element, {map, layer: L.featureGroup().addTo(map)});
        update(element);
    }

    document.addEventListener('DOMContentLoaded', () => {
        document.querySelectorAll('[data-substation-map]').forEach(init);
    });
    // Search, filters and pagination replace the table: refresh the markers of the maps that read it
    document.addEventListener('htmx:afterSettle', () => {
        maps.forEach((_, element) => {
            if (element.dataset.markers) {
                update(element);
            }
        });
    });
})();
