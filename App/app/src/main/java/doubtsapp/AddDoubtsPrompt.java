package doubtsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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

    interface EditCancelListener {
        void onEditCancel();
    }

    private PostDoubtListener listener;
    private EditCancelListener editCancelListener;
    private Activity activity;
    private String initialText;
    private boolean edit;
    private int extendCount;

    public AddDoubtsPrompt(Activity activity) {
        super(activity);
        this.activity = activity;
        this.listener = (PostDoubtListener) activity;
        this.editCancelListener = null;
        this.initialText = null;
        edit = false;
    }

    public AddDoubtsPrompt(
        Activity activity,
        PostDoubtListener listener,
        EditCancelListener editCancelListener,
        String initialText) {
        super(activity);
        this.editCancelListener = editCancelListener;
        this.listener = listener;
        this.activity = activity;
        this.initialText = initialText;
        edit = true;
        extendCount = 0;
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
            .setNegativeButton("Cancel", null);
        if (edit) {
            builder.setNeutralButton("Extend", null);
        }
        final AlertDialog dialog = builder.create();
        final CountDownTimer timer = new CountDownTimer(45000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
                    .setText("Extend (" + Long.toString(millisUntilFinished / 1000) + ")");
            }

            @Override
            public void onFinish() {
                dialog.dismiss();
                editCancelListener.onEditCancel();
            }
        };
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                if (edit) {
                    dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (extendCount < 2) {
                                    timer.cancel();
                                    extendCount++;
                                    timer.start();
                                }
                                else {
                                    Toast.makeText(
                                        v.getContext(),
                                        "Only 2 Extends Allowed",
                                        Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        timer.start();
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(new View.OnClickListener() {
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
                                timer.cancel();
                            }
                        }
                    });
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            if (edit) {
                                editCancelListener.onEditCancel();
                                timer.cancel();
                            }
                        }
                    });
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }
}
