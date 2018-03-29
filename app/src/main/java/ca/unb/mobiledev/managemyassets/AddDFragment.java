package ca.unb.mobiledev.managemyassets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import static ca.unb.mobiledev.managemyassets.AddAssetActivity.INTENT_NEW_ASSET;
import static ca.unb.mobiledev.managemyassets.Asset.OBJECT_NAME;

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
                Intent addAsset = new Intent(getActivity(), AddAssetActivity.class);
                addAsset.putExtra(OBJECT_NAME, (Asset) getArguments().get(OBJECT_NAME));
                addAsset.putExtra(INTENT_NEW_ASSET,  true);
                startActivity(addAsset);
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
