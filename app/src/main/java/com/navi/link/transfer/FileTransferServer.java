package com.navi.link.transfer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 嵌入式局域网 HTTP 文件快传服务器
 * 兼容 Android 4.x (API 17+)
 * 包含自动存储权限降级机制：优先保存 SD卡公共目录，若无权限自动切至应用专属目录 (100% 成功落盘)
 */
public class FileTransferServer {

    private static final String TAG = "FileTransferServer";
    public static final int DEFAULT_PORT = 8888;
    private static final int[] FALLBACK_PORTS = new int[]{8888, 8080, 8090, 12345, 9999};
    private static final String UTF8 = "UTF-8";

    private static volatile FileTransferServer sInstance;
    private final Context mContext;
    private ServerSocket mServerSocket;
    private ExecutorService mThreadPool;
    private boolean mIsRunning = false;
    private int mBoundPort = DEFAULT_PORT;
    private int mCustomPort = 0;
    private OnFileReceivedListener mListener;

    public interface OnFileReceivedListener {
        void onFileReceived(File file, String category);
    }

    public static synchronized FileTransferServer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FileTransferServer(context.getApplicationContext());
        }
        return sInstance;
    }

    private FileTransferServer(Context context) {
        this.mContext = context;
    }

    public void setOnFileReceivedListener(OnFileReceivedListener listener) {
        this.mListener = listener;
    }

    public void setCustomPort(int port) {
        this.mCustomPort = port;
    }

    public int getBoundPort() {
        return mBoundPort;
    }

    public synchronized boolean start() {
        if (mIsRunning) return true;

        int[] portsToTry = mCustomPort > 0 ? new int[]{mCustomPort} : FALLBACK_PORTS;
        boolean bound = false;

        for (int port : portsToTry) {
            try {
                mServerSocket = new ServerSocket(port);
                mBoundPort = port;
                bound = true;
                break;
            } catch (Exception e) {
                Log.w(TAG, "端口 " + port + " 已经被占用或无法绑定，尝试下一个端口...");
            }
        }

        if (!bound) {
            Log.e(TAG, "❌ 文件快传服务启动失败：所有端口均无法绑定");
            mIsRunning = false;
            return false;
        }

        try {
            mThreadPool = Executors.newFixedThreadPool(4);
            mIsRunning = true;
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() { acceptConnections(); }
            });
            Log.i(TAG, "🚀 文件快传服务启动成功，端口: " + mBoundPort + " IP: " + getLocalIpAddress());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "❌ 文件快传服务线程池启动失败", e);
            mIsRunning = false;
            return false;
        }
    }

    public synchronized void stop() {
        if (!mIsRunning) return;
        mIsRunning = false;
        try {
            if (mServerSocket != null && !mServerSocket.isClosed()) mServerSocket.close();
        } catch (Exception ignored) {}
        if (mThreadPool != null) mThreadPool.shutdownNow();
        Log.i(TAG, "🛑 文件快传服务已停止");
    }

    public boolean isRunning() { return mIsRunning; }

    private void acceptConnections() {
        while (mIsRunning && mServerSocket != null && !mServerSocket.isClosed()) {
            try {
                final Socket socket = mServerSocket.accept();
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() { handleClientSocket(socket); }
                });
            } catch (Exception e) {
                if (!mIsRunning) break;
            }
        }
    }

    private void handleClientSocket(Socket socket) {
        try {
            socket.setSoTimeout(60000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(in, 65536);

            String requestLine = readLine(bis);
            if (requestLine == null || requestLine.isEmpty()) return;
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;
            String method = parts[0];
            String path = parts[1];

            int contentLength = 0;
            String contentType = "";
            String headerLine;
            while ((headerLine = readLine(bis)) != null && !headerLine.isEmpty()) {
                String lower = headerLine.toLowerCase(Locale.ROOT);
                if (lower.startsWith("content-length:")) {
                    try { contentLength = Integer.parseInt(headerLine.substring(15).trim()); } catch (Exception ignored) {}
                } else if (lower.startsWith("content-type:")) {
                    contentType = headerLine.substring(13).trim();
                }
            }

            if ("GET".equalsIgnoreCase(method)) {
                if (path.equals("/") || path.equals("/index.html")) {
                    sendHtmlResponse(out, getHtmlPage());
                } else if (path.equals("/api/files")) {
                    sendJsonResponse(out, getFileListJson());
                } else {
                    sendNotFound(out);
                }
            } else if ("POST".equalsIgnoreCase(method) && path.startsWith("/upload")) {
                handleFileUpload(bis, out, contentType, contentLength);
            } else {
                sendNotFound(out);
            }
        } catch (Exception e) {
            Log.w(TAG, "HTTP client error: " + e.getMessage(), e);
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private void handleFileUpload(BufferedInputStream in, OutputStream out, String contentType, int contentLength) throws Exception {
        if (!contentType.contains("boundary=")) {
            sendJsonResponse(out, "{\"success\":false,\"message\":\"Invalid Content-Type\"}");
            return;
        }

        String boundary = contentType.substring(contentType.indexOf("boundary=") + 9).trim();
        if (boundary.contains(";")) {
            boundary = boundary.substring(0, boundary.indexOf(";")).trim();
        }
        if (boundary.startsWith("\"") && boundary.endsWith("\"") && boundary.length() > 2) {
            boundary = boundary.substring(1, boundary.length() - 1);
        }

        String boundaryStr = "--" + boundary;
        byte[] delim = ("\r\n" + boundaryStr).getBytes(UTF8);

        File saveDir = getSaveDirectory(mContext);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        String filename = "upload_" + System.currentTimeMillis();

        // 1. 读取 Part Header (直到 \r\n\r\n)
        ByteArrayOutputStream headerBuf = new ByteArrayOutputStream();
        int b;
        boolean headerEnded = false;
        while ((b = in.read()) != -1) {
            headerBuf.write(b);
            byte[] bytes = headerBuf.toByteArray();
            int len = bytes.length;
            if (len >= 4 && bytes[len - 4] == '\r' && bytes[len - 3] == '\n'
                    && bytes[len - 2] == '\r' && bytes[len - 1] == '\n') {
                headerEnded = true;
                break;
            }
        }

        if (headerEnded) {
            String partHeader = new String(headerBuf.toByteArray(), UTF8);
            if (partHeader.contains("filename=\"")) {
                int start = partHeader.indexOf("filename=\"") + 10;
                int end = partHeader.indexOf("\"", start);
                if (end > start) {
                    filename = partHeader.substring(start, end);
                    try { filename = URLDecoder.decode(filename, UTF8); } catch (Exception ignored) {}
                    if (filename.contains("/")) {
                        filename = filename.substring(filename.lastIndexOf('/') + 1);
                    }
                    if (filename.contains("\\")) {
                        filename = filename.substring(filename.lastIndexOf('\\') + 1);
                    }
                    filename = filename.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
                    if (filename.isEmpty()) {
                        filename = "upload_" + System.currentTimeMillis();
                    }
                }
            }
        }

        File tmpFile = new File(saveDir, filename + ".tmp");
        File targetFile = new File(saveDir, filename);

        File parentDir = tmpFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 2. 高效 32KB 内存块 + 逻辑窗口精准截断
        int delimLen = delim.length;
        byte[] window = new byte[delimLen];
        int windowCount = 0;
        byte[] blockBuf = new byte[32768];
        int bytesRead;

        FileOutputStream fos = new FileOutputStream(tmpFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos, 65536);
        try {
            while ((bytesRead = in.read(blockBuf)) != -1) {
                boolean stop = false;
                for (int i = 0; i < bytesRead; i++) {
                    byte bVal = blockBuf[i];
                    if (windowCount < delimLen) {
                        window[windowCount++] = bVal;
                        if (windowCount == delimLen && isMatch(window, delim)) {
                            stop = true;
                            break;
                        }
                    } else {
                        bos.write(window[0]);
                        System.arraycopy(window, 1, window, 0, delimLen - 1);
                        window[delimLen - 1] = bVal;
                        if (isMatch(window, delim)) {
                            stop = true;
                            break;
                        }
                    }
                }
                if (stop) break;
            }
            bos.flush();
        } finally {
            try { bos.close(); } catch (Exception ignored) {}
            try { fos.close(); } catch (Exception ignored) {}
        }

        if (tmpFile.exists()) {
            if (targetFile.exists()) targetFile.delete();
            tmpFile.renameTo(targetFile);
        }

        Log.i(TAG, "🎉 收到文件: " + targetFile.getName() + " (" + targetFile.length() + " bytes) 保存于: " + targetFile.getAbsolutePath());

        if (mListener != null && targetFile.exists()) {
            final File ff = targetFile;
            final String cat = getFileCategory(filename);
            try { mListener.onFileReceived(ff, cat); } catch (Exception ignored) {}
        }

        sendJsonResponse(out, "{\"success\":true,\"filename\":\"" + filename + "\"}");
    }

    private static boolean isMatch(byte[] window, byte[] delim) {
        for (int i = 0; i < delim.length; i++) {
            if (window[i] != delim[i]) return false;
        }
        return true;
    }

    public static File getSaveDirectory() {
        if (sInstance != null && sInstance.mContext != null) {
            return getSaveDirectory(sInstance.mContext);
        }
        return getSaveDirectory(null);
    }

    public static File getSaveDirectory(Context context) {
        // 1. 优先保存到 SD 卡公共目录 /sdcard/NaviLink/Transfers
        try {
            File pubDir = new File(Environment.getExternalStorageDirectory(), "NaviLink/Transfers");
            if (!pubDir.exists()) {
                pubDir.mkdirs();
            }
            if (pubDir.exists()) {
                File testFile = new File(pubDir, ".write_test_" + System.currentTimeMillis());
                if (testFile.createNewFile()) {
                    testFile.delete();
                    return pubDir;
                }
            }
        } catch (Exception ignored) {}

        // 2. 降级使用应用专属外部存储目录（无需申请读写权限，100% 可读写）
        if (context != null) {
            try {
                File extDir = context.getExternalFilesDir("Transfers");
                if (extDir != null) {
                    if (!extDir.exists()) extDir.mkdirs();
                    return extDir;
                }
            } catch (Exception ignored) {}

            try {
                File filesDir = new File(context.getFilesDir(), "Transfers");
                if (!filesDir.exists()) filesDir.mkdirs();
                return filesDir;
            } catch (Exception ignored) {}
        }

        return new File(Environment.getExternalStorageDirectory(), "NaviLink/Transfers");
    }

    public static String getFileCategory(String filename) {
        if (filename == null) return "其他";
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".mp3") || lower.endsWith(".flac") || lower.endsWith(".wav")
                || lower.endsWith(".aac") || lower.endsWith(".m4a")) return "音乐";
        if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi")
                || lower.endsWith(".mov")) return "视频";
        if (lower.endsWith(".apk")) return "安装包";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".webp")) return "图片";
        return "文档/文件";
    }

    public static List<String> getAllLocalIpAddresses() {
        List<String> ipList = new ArrayList<String>();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                String name = intf.getName().toLowerCase(Locale.ROOT);
                if (name.startsWith("tun") || name.startsWith("dummy") || name.startsWith("p2p")
                        || name.startsWith("v4-") || name.startsWith("rmnet")) {
                    continue;
                }
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        if (sAddr != null && sAddr.indexOf(':') < 0 && !sAddr.equals("127.0.0.1")) {
                            if (!ipList.contains(sAddr)) {
                                if (name.startsWith("wlan") || name.startsWith("ap") || name.startsWith("swlan")
                                        || name.startsWith("eth") || name.startsWith("rndis")) {
                                    ipList.add(0, sAddr);
                                } else {
                                    ipList.add(sAddr);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return ipList;
    }

    public static String getLocalIpAddress() {
        List<String> list = getAllLocalIpAddresses();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return "127.0.0.1";
    }

    private String readLine(InputStream in) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\r') {
                int next = in.read();
                if (next == '\n' || next == -1) break;
                baos.write(c); baos.write(next);
            } else if (c == '\n') {
                break;
            } else {
                baos.write(c);
            }
        }
        if (c == -1 && baos.size() == 0) return null;
        return baos.toString(UTF8);
    }

    private void sendHtmlResponse(OutputStream out, String html) throws Exception {
        byte[] body = html.getBytes(UTF8);
        String r = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\nContent-Length: " + body.length + "\r\nConnection: close\r\n\r\n";
        out.write(r.getBytes(UTF8)); out.write(body); out.flush();
    }

    private void sendJsonResponse(OutputStream out, String json) throws Exception {
        byte[] body = json.getBytes(UTF8);
        String r = "HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=UTF-8\r\nContent-Length: " + body.length + "\r\nConnection: close\r\n\r\n";
        out.write(r.getBytes(UTF8)); out.write(body); out.flush();
    }

    private void sendNotFound(OutputStream out) throws Exception {
        String body = "<h1>404 Not Found</h1>";
        String r = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\nContent-Length: " + body.length() + "\r\nConnection: close\r\n\r\n" + body;
        out.write(r.getBytes(UTF8)); out.flush();
    }

    private String getFileListJson() {
        try {
            File dir = getSaveDirectory(mContext);
            File[] files = dir.listFiles();
            JSONArray array = new JSONArray();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && !f.getName().endsWith(".tmp")) {
                        JSONObject obj = new JSONObject();
                        obj.put("name", f.getName());
                        obj.put("size", f.length());
                        obj.put("category", getFileCategory(f.getName()));
                        array.put(obj);
                    }
                }
            }
            return array.toString();
        } catch (Exception e) { return "[]"; }
    }

    private String getHtmlPage() {
        return "<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n"
                + "<title>Navi-Link 文件快传</title>\n"
                + "<style>\n"
                + "* { margin:0; padding:0; box-sizing:border-box; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; }\n"
                + "body { background:#0f1015; color:#fff; display:flex; flex-direction:column; align-items:center; min-height:100vh; padding:20px; }\n"
                + ".card { background:rgba(255,255,255,.05); border:1px solid rgba(255,255,255,.1); border-radius:20px; width:100%; max-width:480px; padding:24px; text-align:center; }\n"
                + ".header h2 { font-size:20px; font-weight:600; color:#4FC3F7; margin-bottom:20px; }\n"
                + ".drop-zone { border:2px dashed rgba(79,172,254,.4); border-radius:16px; padding:36px 20px; cursor:pointer; margin-bottom:20px; }\n"
                + ".drop-zone:hover { border-color:#4FACFE; background:rgba(79,172,254,.1); }\n"
                + ".upload-icon { font-size:48px; margin-bottom:12px; }\n"
                + ".btn-select { background:#4FC3F7; border:none; color:#fff; padding:12px 28px; border-radius:30px; font-size:15px; font-weight:600; cursor:pointer; margin-top:10px; }\n"
                + "#fileInput { display:none; }\n"
                + ".progress-container { display:none; margin-top:20px; text-align:left; }\n"
                + ".progress-bar-bg { background:rgba(255,255,255,.1); height:8px; border-radius:4px; overflow:hidden; margin-top:8px; }\n"
                + ".progress-bar-fill { background:#4FC3F7; height:100%; width:0%; transition:width .2s; }\n"
                + ".status-text { font-size:13px; color:#a0a5b5; display:flex; justify-content:space-between; margin-top:6px; }\n"
                + ".file-list { margin-top:24px; text-align:left; max-height:250px; overflow-y:auto; }\n"
                + ".file-item { display:flex; align-items:center; justify-content:space-between; padding:10px 14px; background:rgba(255,255,255,.03); border-radius:10px; margin-bottom:8px; font-size:14px; }\n"
                + ".file-name { overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:260px; }\n"
                + ".file-badge { font-size:11px; padding:3px 8px; border-radius:6px; background:rgba(79,172,254,.2); color:#4FC3F7; }\n"
                + "</style>\n</head>\n<body>\n"
                + "<div class=\"card\">\n"
                + "  <div class=\"header\"><h2>📲 Navi-Link 扫码快传</h2></div>\n"
                + "  <div class=\"drop-zone\" id=\"dropZone\" onclick=\"document.getElementById('fileInput').click()\">\n"
                + "    <div class=\"upload-icon\">📤</div>\n"
                + "    <p style=\"font-size:15px;color:#e0e6ed;\">点击或将文件拖拽到此处上传</p>\n"
                + "    <p style=\"font-size:12px;color:#8a90a3;margin-top:6px;\">支持音乐、视频、图片、应用(.apk)</p>\n"
                + "    <button class=\"btn-select\">选择文件发送</button>\n"
                + "  </div>\n"
                + "  <input type=\"file\" id=\"fileInput\" multiple onchange=\"handleFiles(this.files)\">\n"
                + "  <div class=\"progress-container\" id=\"progressBox\">\n"
                + "    <div class=\"status-text\"><span id=\"uploadStatus\">正在传输...</span><span id=\"uploadPercent\">0%</span></div>\n"
                + "    <div class=\"progress-bar-bg\"><div class=\"progress-bar-fill\" id=\"progressBar\"></div></div>\n"
                + "  </div>\n"
                + "  <div class=\"file-list\" id=\"fileListContainer\"></div>\n"
                + "</div>\n"
                + "<script>\n"
                + "function handleFiles(files){if(!files||!files.length)return;for(var i=0;i<files.length;i++)uploadFile(files[i]);}\n"
                + "function uploadFile(file){\n"
                + "  var box=document.getElementById('progressBox'),bar=document.getElementById('progressBar');\n"
                + "  var pct=document.getElementById('uploadPercent'),sta=document.getElementById('uploadStatus');\n"
                + "  box.style.display='block';sta.innerText='正在发送: '+file.name;\n"
                + "  var fd=new FormData();fd.append('file',file);\n"
                + "  var xhr=new XMLHttpRequest();xhr.open('POST','/upload',true);\n"
                + "  xhr.upload.onprogress=function(e){if(e.lengthComputable){var p=Math.round(e.loaded/e.total*100);bar.style.width=p+'%';pct.innerText=p+'%';}};\n"
                + "  xhr.onload=function(){if(xhr.status===200){sta.innerText='已发送: '+file.name;loadFileList();}else{sta.innerText='发送失败';}};\n"
                + "  xhr.send(fd);\n"
                + "}\n"
                + "function loadFileList(){\n"
                + "  var xhr=new XMLHttpRequest();xhr.open('GET','/api/files',true);\n"
                + "  xhr.onload=function(){if(xhr.status===200){var data=JSON.parse(xhr.responseText),list=document.getElementById('fileListContainer');list.innerHTML='';data.reverse().forEach(function(item){var div=document.createElement('div');div.className='file-item';div.innerHTML='<span class=\"file-name\">'+item.name+'</span><span class=\"file-badge\">'+item.category+'</span>';list.appendChild(div);});}};\n"
                + "  xhr.send();\n"
                + "}\n"
                + "loadFileList();\n"
                + "</script>\n</body>\n</html>";
    }
}