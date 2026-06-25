# 自动更新系统说明

本仓库新增了一套完整的「自动更新」能力，由三部分组成：

```
┌─────────────────┐   推送 v* tag    ┌──────────────────────┐
│  开发者 git tag  │ ───────────────> │ GitHub Action 自动构建 │
└─────────────────┘                  │  签名 APK + 发布 Release │
                                     └──────────┬───────────┘
                                                │ Release + APK
                                                ▼
                                  ┌──────────────────────────────┐
                                  │   树莓派服务（Node）            │
                                  │   从 GitHub 拉取并缓存版本/APK   │
                                  │   /api/latest  ·  /download/*  │
                                  └───────┬───────────────┬────────┘
                                          │               │
                       官网下载页 (浏览器)  ▼               ▼  App 内更新检查
                          ┌────────────────────┐   ┌──────────────────────────┐
                          │ 苹果液态玻璃风官网    │   │ 请求服务 /api/latest 比对   │
                          │ 下载走 /download/*    │   │ 有新版本则弹窗，下载走服务   │
                          └────────────────────┘   └──────────────────────────┘
```

> **关键设计**：App **不直连 GitHub**（国内网络不稳定），而是请求自有服务获取版本信息，
> 安装包也由服务从 GitHub 缓存后提供。服务地址在 App 中仅为**编译期常量**
> （`BuildConfig.UPDATE_BASE_URL`），**不在任何界面显示**。

---

## 一、官网 + Release API（树莓派）

代码位于 [`server/`](server/)。零依赖 Node.js 服务：

- `GET /` —— 苹果液态玻璃风官网首页，动态展示最新版本与下载按钮
- `GET /api/latest` —— 返回 GitHub 最新 release（带缓存）
- `POST /api/sync` —— 手动强制刷新缓存
- `GET /download/<文件名>` —— APK 下载代理：本地有缓存直接发，否则从 GitHub 回源并落盘
- `GET /api/health` —— 健康检查

本地启动：

```bash
cd server
node server.js          # 打开 http://127.0.0.1:3000
# 若本机网络使用企业自签 CA，导致 Node 无法校验 GitHub 证书：
node --use-system-ca server.js
```

部署详见 [`server/README.md`](server/README.md)（含 systemd 与反向代理配置）。
将 `navi-link.zuoqirun.top` 反代到该服务即可。

## 二、App 内更新提示

| 文件 | 作用 |
|------|------|
| `app/.../UpdateChecker.java` | 请求自有服务 `/api/latest`，比对版本（语义化数字比较），后台线程执行 |
| `app/.../UpdateDialog.java` | 渲染深色风格更新弹窗，处理下载跳转 |
| `app/.../res/layout/dialog_update.xml` | 弹窗布局 |
| `MainActivity` | 启动时静默检查；设置页底部「检查更新」入口可手动检查 |

- **静默检查**：App 启动时自动检查，仅在有新版本时弹窗，不打扰用户。
- **手动检查**：设置页底部「检查更新」卡片，点击后即使已是最新也会 Toast 提示。
- 服务地址通过 `BuildConfig.UPDATE_BASE_URL` 配置（默认 `https://navi-link.zuoqirun.top`），
  在 `app/build.gradle` 中修改，或用 gradle 属性 / 环境变量 `NAVI_UPDATE_BASE_URL` 覆盖。
  该地址**不在任何界面显示**。

## 三、GitHub Action 自动发布

工作流：[`.github/workflows/release.yml`](.github/workflows/release.yml)

**触发方式**：推送 `v` 开头的 tag，例如：

```bash
git tag v2.5.4
git push origin v2.5.4
```

流程：检出 → JDK 17 → 构建**签名** Release APK → 创建 GitHub Release（自动生成更新日志）→ 上传 APK。

### 需要配置的仓库 Secrets

在 GitHub 仓库 `Settings → Secrets and variables → Actions` 添加：

| Secret | 说明 |
|--------|------|
| `SIGNING_STORE_PASSWORD` | 密钥库（navi.jks）密码 |
| `SIGNING_KEY_ALIAS` | 密钥别名 |
| `SIGNING_KEY_PASSWORD` | 密钥密码 |
| `SIGNING_KEYSTORE_BASE64` | （可选）密钥库的 Base64。不配置则使用仓库内的 `navi.jks` |

> 仓库已包含 `navi.jks`，若沿用它，只需配置上面三个密码类 secret，
> 不必配置 `SIGNING_KEYSTORE_BASE64`。
>
> 若想用独立密钥库（推荐，避免私钥进仓库），用以下命令生成 Base64 后填入
> `SIGNING_KEYSTORE_BASE64`：
> ```bash
> base64 -w0 your-release.jks        # Linux/Mac
> ```

### 版本号

发布前请同步修改 `app/build.gradle` 中的 `versionCode` 与 `versionName`，
使其与 tag 对应（App 比对的是 `versionName`）。

---

## 本地构建说明

- 项目路径含中文，已在 `gradle.properties` 加 `android.overridePathCheck=true`。
- `local.properties`（SDK 路径）与签名密码均不入库；本地无签名配置时
  Release 构建会自动回退为未签名，不会失败。
