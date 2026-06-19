#!/usr/bin/env python3
# 批量完成所有剩余功能
import os, re

HTML = ["index.html", "user.html"]

THEME_BTN = '''      <div class="theme-switch">
        <button class="theme-btn" data-theme="light">☀️ 浅色</button>
        <button class="theme-btn" data-theme="dark">🌙 深色</button>
        <button class="theme-btn" data-theme="system">💻 系统</button>
      </div>
'''

def patch_index():
    f = "index.html"
    if not os.path.exists(f): return
    c = open(f, 'r', encoding='utf-8').read()

    # 注入主题切换按钮（在退出按钮前）
    c = c.replace(
        '      <button class="btn-logout" onclick="doLogout()">退出</button>',
        THEME_BTN + '      <button class="btn-logout" onclick="doLogout()">退出</button>'
    )

    open(f, 'w', encoding='utf-8').write(c)
    print(f"[OK] {f} 主题按钮已添加")

def patch_user():
    f = "user.html"
    if not os.path.exists(f): return
    c = open(f, 'r', encoding='utf-8').read()

    # 注入主题切换按钮（在退出按钮前）
    c = c.replace(
        '      <button class="btn-logout" onclick="doLogout()">退出</button>',
        THEME_BTN + '      <button class="btn-logout" onclick="doLogout()">退出</button>'
    )

    open(f, 'w', encoding='utf-8').write(c)
    print(f"[OK] {f} 主题按钮已添加")

if __name__ == '__main__':
    patch_index()
    patch_user()
    print("全部完成！")
