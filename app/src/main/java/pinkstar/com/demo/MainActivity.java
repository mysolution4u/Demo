package pinkstar.com.demo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity implements LocationListener, View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    protected LocationManager locationManager;
    protected Context context;
    private double latitude = 0, longitude = 0;
    private TextView lat, lng, adres, tcity, tstate, tpincode, tcountry;
    private Button submit_latlng, refresh;
    private EditText form_id;
    private ProgressDialog dialog;
    Geocoder geocoder;
    private List<Address> addresses;
    private String address, city, state, country, postalCode, get_form_id, token;
    private ProgressDialog pDialog;
    PrefManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        session = new PrefManager(MainActivity.this);
        HashMap<String, String> user = session.getUserDetails();
        // name
        token = user.get(PrefManager.KEY_IS_LOGGEDIN);
        Log.d("TOKENID", "" + token);
        lat = (TextView) findViewById(R.id.lat);
        lng = (TextView) findViewById(R.id.lng);
        adres = (TextView) findViewById(R.id.address);
        tcity = (TextView) findViewById(R.id.city);
        tcountry = (TextView) findViewById(R.id.country);
        tstate = (TextView) findViewById(R.id.state);
        tpincode = (TextView) findViewById(R.id.pincode);
        geocoder = new Geocoder(this, Locale.getDefault());
        refresh = (Button) findViewById(R.id.refresh);
        submit_latlng = (Button) findViewById(R.id.submit_latlng);
        submit_latlng.setOnClickListener(this);
        form_id = (EditText) findViewById(R.id.form);
        refresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                refresh();
            }
        });

        dialog = new ProgressDialog(MainActivity.this);
        dialog.show();
        dialog.setMessage("Getting Coordinates");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 10000,
                    1, this);
        } else if (locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 10000,
                    1, this);
        } else {
            dialog.dismiss();

            Toast.makeText(getApplicationContext(), "Enable Location", Toast.LENGTH_LONG).show();
        }
    }

    protected void refresh() {

        super.onResume();
        this.onCreate(null);

    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        dialog.show();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        if (latitude != 0 && longitude != 0) {

            lat.setText("Latitude is :" + location.getLatitude());
            lng.setText("Longitude is :" + location.getLongitude());
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }
//
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
            country = addresses.get(0).getCountryName();
            postalCode = addresses.get(0).getPostalCode();
            adres.setText("Address is :" + address);
            tcity.setText("City is :" + city);
            tstate.setText("State is :" + state);
            tcountry.setText("Country is :" + country);
            tpincode.setText("Postal code is :" + postalCode);
            dialog.dismiss();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onClick(View v) {
        if (v == submit_latlng) {
            if (AppConfig.isNetworkAvailable(MainActivity.this)) {
                get_form_id = form_id.getText().toString();
                if (!get_form_id.isEmpty()) {

                    checkLogin(latitude, longitude, country, city, state, postalCode, get_form_id);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "No internet", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();


            }

        }
    }

    private void checkLogin(final double latitude, final double longitude, final String country, final String city, final String state, final String postalCode, final String get_form_id) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Submitting in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_FORM_SUBMIT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {


                    JSONObject jObj = new JSONObject(response);
                    int udata = jObj.getInt("udata");
                    if (udata == 0) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Unsuccessful", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                    }
                    if (udata == 1) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        startActivity(new Intent(MainActivity.this, ThankYou.class));
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
                params.put("token_id", token);
                params.put("form_id", get_form_id);
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));
                params.put("city", city);
                params.put("state", state);
                params.put("country", country);
                params.put("pincode", " " + postalCode);

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



