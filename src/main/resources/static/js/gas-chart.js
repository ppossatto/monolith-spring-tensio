// Renders every <canvas data-gas-chart> as a radar chart (latest analysis vs 12-month average).
// Values are percentages of each gas reference; "null" means not measured / no reference.
htmx.onLoad(root => {
    root.querySelectorAll('canvas[data-gas-chart]').forEach(canvas => {
        Chart.getChart(canvas)?.destroy();
        const parse = s => JSON.parse('[' + s + ']');

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
                scales: { r: { beginAtZero: true, ticks: { callback: v => v + '%' } } }
            }
        });
    });
});
