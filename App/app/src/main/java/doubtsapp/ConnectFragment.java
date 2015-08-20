package doubtsapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import in.ac.iitb.doubtsapp.R;

/**
 * Created by Ramprakash on 19-08-2015.
 * Fragment that gets IP and PORT of server to connect.
 */
public class ConnectFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        final View view = inflater.inflate(R.layout.connect_fragment, container, false);
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
                }
        );
        return view;
    }


}
