package com.example.alphacom.xmppchat.ui;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alphacom.xmppchat.R;
import com.example.alphacom.xmppchat.utils.DialogFactory;
import com.example.alphacom.xmppchat.xmpp.Const;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText mEditUser;
    private EditText mEditEmail;
    private EditText mEditPassword;
    private XMPPTCPConnection xmppConnection;
    private Map<String, String> attributes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initView();
    }

    public void onRegister(View view) {
        String userName = mEditUser.getText().toString();
        String password = mEditPassword.getText().toString();
        String email = mEditEmail.getText().toString();
        //String mingcheng = nameMCH.getText().toString();
        xmppConnection = Const.connection;
        attributes = new HashMap<String, String>();
        attributes.put("email", email);
        String[] signInfo = {userName, password};
        new DoRegister().execute(signInfo);
//        reg.setPassword(password);
//        reg.addAttribute("name", mingcheng);
//        reg.addAttribute("email", email);
//
//        reg.addAttribute("android", "geolo_createUser_android");
//        PacketFilter filter = new AndFilter(new PacketIDFilter(
//                reg.getPacketID()), new PacketTypeFilter(
//                IQ.class));
//        PacketCollector collector = XmppConnection.getConnection().
//                createPacketCollector(filter);
//        XmppConnection.getConnection().sendPacket(reg);
//        IQ result = (IQ) collector.nextResult(SmackConfiguration
//                .getPacketReplyTimeout());
//        // Stop queuing results
//        collector.cancel();// 停止请求results（是否成功的结果）
//        if (result == null) {
//            Toast.makeText(getApplicationContext(), "服务器没有返回结果", Toast.LENGTH_SHORT).show();
//        } else if (result.getType() == IQ.Type.ERROR) {
//            if (result.getError().toString()
//                    .equalsIgnoreCase("conflict(409)")) {
//                Toast.makeText(getApplicationContext(), "这个账号已经存在", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getApplicationContext(), "注册失败",
//                        Toast.LENGTH_SHORT).show();
//            }
//        } else if (result.getType() == IQ.Type.RESULT) {
//            try {
//                XmppConnection.getConnection().login(accounts, password);
//                Presence presence = new Presence(Presence.Type.available);
//                XmppConnection.getConnection().sendPacket(presence);
//                DialogFactory.ToastDialog(this, "QQ注册", "亲，恭喜你，注册成功了！");
//                Intent intent = new Intent();
//                intent.putExtra("USERID", accounts);
//                intent.setClass(RegisterActivity.this, FriendListActivity.class);
//                startActivity(intent);
//            } catch (XMPPException e) {
//                e.printStackTrace();
//            }
//        }

    }

    private class DoRegister extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                if (!xmppConnection.isConnected())
                    xmppConnection.connect();
                if (xmppConnection.isAuthenticated()){
                    xmppConnection.disconnect();
                    xmppConnection.connect();
                }
                //SmackConfiguration.DEBUG = true;
                AccountManager accountManager = AccountManager.getInstance(xmppConnection);
                //accountManager.sensitiveOperationOverInsecureConnection(true);
                accountManager.createAccount(params[0], params[1], attributes);
            } catch (SmackException | IOException | XMPPException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(2);
                return "FAIL";
            }
            return "SUCCEED";
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals("FAIL"))
            Toast.makeText(SignUpActivity.this, "Sign up succeeded. \nPlease Sign in.",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {

        }
    }
    private void initView() {
        mEditUser = (EditText) findViewById(R.id.edit_username);
        mEditEmail = (EditText) findViewById(R.id.edit_email);
        mEditPassword = (EditText) findViewById(R.id.edit_password);
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 2) {
                DialogFactory.ToastDialog(SignUpActivity.this, "Sign Up",
                        "User already exist. \nPlease try it again.");
            }
        }
    };
}
