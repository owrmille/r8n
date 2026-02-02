const results = document.getElementById('results');
const input = document.getElementById('searchInput');
const modal = document.getElementById('modal');
const modalText = document.getElementById('modalText');

document.getElementById('searchBtn').onclick = search;
input.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    search();
  }
});

function search() {
  const q = input.value.trim();
  results.innerHTML = '';
  if (!q) return;

  ['maria', 'michael'].forEach(user => {
    const name = `${user}_${q}`;
    const div = document.createElement('div');
    div.className = 'result';
    div.innerHTML = `
      <span>${name}</span>
      <button class="btn secondary">Request access</button>
    `;
    div.querySelector('button').onclick = () => openModal(user);
    results.appendChild(div);
  });
}

function openModal(user) {
  modalText.textContent = `${user} approved access!`;
  modal.style.display = 'flex';
}

document.getElementById('cancelBtn').onclick = () => {
  modal.style.display = 'none';
};


