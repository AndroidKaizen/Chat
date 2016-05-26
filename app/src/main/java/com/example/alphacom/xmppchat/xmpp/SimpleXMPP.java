//package com.example.alphacom.xmppchat.xmpp;
//
//import android.content.Context;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.example.alphacom.xmppmessenger.R;
//import com.example.alphacom.xmppmessenger.core.ChatMessage;
//import com.example.alphacom.xmppmessenger.service.MyService;
//import com.example.alphacom.xmppmessenger.ui.Chats;
//import com.google.gson.Gson;
//
//import org.jivesoftware.smack.ConnectionConfiguration;
//import org.jivesoftware.smack.ConnectionListener;
//import org.jivesoftware.smack.SmackException;
//import org.jivesoftware.smack.XMPPConnection;
//import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smack.chat.Chat;
//import org.jivesoftware.smack.chat.ChatManager;
//import org.jivesoftware.smack.chat.ChatManagerListener;
//import org.jivesoftware.smack.chat.ChatMessageListener;
//import org.jivesoftware.smack.packet.Message;
//import org.jivesoftware.smack.packet.Stanza;
//import org.jivesoftware.smack.tcp.XMPPTCPConnection;
//import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
//import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
//import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
//
//import java.io.IOException;
//
///**
// * Created by alphacom on 5/3/2016.
// */
//public class SimpleXMPP {
//
//    public static XMPPTCPConnection connection;
//    public static String loginUser;
//    public static String userPassword;
//    public static SimpleXMPP instance = null;
//    public static boolean instanceCreated = false;
//    public static boolean connected = false;
//    public static boolean isConnecting = false;
//    public static boolean isToasted = true;
//    public boolean loggedin = false;
//    public Chat MyChat;
//
//    String text = "";
//    String mMessage = "";
//    String mReceiver = "";
//    Gson gson;
//    MyService context;
//    ChatManagerListenerImpl mChatManagerListener;
//    MMessageListener mMessageListener;
//    private boolean chat_created = false;
//    private String serverAddress;
//
//    static {
//        try {
//            Class.forName("org.jivesoftware.smack.ReconnectionManager");
//        } catch (ClassNotFoundException ex) {
//            // problem with loading reconnection manager
//        }
//    }
//    public SimpleXMPP(String serverAddress, String loginUser,
//                      String userPassword) {
//        this.serverAddress = serverAddress;
//        this.loginUser = loginUser;
//        this.userPassword = userPassword;
//        init();
//    }
//
//    public static SimpleXMPP getInstance(String server,
//                                         String user, String password) {
//        if (instance == null) {
//            instance = new SimpleXMPP(server, user, password);
//            instanceCreated = true;
//        }
//        return instance;
//    }
//
//    public void init() {
//        gson = new Gson();
//        mMessageListener = new MMessageListener(context);
//        mChatManagerListener = new ChatManagerListenerImpl();
//        initialiseConnection();
//    }
//
//    public void disconnect() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                connection.disconnect();
//            }
//        }).start();
//    }
//
//    public void connect(final String caller) {
//        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
//            @Override
//            protected synchronized Boolean doInBackground(Void... arg0) {
//                if (connection.isConnected())
//                    return false;
//                isConnecting = true;
//                if (isToasted)
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(context, caller + "=>connecting....",
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    });
//                Log.d("Connect() Function", caller + "=>connecting....");
//
//                try {
//                    connection.connect();
//                    DeliveryReceiptManager dm = DeliveryReceiptManager.getInstanceFor(connection);
//                    dm.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
//                    dm.addReceiptReceivedListener(new ReceiptReceivedListener() {
//                        @Override
//                        public void onReceiptReceived(String fromId, String toId, String msgId, Stanza packet) {
//
//                        }
//                    });
//                    connected = true;
//                } catch (IOException e) {
//                    if (isToasted)
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(context, "(" + caller + ")" + "IOException: ",
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    Log.e("(" + caller + ")", "IOException: " + e.getMessage());
//                } catch (SmackException e) {
//                    if (isToasted)
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(context, "(" + caller + ")" + "SMACKException: ",
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    Log.e("(" + caller + ")", "SMACKException: " + e.getMessage());
//                } catch (XMPPException e) {
//                    if (isToasted)
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(context, "(" + caller + ")" + "XMPPException: ",
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    Log.e("(" + caller + ")", "XMPPException: " + e.getMessage());
//                }
//                return isConnecting = false;
//            }
//        };
//        connectionThread.execute();
//    }
//
//    public void login(String userName, String password) {
//        try {
//            connection.login(userName, password);
//            Log.i("LOGIN", "Connected to the XMPP Server");
//        } catch (XMPPException | SmackException | IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//        }
//    }
//
//    public void sendMessage(ChatMessage chatMessage) {
//        String body = gson.toJson(chatMessage);
//        if (!chat_created) {
//            MyChat = ChatManager.getInstanceFor(connection).createChat(
//                    chatMessage.receiver + "@" + context.getString(R.string.server),
//                    mMessageListener);
//            chat_created = true;
//        }
//        final Message message = new Message();
//        message.setBody(body);
//        message.setStanzaId(chatMessage.msgId);
//        message.setType(Message.Type.chat);
//        try {
//            if (connection.isAuthenticated()) {
//                MyChat.sendMessage(message);
//            } else {
//                login(loginUser, userPassword);
//            }
//        } catch (SmackException.NotConnectedException e) {
//            Log.e("XMPP.SendMessage", "msg not sent!" + e.getMessage());
//        } catch (Exception e) {
//            Log.e("XMPP.SendMessage", e.getMessage());
//        }
//    }
//    public class XMPPConnectionListener implements ConnectionListener {
//
//        @Override
//        public void connected(final XMPPConnection connection) {
//            Log.d("XMPP", "Connected!");
//            connected = true;
//            if (!connection.isAuthenticated()){
//                login(loginUser, userPassword);
//            }
//        }
//
//        @Override
//        public void connectionClosed() {
//            if (isToasted)
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        //TODO Auto-generated method stub
//                        Toast.makeText(context, "Connection Closed!",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            Log.d("XMPP", "Connection Closed!");
//            connected = false;
//            chat_created = false;
//            loggedin = false;
//        }
//
//        @Override
//        public void connectionClosedOnError(Exception arg0) {
//            if (isToasted)
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, "Connection Closed On Error!",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            Log.d("XMPP", "Connection Closed!");
//            connected = false;
//            chat_created = false;
//            loggedin = false;
//        }
//
//        @Override
//        public void reconnectingIn(int arg0) {
//            Log.d("XMPP", "ReconnectingIn" + arg0);
//            loggedin = false;
//        }
//
//        @Override
//        public void reconnectionFailed(Exception arg0) {
//            if (isToasted)
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, "Reconnection Failed!",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            Log.d("XMPP", "Reconnection Failed");
//            connected = false;
//            chat_created = false;
//            loggedin = false;
//        }
//
//        @Override
//        public void reconnectionSuccessful() {
//            if (isToasted)
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, "Reconnected",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            Log.d("XMPP", "Reconnection Succeeded");
//            connected = true;
//            chat_created = false;
//            loggedin = false;
//        }
//
//        @Override
//        public void authenticated(XMPPConnection arg0, boolean arg1) {
//            Log.d("XMPP", "Authenticated!");
//            loggedin = true;
//            ChatManager.getInstanceFor(connection).addChatListener(
//                    mChatManagerListener);
//            chat_created = false;
//
//            // delaying thread
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//
//            if (isToasted)
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, "Connected!",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//        }
//    }
//
//    private class ChatManagerListenerImpl implements ChatManagerListener{
//        @Override
//        public void chatCreated(final Chat chat, final boolean createdLocally) {
//            if (!createdLocally)
//                chat.addMessageListener(mMessageListener);
//        }
//    }
//    private class MMessageListener implements ChatMessageListener {
//        public MMessageListener(Context context){}
//
//        @Override
//        public void processMessage(final Chat chat, final Message message) {
//            Log.i("SimpleXMPP_MSG_Listener", "XMPP Message Received: "
//            + message);
//            if (message.getType() == Message.Type.chat
//                    && message.getBody() != null) {
//                final ChatMessage chatMessage = gson.fromJson(message.getBody(), ChatMessage.class);
//                processMessage(chatMessage);
//            }
//        }
//
//        private void processMessage(final ChatMessage chatMessage) {
//            chatMessage.isMine = false;
//            Chats.chatList.add(chatMessage);
//            new Handler(Looper.getMainLooper()).post(new Runnable(){
//                @Override
//                public void run(){
//                    Chats.chatAdapter.notifyDataSetChanged();
//                }
//            });
//        }
//
//    }
//
//    private void initialiseConnection() {
//        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
//        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//        config.setServiceName(serverAddress);
//        config.setHost(serverAddress);
//        config.setPort(5222);
//        config.setDebuggerEnabled(true);
//        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
//        XMPPTCPConnection.setUseStreamManagementDefault(true);
//        connection = new XMPPTCPConnection(config.build());
//        XMPPConnectionListener connectionListener = new XMPPConnectionListener();
//        connection.addConnectionListener(connectionListener);
//    }
//}
