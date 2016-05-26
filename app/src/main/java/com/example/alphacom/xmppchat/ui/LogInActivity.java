package com.example.alphacom.xmppchat.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alphacom.xmppchat.R;
import com.example.alphacom.xmppchat.utils.DialogFactory;
import com.example.alphacom.xmppchat.utils.Utils;
import com.example.alphacom.xmppchat.xmpp.Const;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class LogInActivity extends AppCompatActivity {

    private static final String RESULT_FAIL = "FAIL";
    private static final String EXTRA_FRIENDID = "FriendID";
    private static final String EXTRA_USERID = "UserID";
    private static final String EXTRA_FROM_JID = "FromUserJID";

    private Button mBtnLogIn;
    private Button mBtnRegister;
    private EditText mEditUser;
    private EditText mEditPassword;
    private XMPPTCPConnection xmppConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        initView();
    }

    public void initView() {
        mBtnLogIn = (Button) findViewById(R.id.btn_login);
        mBtnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        mEditUser = (EditText) findViewById(R.id.edit_jabberid);
        mEditPassword = (EditText) findViewById(R.id.edit_password);
    }

    public void logIn() {
        String jid = mEditUser.getText().toString();
        String userPassword = mEditPassword.getText().toString();
        String serviceName = Utils.getJidToServerName(jid);
        String userName = Utils.getJidToUsername(jid);

        if (userName.length() == 0 || userPassword.length() == 0) {
            DialogFactory.ToastDialog(this, "Log In", "Name and Password cannot be empty.");
            return;
        }
//        XMPPTCPConnectionConfiguration.Builder conf = XMPPTCPConnectionConfiguration.builder();
//
//        conf.setServiceName(serviceName);
//        conf.setHost(serviceName);
//        conf.setPort(5222);
//        conf.setUsernameAndPassword(userName, userPassword);
//        conf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
////		conf.setLegacySessionDisabled(true);
//        conf.setCompressionEnabled(true);
//        try {
//            TLSUtils.acceptAllCertificates(conf);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        }
        xmppConnection = Const.connection;
        String[] logInInfo= {userName, userPassword};
        new DoLogIn().execute(logInInfo);

    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 2) {
                DialogFactory.ToastDialog(LogInActivity.this, "Log In",
                        "Log in failed. \nPlease try it again.");
            }
        };
    };

    public void register() {
        Intent intent = new Intent(this, SignUpActivity.class);
        Const.connection = xmppConnection;
        startActivity(intent);
    }

    private class DoLogIn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                if (!xmppConnection.isConnected())
                    xmppConnection.connect();
                if (!xmppConnection.isAuthenticated())
                    xmppConnection.login(params[0], params[1]);
                Presence presence = new Presence(Presence.Type.available);
                xmppConnection.sendStanza(presence);
                //DialogFactory.ToastDialog(this, "登录提示", "亲，恭喜你，登录成功了！");
            } catch (SmackException | IOException | XMPPException e) {
                e.printStackTrace();
                xmppConnection.disconnect();
                handler.sendEmptyMessage(2);
                return RESULT_FAIL;
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals(RESULT_FAIL))
                return;
            Toast.makeText(LogInActivity.this, "Log in succeeded",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LogInActivity.this, FriendListActivity.class);
            intent.putExtra(EXTRA_USERID, result);
            Const.connection = xmppConnection;
            startActivity(intent);
        }

        @Override
        protected void onPreExecute() {

        }
    }
}
