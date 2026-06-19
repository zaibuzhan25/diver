import os

file = "user.html"
with open(file, 'r', encoding='utf-8') as f:
    content = f.read()

old_str = "  const data = await r.json();\n  if (!r.ok) throw new Error(data.error || '请求失败');"
new_str = "  const data = await r.json();\n  if (r.status === 401 || r.status === 403) { doLogout(); throw new Error('登录已过期，请重新登录'); }\n  if (!r.ok) throw new Error(data.error || '请求失败');"
content = content.replace(old_str, new_str)

with open(file, 'w', encoding='utf-8') as f:
    f.write(content)

print("user.html 401 拦截已添加")
