package doubtsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import in.ac.iitb.doubtsapp.R;

/**
 * AlertDialog to add a doubt
 */
public class AddDoubtsPrompt extends DialogFragment {

    interface PostDoubtListener {
        void onPostDoubt(String doubt);
    }

    private PostDoubtListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        listener = (PostDoubtListener) getActivity();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final EditText text = (EditText) inflater.inflate(R.layout.add_doubt_prompt, null);
        builder
            .setView(text)
            .setCancelable(true)
            .setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (text.getText().toString().equals("I'm Out") ||
                            text.getText().toString().equals("") ||
                            text.getText().toString().equals("\n")) {
                            Toast.makeText(
                                getActivity(),
                                "Invalid Doubt",
                                Toast.LENGTH_SHORT).show();
                        } else {
                            listener.onPostDoubt(text.getText().toString());
                        }
                    }
                })
            .setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }
}
