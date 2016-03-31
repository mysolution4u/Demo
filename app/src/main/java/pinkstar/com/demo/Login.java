package pinkstar.com.demo;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.view.Gravity.CENTER;

public class Login extends Activity {
    private static final String TAG = Login.class.getSimpleName();
    private EditText et_userid, et_password;
    private Button btn_submit;
    private String str_userid, str_password;
    private ProgressDialog pDialog;
    private PrefManager session;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler

        // Session manager
        session = new PrefManager(getApplicationContext());
        et_userid = (EditText) findViewById(R.id.userid);
        et_password = (EditText) findViewById(R.id.login_password);
        btn_submit = (Button) findViewById(R.id.btn_login);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppConfig.isNetworkAvailable(Login.this)) {
                    str_userid = et_userid.getText().toString();
                    str_password = et_password.getText().toString();
                    if (!str_userid.isEmpty() && !str_password.isEmpty()) {
                        // login user
                        checkLogin(str_userid, str_password);
                    } else {
                        // Prompt user to enter credentials
                        Toast.makeText(getApplicationContext(),
                                "Please enter the credentials!", Toast.LENGTH_LONG)
                                .show();
                    }
                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "No internet", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

            }
        });
    }

    private void checkLogin(final String str_userid, final String str_password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    int udata = jObj.getInt("udata");
                    if (udata == 0) {
                        Toast toast = Toast.makeText(getApplicationContext(), "User not regsitered", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                    }
                    if (udata == 1) {
                        Toast toast = Toast.makeText(getApplicationContext(), "User not active", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    if (udata == 2) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Password is wrong", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    if (udata == 4) {
                        JSONArray jsonArray = jObj.getJSONArray("result");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String userid = jsonObject.getString("user_id");
                            Toast.makeText(getApplicationContext(), "" + userid, Toast.LENGTH_SHORT).show();
                            session.setLogin(userid);
                        }
                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();


                    }
                    // Check for error node in json
                    // session.setLogin(true);


                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("rquest", "user_login");
                params.put("username", str_userid);
                params.put("password", str_password);

                return params;
            }

        };

        // Adding request to request queue
        MyApp.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}