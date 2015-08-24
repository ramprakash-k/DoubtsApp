package doubtsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import in.ac.iitb.doubtsapp.R;

public class MainActivity extends Activity {

    private DoubtsFragment doubtsFragment;
    private ConnectFragment connectFragment;
    private View logoutButton;
    private View filterButton;
    private boolean isConnected = false;
    private Socket server;

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

    @Override
    protected void onDestroy() {
        if (isConnected) {
            new DoubtOutAsync().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                "I'm Out");
            try {
                Thread.sleep(500);
                server.close();
            } catch(Exception ignored) {}
        }
        super.onDestroy();
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
                server = socket;
                isConnected = true;
                doubtsFragment = new DoubtsFragment();
                doubtsFragment.setAddDoubtListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder =
                                new AlertDialog.Builder(MainActivity.this);
                            final EditText text = new EditText(MainActivity.this);
                            int p = getResources().getDimensionPixelOffset(R.dimen.default_margin);
                            text.setPadding(p, p, p, p);
                            text.setHint(R.string.doubt_hint);
                            text.setLines(2);
                            int size = getResources().getDimensionPixelSize(R.dimen.text_size);
                            text.setTextSize(size);
                            builder
                                .setView(text)
                                .setCancelable(true)
                                .setPositiveButton("Confirm",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            if (text.getText().toString().equals("I'm Out") ||
                                                text.getText().toString().equals("")) {
                                                Toast.makeText(
                                                    MainActivity.this,
                                                    "Invalid Doubt",
                                                    Toast.LENGTH_SHORT).show();
                                            } else {
                                                new DoubtOutAsync().executeOnExecutor(
                                                    THREAD_POOL_EXECUTOR,
                                                    text.getText().toString());
                                            }
                                        }
                                    })
                                .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });
                            builder.create().show();
                        }
                    });
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
                                new DoubtOutAsync().executeOnExecutor(
                                    THREAD_POOL_EXECUTOR,
                                    "I'm Out");
                                doubtsFragment.clearAll();
                                doubtsFragment.onDestroy();
                                getFragmentManager()
                                        .beginTransaction()
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                        .replace(R.id.main_fragment, connectFragment)
                                        .commit();
                                logoutButton.setVisibility(View.GONE);
                                filterButton.setVisibility(View.GONE);
                                try {
                                    Thread.sleep(500);
                                    socket.close();
                                } catch (Exception ignored) {
                                }
                                isConnected = false;
                            }
                        });
                new DoubtInAsync().execute();
            }
        }
    }

    private class DoubtInAsync extends AsyncTask<Void,String,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            } catch (IOException e) {
                isConnected = false;
                return null;
            }
            while (isConnected) {
                try {
                    String msg = in.readLine();
                    publishProgress(msg);
                } catch (IOException e) {
                    isConnected = false;
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... doubts) {
            doubtsFragment.addDoubt(doubts[0]);
        }
    }

    private class DoubtOutAsync extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... params) {
            String doubt = params[0] + "\n";
            try {
                DataOutputStream out = new DataOutputStream(
                    server.getOutputStream());
                out.writeBytes(doubt);
            } catch (IOException e) {
                isConnected = false;
            }
            return null;
        }
    }
}
