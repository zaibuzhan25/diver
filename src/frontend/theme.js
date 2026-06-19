/* ===== 主题切换逻辑 ===== */
(function () {
  const root = document.documentElement;
  const STORE_KEY = 'theme';

  function getSystem() {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  function applyTheme(t) {
    // 先移除旧的内联主题样式（如果有）
    removeDarkInlineStyles();

    if (t === 'system') {
      root.removeAttribute('data-theme');
      localStorage.removeItem(STORE_KEY);
    } else {
      root.setAttribute('data-theme', t);
      localStorage.setItem(STORE_KEY, t);
    }
    updateUI(t);

    // 如果是深色模式，直接强制设置内联样式
    if (t === 'dark') {
      applyDarkInlineStyles();
    }
  }

  // 强制用 JS 设置深色内联样式，绕过所有 CSS 级联问题
  function applyDarkInlineStyles() {
    const css = `
      body { background: #080C14 !important; color: #E8EDF4 !important; }
      .header { background: rgba(8,12,20,0.88) !important; border-bottom-color: rgba(255,255,255,0.07) !important; }
      .tabs { background: #131820 !important; border-bottom-color: rgba(255,255,255,0.08) !important; }
      .plan-card, .hot-plan-card { background: #131820 !important; border-color: rgba(255,255,255,0.08) !important; }
      .dest-card { background: rgba(255,255,255,0.03) !important; border-color: rgba(255,255,255,0.08) !important; backdrop-filter: saturate(180%) blur(20px) !important; }
      .modal-panel, .dest-detail-card, .wizard-panel, .search-panel { background: #131820 !important; color: #E8EDF4 !important; }
      .detail-card { background: #131820 !important; color: #E8EDF4 !important; }
      .detail-card h3 { color: #E8EDF4 !important; }
      .detail-card .close-btn { background: rgba(255,255,255,0.06) !important; color: #8492A6 !important; }
      .audit-box-reject { background: rgba(239,68,68,0.15) !important; border-color: rgba(239,68,68,0.3) !important; }
      .audit-box-approve { background: rgba(16,185,129,0.15) !important; border-color: rgba(16,185,129,0.3) !important; }
      .itinerary-item { background: rgba(255,255,255,0.03) !important; }
      input, select, textarea { background: #1A2332 !important; color: #E8EDF4 !important; border-color: rgba(255,255,255,0.08) !important; }
    `;
    let styleEl = document.getElementById('dark-inline-styles');
    if (!styleEl) {
      styleEl = document.createElement('style');
      styleEl.id = 'dark-inline-styles';
      document.head.appendChild(styleEl);
    }
    styleEl.textContent = css;
  }

  function removeDarkInlineStyles() {
    const styleEl = document.getElementById('dark-inline-styles');
    if (styleEl) {
      styleEl.remove();
    }
  }

  // 暴露给全局，供 HTML onclick 调用
  window.setTheme = applyTheme;

  function updateUI(t) {
    // 支持 .theme-btn / .theme-option / .theme-opt 所有类名
    document.querySelectorAll('.theme-btn, .theme-option, .theme-opt').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.theme === t);
    });
  }

  function init() {
    const saved = localStorage.getItem(STORE_KEY);
    const theme = saved || 'system';
    applyTheme(theme);

    // 使用事件委托，兼容动态插入的按钮
    document.addEventListener('click', function(e) {
      const btn = e.target.closest('.theme-btn, .theme-option, .theme-opt');
      if (btn && btn.dataset.theme) {
        applyTheme(btn.dataset.theme);
      }
    });

    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
      if (!localStorage.getItem(STORE_KEY)) applyTheme('system');
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
