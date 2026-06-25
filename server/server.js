/**
 * Navi-Link 服务端
 * ─────────────────────────────────────────────────────────────
 * 职责：
 *   1. 托管官网静态页面（public/ 目录，苹果液态玻璃风）
 *   2. 提供 Release 更新 API：
 *        GET /api/latest  —— 返回 GitHub 最新 release（带缓存）
 *        POST /api/sync   —— 手动强制刷新缓存
 *        GET /api/health  —— 健康检查
 *
 * 设计：仅依赖 Node 内置模块，方便在树莓派上零依赖部署。
 */

import http from 'node:http';
import https from 'node:https';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// ── 配置（环境变量优先，含默认值）──────────────────────────────
loadDotEnv(path.join(__dirname, '.env'));

const CONFIG = {
  port: parseInt(process.env.PORT || '3000', 10),
  host: process.env.HOST || '0.0.0.0',
  repo: process.env.GITHUB_REPO || 'shuhao1022/Navi-Link',
  token: process.env.GITHUB_TOKEN || '',
  cacheTtl: parseInt(process.env.CACHE_TTL || '600', 10) * 1000,
  // APK 缓存目录：从 GitHub 拉取的安装包落盘于此，之后由树莓派直接提供，
  // 避免客户端直连 GitHub（国内不稳定）。
  apkCacheDir: process.env.APK_CACHE_DIR || path.join(__dirname, '.cache'),
};

const PUBLIC_DIR = path.join(__dirname, 'public');

// 确保 APK 缓存目录存在
try {
  fs.mkdirSync(CONFIG.apkCacheDir, { recursive: true });
} catch (e) {
  console.warn('[WARN] 无法创建 APK 缓存目录:', e.message);
}

// ── Release 缓存 ────────────────────────────────────────────────
const cache = {
  data: null,        // 规范化后的 release 对象
  fetchedAt: 0,      // 上次抓取时间戳（ms）
  raw: null,         // GitHub 原始响应（调试用）
};

// ── HTTP 服务 ──────────────────────────────────────────────────
const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://${req.headers.host}`);
  const pathname = decodeURIComponent(url.pathname);

  // 统一 CORS（官网前端 fetch 用）
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    return res.end();
  }

  try {
    if (pathname === '/api/latest' && req.method === 'GET') {
      return await handleLatest(req, res, false);
    }
    if (pathname === '/api/sync' && (req.method === 'POST' || req.method === 'GET')) {
      return await handleLatest(req, res, true);
    }
    if (pathname === '/api/health' && req.method === 'GET') {
      return sendJson(res, 200, {
        status: 'ok',
        repo: CONFIG.repo,
        cached: !!cache.data,
        cacheAgeSec: cache.fetchedAt ? Math.round((Date.now() - cache.fetchedAt) / 1000) : null,
      });
    }
    // APK 下载代理：/download/<文件名> —— 优先本地缓存，未命中则从 GitHub 拉取并落盘
    if (pathname.startsWith('/download/') && (req.method === 'GET' || req.method === 'HEAD')) {
      return await handleDownload(pathname, req, res);
    }
    // 其余交给静态文件服务（官网）
    return serveStatic(pathname, res);
  } catch (err) {
    console.error('[ERROR]', err);
    sendJson(res, 500, { error: 'internal_error', message: String(err && err.message || err) });
  }
});

server.listen(CONFIG.port, CONFIG.host, () => {
  console.log(`\n  Navi-Link 服务已启动`);
  console.log(`  ├─ 监听地址 : http://${CONFIG.host}:${CONFIG.port}`);
  console.log(`  ├─ 仓库     : ${CONFIG.repo}`);
  console.log(`  ├─ 缓存时长 : ${CONFIG.cacheTtl / 1000}s`);
  console.log(`  ├─ Token    : ${CONFIG.token ? '已配置' : '匿名（限流 60/h）'}`);
  console.log(`  ├─ APK 缓存 : ${CONFIG.apkCacheDir}`);
  console.log(`  └─ 官网目录 : ${PUBLIC_DIR}\n`);
});

// ── 处理 /api/latest 与 /api/sync ──────────────────────────────
async function handleLatest(req, res, forceRefresh) {
  const fresh = !forceRefresh && cache.data && (Date.now() - cache.fetchedAt) < CONFIG.cacheTtl;

  if (fresh) {
    return sendJson(res, 200, { ...cache.data, _cache: 'hit', _ageSec: Math.round((Date.now() - cache.fetchedAt) / 1000) });
  }

  try {
    const release = await fetchLatestRelease();
    cache.data = release;
    cache.fetchedAt = Date.now();
    return sendJson(res, 200, { ...release, _cache: forceRefresh ? 'refreshed' : 'miss' });
  } catch (err) {
    // 抓取失败时，若有旧缓存则降级返回旧数据，保证可用性
    if (cache.data) {
      return sendJson(res, 200, { ...cache.data, _cache: 'stale', _warning: String(err.message || err) });
    }
    const code = err.statusCode === 404 ? 404 : 502;
    return sendJson(res, code, { error: 'fetch_failed', message: String(err.message || err) });
  }
}

