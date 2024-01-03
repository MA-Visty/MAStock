package be.heh.projet_mastock.Activity

import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import be.heh.projet_mastock.R

class WebActivity : AppCompatActivity() {
    // WebView instance to display the web content
    private lateinit var wa_wv: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for the activity
        setContentView(R.layout.activity_web)

        // Retrieve the URL from the intent
        val intent = intent
        if (intent.hasExtra("url")) {
            val url = intent.getStringExtra("url")
            // Initialize WebView
            wa_wv = findViewById<WebView>(R.id.wa_wv)
            if (url != null) {
                // Ensure the URL starts with "http://" or "https://"
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    wa_wv.loadUrl("https://$url")
                } else {
                    wa_wv.loadUrl(url)
                }
            }
            // Enable JavaScript in the WebView
            wa_wv.settings.javaScriptEnabled = true
            // Set a WebViewClient to handle URL loading within the WebView
            wa_wv.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    // Load the URL within the WebView
                    view!!.loadUrl(request!!.url.toString())
                    return true
                }
            }
        }
    }

    // Handle the back key press to navigate back in WebView history
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && wa_wv.canGoBack()) {
            // Navigate back in WebView history
            wa_wv.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
