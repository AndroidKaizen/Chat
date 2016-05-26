package com.example.alphacom.xmppchat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.alphacom.xmppchat.R;
import com.example.alphacom.xmppchat.xmpp.Const;
import com.example.alphacom.xmppchat.xmpp.MyXmppConnection;
import com.example.alphacom.xmppchat.xmpp.XMPPOperate;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings("all")
public class FriendAddActivity extends AppCompatActivity{

	private static final String EXTRA_FRIEND_ID = "FriendID";
	private static final String EXTRA_FRIEND_NAME = "FriendID";
	private static final String EXTRA_USERID = "UserID";
	private static final String EXTRA_FROM_JID = "FromUserJID";
	private static final String EXTRA_GROUP_NAME = "GroupName";

	private String mUserId;
	private Button btnSearch;
	private Button btnGoBack;
	private String queryResult = "";
	private ListView listFriends;
	private XMPPTCPConnection connection;
	Roster roster;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		connection = Const.connection;
		//connection = MyXmppConnection.getConnection();
		//requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.activity_friend_add);
        initView();
	}



	public void searchFriend() {
		String search_text = ((EditText) findViewById(R.id.text_search)).getText().toString();
		if (search_text.equals("")) {
			Toast.makeText(FriendAddActivity.this, "输入信息不能为空！", Toast.LENGTH_SHORT).show();
		} else {
			try{
				//XMPPTCPConnection connection = MyXmppConnection.getConnection();
				UserSearchManager search = new UserSearchManager(connection);
				//此处一定要加上 search.
				Form searchForm = search.getSearchForm("search."+connection.getServiceName());
				Form answerForm = searchForm.createAnswerForm();
				answerForm.setAnswer("Username", true);
				answerForm.setAnswer("search", search_text.toString().trim());
				ReportedData data = search.getSearchResults(answerForm,"search."
						+ connection.getServiceName());
				Iterator<ReportedData.Row> it = data.getRows().iterator();
				ReportedData.Row row = null;
				while(it.hasNext()){
					row = it.next();
					queryResult = row.getValues("Username").iterator().next().toString();
				}
			}catch(Exception e){
				Toast.makeText(FriendAddActivity.this,e.getMessage()+" "+e.getClass().toString(),
						Toast.LENGTH_SHORT).show();
			}
			if(!queryResult.equals("")){
				// 生成动态数组，加入数据
				ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
				    HashMap<String, Object> map = new HashMap<String, Object>();
				    map.put("name", queryResult); //会员昵称
					listItem.add(map);
				// 生成适配器的Item和动态数组对应的元素
				SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,// 数据源
						R.layout.friend_search_view,// ListItem的XML实现
						// 动态数组与ImageItem对应的子项
						new String[] { "name", },
						// ImageItem的XML文件里面的一个ImageView,两个TextView ID
						new int[] { R.id.itemtext });
				// 添加并且显示
				listFriends.setAdapter(listItemAdapter);
				// 添加短点击事件
				listFriends.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						HashMap<String, String> map = (HashMap<String, String>)
								listFriends.getItemAtPosition(position);
						final String name = map.get("name");
						AlertDialog.Builder dialog=new AlertDialog.Builder(FriendAddActivity.this);
						dialog.setTitle("添加好友")
						      .setIcon(R.drawable.default_head)
						      .setMessage("您确定要添加【"+name+"】为好友吗？")
						      .setPositiveButton("确定", new DialogInterface.OnClickListener() {
									 @Override
									 public void onClick(DialogInterface dialog, int which) {
										 // TODO Auto-generated method stub
										 Roster roster = Roster.getInstanceFor(connection);
										 String userName = name + "@" + connection.getServiceName();
										 //默认添加到【我的好友】分组
										 String groupName = "我的好友";
										 XMPPOperate.addUsers(roster, userName, name, groupName);
										 Presence subscription = new Presence(Presence.Type.subscribe);
										 subscription.setTo(userName);
										 dialog.cancel();//取消弹出框
										 finish();
										 Intent intent = new Intent();
										 intent.putExtra(EXTRA_USERID, mUserId);
										 intent.putExtra(EXTRA_GROUP_NAME, groupName);
										 intent.setClass(FriendAddActivity.this, FriendListActivity.class);
										 Const.connection = connection;
										 startActivity(intent);
									 }
								   })
						       .setNegativeButton("取消", new DialogInterface.OnClickListener() {
						                 public void onClick(DialogInterface dialog, int which) {
						                     // TODO Auto-generated method stub
						                     dialog.cancel();//取消弹出框
						                 }
						               }).create().show();
					       }
				     });
			  }else{
				  Toast.makeText(FriendAddActivity.this, "此用户不存在，请确保输入的信息正确！",
						  Toast.LENGTH_SHORT).show();
			  }
		}
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        if(!queryResult.equals("")){
        	menu.clear();
    		menu.add(Menu.NONE, Menu.FIRST + 1, 1,"新建分组").setIcon(R.drawable.addfriends_icon_icon);
    		menu.add(Menu.NONE, Menu.FIRST + 2, 1,"即时聊天").setIcon(R.drawable.menu_exit);
		}else{
			menu = null;
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			View view = View.inflate(this, R.layout.dialog, null);
			final PopupWindow mPopupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
			mPopupWindow.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.showAtLocation(((Activity) this).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
			mPopupWindow.setAnimationStyle(R.style.animationmsg);
			mPopupWindow.setFocusable(true);
			mPopupWindow.setTouchable(true);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.update();
			final EditText addFriend = (EditText) view.findViewById(R.id.addfriend);
			Button sure = (Button) view.findViewById(R.id.sure);
			Button cancle = (Button) view.findViewById(R.id.cancle);
			sure.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String groupName = addFriend.getText().toString().trim();
					if (groupName.equals("") || groupName.equals("")) {
						Toast.makeText(FriendAddActivity.this, "群组名称不能为空!",
								Toast.LENGTH_SHORT).show();
					} else {
						boolean result = false;
						result = XMPPOperate.addGroup(roster, groupName);
						if (result) {
							 Roster roster = Roster.getInstanceFor(connection);
	                    	 String userName = queryResult + "@"
									 + connection.getServiceName();
							 XMPPOperate.addUsers(roster, userName, queryResult, groupName);
                    	     Intent intent = new Intent(FriendAddActivity.this, FriendListActivity.class);
                    		 intent.putExtra(EXTRA_USERID, mUserId);
							 Const.connection = connection;
                 			 startActivity(intent);
						} else {
							Toast.makeText(FriendAddActivity.this, "群组添加失败!",
									Toast.LENGTH_SHORT).show();
						}
					}
					mPopupWindow.dismiss();
				}
			});
			cancle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mPopupWindow.dismiss();
				}
			});
			break;
		case Menu.FIRST + 2:
			Intent intent = new Intent(this, ChatActivity.class);
			String pFRIENDID = queryResult + "@" + connection.getServiceName();
			intent.putExtra(EXTRA_FRIEND_ID, pFRIENDID);
			intent.putExtra(EXTRA_FRIEND_NAME, pFRIENDID);
			intent.putExtra(EXTRA_USERID, mUserId);
			Const.connection = connection;
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

    private void initView() {
        connection = Const.connection;
        roster = Roster.getInstanceFor(connection);
        mUserId = getIntent().getStringExtra(EXTRA_USERID);
        listFriends = (ListView) findViewById(R.id.list_friends);
        btnSearch = (Button) findViewById(R.id.btn_find);
        btnSearch.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String username = ((EditText) findViewById(R.id.text_search)).getText().toString();
                String[] temp = username.split("@");
                if (temp.length <= 1){
                    username += "@" + connection.getServiceName();
                }
                roster = Roster.getInstanceFor(connection);
                String groupName = "My Friends";
                XMPPOperate.addUser(roster, username, username);
                Presence subscrption = new Presence(Presence.Type.subscribe);
                subscrption.setTo(username);
                Intent intent = new Intent(FriendAddActivity.this, FriendListActivity.class);
//   		 	intent.putExtra("USERID", mUserId);
//   		 	intent.putExtra("GROUPNAME", groupName);
				Const.connection = connection;
                startActivity(intent);
                finish();
            }
        });
        // back pressed
        btnGoBack = (Button) findViewById(R.id.btn_back);
        btnGoBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

    }
}
