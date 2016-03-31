package pinkstar.com.demo;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.util.Log;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebTestActivity extends Activity{

    WebView webView2;
    Button paymentbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webtest);
        paymentbutton=(Button)findViewById(R.id.paymentbutton);
        webView2=(WebView)findViewById(R.id.webView2);
        webView2.getSettings().setLoadsImagesAutomatically(true);
        webView2.getSettings().setJavaScriptEnabled(true);
        webView2.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        paymentbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView2.loadUrl("http://www.meratask.com/PayOrders/welcomePay.php?mobileReq=mobileReq&BookCost=BookCost&clitName=clitName&CltID=CltID&phone=phone&CliEmail=CliEmail");
                webView2.setVisibility(View.VISIBLE);
            }
        });
        webView2.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Here put your code
                Log.d("My Webview", url);
                if (url.contains("failed.php")) {
                    Toast.makeText(WebTestActivity.this,"Payment Failed!",Toast.LENGTH_LONG).show();
                    webView2.setVisibility(View.GONE);
                }else  if (url.contains("success.php")){
                    Toast.makeText(WebTestActivity.this,"Payment Success.",Toast.LENGTH_LONG).show();
                    webView2.setVisibility(View.GONE);
                }else  if (url.contains("cancelled.php")){
                    Toast.makeText(WebTestActivity.this,"Payment Cancelled!",Toast.LENGTH_LONG).show();
                    webView2.setVisibility(View.GONE);
                }
                // https://board-ncf.cs8.force.com/VF_BoardHome_Page?iosid=003L000000dV6pVIAS

                // return true; //Indicates WebView to NOT load the url;
                return false; // Allow WebView to load url
            }
        });
    }
}
