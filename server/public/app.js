/**
 * Navi-Link 官网前端脚本
 * ─────────────────────────────────────────────────
 * - 从 /api/latest 获取最新 release 信息
 * - 动态渲染下载按钮、版本号、更新日志
 * - 手动同步按钮逻辑
 * - 滚动入场动效
 */

(function () {
  'use strict';

  // ── 液态玻璃折射门控 ─────────────────────────────────
  // 真折射(SVG backdrop-filter:url) 仅 Chromium 生效，且开销大。
  // 只在 Chromium + 桌面/高端 + 未要求降级时启用，车机/移动端停留在基线毛玻璃。
  (function gateLiquid() {
    try {
      const ua = navigator.userAgent;
      const isChromium = (!!window.chrome && !/Edg|OPR/.test(ua)) ||
        (/Chrome\/\d+/.test(ua) && !/\bVersion\/[\d.]+\b/.test(ua));
      const cores = navigator.hardwareConcurrency || 2;
      const mem = navigator.deviceMemory || 2;
      const desktop = window.matchMedia('(pointer:fine)').matches;
      const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches ||
        window.matchMedia('(prefers-reduced-transparency: reduce)').matches;
      if (isChromium && desktop && cores >= 4 && mem >= 4 && !reduce) {
        document.documentElement.classList.add('has-liquid');
      }
    } catch (e) {
      /* 探测失败则保持基线毛玻璃 */
    }
  })();

  // ── DOM refs ────────────────────────────────────────
  const $ = (sel) => document.querySelector(sel);

  const refs = {
    heroVersion: $('#hero-version-tag'),
    heroDownload: $('#hero-download'),
    heroDownloadText: $('#hero-download-text'),
    downloadMeta: $('#download-meta'),
    downloadBtn: $('#download-btn'),
    downloadBtnText: $('#download-btn-text'),
    downloadSize: $('#download-size'),
    downloadCount: $('#download-count'),
    releaseLoading: $('#release-loading'),
    releaseContent: $('#release-content'),
    releaseError: $('#release-error'),
    releaseTitle: $('#release-title'),
    releaseDate: $('#release-date'),
    releaseNotes: $('#release-notes'),
    syncBtn: $('#sync-btn'),
    syncText: $('#sync-text'),
    syncStatus: $('#sync-status'),
    year: $('#year'),
  };

  // ── 初始化 ──────────────────────────────────────────
  if (refs.year) refs.year.textContent = new Date().getFullYear();
  initRevealObserver();
  loadRelease(false);
  if (refs.syncBtn) refs.syncBtn.addEventListener('click', () => loadRelease(true));

  // ── 加载 release 数据 ────────────────────────────────
  async function loadRelease(force) {
    setSyncing(force);
    try {
      const url = force ? '/api/sync' : '/api/latest';
      const resp = await fetch(url, { method: force ? 'POST' : 'GET' });
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
      const data = await resp.json();

      if (data.error) throw new Error(data.message || data.error);
      renderRelease(data);
      if (force) showSyncStatus('✓ 同步成功', 'ok');
    } catch (err) {
      showError(err.message);
      if (force) showSyncStatus(`✗ 同步失败: ${err.message}`, 'err');
    } finally {
      setSyncing(false);
    }
  }

  // ── 渲染 release 信息到页面 ────────────────────────────
  function renderRelease(data) {
    const version = data.version || '未知版本';
    const name = data.name || version;
    const apk = data.apk;
    const pub = data.publishedAt;

    // Hero
    if (refs.heroVersion) refs.heroVersion.textContent = `最新发布 ${version}`;
    if (refs.heroDownloadText) refs.heroDownloadText.textContent = `下载 ${version}`;
    if (apk) {
      setDownloadLink(refs.heroDownload, apk.downloadUrl);
    }

    // Download card
    if (refs.downloadMeta) refs.downloadMeta.textContent = `${name}  ·  ${formatDate(pub)}`;
    if (refs.downloadBtnText) refs.downloadBtnText.textContent = apk ? `下载 ${version} (.apk)` : `前往 Release 页面`;
    if (refs.downloadBtn) {
      setDownloadLink(refs.downloadBtn, apk ? apk.downloadUrl : data.htmlUrl);
      refs.downloadBtn.removeAttribute('aria-disabled');
    }
    if (refs.downloadSize && apk) refs.downloadSize.textContent = `大小 ${formatSize(apk.size)}`;
    if (refs.downloadCount && apk) refs.downloadCount.textContent = `下载次数 ${apk.downloadCount}`;

    // Release notes
    if (refs.releaseLoading) refs.releaseLoading.hidden = true;
    if (refs.releaseError) refs.releaseError.hidden = true;
    if (refs.releaseContent) refs.releaseContent.hidden = false;
    if (refs.releaseTitle) refs.releaseTitle.textContent = name;
    if (refs.releaseDate) refs.releaseDate.textContent = formatDate(pub);
    if (refs.releaseNotes) refs.releaseNotes.innerHTML = markdownToHtml(data.body || '暂无更新说明');
  }

  function showError(msg) {
    if (refs.releaseLoading) refs.releaseLoading.hidden = true;
    if (refs.releaseContent) refs.releaseContent.hidden = true;
    if (refs.releaseError) {
      refs.releaseError.hidden = false;
      refs.releaseError.textContent = msg || '加载失败';
    }
    if (refs.heroVersion) refs.heroVersion.textContent = '暂无版本信息';
  }

  // ── 同步按钮状态 ────────────────────────────────────
  function setSyncing(active) {
    if (!refs.syncBtn) return;
    refs.syncBtn.classList.toggle('spinning', active);
    refs.syncBtn.disabled = active;
    if (refs.syncText) refs.syncText.textContent = active ? '同步中…' : '手动同步';
  }

  function showSyncStatus(msg, type) {
    if (!refs.syncStatus) return;
    refs.syncStatus.textContent = msg;
    refs.syncStatus.style.color = type === 'ok' ? '#30d158' : type === 'err' ? '#ff6b6b' : '';
    clearTimeout(showSyncStatus._t);
    showSyncStatus._t = setTimeout(() => { refs.syncStatus.textContent = ''; }, 4000);
  }

  // ── 工具 ────────────────────────────────────────────
  function setDownloadLink(el, url) {
    if (!el || !url) return;
    el.href = url;
    el.target = '_blank';
    el.rel = 'noopener';
  }

  function formatDate(iso) {
    if (!iso) return '';
    try {
      const d = new Date(iso);
      return d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
    } catch {
      return iso.slice(0, 10);
    }
  }

  function formatSize(bytes) {
    if (!bytes) return '';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }

  /** 极简 markdown → HTML（支持 h1-h3, bold, code, links, lists, paragraphs） */
  function markdownToHtml(md) {
    let html = md
      // code blocks (fenced)
      .replace(/```[\s\S]*?```/g, (m) => `<pre><code>${esc(m.slice(3, -3).trim())}</code></pre>`)
      // inline code
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      // headings
      .replace(/^### (.+)$/gm, '<h3>$1</h3>')
      .replace(/^## (.+)$/gm, '<h2>$1</h2>')
      .replace(/^# (.+)$/gm, '<h1>$1</h1>')
      // bold
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      // links
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')
      // unordered list
      .replace(/^[*\-+] (.+)$/gm, '<li>$1</li>')
      // paragraphs
      .replace(/\n{2,}/g, '</p><p>');

    // wrap loose <li> in <ul>
    html = html.replace(/((?:<li>.*<\/li>\s*)+)/g, '<ul>$1</ul>');
    return '<p>' + html + '</p>';
  }

  function esc(s) {
    return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  // ── 滚动入场动效 ────────────────────────────────────
  function initRevealObserver() {
    // 给所有 feature / download / release 区域加 reveal class
    document.querySelectorAll('.feature, .download-card, .release-card').forEach((el) => {
      el.classList.add('reveal');
    });

    if (!('IntersectionObserver' in window)) {
      // 降级：直接显示
      document.querySelectorAll('.reveal').forEach((el) => el.classList.add('in'));
      return;
    }

    const io = new IntersectionObserver(
      (entries) => entries.forEach((e) => { if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target); } }),
      { threshold: 0.15 }
    );
    document.querySelectorAll('.reveal').forEach((el) => io.observe(el));
  }
})();
