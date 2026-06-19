/* ===== Toast 通知系统 ===== */
function showToast(msg, type = 'success', duration = 3000) {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const colors = {
    success: { bg:'#F0FFF4', color:'#22543D', border:'#C6F6D5', icon:'✅' },
    error:   { bg:'#FFF5F5', color:'#742A2A', border:'#FED7D7', icon:'❌' },
    warning: { bg:'#FFFAF0', color:'#744210', border:'#FEEBC8', icon:'⚠️' },
    info:    { bg:'#EBF8FF', color:'#2A4365', border:'#BEE3F8', icon:'ℹ️' },
  };
  const c = colors[type] || colors.info;

  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.innerHTML = `
    <span class="toast-icon">${c.icon}</span>
    <span class="toast-msg">${msg}</span>
    <button class="toast-close" onclick="this.parentElement.classList.add('hiding');setTimeout(()=>this.parentElement.remove(),300)">✕</button>
  `;
  container.appendChild(el);

  if (duration > 0) {
    setTimeout(() => {
      el.classList.add('hiding');
      setTimeout(() => el.remove(), 300);
    }, duration);
  }
}
