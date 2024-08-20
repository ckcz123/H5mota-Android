import android.net.Uri
import android.util.Log
import android.webkit.CookieManager
import com.h5mota.core.base.Constant
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SNIHostName
import javax.net.ssl.SNIServerName
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

val httpClient = OkHttpClient()

val hostList = listOf("baidu.com", "www.bilibili.com", "weibo.com", "www.zhihu.com")

val proxyHost = hostList.random()

/**
 * A [SSLSocketFactory] that delegates calls. Sockets can be configured after creation by
 * overriding [.configureSocket].
 */
open class DelegatingSSLSocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {
    @Throws(IOException::class)
    override fun createSocket(): SSLSocket {
        val sslSocket = delegate.createSocket() as SSLSocket
        return configureSocket(sslSocket)
    }

    @Throws(IOException::class)
    override fun createSocket(
        host: String,
        port: Int,
    ): SSLSocket {
        val sslSocket = delegate.createSocket(configureHost(host), port) as SSLSocket
        return configureSocket(sslSocket)
    }

    @Throws(IOException::class)
    override fun createSocket(
        host: String,
        port: Int,
        localAddress: InetAddress,
        localPort: Int,
    ): SSLSocket {
        val sslSocket = delegate.createSocket(configureHost(host), port, localAddress, localPort) as SSLSocket
        return configureSocket(sslSocket)
    }

    @Throws(IOException::class)
    override fun createSocket(
        host: InetAddress,
        port: Int,
    ): SSLSocket {
        val sslSocket = delegate.createSocket(configureHost(host), port) as SSLSocket
        return configureSocket(sslSocket)
    }

    @Throws(IOException::class)
    override fun createSocket(
        host: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int,
    ): SSLSocket {
        val sslSocket = delegate.createSocket(configureHost(host), port, localAddress, localPort) as SSLSocket
        return configureSocket(sslSocket)
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return delegate.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return delegate.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(
        socket: Socket,
        host: String,
        port: Int,
        autoClose: Boolean,
    ): SSLSocket {
        val sslSocket = delegate.createSocket(socket, host, port, autoClose) as SSLSocket
        return configureSocket(sslSocket)
    }

    @Throws(IOException::class)
    protected open fun configureSocket(sslSocket: SSLSocket): SSLSocket {
        // No-op by default.
        return sslSocket
    }

    protected open fun configureHost(host: String): String {
        return host
    }

    protected open fun configureHost(host: InetAddress): InetAddress {
        return host
    }
}

class CustomSSLSocketFactory(
    delegate: SSLSocketFactory,
) : DelegatingSSLSocketFactory(delegate) {

    override fun configureSocket(sslSocket: SSLSocket): SSLSocket {
        val parameters = sslSocket.sslParameters
        val sniList = parameters.serverNames
        val host = Uri.parse(Constant.DOMAIN).host
        val matcher = SNIHostName.createSNIMatcher(host)
        if (sniList.any { sni -> matcher.matches(sni) }) {
            parameters.serverNames = mutableListOf<SNIServerName>(SNIHostName(proxyHost))
            sslSocket.sslParameters = parameters
        }
        return sslSocket
    }
}

class CustomCookieJar: CookieJar {
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieManager = CookieManager.getInstance()
        val cookie = cookieManager.getCookie(url.toString())
        return cookie?.split(";")?.mapNotNull {
            span -> Cookie.parse(url, span)
        } ?: listOf()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString();
        val cookieManager = CookieManager.getInstance()
        cookies.forEach {
            cookie -> cookieManager.setCookie(urlString, cookie.toString())
        }
    }
}

fun useHttpClientBuilder(): OkHttpClient.Builder {
    return if (Constant.USE_PROXY) {
        httpClient
            .newBuilder()
            .cookieJar(CustomCookieJar())
            .sslSocketFactory(CustomSSLSocketFactory(httpClient.sslSocketFactory), httpClient.x509TrustManager!!)
    } else {
        httpClient.newBuilder()
    }
}