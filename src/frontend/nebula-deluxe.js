/**
 * ═══════════════════════════════════════════
 *  Nebula Deluxe — 自定义光标 | 拖尾 | 粒子爆炸 | 转场
 * ═══════════════════════════════════════════
 *  替换原生鼠标为发光拖尾圆点
 *  点击产生彩色粒子爆破
 *  页面切换全屏渐变过渡
 */

(function () {
  'use strict';

  // ═══════════════════════════════════
  //  自定义光标 + Canvas拖尾
  // ═══════════════════════════════════

  var cursorEl = null;
  var trailCanvas = null;
  var trailCtx = null;
  var trailPoints = [];
  var MAX_TRAIL = 25;
  var cursorX = -100;
  var cursorY = -100;
  var targetX = -100;
  var targetY = -100;
  var animId = null;
  var enabled = true;

  function initCursor() {
    if (window.innerWidth < 768) return;
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    if (cursorEl) return; // 已初始化

    // 游标元素
    cursorEl = document.createElement('div');
    cursorEl.className = 'cursor-custom';
    document.body.appendChild(cursorEl);

    // 拖尾 Canvas
    trailCanvas = document.createElement('canvas');
    trailCanvas.className = 'cursor-trail-canvas';
    document.body.appendChild(trailCanvas);
    trailCtx = trailCanvas.getContext('2d');
    resizeTrailCanvas();

    // 隐藏原生光标
    document.body.classList.add('cursor-hidden');

    bindCursorEvents();
    startTrailLoop();
  }

  function resizeTrailCanvas() {
    if (!trailCanvas) return;
    var dpr = Math.min(window.devicePixelRatio || 1, 2);
    trailCanvas.width = window.innerWidth * dpr;
    trailCanvas.height = window.innerHeight * dpr;
    trailCanvas.style.width = window.innerWidth + 'px';
    trailCanvas.style.height = window.innerHeight + 'px';
    if (trailCtx) trailCtx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  function bindCursorEvents() {
    document.addEventListener('mousemove', function (e) {
      targetX = e.clientX;
      targetY = e.clientY;
    });

    // hover 可交互元素时放大光标
    document.addEventListener('mouseover', function (e) {
      var target = e.target;
      if (
        target.closest('button') ||
        target.closest('a') ||
        target.closest('input') ||
        target.closest('.tilt-card') ||
        target.closest('.chip') ||
        target.closest('[onclick]')
      ) {
        if (cursorEl) cursorEl.classList.add('hovering');
      }
    });

    document.addEventListener('mouseout', function (e) {
      var target = e.target;
      if (
        target.closest('button') ||
        target.closest('a') ||
        target.closest('input') ||
        target.closest('.tilt-card') ||
        target.closest('.chip') ||
        target.closest('[onclick]')
      ) {
        if (cursorEl) cursorEl.classList.remove('hovering');
      }
    });

    window.addEventListener('resize', resizeTrailCanvas);
  }

  function startTrailLoop() {
    function loop() {
      // 光标点直跟 — 零延迟
      cursorX = targetX;
      cursorY = targetY;

      if (cursorEl) {
        cursorEl.style.left = cursorX + 'px';
        cursorEl.style.top = cursorY + 'px';
      }

      // 拖尾也从真实鼠标位置生成
      if (trailCtx && trailCanvas) {
        trailPoints.push({
          x: targetX,
          y: targetY,
          life: 1,
        });

        if (trailPoints.length > MAX_TRAIL) {
          trailPoints.shift();
        }

        trailCtx.clearRect(0, 0, trailCanvas.width, trailCanvas.height);

        for (var i = 0; i < trailPoints.length; i++) {
          var p = trailPoints[i];
          p.life -= 0.04;

          if (p.life > 0) {
            var alpha = p.life * 0.5;
            var size = p.life * 6;
            var grad = trailCtx.createRadialGradient(
              p.x, p.y, 0,
              p.x, p.y, size
            );
            grad.addColorStop(
              0,
              'rgba(0,240,255,' + alpha.toFixed(3) + ')'
            );
            grad.addColorStop(
              0.5,
              'rgba(124,58,237,' + (alpha * 0.5).toFixed(3) + ')'
            );
            grad.addColorStop(1, 'rgba(0,240,255,0)');

            trailCtx.beginPath();
            trailCtx.arc(p.x, p.y, size, 0, Math.PI * 2);
            trailCtx.fillStyle = grad;
            trailCtx.fill();
          }
        }

        trailPoints = trailPoints.filter(function (p) {
          return p.life > 0;
        });
      }

      animId = requestAnimationFrame(loop);
    }

    loop();
  }

  // ═══════════════════════════════════
  //  点击粒子爆炸
  // ═══════════════════════════════════

  var burstColors = [
    [0, 240, 255],   // neon-cyan
    [255, 45, 149],  // neon-pink
    [179, 71, 234],  // neon-purple
    [57, 255, 20],   // neon-green
    [255, 107, 53],  // neon-orange
    [68, 136, 255],  // neon-blue
  ];

  function initClickBurst() {
    document.addEventListener('click', function (e) {
      if (window.innerWidth < 768) return;
      createBurst(e.clientX, e.clientY);
    });
  }

  function createBurst(x, y) {
    var count = 18;
    var fragment = document.createDocumentFragment();

    for (var i = 0; i < count; i++) {
      var particle = document.createElement('div');
      particle.className = 'particle-burst';

      var angle = (Math.PI * 2 * i) / count + (Math.random() - 0.5) * 0.5;
      var distance = 40 + Math.random() * 50;
      var bx = Math.cos(angle) * distance;
      var by = Math.sin(angle) * distance;

      particle.style.cssText =
        'left:' +
        x +
        'px;top:' +
        y +
        'px;--bx:' +
        bx.toFixed(0) +
        'px;--by:' +
        by.toFixed(0) +
        'px;';

      var color = burstColors[Math.floor(Math.random() * burstColors.length)];
      particle.style.backgroundColor =
        'rgb(' + color[0] + ',' + color[1] + ',' + color[2] + ')';
      particle.style.boxShadow =
        '0 0 8px rgb(' +
        color[0] +
        ',' +
        color[1] +
        ',' +
        color[2] +
        '), 0 0 20px rgb(' +
        color[0] +
        ',' +
        color[1] +
        ',' +
        color[2] +
        ')';

      fragment.appendChild(particle);
    }

    document.body.appendChild(fragment);

    // 动画结束后清理
    setTimeout(function () {
      var particles = document.querySelectorAll('.particle-burst');
      particles.forEach(function (p) {
        p.remove();
      });
    }, 900);
  }

  // ═══════════════════════════════════
  //  页面切换转场
  // ═══════════════════════════════════

  var transitionEl = null;

  function initPageTransition() {
    transitionEl = document.createElement('div');
    transitionEl.className = 'page-transition';
    transitionEl.innerHTML = '<div class="page-transition-text">✦ LOADING ✦</div>';
    document.body.appendChild(transitionEl);
  }

  function pageOut(callback) {
    if (!transitionEl) initPageTransition();
    transitionEl.classList.add('active');

    setTimeout(function () {
      if (callback) callback();
    }, 500);
  }

  function pageIn() {
    if (!transitionEl) return;
    setTimeout(function () {
      transitionEl.classList.remove('active');
    }, 100);
  }

  /**
   * 完整转场流程
   * nebulaTransition(function() {
   *   // 切换页面内容
   * });
   */
  function transition(callback) {
    pageOut(function () {
      callback();
      pageIn();
    });
  }

  // ═══════════════════════════════════
  //  网格背景动态偏移
  // ═══════════════════════════════════

  function initGridShift() {
    // 给 body 添加动态网格（如果尚未添加）
    if (!document.body.classList.contains('bg-grid-animate')) {
      document.body.classList.add('bg-grid-animate');
    }
  }

  // ═══════════════════════════════════
  //  销毁
  // ═══════════════════════════════════

  function destroy() {
    if (animId) cancelAnimationFrame(animId);
    if (cursorEl && cursorEl.parentNode) cursorEl.parentNode.removeChild(cursorEl);
    if (trailCanvas && trailCanvas.parentNode) trailCanvas.parentNode.removeChild(trailCanvas);
    if (transitionEl && transitionEl.parentNode) transitionEl.parentNode.removeChild(transitionEl);
    document.body.classList.remove('cursor-hidden', 'bg-grid-animate');
    cursorEl = null;
    trailCanvas = null;
    transitionEl = null;
    enabled = false;
    window.NebulaDeluxe._active = false;
  }

  // ═══════════════════════════════════
  //  API
  // ═══════════════════════════════════

  window.NebulaDeluxe = {
    _active: true,
    init: function () {
      initCursor();
      initClickBurst();
      initPageTransition();
      initGridShift();
    },
    initCursor: initCursor,
    initClickBurst: initClickBurst,
    transition: transition,
    pageOut: pageOut,
    pageIn: pageIn,
    burst: createBurst,
    destroy: destroy,
    refresh: function () {
      resizeTrailCanvas();
    },
  };

  // 自动初始化
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      window.NebulaDeluxe.init();
    });
  } else {
    window.NebulaDeluxe.init();
  }
})();
