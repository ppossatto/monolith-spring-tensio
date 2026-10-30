// Renders every <canvas data-gas-chart> as a radar chart (latest analysis vs 12-month average).
// Values are percentages of each gas reference; "null" means not measured / no reference.
// Text and grid colors follow the Bootstrap color mode and are updated when the theme changes.
(() => {
    const parse = s => JSON.parse('[' + s + ']');

    const themeColors = () => {
        const css = getComputedStyle(document.documentElement);
        return {
            text: css.getPropertyValue('--bs-body-color').trim(),
            grid: css.getPropertyValue('--bs-border-color').trim()
        };
    };

    const render = canvas => {
        Chart.getChart(canvas)?.destroy();
        const colors = themeColors();

        new Chart(canvas, {
            type: 'radar',
            data: {
                labels: canvas.dataset.labels.split(','),
                datasets: [
                    {
                        label: 'Latest analysis',
                        data: parse(canvas.dataset.latest),
                        borderColor: 'rgb(13, 110, 253)',
                        backgroundColor: 'rgba(13, 110, 253, 0.2)'
                    },
                    {
                        label: '12-month average',
                        data: parse(canvas.dataset.average),
                        borderColor: 'rgb(220, 53, 69)',
                        backgroundColor: 'rgba(220, 53, 69, 0.2)'
                    }
                ]
            },
            options: {
                spanGaps: true,
                plugins: {legend: {labels: {color: colors.text}}},
                scales: {
                    r: {
                        beginAtZero: true,
                        grid: {color: colors.grid},
                        angleLines: {color: colors.grid},
                        pointLabels: {color: colors.text},
                        ticks: {color: colors.text, backdropColor: 'transparent', callback: v => v + '%'}
                    }
                }
            }
        });
    };

    const renderAll = root => root.querySelectorAll('canvas[data-gas-chart]').forEach(render);

    htmx.onLoad(renderAll);
    document.addEventListener('tensio:themechange', () => renderAll(document));
})();
