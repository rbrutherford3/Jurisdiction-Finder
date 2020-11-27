package com.rsquared.jurisdictionfinder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    // Tracking elements
    private static final int LOCATION_PERMISSIONS = 100;
    private static boolean hasGPS;
    private static double latitude;
    private static double longitude;

    // Screen input elements
    Button useGpsButton;
    Button useAddressButton;
    EditText enterAddressEditText;

    // Screen output elements
    TextView latitudeView;
    TextView longitudeView;
    TextView jurisdictionView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get screen input elements
        useGpsButton = findViewById(R.id.useGpsButton);
        useAddressButton = findViewById(R.id.useAddressButton);
        enterAddressEditText = findViewById(R.id.enterAddressEditText);

        // Get screen output elements
        latitudeView = findViewById(R.id.latitudeValue);
        longitudeView = findViewById(R.id.longitudeValue);
        jurisdictionView = findViewById(R.id.jurisdictionText);

        // When user clicks the "Use Address" button...
        useAddressButton.setOnClickListener(v -> {
            enableInputs(false);
            clearGpsOutputs();
            try {
                getAddressesFromAddress();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            enableInputs(true);
        });

        // When user clicks the "Use GPS" button...
        useGpsButton.setOnClickListener(v -> {
            enableInputs(false);
            clearGpsOutputs();
            clearAddressInput();
            checkLocationPermissions();
        });
    }

    // Enable or disable inputs, based on "enabled" parameter - used for processing time
    private void enableInputs(boolean enabled) {
        useGpsButton.setEnabled(enabled);
        useAddressButton.setEnabled(enabled);
        enterAddressEditText.setEnabled(enabled);
    }

    // Clear the address input (when GPS button is clicked)
    private void clearAddressInput() {
        enterAddressEditText.setText("");
    }

    // Clear GPS outputs
    private void clearGpsOutputs() {
        hasGPS = false;
        latitudeView.setText("");
        longitudeView.setText("");
    }

    // Checks permission to use location services (GPS included) and seeks it if not present
    // getGPS() is called if the permissions are there
    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            getGPS();   // Proceed to grab GPS coordinates if permissions were already present
        else {
            Toast.makeText(MainActivity.this, "Please permit location services in order to continue", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSIONS);
        }
    }

    // Reacts to user prompt to allow access to Location permissions and provides feedback
    // getGPS() is called if the correct permissions were granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // Make sure the permission change that invoked this method is for location
        if (requestCode == LOCATION_PERMISSIONS) {
            // Make sure both fine and coarse location permissions were granted
            boolean allApproved = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allApproved = false;
                    break;
                }
            }
            if (allApproved) {
                getGPS();   // Proceed to grab GPS coordinates if permissions were granted
            } else
                Toast.makeText(MainActivity.this, "Not all location services were permitted", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(MainActivity.this, "Error: Unknown request code received", Toast.LENGTH_LONG).show();
        enableInputs(true);
    }

    // Seeks current Location information (once, not repeatedly) and saves and displays coordinates
    @SuppressLint({"ShowToast", "SetTextI18n"})
    private void getGPS() {

        Toast.makeText(MainActivity.this, "Getting GPS coordinates", Toast.LENGTH_SHORT).show();

        // Set up GPS request
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        CancellationTokenSource cts = new CancellationTokenSource();

        // Invoke the location request (Suppression is okay because permissions are granted at this point)
        @SuppressLint("MissingPermission") final Task<Location> locationTask =
                fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cts.getToken());

        // Wait for the location services to finish
        locationTask.addOnCompleteListener(task -> {
            Context context = getApplicationContext();

            // Save and display GPS coordinates once found
            if (task.isSuccessful()) {
                Location result = task.getResult();
                hasGPS = true;
                latitude = result.getLatitude();
                longitude = result.getLongitude();
                try {
                    getAddressesFromGPS(latitude, longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            // Provide feedback for cases when the GPS coordinate search was cancelled
            } else if (task.isCanceled()) {
                Toast.makeText(context, "Canceled: " + cts.getToken().toString(), Toast.LENGTH_SHORT);
            // Provide feedback for cases when the GPS coordinate search was cancelled
            } else {
                // Task failed with an exception
                Exception exception = task.getException();
                assert exception != null;
                Toast.makeText(context, "Failure: " + exception.getLocalizedMessage(), Toast.LENGTH_SHORT);
            }
            enableInputs(true);
        });
    }

    // Obtains all location information about a user-provided address
    private void getAddressesFromAddress() throws IOException {
        // Set up relevant variables and devices
        Context context = getApplicationContext();
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        String addressQuery;
        addressQuery = enterAddressEditText.getText().toString();
        if (addressQuery.isEmpty()) {
            Toast.makeText(context, "Please enter an address", Toast.LENGTH_LONG).show();
        }
        else {
            // Search address information using the given address
            addresses = geocoder.getFromLocationName(addressQuery, 1);

            // If there was a result, process it
            if (addresses.size() >= 1)
                showAddress(addresses);
            else {
                Toast.makeText(context, "No matching address found", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Retrieves the address nearest to the given GPS coordinates
    private void getAddressesFromGPS(Double latitude, Double longitude) throws IOException {
        Context context = getApplicationContext();
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        // If there was a result, process it
        if (addresses.size() > 0)
            showAddress(addresses);
        else
            Toast.makeText(context, "No matching address found", Toast.LENGTH_LONG).show();
    }

    // Translates GPS coordinates into location information (such as address)
    private void showAddress(List<Address> addresses) {

        String notFound = "Not Found";

        // Grab address if it exists and display jurisdiction (either in SubAdminArea or AdminArea)
        if (addresses.size() > 0) {
            Address address = addresses.get(0);
            if (address.getSubAdminArea() == null)
                jurisdictionView.setText(address.getAdminArea());
            else
                jurisdictionView.setText(address.getSubAdminArea());
            StringBuilder addressLine = new StringBuilder();
            for (int i=0; i<=address.getMaxAddressLineIndex(); i++)
                addressLine.append(address.getAddressLine(i));

            if (addressLine.length() == 0)
                addressLine = new StringBuilder("Not Found");

            enterAddressEditText.setText(addressLine.toString());

            if (address.hasLatitude() && address.hasLongitude()) {
                latitudeView.setText(String.valueOf(address.getLatitude()));
                longitudeView.setText(String.valueOf(address.getLongitude()));
            }
            else if (hasGPS) {
                latitudeView.setText(String.valueOf(latitude));
                longitudeView.setText(String.valueOf(longitude));
            }
            else {
                latitudeView.setText(notFound);
                longitudeView.setText(notFound);
            }
        }
        else {
            jurisdictionView.setText(notFound);
        }
    }
}