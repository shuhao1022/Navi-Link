# Navi-Link 服务端（官网 + Release 更新 API）

零依赖 Node.js 服务，部署在树莓派上：

- **官网**：根路径 `/` 提供苹果液态玻璃风官网首页，动态展示最新版本与下载入口。
- **更新 API**：供官网与 App 查询 GitHub 最新 Release，并代理缓存安装包下载。

> App 与官网都通过**本服务**获取版本信息与安装包：本服务从 GitHub 拉取并在树莓派本地缓存，
> 客户端无需直连 GitHub（国内网络更稳定）。App 中以编译期常量配置本服务地址，界面不展示该地址。

---

## 一、目录结构

```
server/
├── server.js          # 主服务（仅用 Node 内置模块）
├── package.json
├── .env.example       # 配置样例
└── public/            # 官网静态资源
    ├── index.html
    ├── styles.css     # 液态玻璃风样式
    └── app.js
```

## 二、配置

复制 `.env.example` 为 `.env` 并按需修改（也可直接用环境变量）：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `PORT` | `3000` | 监听端口 |
| `HOST` | `0.0.0.0` | 监听地址 |
| `GITHUB_REPO` | `shuhao1022/Navi-Link` | Release 数据来源仓库 |
| `GITHUB_TOKEN` | 空 | 可选。配置后 GitHub API 限流从 60/h 提升到 5000/h |
| `CACHE_TTL` | `600` | Release 缓存秒数；`/api/sync` 可强制刷新 |

## 三、本地运行

```bash
cd server
node server.js
# 打开 http://127.0.0.1:3000
```

> **本地开发提示**：若你的网络环境使用企业自签 CA（代理），Node 直连 `api.github.com` 可能报
> `unable to verify the first certificate`。此时用系统证书库启动即可：
> ```bash
> node --use-system-ca server.js
> ```
> 树莓派等正常环境无需此参数。

## 四、API

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/latest` | 返回最新 Release（命中缓存则直接返回） |
| `POST`/`GET` | `/api/sync` | 强制从 GitHub 拉取并刷新缓存 |
| `GET`/`HEAD` | `/download/<文件名>` | APK 下载代理：本地有缓存直接发，否则从 GitHub 回源并落盘 |
| `GET` | `/api/health` | 健康检查 |

`/api/latest` 返回的 `apk.downloadUrl` 是**指向本服务**的相对地址（如 `/download/xxx.apk`），
客户端据此下载，由本服务回源缓存；`apk.githubUrl` 为内部回源地址。

返回示例（`/api/latest`）：

```json
{
  "version": "v2.5.4",
  "name": "Navi-Link v2.5.4",
  "body": "更新日志（markdown）...",
  "publishedAt": "2026-06-23T10:00:00Z",
  "htmlUrl": "https://github.com/shuhao1022/Navi-Link/releases/tag/v2.5.4",
  "prerelease": false,
  "apk": {
    "name": "Navi-Link-v2.5.4-release-xxx.apk",
    "size": 12345678,
    "downloadUrl": "/download/Navi-Link-v2.5.4-release-xxx.apk",
    "githubUrl": "https://github.com/.../xxx.apk",
    "downloadCount": 42
  },
  "assets": [ ... ],
  "_cache": "hit | miss | refreshed | stale"
}
```

## 五、树莓派部署（systemd）

1. 拷贝 `server/` 到树莓派，例如 `/opt/navi-link/server`。
2. 安装 Node ≥18（`sudo apt install nodejs npm` 或用 nvm）。
3. 配置 `.env`。
4. 安装 systemd 服务：

```bash
sudo cp navi-link.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now navi-link
sudo systemctl status navi-link
```

5. 配合反向代理（Nginx/Caddy）将 `navi-link.zuoqirun.top` 指向 `127.0.0.1:3000`，并启用 HTTPS。

Caddy 示例（自动 HTTPS）：

```
navi-link.zuoqirun.top {
    reverse_proxy 127.0.0.1:3000
}
```
