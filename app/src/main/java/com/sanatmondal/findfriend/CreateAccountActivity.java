package com.sanatmondal.findfriend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {
    private static final String TAG = "CreateAccountActivity";

    EditText _input_user_name, _input_last_name, _nameText, _emailText, _passwordText, _input_contact, _address_contact;
    Button _signupButton;
    TextView _loginLink;

    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initWidget();
    }

    private void initWidget() {

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        _signupButton = (Button) findViewById(R.id.btn_signup);
        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });

        _input_user_name = (EditText) findViewById(R.id.input_user_name);
        _nameText = (EditText) findViewById(R.id.input_name);
        _emailText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _input_last_name = (EditText) findViewById(R.id.input_last_name);
        _input_contact = (EditText) findViewById(R.id.input_contact);
        _address_contact = (EditText) findViewById(R.id.address);

    }

    private void signup() {

        if (!validate()) {
            onSignupFailed();
            return;
        } else {
            post_request();
        }
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String user_id = _input_user_name.getText().toString();
        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String input_last_name = _input_last_name.getText().toString();
        String input_contact = _input_contact.getText().toString();
        String address_contact = _address_contact.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (user_id.isEmpty() || user_id.length() < 3) {
            _input_user_name.setError("enter a valid User ID");
            valid = false;
        } else {
            _input_user_name.setError(null);
        }

        if (input_last_name.isEmpty() || input_last_name.length() < 3) {
            _input_last_name.setError("enter a valid Last Name");
            valid = false;
        } else {
            _input_last_name.setError(null);
        }

        if (address_contact.isEmpty() || address_contact.length() < 3) {
            _address_contact.setError("enter a valid Address");
            valid = false;
        } else {
            _address_contact.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (input_contact.isEmpty() || input_contact.length() < 3) {
            _input_contact.setError("enter a valid contact");
            valid = false;
        } else {
            _input_contact.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private void createAccount() {

        String SendURL = getResources().getString(R.string.createAccount);

        SendURL = SendURL + "username=" + _nameText.getText().toString().trim() + "&email=" + _emailText.getText().toString().trim() +
                "&firstname=" + _nameText.getText().toString().trim() + "&lastname=" + _input_last_name.getText().toString().trim() +
                "&contact=" + _input_contact.getText().toString().trim() + "&address=" + _address_contact.getText().toString().trim() +
                "&lat=" + "23.779392" + "&lng=" + "90.432743";

        Log.d(TAG, "SendURL: " + SendURL);

        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                SendURL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                Log.d(TAG, "onResponse:========>>>>>>> " + response.toString());

                try {
                    // Parsing json object response
                    // response will be a json object

                    String status = response.getString("success");
                    if (status.equalsIgnoreCase("1")) {

                        JSONArray message_info = response.getJSONArray("message_info");

                    } else {
                        Toast.makeText(getApplicationContext(), "Device Location not found", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Data not found. Please try again later.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Log.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Data not found. Please try again later.", Toast.LENGTH_LONG).show();
            }
        });

        mRequestQueue.add(jsonObjReq);

    }

    private void post_request(){
        String SendURL = getResources().getString(R.string.createAccount);
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest postRequest = new StringRequest(Request.Method.POST, SendURL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("username", _nameText.getText().toString().trim());
                params.put("email", _emailText.getText().toString().trim());
                params.put("firstname", _nameText.getText().toString().trim());
                params.put("lastname", _input_last_name.getText().toString().trim());
                params.put("contact", _input_contact.getText().toString().trim());
                params.put("address", _address_contact.getText().toString().trim());
                params.put("lat", "23.779392");
                params.put("lng", "90.432743");
                return params;
            }
        };
        mRequestQueue.add(postRequest);
    }
}