// ── 抓取并规范化 GitHub 最新 release ───────────────────────────
async function fetchLatestRelease() {
  const apiPath = `/repos/${CONFIG.repo}/releases/latest`;
  const body = await githubGet(apiPath);
  const r = JSON.parse(body);

  // 找出 APK 资产
  const assets = Array.isArray(r.assets) ? r.assets : [];
  const apk = assets.find((a) => /\.apk$/i.test(a.name)) || null;

  return {
    version: r.tag_name || r.name || '',
    name: r.name || r.tag_name || '',
    body: r.body || '',
    publishedAt: r.published_at || '',
    htmlUrl: r.html_url || '',
    prerelease: !!r.prerelease,
    apk: apk
      ? {
          name: apk.name,
          size: apk.size,
          // downloadUrl：客户端使用的地址，指向本服务器的下载代理（相对路径），
          // 由树莓派缓存并提供，客户端无需直连 GitHub。
          downloadUrl: '/download/' + encodeURIComponent(apk.name),
          // githubUrl：真实回源地址，仅服务端下载代理内部使用。
          githubUrl: apk.browser_download_url,
          downloadCount: apk.download_count,
        }
      : null,
    assets: assets.map((a) => ({
      name: a.name,
      size: a.size,
      downloadUrl: /\.apk$/i.test(a.name)
        ? '/download/' + encodeURIComponent(a.name)
        : a.browser_download_url,
      githubUrl: a.browser_download_url,
      downloadCount: a.download_count,
    })),
  };
}

// ── 调用 GitHub API（https + 重定向 + UA + token）────────────────
function githubGet(apiPath) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'api.github.com',
      path: apiPath,
      method: 'GET',
      headers: {
        'User-Agent': 'Navi-Link-Server',
        Accept: 'application/vnd.github+json',
        'X-GitHub-Api-Version': '2022-11-28',
      },
    };
    if (CONFIG.token) options.headers.Authorization = `Bearer ${CONFIG.token}`;

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => (data += chunk));
      res.on('end', () => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(data);
        } else {
          const err = new Error(`GitHub API ${res.statusCode}: ${data.slice(0, 200)}`);
          err.statusCode = res.statusCode;
          reject(err);
        }
      });
    });
    req.on('error', reject);
    req.setTimeout(10000, () => req.destroy(new Error('GitHub API 请求超时')));
    req.end();
  });
}

// ── APK 下载代理 ───────────────────────────────────────────────
// 进行中的回源下载：文件名 → Promise，避免并发重复下载同一文件
const inflightDownloads = new Map();

/**
 * 处理 /download/<filename>：
 *   1. 文件名安全校验（仅允许 release 资产名，禁止路径穿越）
 *   2. 命中本地缓存 → 直接发送
 *   3. 未命中 → 从 GitHub 回源下载并落盘，完成后发送
 */
async function handleDownload(pathname, req, res) {
  const filename = path.basename(decodeURIComponent(pathname.slice('/download/'.length)));

  // 安全：文件名不得为空、不得含分隔符、必须是合理的资产名
  if (!filename || filename.includes('/') || filename.includes('\\') || filename === '..') {
    return sendJson(res, 400, { error: 'bad_filename' });
  }

  // 找到该文件对应的 GitHub 回源地址：从缓存的 release 资产中查
  const sourceUrl = await resolveAssetUrl(filename);
  if (!sourceUrl) {
    return sendJson(res, 404, { error: 'asset_not_found', message: '未在最新发布中找到该文件' });
  }

  const localPath = path.join(CONFIG.apkCacheDir, filename);

  // 命中本地缓存
  if (fs.existsSync(localPath)) {
    return sendFile(res, localPath, filename, req.method === 'HEAD');
  }

  // HEAD 请求且无缓存：直接回 200（避免触发下载），交由客户端 GET 时再缓存
  if (req.method === 'HEAD') {
    res.writeHead(200, { 'Content-Type': 'application/vnd.android.package-archive' });
    return res.end();
  }

  // 回源下载（并发去重）
  try {
    if (!inflightDownloads.has(filename)) {
      inflightDownloads.set(filename, fetchToFile(sourceUrl, localPath).finally(() => {
        inflightDownloads.delete(filename);
      }));
    }
    await inflightDownloads.get(filename);
  } catch (err) {
    console.error('[DOWNLOAD] 回源失败:', err.message);
    // 清理可能的半成品
    try { fs.existsSync(localPath) && fs.unlinkSync(localPath); } catch {}
    return sendJson(res, 502, { error: 'origin_fetch_failed', message: String(err.message || err) });
  }

  return sendFile(res, localPath, filename, false);
}

