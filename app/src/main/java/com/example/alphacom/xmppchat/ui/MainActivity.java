package com.example.alphacom.xmppchat.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.example.alphacom.xmppchat.R;
import com.example.alphacom.xmppchat.xmpp.Const;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private XMPPTCPConnection mXmppConnection;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView() {

        XMPPTCPConnectionConfiguration.Builder conf = XMPPTCPConnectionConfiguration.builder();
        conf.setServiceName("alpha");
        conf.setHost(Const.serverName);
        conf.setPort(5222);
        conf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        conf.setCompressionEnabled(true);
        try {
            TLSUtils.acceptAllCertificates(conf);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        mXmppConnection = new XMPPTCPConnection(conf.build());
        mXmppConnection.setUseStreamManagement(true);
        Const.connection = mXmppConnection;
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                new DoConnect().execute("");
            }
        }, 3000);
    }

    public void goLoginActivity() {
        Intent intent = new Intent(this, LogInActivity.class);
        Const.connection = mXmppConnection;
        startActivity(intent);
        finish();
    }

    private class DoConnect extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                mXmppConnection.connect();
            } catch (SmackException | IOException | XMPPException e) {
                e.printStackTrace();
                return "FAIL";
            }
            return "SUCCEED";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("SUCCEED")) {
                Toast.makeText(MainActivity.this, "Connecting succeeded",
                        Toast.LENGTH_SHORT).show();
                goLoginActivity();
            }
        }

        @Override
        protected void onPreExecute() {

        }
    }
    /**
     *  Create shortcut at screen
     */
//    public void createShortCut() {
//        Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//        String title = getResources().getString(R.string.app_name);
//        Parcelable icon = Intent.ShortcutIconResource.fromContext(MainActivity.this, R.drawable.icon);
//        Intent myIntent = new Intent(MainActivity.this, MainActivity.class);
//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, myIntent);
//        sendBroadcast(addIntent);
//    }
}
