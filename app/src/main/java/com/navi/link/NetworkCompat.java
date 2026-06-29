package com.navi.link;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Network compatibility helpers for legacy Android releases.
 *
 * <p>Android 4.2-4.4 includes TLS 1.2 but does not enable it by default for
 * client sockets. Modern update servers commonly require TLS 1.2 or newer.</p>
 */
final class NetworkCompat {

    private static final String TAG = "NetworkCompat";

    private NetworkCompat() {}

    static HttpURLConnection open(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        if (connection instanceof HttpsURLConnection
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);
                ((HttpsURLConnection) connection).setSSLSocketFactory(
                        new Tls12SocketFactory(sslContext.getSocketFactory()));
            } catch (GeneralSecurityException e) {
                // Keep the platform default as a fallback for unusual vendor ROMs.
                Log.w(TAG, "Unable to enable TLS 1.2 on this device", e);
            }
        }
        return (HttpURLConnection) connection;
    }

    private static final class Tls12SocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;

        Tls12SocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        private Socket enableTls12(Socket socket) {
            if (socket instanceof SSLSocket) {
                SSLSocket sslSocket = (SSLSocket) socket;
                if (Arrays.asList(sslSocket.getSupportedProtocols()).contains("TLSv1.2")) {
                    sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
                }
            }
            return socket;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException {
            return enableTls12(delegate.createSocket(socket, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return enableTls12(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
                throws IOException {
            return enableTls12(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enableTls12(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port,
                                   InetAddress localAddress, int localPort)
                throws IOException {
            return enableTls12(delegate.createSocket(address, port, localAddress, localPort));
        }
    }
}
