/**
 * ═══════════════════════════════════════
 *  Nebula Particles — 星云粒子背景引擎
 * ═══════════════════════════════════════
 *  80-120 粒子 | 连线距离 150px | 鼠标吸引
 *  霓虹色彩渐变 | requestAnimationFrame 60fps
 */

(function () {
  'use strict';

  const CONFIG = {
    particleCount: 100,
    connectDistance: 150,
    mouseRadius: 180,
    mouseForce: 0.03,
    baseSpeed: 0.3,
    lineOpacity: 0.15,
    dotRadius: 1.8,
    respawnMargin: 50,
    colors: [
      [255, 141, 83],   // accent-1 珊瑚橙
      [124, 58, 237],   // 紫色
      [0, 180, 216],    // 青色
      [255, 107, 157],  // 霓虹粉
      [96, 165, 250],   // 蓝
      [0, 214, 143],    // 翠绿
    ],
  };

  let canvas, ctx;
  let particles = [];
  let mouse = { x: -9999, y: -9999, active: false };
  let animId;
  let width, height;
  let dpr;

  function init() {
    canvas = document.getElementById('nebula-canvas');
    if (!canvas) {
      canvas = document.createElement('canvas');
      canvas.id = 'nebula-canvas';
      canvas.style.cssText =
        'position:fixed;top:0;left:0;width:100%;height:100%;z-index:-1;pointer-events:none;';
      // 强制确保 pointer-events: none，防止被其他 CSS 覆盖
      canvas.style.setProperty('pointer-events', 'none', 'important');
      document.body.prepend(canvas);
    }
    ctx = canvas.getContext('2d');
    resize();
    spawnParticles();
    bindEvents();
    loop();
  }

  function resize() {
    dpr = Math.min(window.devicePixelRatio || 1, 2);
    width = window.innerWidth;
    height = window.innerHeight;
    canvas.width = width * dpr;
    canvas.height = height * dpr;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  function spawnParticles() {
    particles = [];
    for (let i = 0; i < CONFIG.particleCount; i++) {
      particles.push(createParticle());
    }
  }

  function createParticle(x, y) {
    return {
      x: x !== undefined ? x : Math.random() * width,
      y: y !== undefined ? y : Math.random() * height,
      vx: (Math.random() - 0.5) * CONFIG.baseSpeed,
      vy: (Math.random() - 0.5) * CONFIG.baseSpeed,
      radius: CONFIG.dotRadius * (0.6 + Math.random() * 0.8),
      colorIdx: Math.floor(Math.random() * CONFIG.colors.length),
      life: 1,
    };
  }

  function bindEvents() {
    let moveThrottle;
    document.addEventListener('mousemove', function (e) {
      if (moveThrottle) return;
      moveThrottle = requestAnimationFrame(function () {
        mouse.x = e.clientX;
        mouse.y = e.clientY;
        mouse.active = true;
        moveThrottle = null;
      });
    });

    document.addEventListener('mouseleave', function () {
      mouse.active = false;
    });

    window.addEventListener('resize', function () {
      resize();
      // 粒子位置按比例缩放
      const scaleX = width / (width - (window.innerWidth - width));
      const scaleY = height / (height - (window.innerHeight - height));
      // 简单重算：让粒子在接近边缘的被重新分布
      particles.forEach(function (p) {
        if (p.x > width) p.x = width - 50;
        if (p.y > height) p.y = height - 50;
      });
    });
  }

  function update() {
    for (var i = 0; i < particles.length; i++) {
      var p = particles[i];

      // 鼠标吸引
      if (mouse.active) {
        var dx = mouse.x - p.x;
        var dy = mouse.y - p.y;
        var dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < CONFIG.mouseRadius && dist > 0) {
          var force = (1 - dist / CONFIG.mouseRadius) * CONFIG.mouseForce;
          p.vx += (dx / dist) * force;
          p.vy += (dy / dist) * force;
          // 鼠标附近的粒子变亮
          p.life = Math.min(1, p.life + 0.02);
        }
      }

      // 更新位置
      p.x += p.vx;
      p.y += p.vy;

      // 阻尼
      p.vx *= 0.995;
      p.vy *= 0.995;

      // 速度上限
      var speed = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
      var maxSpeed = 1.2;
      if (speed > maxSpeed) {
        p.vx = (p.vx / speed) * maxSpeed;
        p.vy = (p.vy / speed) * maxSpeed;
      }

      // 边界回弹
      var margin = CONFIG.respawnMargin;
      if (p.x < -margin) p.x = width + margin;
      if (p.x > width + margin) p.x = -margin;
      if (p.y < -margin) p.y = height + margin;
      if (p.y > height + margin) p.y = -margin;

      // 生命值衰减
      p.life = Math.max(0.3, p.life - 0.0005);
    }
  }

  function draw() {
    ctx.clearRect(0, 0, width, height);

    // 绘制连线
    for (var i = 0; i < particles.length; i++) {
      for (var j = i + 1; j < particles.length; j++) {
        var a = particles[i];
        var b = particles[j];
        var dx = a.x - b.x;
        var dy = a.y - b.y;
        var dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < CONFIG.connectDistance) {
          var alpha = (1 - dist / CONFIG.connectDistance) * CONFIG.lineOpacity;
          var midLife = (a.life + b.life) / 2;
          alpha *= midLife;

          // 混合两个粒子颜色
          var ca = CONFIG.colors[a.colorIdx];
          var cb = CONFIG.colors[b.colorIdx];
          var cr = Math.round((ca[0] + cb[0]) / 2);
          var cg = Math.round((ca[1] + cb[1]) / 2);
          var cb2 = Math.round((ca[2] + cb[2]) / 2);

          ctx.beginPath();
          ctx.moveTo(a.x, a.y);
          ctx.lineTo(b.x, b.y);
          ctx.strokeStyle =
            'rgba(' + cr + ',' + cg + ',' + cb2 + ',' + alpha.toFixed(3) + ')';
          ctx.lineWidth = 0.6;
          ctx.stroke();
        }
      }
    }

    // 绘制粒子
    for (var i = 0; i < particles.length; i++) {
      var p = particles[i];
      var c = CONFIG.colors[p.colorIdx];
      var alpha = p.life * 0.85;

      // 外发光
      var glow = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.radius * 3);
      glow.addColorStop(0, 'rgba(' + c[0] + ',' + c[1] + ',' + c[2] + ',' + alpha + ')');
      glow.addColorStop(0.5, 'rgba(' + c[0] + ',' + c[1] + ',' + c[2] + ',' + (alpha * 0.3) + ')');
      glow.addColorStop(1, 'rgba(' + c[0] + ',' + c[1] + ',' + c[2] + ',0)');

      ctx.beginPath();
      ctx.arc(p.x, p.y, p.radius * 3, 0, Math.PI * 2);
      ctx.fillStyle = glow;
      ctx.fill();

      // 核心亮点
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(' + c[0] + ',' + c[1] + ',' + c[2] + ',' + (alpha * 1.1).toFixed(3) + ')';
      ctx.fill();
    }
  }

  function loop() {
    update();
    draw();
    animId = requestAnimationFrame(loop);
  }

  // ── 动态重配色 ──
  window.NebulaParticles = {
    init: function(opts) {
      if (opts) {
        if (opts.count) CONFIG.particleCount = opts.count;
        if (opts.connectDistance || opts.connectDist) CONFIG.connectDistance = opts.connectDistance || opts.connectDist;
        if (opts.mouseRadius || opts.mouseDist) CONFIG.mouseRadius = opts.mouseRadius || opts.mouseDist;
        if (opts.colors) CONFIG.colors = opts.colors;
      }
      // 如果已经初始化过，先销毁旧的
      if (animId) cancelAnimationFrame(animId);
      particles = [];
      init();
    },
    setColors: function (colors) {
      CONFIG.colors = colors;
    },
    setCount: function (n) {
      CONFIG.particleCount = n;
      spawnParticles();
    },
    setConnectDistance: function (d) {
      CONFIG.connectDistance = d;
    },
    destroy: function () {
      if (animId) cancelAnimationFrame(animId);
      if (canvas && canvas.parentNode) canvas.parentNode.removeChild(canvas);
    },
    refresh: function () {
      resize();
      spawnParticles();
    },
  };

  // 页面加载后初始化
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
