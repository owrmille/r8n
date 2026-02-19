
const details = document.getElementById('details');
let detailsOpen = false;

document.querySelectorAll('.toggle-details').forEach(cell => {
  cell.addEventListener('click', e => {
    e.stopPropagation();

    if (!detailsOpen) {
      details.classList.remove('hidden');
      detailsOpen = true;
    } else {
      details.classList.add('hidden');
      detailsOpen = false;
    }
  });
});

const headers = document.querySelectorAll('.th-filter');

  headers.forEach(th => {
    th.addEventListener('click', e => {
      e.stopPropagation();

      document.querySelectorAll('.filter-popup').forEach(p => {
        if (!th.contains(p)) p.classList.remove('open');
      });

      const popup = th.querySelector('.filter-popup');
      popup.classList.toggle('open');
    });
  });

  document.addEventListener('click', () => {
    document.querySelectorAll('.filter-popup')
      .forEach(p => p.classList.remove('open'));
  });

  const dateInput = document.getElementById('date-filter');
  if (dateInput) {
    const d = new Date();
    d.setMonth(d.getMonth() - 3);
    dateInput.value = d.toISOString().split('T')[0];
  }
