package doubtsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import in.ac.iitb.doubtsapp.R;

/**
 * Created by Ramprakash on 19-08-2015.
 * Prompt that gets IP and PORT of server to connect.
 */
public class ConnectPrompt extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.connect_prompt, null);
        final EditText ipTextView = ((EditText) view.findViewById(R.id.ip_text));
        final EditText portView = ((EditText) view.findViewById(R.id.port_text));
        ipTextView.setText("10.2.64.66");
        portView.setText("8000");
        view.findViewById(R.id.connect_button).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ipText = ipTextView.getText().toString();
                    String port = portView.getText().toString();
                    ((MainActivity) getActivity()).connect(ipText, port);
                }
            });
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }
}
