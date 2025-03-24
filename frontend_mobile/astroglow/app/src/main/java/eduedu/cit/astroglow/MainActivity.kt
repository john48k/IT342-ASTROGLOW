package eduedu.cit.astroglow

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true  // Enables local storage
        webView.webViewClient = WebViewClient()  // Prevents opening links in an external browser
        webView.loadUrl("file:///android_asset/index.html")  // Loads your React app

        setContentView(webView)  // Replaces Jetpack Compose UI with WebView
    }
}
