/**
 * ═══════════════════════════════════════════
 *  Nebula Interactions — 3D倾斜 | 光效跟随 | 视差 | 数字滚动
 * ═══════════════════════════════════════════
 *  鼠标驱动 3D 倾斜 & 光效 | 滚动视差纵深
 *  数字递增动画 | 高性能 requestAnimationFrame
 */

(function () {
  'use strict';

  // ═══════════════════════════════════
  //  3D 倾斜卡片
  // ═══════════════════════════════════

  function initTiltCards() {
    var cards = document.querySelectorAll('.tilt-card');

    cards.forEach(function (card) {
      card.addEventListener('mousemove', function (e) {
        var rect = card.getBoundingClientRect();
        var x = e.clientX - rect.left;
        var y = e.clientY - rect.top;

        var centerX = rect.width / 2;
        var centerY = rect.height / 2;

        // 偏移比例 -1 ~ 1
        var rotateY = ((x - centerX) / centerX) * 8; // 最大 ±8°
        var rotateX = -((y - centerY) / centerY) * 8;

        card.style.transform =
          'perspective(800px) rotateX(' +
          rotateX.toFixed(2) +
          'deg) rotateY(' +
          rotateY.toFixed(2) +
          'deg) scale3d(1.02, 1.02, 1.02)';

        // 动态光效（如果有 light-follow 子元素）
        var lightEl = card.querySelector('.light-follow');
        if (lightEl) {
          lightEl.style.setProperty('--mouse-x', x + 'px');
          lightEl.style.setProperty('--mouse-y', y + 'px');
        }
      });

      card.addEventListener('mouseleave', function () {
        card.style.transform =
          'perspective(800px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
      });

      // 初始CSS transition做回弹，mousemove期间移除
      card.addEventListener('mouseenter', function () {
        card.style.transition = 'none';
      });
      card.addEventListener('mouseleave', function () {
        card.style.transition =
          'transform 0.5s cubic-bezier(0.23, 1, 0.32, 1)';
      });
    });
  }

  // ═══════════════════════════════════
  //  光效跟随（独立 .light-follow 元素）
  // ═══════════════════════════════════

  function initLightFollow() {
    var els = document.querySelectorAll('.light-follow:not(.tilt-card .light-follow)');

    els.forEach(function (el) {
      el.addEventListener('mousemove', function (e) {
        var rect = el.getBoundingClientRect();
        var x = e.clientX - rect.left;
        var y = e.clientY - rect.top;
        el.style.setProperty('--mouse-x', x + 'px');
        el.style.setProperty('--mouse-y', y + 'px');
      });
    });
  }

  // ═══════════════════════════════════
  //  全局光标光效 (body级跟随)
  // ═══════════════════════════════════

  function initGlobalLightFollow() {
    var glow = document.getElementById('cursor-glow');
    if (!glow) {
      glow = document.createElement('div');
      glow.id = 'cursor-glow';
      glow.style.cssText =
        'position:fixed;pointer-events:none;z-index:9997;' +
        'width:300px;height:300px;border-radius:50%;' +
        'transform:translate(-50%,-50%);' +
        'background:radial-gradient(circle,' +
        'rgba(0,180,216,0.08) 0%,' +
        'rgba(124,58,237,0.04) 40%,' +
        'transparent 70%);' +
        'filter:blur(30px);' +
        'transition:opacity 0.3s;' +
        'opacity:0;';
      document.body.appendChild(glow);
    }

    var ticking = false;
    document.addEventListener('mousemove', function (e) {
      if (!ticking) {
        requestAnimationFrame(function () {
          glow.style.left = e.clientX + 'px';
          glow.style.top = e.clientY + 'px';
          glow.style.opacity = '1';
          ticking = false;
        });
        ticking = true;
      }
    });

    document.addEventListener('mouseleave', function () {
      glow.style.opacity = '0';
    });
  }

  // ═══════════════════════════════════
  //  视差滚动 (JS驱动)
  // ═══════════════════════════════════

  function initParallax() {
    var layers = document.querySelectorAll('[data-parallax-speed]');
    if (!layers.length) return;

    var ticking = false;

    function update() {
      var scrollY = window.pageYOffset || window.scrollY;

      layers.forEach(function (el) {
        var speed = parseFloat(el.getAttribute('data-parallax-speed')) || 0.5;
        var offset = scrollY * speed;
        // 只影响Y轴（常见视差）或影响所有方向
        var direction = el.getAttribute('data-parallax-direction') || 'y';
        if (direction === 'y') {
          el.style.transform = 'translate3d(0, ' + offset + 'px, 0)';
        } else if (direction === 'x') {
          el.style.transform = 'translate3d(' + offset + 'px, 0, 0)';
        } else {
          el.style.transform =
            'translate3d(' + offset + 'px, ' + offset * 0.5 + 'px, 0)';
        }
      });

      ticking = false;
    }

    window.addEventListener(
      'scroll',
      function () {
        if (!ticking) {
          requestAnimationFrame(update);
          ticking = true;
        }
      },
      { passive: true }
    );

    // 初始调用
    update();
  }

  // ═══════════════════════════════════
  //  数字滚动动画
  // ═══════════════════════════════════

  function initCountUp() {
    var els = document.querySelectorAll('.count-up');

    // IntersectionObserver 触发
    if ('IntersectionObserver' in window) {
      var observer = new IntersectionObserver(
        function (entries) {
          entries.forEach(function (entry) {
            if (entry.isIntersecting) {
              animateCountUp(entry.target);
              observer.unobserve(entry.target);
            }
          });
        },
        { threshold: 0.3 }
      );

      els.forEach(function (el) {
        observer.observe(el);
      });
    } else {
      // Fallback: 立即执行
      els.forEach(function (el) {
        animateCountUp(el);
      });
    }
  }

  function animateCountUp(el) {
    var target = parseFloat(el.getAttribute('data-count')) || 0;
    var duration = parseInt(el.getAttribute('data-duration')) || 2000;
    var prefix = el.getAttribute('data-prefix') || '';
    var suffix = el.getAttribute('data-suffix') || '';
    var decimals = parseInt(el.getAttribute('data-decimals')) || 0;
    var startTime = null;
    var startVal = 0;

    function step(timestamp) {
      if (!startTime) startTime = timestamp;
      var progress = Math.min((timestamp - startTime) / duration, 1);

      // easeOutExpo
      var eased = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);

      var current = startVal + (target - startVal) * eased;
      el.textContent = prefix + current.toFixed(decimals) + suffix;

      if (progress < 1) {
        requestAnimationFrame(step);
      } else {
        el.textContent = prefix + target.toFixed(decimals) + suffix;
        el.classList.add('count-done');
      }
    }

    requestAnimationFrame(step);
  }

  // ═══════════════════════════════════
  //  动态扫描光条初始化
  // ═══════════════════════════════════

  function initScanBar() {
    if (document.querySelector('.nebula-scan-bar')) return;
    var bar = document.createElement('div');
    bar.className = 'nebula-scan-bar';
    document.body.appendChild(bar);
  }

  // ═══════════════════════════════════
  //  入口
  // ═══════════════════════════════════

  function initAll() {
    initTiltCards();
    initLightFollow();
    initGlobalLightFollow();
    initParallax();
    initCountUp();
    initScanBar();
  }

  // 对外 API
  window.NebulaInteractions = {
    init: initAll,
    initTiltCards: initTiltCards,
    initLightFollow: initLightFollow,
    initParallax: initParallax,
    initCountUp: initCountUp,
    refreshTilt: function () {
      initTiltCards();
    },
    refresh: function () {
      initAll();
    },
  };

  // 自动初始化
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAll);
  } else {
    initAll();
  }
})();