/** 根据资产文件名，从（必要时刷新的）release 数据里找到 GitHub 真实下载地址。 */
async function resolveAssetUrl(filename) {
  // 确保有 release 数据
  if (!cache.data) {
    try {
      cache.data = await fetchLatestRelease();
      cache.fetchedAt = Date.now();
    } catch {
      return null;
    }
  }
  const assets = (cache.data && cache.data.assets) || [];
  const hit = assets.find((a) => a.name === filename);
  return hit ? hit.githubUrl : null;
}

/** 从 url 下载到 destPath（先写临时文件再原子重命名），自动跟随重定向。 */
function fetchToFile(url, destPath, redirectsLeft = 5) {
  return new Promise((resolve, reject) => {
    if (redirectsLeft < 0) return reject(new Error('重定向次数过多'));

    const tmpPath = destPath + '.part';
    const options = {
      headers: { 'User-Agent': 'Navi-Link-Server', Accept: 'application/octet-stream' },
    };
    if (CONFIG.token && url.startsWith('https://api.github.com')) {
      options.headers.Authorization = `Bearer ${CONFIG.token}`;
    }

    const req = https.get(url, options, (res) => {
      // 跟随重定向（GitHub 资产会 302 到 objects.githubusercontent.com）
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        res.resume();
        return resolve(fetchToFile(res.headers.location, destPath, redirectsLeft - 1));
      }
      if (res.statusCode !== 200) {
        res.resume();
        return reject(new Error(`回源 HTTP ${res.statusCode}`));
      }
      const out = fs.createWriteStream(tmpPath);
      res.pipe(out);
      out.on('finish', () => out.close(() => {
        try {
          fs.renameSync(tmpPath, destPath);
          console.log('[DOWNLOAD] 已缓存:', path.basename(destPath));
          resolve();
        } catch (e) {
          reject(e);
        }
      }));
      out.on('error', (e) => {
        try { fs.unlinkSync(tmpPath); } catch {}
        reject(e);
      });
    });
    req.on('error', reject);
    req.setTimeout(120000, () => req.destroy(new Error('回源下载超时')));
  });
}

/** 发送本地文件（APK），带正确的下载头。 */
function sendFile(res, filePath, filename, headOnly) {
  let stat;
  try {
    stat = fs.statSync(filePath);
  } catch {
    return sendJson(res, 404, { error: 'not_found' });
  }
  res.writeHead(200, {
    'Content-Type': 'application/vnd.android.package-archive',
    'Content-Length': stat.size,
    'Content-Disposition': `attachment; filename="${filename}"`,
    'Cache-Control': 'public, max-age=86400',
  });
  if (headOnly) return res.end();
  fs.createReadStream(filePath).pipe(res);
}


const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.ico': 'image/x-icon',
  '.webp': 'image/webp',
  '.woff2': 'font/woff2',
};

function serveStatic(pathname, res) {
  // 根路径 → index.html
  let rel = pathname === '/' ? '/index.html' : pathname;
  // 防目录穿越
  const safePath = path.normalize(path.join(PUBLIC_DIR, rel));
  if (!safePath.startsWith(PUBLIC_DIR)) {
    return sendJson(res, 403, { error: 'forbidden' });
  }

  fs.stat(safePath, (err, stat) => {
    if (err || !stat.isFile()) {
      // SPA 回退：未知路径返回首页
      const fallback = path.join(PUBLIC_DIR, 'index.html');
      return fs.readFile(fallback, (e2, buf) => {
        if (e2) return sendJson(res, 404, { error: 'not_found' });
        res.writeHead(200, { 'Content-Type': MIME['.html'] });
        res.end(buf);
      });
    }
    const ext = path.extname(safePath).toLowerCase();
    res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' });
    fs.createReadStream(safePath).pipe(res);
  });
}

// ── 工具函数 ────────────────────────────────────────────────────
function sendJson(res, code, obj) {
  const buf = Buffer.from(JSON.stringify(obj, null, 2));
  res.writeHead(code, { 'Content-Type': 'application/json; charset=utf-8' });
  res.end(buf);
}

/** 极简 .env 加载器（无外部依赖）。文件不存在则静默跳过。 */
function loadDotEnv(file) {
  try {
    const text = fs.readFileSync(file, 'utf8');
    for (const line of text.split('\n')) {
      const m = line.match(/^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)\s*$/);
      if (!m) continue;
      const key = m[1];
      let val = m[2].trim();
      if (val.startsWith('#')) continue;
      // 去引号
      if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) {
        val = val.slice(1, -1);
      }
      if (process.env[key] === undefined) process.env[key] = val;
    }
  } catch {
    /* .env 不存在，忽略 */
  }
}
