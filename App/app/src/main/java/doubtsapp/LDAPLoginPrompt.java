package doubtsapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;

import in.ac.iitb.doubtsapp.R;

/**
 * Prompt to login with LDAP login
 */
public class LDAPLoginPrompt extends DialogFragment {
    interface PostLoginListener {
        void onPostLoginSuccess(String userId, String cn);
        void onLoginCancelled();
        void onPostLoginFailed();
    }

    private PostLoginListener listener;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        listener = (PostLoginListener) getActivity();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.login_prompt, null);
        final EditText userText = (EditText) view.findViewById(R.id.username);
        final EditText passText = (EditText) view.findViewById(R.id.password);
        final CheckBox saveCheckBox = (CheckBox) view.findViewById(R.id.save_check);
        view.findViewById(R.id.save_password).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveCheckBox.toggle();
                }
            });
        final SharedPreferences preferences =
            getActivity().getSharedPreferences("LoginPref", Context.MODE_PRIVATE);
        userText.setText(preferences.getString("login_user",null));
        passText.setText(preferences.getString("login_pass",null));
        builder
            .setTitle(R.string.ldap_title)
            .setView(view)
            .setPositiveButton("Login",null)
            .setNeutralButton("Clear", null)
            .setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onLoginCancelled();
                    }
                });
        final AlertDialog dialog =  builder.create();
        dialog.setOnShowListener(
            new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface d) {
                    Button clearButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                    clearButton.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                userText.setText(null);
                                passText.setText(null);
                                new AlertDialog.Builder(getActivity())
                                    .setMessage(getString(R.string.forget_pass))
                                    .setPositiveButton("Confirm",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SharedPreferences.Editor editor = preferences.edit();
                                                editor.remove("login_user");
                                                editor.remove("login_pass");
                                                editor.apply();
                                            }
                                        })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                            }
                        });
                    Button loginButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    loginButton.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String user = userText.getText().toString();
                                String pass = passText.getText().toString();
                                if (user.isEmpty() || pass.isEmpty())
                                    listener.onPostLoginFailed();
                                try {
                                    LDAPConnection connection =
                                        new LDAPConnection("10.200.1.31", 389);
                                    Filter filter = Filter.createEqualityFilter("uid",user);
                                    SearchRequest request =
                                        new SearchRequest(
                                            "dc=iitb,dc=ac,dc=in",
                                            SearchScope.SUB,
                                            filter,
                                            "employeeNumber",
                                            "cn");
                                    SearchResult result = connection.search(request);
                                    String dn = result.getSearchEntries().get(0).getDN();
                                    connection.bind(dn, pass);
                                    if (saveCheckBox.isChecked()) {
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putString("login_user",user);
                                        editor.putString("login_pass",pass);
                                        editor.apply();
                                    }
                                    listener.onPostLoginSuccess(
                                        result.getSearchEntries().get(0)
                                            .getAttributeValue("employeeNumber"),
                                        result.getSearchEntries().get(0).getAttributeValue("cn"));
                                    dialog.dismiss();
                                } catch (Exception e) {
                                    listener.onPostLoginFailed();
                                }
                            }
                        });
                }
            });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }
}
