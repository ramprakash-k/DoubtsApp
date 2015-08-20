package doubtsapp;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.net.Socket;

import in.ac.iitb.doubtsapp.R;

public class MainActivity extends Activity {

    private DoubtsFragment doubtsFragment;
    private ConnectFragment connectFragment;
    private View logoutButton;
    private View filterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        (logoutButton = findViewById(R.id.logout_button)).setVisibility(View.GONE);
        (filterButton = findViewById(R.id.filter_button)).setVisibility(View.GONE);
        connectFragment = new ConnectFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.main_fragment, connectFragment).commit();
    }

    public void connect(String ip, String port) {
        new ConnectAsync().execute(ip,port);
    }

    private class ConnectAsync extends AsyncTask<String,Void,Socket> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, "Wait", "Connecting...", false);
        }

        @Override
        protected Socket doInBackground(String... params) {
            try { Thread.sleep(1000);
            }catch (Exception ignored) {}
            Socket server;
            String ipString = params[0];
            int port = Integer.parseInt(params[1]);
            try {
                server = new Socket(ipString, port);
                return server;
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Socket socket) {
            progressDialog.dismiss();
            if (socket == null) {
                Toast.makeText(MainActivity.this, "Unable to connect", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                doubtsFragment = new DoubtsFragment();
                getFragmentManager()
                        .beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.main_fragment, doubtsFragment)
                        .commit();
                logoutButton.setVisibility(View.VISIBLE);
                filterButton.setVisibility(View.VISIBLE);
                logoutButton.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try { socket.close();
                                } catch (Exception ignored) {}
                                doubtsFragment.clearAll();
                                doubtsFragment.onDestroy();
                                getFragmentManager()
                                        .beginTransaction()
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                        .replace(R.id.main_fragment, connectFragment)
                                        .commit();
                                logoutButton.setVisibility(View.GONE);
                                filterButton.setVisibility(View.GONE);
                            }
                        }
                );
            }
        }
    }
}
