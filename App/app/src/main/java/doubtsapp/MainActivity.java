package doubtsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import in.ac.iitb.doubtsapp.R;

public class MainActivity
    extends Activity
    implements AddDoubtsPrompt.PostDoubtListener,
        LDAPLoginPrompt.PostLoginListener,
        DoubtItemViewBinder.DoubtHandler {

    private enum STATE {
        CONNECT,
        DOUBT,
    }

    private DoubtInAsync doubtInAsync;
    private DoubtsFragment doubtsFragment;
    private ConnectPrompt connectPrompt;

    private View filterButton;
    private View logoutButton;

    private STATE currentState;
    private boolean isConnected = false;
    private Socket server;
    private String name;
    private String rollNo;

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
        doubtsFragment = new DoubtsFragment();
        final FiltersManager manager = new FiltersManager(doubtsFragment);
        filterButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    manager.onFilterButtonClick(v);
                }
            });
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
                rollNo = null;
                name = null;
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
            }
        }
    }

    @Override
    public void onPostLoginSuccess(String userId, String cn) {
        rollNo = userId;
        switchState(STATE.DOUBT);
        doubtInAsync = new DoubtInAsync();
        doubtInAsync.execute();
        new DoubtOutAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rollNo, "Roll");
        Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams")
        final EditText text =
            (EditText) getLayoutInflater().inflate(R.layout.add_doubt_prompt, null);
        text.setHint(getString(R.string.name_hint));
        text.setText(cn);
        text.setLines(1);
        text.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        builder
            .setTitle(getString(R.string.name_title))
            .setView(text)
            .setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.name = text.getText().toString();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
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
            switch (info[0]) {
                // Add | lines | name | roll | doubtLine1 | time | doubtId
                case "Add":
                    Doubt doubt = new Doubt();
                    doubt.lines = Integer.parseInt(info[1]);
                    doubt.linesReceived++;
                    doubt.name = info[2];
                    doubt.rollNo = info[3];
                    if (rollNo.equals(doubt.rollNo)) {
                        doubt.isOwnDoubt = true;
                    } else {
                        doubt.canUserUpVote = true;
                    }
                    doubt.setDoubtLine(1, info[4]);
                    doubt.time = info[5];
                    doubt.DoubtId = Integer.parseInt(info[6]);
                    doubtsFragment.addDoubt(doubt);
                    break;
                // App | n | doubtLine(n) | doubtId
                case "App":
                    doubtsFragment.appendDoubt(
                        Integer.parseInt(info[3]),
                        Integer.parseInt(info[1]),
                        info[2]);
                    break;
                // Up | roll | doubtId | newCnt
                case "Up":
                    doubtsFragment.updateUpvoteCount(
                        Integer.parseInt(info[2]),
                        Integer.parseInt(info[3]),
                        rollNo.equals(info[1]),
                        true);
                    break;
                // Nup | roll | doubtId | newCnt
                case "Nup":
                    doubtsFragment.updateUpvoteCount(
                        Integer.parseInt(info[2]),
                        Integer.parseInt(info[3]),
                        rollNo.equals(info[1]),
                        false);
                    break;
                // Del | doubtId | roll
                case "Del":
                    if (info[2].equals(rollNo)) {
                        Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                    doubtsFragment.deleteDoubt(Integer.parseInt(info[1]));
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            switchState(STATE.CONNECT);
        }
    }

    @Override
    public void onPostDoubt(String doubt) {
        new DoubtOutAsync().executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR,
            doubt,
            "Add");
    }

    @Override
    public void onUpVoteClick(int doubtId) {
        new DoubtOutAsync().executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR,
            Integer.toString(doubtId),
            "Up");
    }

    @Override
    public void onNupVoteClick(int doubtId) {
        new DoubtOutAsync().executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR,
            Integer.toString(doubtId),
            "Nup");
    }

    @Override
    public boolean onEditDoubt(final int doubtId) {
        new AddDoubtsPrompt(this, new AddDoubtsPrompt.PostDoubtListener() {
            @Override
            public void onPostDoubt(String doubt) {
                new DoubtOutAsync().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    Integer.toString(doubtId),
                    "Del",
                    "-1"); // Don't show deleted toast
                new DoubtOutAsync().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    doubt,
                    "Add");
            }
        }, doubtsFragment.getDoubt(doubtId)).create().show();
        return false;
    }

    @Override
    public boolean onDeleteDoubt(final int doubtId) {
        new AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_confirm))
            .setCancelable(true)
            .setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DoubtOutAsync().executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            Integer.toString(doubtId),
                            "Del",
                            rollNo);
                    }
                })
            .setNegativeButton("Cancel", null)
            .create()
            .show();
        return true;
    }

    private class DoubtOutAsync extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... params) {
            String msg = params[0];
            try {
                DataOutputStream out = new DataOutputStream(
                    server.getOutputStream());
                if (msg.equals("I'm Out")) {
                    out.writeBytes("I'm Out\n");
                    Thread.sleep(250);
                    server.close();
                    isConnected = false;
                    if (params[1] == null) switchState(STATE.CONNECT);
                } else {
                    switch (params[1]) {
                        case "Roll":
                            out.writeBytes("I Am|" + msg + "\n");
                            break;
                        case "Add":
                            String doubt[] = params[0].split("[\n]");
                            int lines = doubt.length;
                            out.writeBytes("Add|" + Integer.toString(lines) + "|"
                                + name + "|" + rollNo + "|" + doubt[0] + "\n");
                            for (int i = 2; i <= lines; i++) {
                                out.writeBytes("App|" + Integer.toString(i) + "|"
                                    + doubt[i - 1] + "\n");
                            }
                            break;
                        case "Up":
                            out.writeBytes("Up|" + rollNo + "|" + msg + "\n");
                            break;
                        case "Nup":
                            out.writeBytes("Nup|" + rollNo + "|" + msg + "\n");
                            break;
                        case "Del":
                            out.writeBytes("Del|" + msg + "|" + params[2] + "\n");
                            break;
                    }
                }
            } catch (Exception e) {
                isConnected = false;
            }
            return null;
        }
    }
}
