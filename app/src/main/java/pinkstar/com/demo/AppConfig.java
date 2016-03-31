package pinkstar.com.demo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Rskaushik on 02-03-2016.
 */
public class AppConfig {
    public static String URL_LOGIN = "http://pinkstarapp.com/api/restservices.php";

    // Server user register url
    public static String URL_FORM_SUBMIT = "http://pinkstarapp.com/api/restservices.php?rquest=latlong_register";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
