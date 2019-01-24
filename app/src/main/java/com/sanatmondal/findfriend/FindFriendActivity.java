package com.sanatmondal.findfriend;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class FindFriendActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = "FindFriendActivity";

    Context context;
    
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final float DEFAULT_ZOOM = 14f;
    private static final long REFRESH_INTERVAL = 10000;

    private GoogleMap mMap;
    Marker mMarker;
    ImageButton btnBack;
    ArrayList<Marker> markerList = new ArrayList<>();

    Handler handler = new Handler();
    ImageView mMarkerImageView;
    View mCustomMarkerView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        initWidget();

        if(isServicesOK()){
            initMap();
        }
    }

    private void initWidget() {
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private Runnable HandlerRunable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: " + "Message from handler...........");
            getAllLocation();
            handler.postDelayed(this, REFRESH_INTERVAL);
        }
    };

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(FindFriendActivity.this);
    }


    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(FindFriendActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(FindFriendActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getAllLocation();

        HandlerRunable.run();
    }

    private void getAllLocation(){
        Log.d(TAG, "sendLocation...........................");

        String SendURL = getResources().getString(R.string.getAllLocation);

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
                    if(status.equalsIgnoreCase("1")){

                        JSONArray message_info = response.getJSONArray("message_info");

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                        for (int i = 0; i < message_info.length(); i++){
                            String user_id = message_info.getJSONObject(i).getString("user_id");
                            String name = message_info.getJSONObject(i).getString("name");
                            String email = message_info.getJSONObject(i).getString("email");
                            String contact = message_info.getJSONObject(i).getString("contact");
                            String lat = message_info.getJSONObject(i).getString("lat");
                            String lng = message_info.getJSONObject(i).getString("lng");

                            builder.include(new MarkerOptions().position(new LatLng(Double.valueOf(lat), Double.valueOf(lng))).title(user_id).getPosition());

                            LatLng latLng = new LatLng(Double.valueOf(lat), Double.valueOf(lng));

                            setCenterMarker(latLng, user_id, name, email, contact);
                        }

                        LatLngBounds bounds = builder.build();
                        int width = getResources().getDisplayMetrics().widthPixels;
                        int height = getResources().getDisplayMetrics().heightPixels;
                        int padding = (int) (width * 0.15); // offset from edges of the map 10% of screen

                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                        mMap.animateCamera(cu);

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

    public void setCenterMarker(LatLng latLng, String user_id, String user_name, String email, String contact) {
        Log.d(TAG, "setCenterMarker: ");
/*
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(user_name);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        mMap.addMarker(markerOptions);

        CameraUpdate CameraUpdatelocation = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
        mMap.animateCamera(CameraUpdatelocation);
*/

      //  CameraUpdate CameraUpdatelocation = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
      //  mMap.animateCamera(CameraUpdatelocation);

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(FindFriendActivity.this));

        try {
            String url = "http://172.16.1.131:8888/where/image/";
            url = url + user_id + ".jpg";
            String snippet = "user_id: " + user_id + ", Name: " + user_name + ", Email: " + email + ", Contact: " + contact;
            downloadImage(url,  latLng,  user_id, snippet, user_name);

            /*******************************/
/*
            String snippet = "user_id: " + user_id + ", Name: " + user_name + ", Email: " + email + ", Contact: " + contact;

            URL url = null;
            Bitmap bmp = null;
            try {
                url = new URL("http://172.16.1.131:8888/where/image/20170546.jpg");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            MarkerOptions options = new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                    .position(latLng)
                    .title(user_name)
                    .snippet(snippet);
            mMarker = mMap.addMarker(options);
*/
           // mMarker.showInfoWindow();
        } catch (NullPointerException e) {
            Log.d(TAG, "moveCamera: NulpointerException: " + e.getMessage());
        }

    }

    public void downloadImage(String url, final LatLng latLng, final String user, final String snippet, final String user_name){
        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                        View viewMarker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                                .inflate(R.layout.view_custom_marker, null);
                        ImageView myImage = (ImageView) viewMarker.findViewById(R.id.profile_image);
                      //  TextView marker_user_name = (TextView) viewMarker.findViewById(R.id.marker_user_name);
                      //  marker_user_name.setText(user_name);
                        myImage.setImageBitmap(resource);
                        Bitmap bmp=createDrawableFromView(FindFriendActivity.this,viewMarker);
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latLng.latitude, latLng.longitude)).title(user_name)
                                .snippet(snippet)
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp)));

                    }
                });
    }


    public static Bitmap createDrawableFromView(Activity context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels,
                displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


    public static Bitmap createCustomMarker(Context context,  Bitmap resource, String _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);

        ImageView markerImage = (ImageView) marker.findViewById(R.id.profile_image);
        markerImage.setImageBitmap(resource);

        TextView text = (TextView) marker.findViewById(R.id.text);
        text.setText(_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

    public Bitmap CreateMarkerIcon() {
        int height = 140;
        int width = 100;
        BitmapDrawable bitmapdraw = null;


        URL url = null;
        Bitmap bmp = null;
        try {
            url = new URL("http://172.16.1.131:8888/where/image/20170546.jpg");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.marker);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return smallMarker;
    }

}
