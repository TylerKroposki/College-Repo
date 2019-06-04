package com.example.maps;

import android.Manifest;

public interface Constants {
    //Camera Zoom Level
    float ZOOM = 15f;

    //Device Permissions
    int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    int ERROR_DIALOG_REQUEST = 9001;

    String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

}//end file
