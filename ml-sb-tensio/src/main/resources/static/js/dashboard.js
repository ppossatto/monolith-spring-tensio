htmx.onLoad(root => {
    root.querySelectorAll('canvas[data-radar]').forEach(canvas => {
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
                scales: { r: { beginAtZero: true, ticks: { callback: v => v + '%' } } }
            }
        });
    });
});