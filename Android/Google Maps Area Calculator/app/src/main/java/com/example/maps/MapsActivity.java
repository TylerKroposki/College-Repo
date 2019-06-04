package com.example.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyler Kroposki
 * @MapsActivity - Functions:
 * - Allow user to place a marker on the screen, and be able to remove the marker
 * - Calculate the area between markers oriented in a polygonal shape
 * - Search for locations, move to the location, and place a marker accordingly
 * - Change units for area, with the output formatted
 *
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Map Object
    private GoogleMap appMap;

    //Search Location TextView and console text
    private EditText searchLoc;
    private TextView consoleText;

    //Logging Tag
    private static final String TAG = "MapActivity";

    //Stores the points as a list of LatLng objects.
    private static List<LatLng> points = new ArrayList();

    //Variables
    private double multiplier = 1;
    private String units = "m\u00B2";
    private Boolean locationPermissionsGranted = false;
    private FusedLocationProviderClient locProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Search Bar
        searchLoc = findViewById(R.id.input_search);

        //Location permissions
        getLocationPermission();

    }

    /**
     * Creates a listener for the map and handles what happens if anything on the map is clicked.
     * @param map - takes a GoogleMap object as a parameter.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        appMap = map;
        //Check permissions and retrieve the device's location
        if (locationPermissionsGranted) {
            getCurrentLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //Enable location Services
            appMap.setMyLocationEnabled(true);
            appMap.getUiSettings().setMyLocationButtonEnabled(false);

            //Initialize the map
            init();
        }


        //Map Click Listener to add marker.
        appMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                //Since the points will change the polygon, the lines are removed
                removeLines();
                //The point must also be added to the list
                points.add(point);
                appMap.addMarker(new MarkerOptions().position(point).title(point.toString()));
            }
        });

        //Removes a marker from the application using an onClickListener for each marker.
        appMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Remove the marker, and then remove the object from the list of LatLngs
                marker.remove();
                points.remove(marker.getPosition());
                removeLines();
                return true;
            }
        });

    }

    /**
     * Initializes the search bar listener.
     */
    private void init(){

        //Establishes an editor listener to determine if the input is submitted or not.
        searchLoc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //If submitted, find the location based on what was inputted to the location search bar
                    findLocationFromInput();
                }
                //else false
                return false;
            }
        });

    }

    /**
     * Creates the map
     */
    private void createMap(){
        //Create new fragment and start the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    /**
     * Retrieves the current location and sets the camera to that position.
     * @throws SecurityException
     */
    private void getCurrentLocation() throws SecurityException {

        locProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Check location permissions
        if(locationPermissionsGranted) {

            //Retrieve the current location
            Task loc = locProviderClient.getLastLocation();

            //Add listener
            loc.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()) {

                        //Cast current to a task
                        Location current = (Location) task.getResult();

                        //Change view to current location
                        moveCamera(new LatLng(current.getLatitude(), current.getLongitude()), Constants.ZOOM, "Current Loc");

                    } else {
                        //Show user that an error occurred
                        Toast.makeText(MapsActivity.this, "Cannot Retrieve Current Location.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * Retrieves the needed permissions the app will need in order to use Google Map functionalities.
     */
    private void getLocationPermission(){

        //Stores permissions
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        //Checks if the permissions are granted or not.
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Constants.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Constants.COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                //If so, draw the map.
                locationPermissionsGranted = true;
                createMap();
            } else {
                //Request permissions
                ActivityCompat.requestPermissions(this, permissions, Constants.LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, Constants.LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Retrieves the result from permission requests.
     * @param requestCode - Input request code
     * @param permissions - The requested permissions
     * @param grantResults - The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Follows a guilty-until-proven-innocent methodology
        locationPermissionsGranted = false;

        //Check if the request code matches the required request code
        if(requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        locationPermissionsGranted = false;
                        return;
                    }
                }
                //Since the permissions were granted, the map can be created.
                locationPermissionsGranted = true;
                createMap();
            }
        }
    }

    /**
     * Used as an onClick for the Calculate Area button.
     * @param v - the app view
     */
    public void determineArea(View v) {

        //Since a Polygon is defined as a figure with at least three straight sides, there needs to be 3 points
        if(points.size() < 3) {
            Toast.makeText(MapsActivity.this, "You need to place at least 3 markers on the map to create a Polygon.", Toast.LENGTH_LONG).show();
        } else {

            //Each LatLng object within the points list is iterated through and added to polygonOptions
            PolygonOptions polygonOptions = new PolygonOptions().addAll(points);

            //Determine and format the area of the drawn polygon
            Double area = SphericalUtil.computeArea(points) * multiplier;
            String num = new BigDecimal(area).toPlainString();
            double amount = Double.parseDouble(num);

            //Formats the double to have two trailing decimals, and be comma separated
            DecimalFormat formatter = new DecimalFormat("#,###.00");
            String numStr = formatter.format(amount);

            //Display the calculated area, along with changeable units
            consoleText = findViewById(R.id.consoleText);
            consoleText.setText("This polygon has an area of:\n" + numStr + units);

            //Creates a colored area between the points to represent the polygon
            appMap.addPolygon(polygonOptions).setFillColor(Color.CYAN);
        }
    }

    /**
     * Clears the screen of the lines, but keeps the markers that were previously added.
     */
    private void removeLines() {

        //Clear map of everything
        appMap.clear();

        //Re-add the markers to the screen
        for(LatLng p: points) {
            appMap.addMarker(new MarkerOptions().position(p));
        }

        //Set console to default text
        consoleText = findViewById(R.id.consoleText);
        consoleText.setText("No information calculated yet.");
    }

    /**
     * Used as an onClick method for resetBtn. Removes everything from the points array, and the map itself.
     * @param v - view
     */
    public void clearMap(View v) {
        //Remove the calculation from the console
        consoleText = findViewById(R.id.consoleText);
        consoleText.setText("No information calculated yet.");

        //Remove everything from the map, and clear the points list
        appMap.clear();
        points.clear();
    }

    /**
     * Handles radio button clicks to change units.
     */
    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        //Check which radio button was clicked
        switch(view.getId()) {
            //Since meters is the default unit...
            case R.id.metersBtn:
                if(checked)
                    multiplier = 1;
                    units = "m\u00B2";
                break;
                //Square feet
            case R.id.feetBtn:
                if(checked)
                    multiplier = 10.7639;
                    units = " feet\u00B2";
                break;
                //Square Miles
            case R.id.milesBtn:
                if(checked)
                    multiplier = 0.00000038610;
                    units = " miles\u00B2";
                break;
                //Acres
            case R.id.acreBtn:
                if(checked)
                    multiplier = 0.000247105;
                    units = " acres";
                break;
        }
    }

    /**
     * Transforms input text into a latitude longitude pair.
     * From this, the camera is moved to the corresponding latitude and longitude pair.
     */
    private void findLocationFromInput() {
        //Retrieve input text
        String input = searchLoc.getText().toString();

        //Used to find coordinates based on input
        Geocoder locationFinder = new Geocoder(MapsActivity.this);

        List<Address> addresses = new ArrayList<>();

        //Add addresses to geocoder
        try {
            addresses = locationFinder.getFromLocationName(input, 1);
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage() );
        }

        //Move camera to address and zoom
        if(addresses.size() > 0){
            Address address = addresses.get(0);
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), Constants.ZOOM, address.getAddressLine(0));
        }
    }


    /**
     * Moves the camera to a given location with a certain zoom.
     * @param location - A LatLng object to determine where on the map to move the camera
     * @param zoom - How much of a zoom to apply
     * @param locName - Used to determine if the current location is the device location
     */
    private void moveCamera(LatLng location, float zoom, String locName){

        //Move camera with appropriate zoom.
        appMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));

        //Quick check to determine if the current location is the device location
        if(!locName.equals("Current Loc")){
            appMap.addMarker(new MarkerOptions().position(location).title(location.toString()));
            points.add(location);
        }
    }



}//end file
