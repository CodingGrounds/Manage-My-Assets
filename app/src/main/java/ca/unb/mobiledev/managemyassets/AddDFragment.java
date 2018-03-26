package ca.unb.mobiledev.managemyassets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import static ca.unb.mobiledev.managemyassets.Asset.LAT;
import static ca.unb.mobiledev.managemyassets.Asset.LNG;

/**
 * Created by laver on 2018-03-25.
 */

public class AddDFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        return new AlertDialog.Builder(getActivity()).setTitle("Add Asset").setMessage("Do you want to add an asset?").setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("Dialog", "ADD");
                Intent detailsIntent = new Intent(getActivity(), AddAssetActivity.class);
                detailsIntent.putExtra(LAT, (double) getArguments().get(LAT));
                detailsIntent.putExtra(LNG, (double) getArguments().get(LNG));
                startActivity(detailsIntent);
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("Dialog", "Cancel");
            }
        }).create();
    }
}
