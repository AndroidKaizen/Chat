package com.example.alphacom.xmppchat.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alphacom.xmppchat.R;
import com.example.alphacom.xmppchat.model.FriendInfo;
import com.example.alphacom.xmppchat.model.GroupInfo;
import com.example.alphacom.xmppchat.model.MessageInfo;
import com.example.alphacom.xmppchat.utils.TimeRender;
import com.example.alphacom.xmppchat.xmpp.Const;
import com.example.alphacom.xmppchat.xmpp.XMPPOperate;

/**
 * 好友列表
 */
@SuppressWarnings("all")
public class FriendListActivity extends AppCompatActivity implements
        OnGroupClickListener, OnChildClickListener {

    public static final int NOTIF_UI = 1000;
    public static final int ADD_FRIEND = 1003;
    public static final String CHECK = null;
    public static final int NEW_MESSAGE = 1;

    private static final String EXTRA_FRIENDID = "FriendID";
    private static final String EXTRA_USERID = "UserID";
    private static final String EXTRA_FROM_JID = "FromUserJID";
    private static final String EXTRA_FRIEND_NAME = "FriendID";
    private static final String EXTRA_GROUP_NAME = "GroupName";


    public static MyAdapter adapter;
    public static FriendListActivity friendListActivity;
    // RESOURCE
    public static String RESOUCE_NAME = "Spark 2.6.3";
    public static String MY_RESOUCE_NAME = "Smack";
    public static String SERVICE_NAME = "tp";

    FriendInfo friendInfo;
    GroupInfo groupInfo;
    Roster roster = Roster.getInstanceFor(Const.connection);
    XMPPTCPConnection connection = Const.connection;

    private String mUserId;
    private String mGroupName;
    private String fromUserJid = null;// 发送邀请的用户的userJid
    private String toUserJid = null;// 收到邀请的用户的userJid
    private String myMood = null;
    private String friendMood = null;
    private LayoutInflater mChildInflater;
    private ExpandableListView listContact;
    private List<GroupInfo> groupList;
    private List<FriendInfo> friendList;
    private NotificationManager mNotificationManager;
    private TextView textMyStatus = null;
    private Map<String, ArrayList<MessageInfo>> cachedMessages =
            new HashMap<String, ArrayList<MessageInfo>>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_friend_list);
        initView();

    }

    protected void setNotiType(int iconId, String s) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent appIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification myNoti = new Notification();
        myNoti.icon = iconId;
        myNoti.tickerText = s;
        myNoti.defaults = Notification.DEFAULT_SOUND;
        myNoti.flags |= Notification.FLAG_AUTO_CANCEL;
        //myNoti.setLatestEventInfo(this, "Chat Message", s, appIntent);
        mNotificationManager.notify(0, myNoti);
    }

    protected Handler handler = new Handler() {

        //we do not have delayed message, so the leak should not be a problem
        @SuppressLint("HandlerLeak")
        public void handleMessage(android.os.Message msg) {
            System.out.println("new message received!");
            switch (msg.what) {
                case NEW_MESSAGE:
                    Bundle bundle = msg.getData();
                    String from = bundle.getString("from");
                    if (from.endsWith("/Smack")) {
                        from = from.split("/Smack")[0];
                    }
                    String to = bundle.getString("to");
                    String body = bundle.getString("body");
                    for (GroupInfo groupInfo : groupList){
                        List<FriendInfo> friendlist = groupInfo.getFriendInfoList();
                        for(FriendInfo friend : friendlist){
                            if (friend.getUserJid().equals(from)){
                                friend.setMood("new message");
                                ArrayList<MessageInfo> msgs = cachedMessages.get(from);
                                if (msgs == null){
                                    msgs = new ArrayList<MessageInfo>();
                                    cachedMessages.put(from, msgs);
                                }
                                MessageInfo mInfo = new MessageInfo();
                                mInfo.setDate(TimeRender.getDate());
                                mInfo.setMsg(body);
                                mInfo.setFrom(MessageInfo.FROM_TYPE[0]);
                                mInfo.setUserid(from);
                                msgs.add(mInfo);
                                break;
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        };
    };

    public void loadLocalFriend() {
        groupList = new ArrayList<GroupInfo>();
        groupInfo = new GroupInfo();
        groupInfo.setGroupName("My Friend");
        ArrayList<FriendInfo> friends = new ArrayList<FriendInfo>();
        friends.add(new FriendInfo("test1", null));
        groupInfo.setFriendInfoList(friends);

        groupList.add(groupInfo);
        groupInfo = null;

    }

    public void loadFriend() {

        XMPPTCPConnection conn = Const.connection;

        Roster roster = Roster.getInstanceFor(conn);
        Collection<RosterGroup> groups = roster.getGroups();
        groupList = new ArrayList<GroupInfo>();
        for (RosterGroup group : groups) {
            groupInfo = new GroupInfo();
            friendList = new ArrayList<FriendInfo>();
            groupInfo.setGroupName(group.getName());
            Collection<RosterEntry> entries = group.getEntries();
            for (RosterEntry entry : entries) {
                //if ("both".equals(entry.getType().name())) {// Add only bilateral friend
                if (true) {// Add only bilateral friend
                    friendInfo = new FriendInfo();
                    friendInfo.setUserJid(entry.getUser());
                    System.out.println("Friend's mood：" + entry.getStatus()
                            .fromString(entry.getUser()));
                    if (friendMood == null) {
                        friendMood = "";
                    }
                    friendInfo.setMood(friendMood);
                    friendList.add(friendInfo);
                    friendInfo = null;
                }
            }
            groupInfo.setFriendInfoList(friendList);
            groupList.add(groupInfo);
            groupInfo = null;
        }
        if (groupList.isEmpty()) {
            groupInfo = new GroupInfo();
            groupInfo.setGroupName("My Friends");
            ArrayList<FriendInfo> friendList = new ArrayList<FriendInfo>();
            groupInfo.setFriendInfoList(friendList);
            groupList.add(groupInfo);
            Collection<RosterEntry> entries = roster.getEntries();
            if (entries != null) {
                for (RosterEntry entry : entries) {
                    //if ("both".equals(entry.getType().name())) {// Add only bilateral friend
                    if (true) {// Add only bilateral friend
                        friendInfo = new FriendInfo();
                        friendInfo.setUserJid(entry.getUser());
                        System.out.println("Friend mood：" + entry.getStatus()
                                .fromString(entry.getUser()));
                        if (friendMood == null) {
                            friendMood = "";
                        }
                        friendInfo.setMood(friendMood);
                        friendList.add(friendInfo);
                        friendInfo = null;
                    }
                }
            }
            groupInfo = null;
        }
    }

    @Override
    protected void onResume() {
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        XmppConnection.closeConnection();
//        Intent intent = new Intent(this, XmppService.class);
//        stopService(intent);
        friendListActivity = null;
        super.onDestroy();
    }



    @Override
    public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                int childPosition, long id) {
        FriendInfo info = groupList.get(groupPosition).getFriendInfoList().get(childPosition);
        Intent intent = new Intent(this, ChatActivity.class);
        String pFRIENDID = info.getUserJid();
        intent.putExtra(EXTRA_FRIENDID, pFRIENDID);
        intent.putExtra(EXTRA_FRIEND_NAME, pFRIENDID);
        intent.putExtra(EXTRA_USERID, mUserId);
        intent.putExtra("cached", cachedMessages.get(pFRIENDID));
        cachedMessages.remove(pFRIENDID);
        Const.connection = connection;
        startActivity(intent);
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "Refresh List").setIcon(R.drawable.menu_refresh);
        menu.add(Menu.NONE, Menu.FIRST + 2, 1, "Update Mood").setIcon(R.drawable.menu_setting);
        menu.add(Menu.NONE, Menu.FIRST + 3, 1, "Add Friend").setIcon(R.drawable.addfriends_icon_icon);
        menu.add(Menu.NONE, Menu.FIRST + 4, 1, "Log Out").setIcon(R.drawable.menu_exit);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                friendMood = "dd！";
                loadFriend();
                // Intent intent1 = new Intent();
                // intent1.putExtra("USERID", mUserId);
                // intent1.putExtra("MOOD", moods);
                // intent1.setClass(FriendListActivity.this,
                // FriendListActivity.class);
                // startActivity(intent1);
                break;
            case Menu.FIRST + 2:
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                final View myMoodView = layoutInflater.inflate(R.layout.dialog_mood, null);
                Dialog dialog = new AlertDialog.Builder(this).setView(myMoodView)
                        .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myMood = ((EditText) myMoodView.findViewById(R.id.myMood))
                                .getText().toString().trim();
                        System.out.println("Changed the mood to：" + myMood);
                        XMPPOperate.changeStateMessage(connection, myMood);
                        textMyStatus.setText(myMood);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
                dialog.show();
                break;
            case Menu.FIRST + 3:
                Intent intent11 = new Intent(FriendListActivity.this, FriendAddActivity.class);
                intent11.putExtra(EXTRA_USERID, mUserId);
                Const.connection = connection;
                startActivity(intent11);
                break;
            case Menu.FIRST + 4:
                XMPPOperate.disconnectAccount(connection);
                Intent exits = new Intent(Intent.ACTION_MAIN);
                exits.addCategory(Intent.CATEGORY_HOME);
                exits.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(exits);
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Long Click Event to remove friend
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (menuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo) {

            ExpandableListView.ExpandableListContextMenuInfo info =
                    (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

            int type = ExpandableListView.getPackedPositionType(info.packedPosition);

            if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

                int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
                int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
                final FriendInfo dInfo = groupList.get(groupPos).getFriendInfoList().get(childPos);
                final GroupInfo gInfo = groupList.get(groupPos);
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                View delFriendView = layoutInflater.inflate(R.layout.dialog_del_friend, null);
                TextView delname = (TextView) delFriendView.findViewById(R.id.delname);
                delname.setText(dInfo.getUserJid());
                final CheckBox delCheckBox = (CheckBox) delFriendView.findViewById(R.id.delCheckBox);
                Dialog dialog = new AlertDialog.Builder(this).setIcon(R.drawable.default_head)
                        .setTitle("Remove friend").setView(delFriendView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                XMPPOperate.removeUserFromGroup(dInfo.getUserJid(),
                                        gInfo.getGroupName(), connection);
                                if (delCheckBox.isChecked()) {
                                    XMPPOperate.removeUser(roster, dInfo.getUserJid());
                                }
                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_USERID, mUserId);
                                intent.putExtra(EXTRA_FROM_JID, CHECK);
                                intent.setClass(FriendListActivity.this, FriendListActivity.class);
                                Const.connection = connection;
                                startActivity(intent);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create();
                dialog.show();
            }
        }
    }

    public class MyAdapter extends BaseExpandableListAdapter {

        Context context;
        class FriendHolder {
            TextView name;
            TextView mood;
            ImageView iv;
        }

        public MyAdapter(Context context) {
            mChildInflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return groupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return groupList.get(groupPosition).getFriendInfoList().size();
        }

        @Override
        public GroupInfo getGroup(int groupPosition) {
            return groupList.get(groupPosition);
        }

        public GroupInfo getGroup(String groupName) {
            GroupInfo groupInfo = null;
            if (getGroupCount() > 0) {
                for (int i = 0, j = getGroupCount(); i < j; i++) {
                    GroupInfo holder = (GroupInfo) getGroup(i);
                    if (TextUtils.isEmpty(holder.getGroupName())) {
                        groupList.remove(holder);
                    } else {
                        if (holder.getGroupName().equals(groupName)) {
                            groupInfo = holder;
                        }
                    }
                }
            }
            return groupInfo;
        }

        @Override
        public FriendInfo getChild(int groupPosition, int childPosition) {
            return groupList.get(groupPosition).getFriendInfoList().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            FriendHolder holder;
            if (convertView == null) {
                holder = new FriendHolder();
                convertView = mChildInflater.inflate(R.layout.friend_group_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.friend_group_list_name);
                holder.iv = (ImageView) convertView.findViewById(R.id.friend_group_list_icon);
                convertView.setTag(holder);
            } else {
                holder = (FriendHolder) convertView.getTag();
            }
            String groupname = groupList.get(groupPosition).getGroupName();
            holder.name.setText(groupname);
            if (isExpanded) {
                holder.iv.setBackgroundResource(R.drawable.sc_group_expand);
            } else {
                holder.iv.setBackgroundResource(R.drawable.sc_group_unexpand);
            }
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            FriendHolder holder;
            if (convertView == null) {
                holder = new FriendHolder();
                convertView = mChildInflater.inflate(R.layout.friend_child_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.friend_nickname);
                holder.mood = (TextView) convertView.findViewById(R.id.friend_mood);
                convertView.setTag(holder);
            } else {
                holder = (FriendHolder) convertView.getTag();
            }
            FriendInfo groupname = groupList.get(groupPosition)
                    .getFriendInfoList().get(childPosition);
            holder.name.setText(groupname.getUserJid());
            holder.mood.setText(groupname.getMood());
            if (isLastChild) {
                listContact.setItemChecked(groupPosition, true);
            }
            return convertView;
        }


        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private void initView() {
        friendListActivity = this;
        mNotificationManager = (NotificationManager)
                this.getSystemService(Service.NOTIFICATION_SERVICE);
        mUserId = getIntent().getStringExtra(EXTRA_USERID);
        mGroupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);
        fromUserJid = getIntent().getStringExtra(EXTRA_FROM_JID);
        listContact = (ExpandableListView) findViewById(R.id.lv_contact);
        textMyStatus = (TextView) findViewById(R.id.tv_status);

        TextView textMyName = (TextView) findViewById(R.id.tv_myName);
        textMyName.setText(mUserId);
        registerForContextMenu(listContact);
        loadFriend();
        //TODO check necessity
        try {
            // loadLocalFriend();
            loadFriend();
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent(this, LogInActivity.class);
            Const.connection = connection;
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logged out", 0).show();
            return;
        }
        adapter = new MyAdapter(this);
        listContact.setAdapter(adapter);
        listContact.setOnGroupClickListener(this);
        listContact.setOnChildClickListener(this);
        listContact.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        // Send your status to a friend
        String status = textMyStatus.getText().toString();
        XMPPOperate.changeStateMessage(connection, status);

        roster.addRosterListener(new RosterListener() {

            // Listener for friend's request message
            @Override
            public void entriesAdded(Collection<String> invites) {
                System.out.println("Listen to the friend request message：" + invites);
                for (Iterator iter = invites.iterator(); iter.hasNext();) {
                    String fromUserJids = (String) iter.next();
                    System.out.println("fromUserJids：" + fromUserJids);
                    fromUserJid = fromUserJids;
                }
                if (fromUserJid != null) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_USERID, mUserId);
                    intent.putExtra(EXTRA_FROM_JID, fromUserJid);
                    intent.setClass(FriendListActivity.this, FriendListActivity.class);
                    Const.connection = connection;
                    startActivity(intent);
                }
            }

            // Listener for friends agreed to add
            @Override
            public void entriesUpdated(Collection<String> invites) {
                System.out.println("Listening to friends agree：" + invites);
                for (Iterator iter = invites.iterator(); iter.hasNext();) {
                    String fromUserJids = (String) iter.next();
                    System.out.println("Agreed to add a friend：" + fromUserJids);
                    toUserJid = fromUserJids;
                }
                if (toUserJid != null) {
                    XMPPOperate.addUserToGroup(toUserJid, mGroupName, connection);
                    loadFriend();
                }
            }

            // Listener for friends removing message
            @Override
            public void entriesDeleted(Collection<String> delFriends) {
                System.out.println("Removed Friends：" + delFriends);
                if (delFriends.size() > 0) {
                    loadFriend();
                }
            }

            // Listener for friends status changing
            @Override
            public void presenceChanged(Presence presence) {
                friendMood = presence.getStatus();
                System.out.println("Status：" + presence.getStatus());
            }

        });

        ChatManager cm = ChatManager.getInstanceFor(connection);
        // Get any messages sent by the server
        cm.addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean able) {
                chat.addMessageListener(new ChatMessageListener(){
                    @Override
                    public void processMessage(Chat chat2, Message message) {
                        android.os.Message msg = handler.obtainMessage();
                        System.out.println("Messages from server ：" + message.getFrom() + "  " + message.getBody());
                        // setNotiType(R.drawable.log, message.getBody());
//						msg.obj = message.getBody();
                        Bundle b = new Bundle();
                        b.putString("from", message.getFrom());
                        b.putString("to", message.getTo());
                        b.putString("body", message.getBody());
                        msg.setData(b);
                        msg.what= NEW_MESSAGE;
                        msg.sendToTarget();

                    }
                });
            }
        });
        System.out.println("fromUserJid：" + fromUserJid);
        if (fromUserJid != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(FriendListActivity.this);
            dialog.setTitle("Friends Request").setIcon(R.drawable.log)
                    .setMessage("【" + fromUserJid
                            + "】Friend request arrived，wanna add each other as friend?")
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();// Cancel pop up
                            // 允许添加好友则回复消息，被邀请人应当也发送一个邀请请求。
                            Presence subscription = new Presence(Presence.Type.subscribe);
                            subscription.setTo(fromUserJid);
                            try {
                                connection.sendPacket(subscription);
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Group name：" + mGroupName);
                            if (mGroupName == null) {
                                mGroupName = "My Friend";
                            }
                            XMPPOperate.addUserToGroup(fromUserJid, mGroupName, connection);
                            Intent intent = new Intent(FriendListActivity.this,
                                    FriendListActivity.class);
                            intent.putExtra(EXTRA_USERID, mUserId);
                            intent.putExtra(EXTRA_FROM_JID, CHECK);
                            Const.connection = connection;
                            startActivity(intent);
                        }
                    }).setNegativeButton("Refuse", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    XMPPOperate.removeUser(roster, fromUserJid);
                    dialog.cancel();// Cancel pop up
                }
            }).create().show();
        }
    }
}
