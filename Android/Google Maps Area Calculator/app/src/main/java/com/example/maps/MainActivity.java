package com.example.maps;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * @author Tyler Kroposki
 * MainActivity - used to check for Google Play Services prior to
 * starting the MapsActivity.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Checks if Google Play Services are available, and then starts the MapsActivity.
        if(servicesReady()) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Method retrieves whether the Google Play Services are available or not, and as well
     * offers a user-resolvable fix.
     * @return whether the Google Play Services are available or not
     */
    public boolean servicesReady() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        //Services are available
        if(available == ConnectionResult.SUCCESS) {
            return true;
            //Services are unavailable for this device
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, Constants.ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "No Map Request Can Be Made", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}//end file
