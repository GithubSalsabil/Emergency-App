package com.example.emergency_s;

import android.app.AlertDialog;
import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressDialog extends AppCompatDialogFragment {

    private final String longitude;
    private final String latitude;

    // Constructor to manually set latitude and longitude
    public AddressDialog() {
        // Example coordinates set manually
        this.latitude = "22";
        this.longitude = "35";
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Address :- ")
                .setMessage("\n" + convertLocationToAddress() + "\n");
        return builder.create();
    }

    private String convertLocationToAddress() {
        String addressText;

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    Double.parseDouble(latitude),
                    Double.parseDouble(longitude),
                    1
            );
        } catch (Exception e) {
            addressText = "Error: Coordinates cannot be converted into Address.";
            return addressText;
        }

        if (addresses == null || addresses.isEmpty()) {
            addressText = "Error: Coordinates cannot be converted into Address.";
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            addressText =
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments);
        }

        return addressText;
    }
}
