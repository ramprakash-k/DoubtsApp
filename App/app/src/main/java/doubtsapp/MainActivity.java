package doubtsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import in.ac.iitb.doubtsapp.R;

public class MainActivity
    extends Activity
    implements AddDoubtsPrompt.PostDoubtListener, LDAPLoginPrompt.PostLoginListener {

    private enum STATE {
        CONNECT,
        DOUBT,
    }
    private STATE currentState;
    private DoubtsFragment doubtsFragment;
    private ConnectPrompt connectPrompt;
    private View logoutButton;
    private View filterButton;
    private boolean isConnected = false;
    private Socket server;
    private DoubtInAsync doubtInAsync;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        (logoutButton = findViewById(R.id.logout_button)).setVisibility(View.GONE);
        (filterButton = findViewById(R.id.filter_button)).setVisibility(View.GONE);
        logoutButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isConnected) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setMessage(R.string.logout_confirm)
                            .setCancelable(true)
                            .setPositiveButton("Confirm",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new DoubtOutAsync().executeOnExecutor(
                                            AsyncTask.THREAD_POOL_EXECUTOR,
                                            "I'm Out");
                                        switchState(STATE.CONNECT);
                                    }
                                })
                            .setNegativeButton("Cancel", null)
                            .show();
                    }
                }
            });
        filterButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Not yet built!", Toast.LENGTH_SHORT).show();
                }
            });
        doubtsFragment = new DoubtsFragment();
        getFragmentManager()
            .beginTransaction()
            .add(R.id.main_fragment, doubtsFragment)
            .commit();
        connectPrompt = new ConnectPrompt();
        connectPrompt.setCancelable(false);
        connectPrompt.show(getFragmentManager(), null);
        currentState = STATE.CONNECT;
    }

    @Override
    protected void onDestroy() {
        if (isConnected) {
            new DoubtOutAsync().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                "I'm Out", "exit");
            doubtInAsync.cancel(true);
        }
        super.onDestroy();
    }

    public void connect(String ip, String port) {
        new ConnectAsync().execute(ip, port);
    }

    private void switchState(STATE state) {
        if (state == currentState) return;
        switch (state) {
            case DOUBT :
                connectPrompt.dismiss();
                logoutButton.setVisibility(View.VISIBLE);
                filterButton.setVisibility(View.VISIBLE);
                isConnected = true;
                break;
            case CONNECT:
                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
                connectPrompt.show(getFragmentManager(), null);
                logoutButton.setVisibility(View.GONE);
                filterButton.setVisibility(View.GONE);
                isConnected = false;
                userId = null;
                doubtsFragment.clearAll();
                break;
        }
        currentState = state;
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
                LDAPLoginPrompt loginPrompt = new LDAPLoginPrompt();
                loginPrompt.setCancelable(false);
                loginPrompt.show(getFragmentManager(), null);
                switchState(STATE.DOUBT);
                doubtInAsync = new DoubtInAsync();
                doubtInAsync.execute();
            }
        }
    }

    @Override
    public void onPostLoginSuccess(String userId) {
        this.userId = userId;
        Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginCancelled() {
        new DoubtOutAsync().executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR,
            "I'm Out");
        switchState(STATE.CONNECT);
    }

    @Override
    public void onPostLoginFailed() {
        Toast.makeText(this, "Incorrect login", Toast.LENGTH_SHORT).show();

    }

    private class DoubtInAsync extends AsyncTask<Void,String,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            } catch (IOException e) {
                return null;
            }
            while (isConnected) {
                try {
                    String msg = in.readLine();
                    if (msg == null) return null;
                    publishProgress(msg);
                } catch (IOException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... doubts) {
            String s = doubts[0];
            String info[] = s.split("[|]");
            Doubt doubt = new Doubt();
            if (info.length == 2) {
                doubt.userId = info[0];
                doubt.doubt = info[1];
            } else {
                doubt.doubt = info[0];
            }
            doubtsFragment.addDoubt(doubt);
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(MainActivity.this, "Server closed", Toast.LENGTH_SHORT).show();
            switchState(STATE.CONNECT);
        }
    }

    @Override
    public void onPostDoubt(String doubt) {
        new DoubtOutAsync().executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR,
            doubt);
    }

    private class DoubtOutAsync extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... params) {
            String doubt = (userId != null ? userId + "|" : "") + params[0] + "\n";
            try {
                DataOutputStream out = new DataOutputStream(
                    server.getOutputStream());
                out.writeBytes(doubt);
                if (doubt.equals("I'm Out\n")) {
                    Thread.sleep(250);
                    server.close();
                    isConnected = false;
                    if (params[1] == null) {
                        switchState(STATE.CONNECT);
                    }
                }
            } catch (Exception e) {
                isConnected = false;
            }
            return null;
        }
    }
}
