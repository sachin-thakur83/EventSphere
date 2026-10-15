// EventSphere front-end helpers (no frameworks)
document.addEventListener('DOMContentLoaded', function () {

  // 1. Charts (Chart.js is loaded only on dashboard pages)
  var palette = ['#6d4aff', '#14123a', '#8b6dff', '#16a34a', '#d97706', '#dc2626', '#0ea5e9', '#a855f7'];
  document.querySelectorAll('canvas.chart').forEach(function (cv) {
    if (typeof Chart === 'undefined') return;
    var labels = cv.dataset.labels ? cv.dataset.labels.split('|') : [];
    var values = cv.dataset.values ? cv.dataset.values.split(',').map(Number) : [];
    var type = cv.dataset.type || 'bar';
    new Chart(cv, {
      type: type,
      data: { labels: labels, datasets: [{ data: values, backgroundColor: palette }] },
      options: { plugins: { legend: { display: type === 'doughnut' } }, scales: type === 'bar' ? { y: { beginAtZero: true, ticks: { precision: 0 } } } : {} }
    });
  });

  // 2. Confirm dialogs for destructive actions: <form data-confirm="...">
  document.querySelectorAll('form[data-confirm]').forEach(function (f) {
    f.addEventListener('submit', function (e) { if (!window.confirm(f.dataset.confirm)) e.preventDefault(); });
  });

  // 3. Loading feedback: disable submit buttons after click (delayed so the clicked button's value is still sent)
  document.querySelectorAll('form').forEach(function (f) {
    f.addEventListener('submit', function (e) {
      if (e.defaultPrevented) return;
      setTimeout(function () {
        f.querySelectorAll('button[type=submit],button:not([type])').forEach(function (b) { b.disabled = true; });
      }, 30);
    });
  });

  // 4. Live ticket total preview on the event page (the server recalculates the real total)
  var bf = document.getElementById('bookForm');
  if (bf) {
    var totalEl = document.getElementById('total');
    var update = function () {
      var sum = 0;
      bf.querySelectorAll('input[type=number]').forEach(function (i) { sum += (parseInt(i.value, 10) || 0) * (parseFloat(i.dataset.price) || 0); });
      totalEl.textContent = '\u20B9' + sum.toFixed(2);
    };
    bf.addEventListener('input', update);
    update();
  }
});

// Re-enable buttons if the page is restored from the browser's back/forward cache
window.addEventListener('pageshow', function () {
  document.querySelectorAll('form button').forEach(function (b) { b.disabled = b.hasAttribute('data-keep-disabled'); });
});
