package doubtsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import in.ac.iitb.doubtsapp.R;

/**
 * AlertDialog to add a doubt
 */
public class AddDoubtsPrompt extends AlertDialog.Builder {

    interface PostDoubtListener {
        void onPostDoubt(String doubt);
    }

    private PostDoubtListener listener;
    private Activity activity;
    private String initialText;

    public AddDoubtsPrompt(Activity activity) {
        super(activity);
        this.activity = activity;
        this.listener = (PostDoubtListener) activity;
        this.initialText = null;
    }

    public AddDoubtsPrompt(Activity activity, PostDoubtListener listener, String initialText) {
        super(activity);
        this.listener = listener;
        this.activity = activity;
        this.initialText = initialText;
    }

    @Override()
    public @NonNull AlertDialog create() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        final EditText text = (EditText) inflater.inflate(R.layout.add_doubt_prompt, null);
        text.setHint(activity.getString(R.string.doubt_hint));
        text.setText(initialText);
        builder
            .setView(text)
            .setCancelable(true)
            .setPositiveButton("Confirm", null)
            .setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(
            new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface d) {
                    Button postButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    postButton.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String doubt = text.getText().toString();
                                if (doubt.equals("") ||
                                    doubt.equals("I'm Out") ||
                                    (doubt.split("[ \n]").length == 0)) {
                                    Toast.makeText(
                                        activity,
                                        "Invalid Doubt",
                                        Toast.LENGTH_SHORT).show();
                                } else if (doubt.contains("|")) {
                                    Toast.makeText(
                                        activity,
                                        "Avoid '|' in the Doubt",
                                        Toast.LENGTH_SHORT).show();
                                } else {
                                    listener.onPostDoubt(doubt);
                                    dialog.dismiss();
                                }
                            }
                        });
                }
            });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }
}

