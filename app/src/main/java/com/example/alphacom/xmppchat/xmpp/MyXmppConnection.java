package com.example.alphacom.xmppchat.xmpp;

import android.os.Handler;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;
import org.jivesoftware.smackx.address.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.commands.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.delay.provider.DelayInformationProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.iqprivate.PrivateDataManager;
import org.jivesoftware.smackx.muc.packet.GroupChatInvitation;
import org.jivesoftware.smackx.muc.provider.MUCAdminProvider;
import org.jivesoftware.smackx.muc.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.muc.provider.MUCUserProvider;
import org.jivesoftware.smackx.offline.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.offline.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.privacy.provider.PrivacyProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.sharedgroups.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.si.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;
import org.jivesoftware.smackx.xdata.provider.DataFormProvider;
import org.jivesoftware.smackx.xevent.provider.MessageEventProvider;
import org.jivesoftware.smackx.xhtmlim.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.xroster.provider.RosterExchangeProvider;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by alphacom on 5/7/2016.
 */
public class MyXmppConnection {

    //public static String SERVER_HOST = "192.168.1.99";//你openfire服务器所在的ip
    //public static String SERVER_NAME = "aa";//设置openfire时的服务器名

    public static String SERVER_HOST = "192.168.1.104";//你openfire服务器所在的ip
    public static String SERVER_NAME = "alpha";//设置openfire时的服务器名
    public static int    SERVER_PORT = 5222;//服务端口 可以在openfire上设置

    private static XMPPTCPConnection mXmppConnection = null;

    private static void openConnection() {
        try {
            if (null == mXmppConnection || !mXmppConnection.isAuthenticated()) {
                SmackConfiguration.DEBUG = true; // There is already a program to debug smack
                //配置连接
                XMPPTCPConnectionConfiguration.Builder conf = XMPPTCPConnectionConfiguration.builder();
                conf.setHost(SERVER_HOST);
                conf.setServiceName(SERVER_HOST);
                conf.setPort(SERVER_PORT);
                conf.setSendPresence(true);
                conf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                conf.setCompressionEnabled(true);
                try {
                    TLSUtils.acceptAllCertificates(conf);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }
//                ConnectionConfiguration config = new ConnectionConfiguration(
//                        SERVER_HOST, SERVER_PORT,
//                        SERVER_NAME);
//                config.setReconnectionAllowed(true);
//                config.setSendPresence(true);
//                config.setReconnectionAllowed(true);
//                config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
//                config.setSASLAuthenticationEnabled(false);
//                File file =new File("/mnt/sdcard/security/");
//                file.mkdirs();
//                config .setTruststorePath("/mnt/sdcard/security/cacerts.bks");
//                config.setTruststorePassword("123456");
//                config.setTruststoreType("bks");
//                config.setSASLAuthenticationEnabled(true);
//                conf.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.ifpossible);

//                mXmppConnection = new XMPPTCPConnection(conf.build());
//                mXmppConnection.setUseStreamManagement(true);
//                Const.connection = mXmppConnection;
                mXmppConnection = new XMPPTCPConnection(conf.build());
                mXmppConnection.setUseStreamManagement(true);
                mXmppConnection.connect();//连接到服务器
                //配置各种Provider，如果不配置，则会无法解析数据
                //TODO check necessity
                //configureConnection();
            }
        } catch (XMPPException xe) {
            xe.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建连接
     */
    public static XMPPTCPConnection getConnection() {
        if (mXmppConnection == null) {
            openConnection();
        }
        return mXmppConnection;
    }

    /**
     * Close connection
     */
    public static void closeConnection() {
        mXmppConnection.disconnect();
        mXmppConnection = null;
    }
    /**
     * xmpp configuration
     */
    private static void configureConnection() {
        // Private Data Storage
        ProviderManager.addIQProvider("query", "jabber:iq:private",new PrivateDataManager.PrivateDataIQProvider());
        // Time
        try {
            ProviderManager.addIQProvider("query", "jabber:iq:time",Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Roster Exchange
        ProviderManager.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());
        // Message Events
        ProviderManager.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider());
        // Chat State
        ProviderManager.addExtensionProvider("active","http://jabber.org/protocol/chatstates",new ChatStateExtension.Provider());
        ProviderManager.addExtensionProvider("composing","http://jabber.org/protocol/chatstates",new ChatStateExtension.Provider());
        ProviderManager.addExtensionProvider("paused","http://jabber.org/protocol/chatstates",new ChatStateExtension.Provider());
        ProviderManager.addExtensionProvider("inactive","http://jabber.org/protocol/chatstates",new ChatStateExtension.Provider());
        ProviderManager.addExtensionProvider("gone","http://jabber.org/protocol/chatstates",new ChatStateExtension.Provider());
        // XHTML
        ProviderManager.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",new XHTMLExtensionProvider());
        // Group Chat Invitations
        ProviderManager.addExtensionProvider("x", "jabber:x:conference",new GroupChatInvitation.Provider());
        // Service Discovery # Items //解析房间列表
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#items",new DiscoverItemsProvider());
        // Service Discovery # Info //某一个房间的信息
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#info",new DiscoverInfoProvider());
        // Data Forms
        ProviderManager.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
        // MUC User
        ProviderManager.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",new MUCUserProvider());
        // MUC Admin
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/muc#admin",new MUCAdminProvider());
        // MUC Owner
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/muc#owner",new MUCOwnerProvider());
        // Delayed Delivery
        ProviderManager.addExtensionProvider("x", "jabber:x:delay",new DelayInformationProvider());
        // Version
        try {
            ProviderManager.addIQProvider("query", "jabber:iq:version",Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            // Not sure what's happening here.
        }
        // VCard
        ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());
        // Offline Message Requests
        ProviderManager.addIQProvider("offline", "http://jabber.org/protocol/offline",new OfflineMessageRequest.Provider());
        // Offline Message Indicator
        ProviderManager.addExtensionProvider("offline","http://jabber.org/protocol/offline",new OfflineMessageInfo.Provider());
        // Last Activity
        ProviderManager.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
        // User Search
        ProviderManager.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
        // SharedGroupsInfo
        ProviderManager.addIQProvider("sharedgroup","http://www.jivesoftware.org/protocol/sharedgroup",new SharedGroupsInfo.Provider());
        // JEP-33: Extended Stanza Addressing
        ProviderManager.addExtensionProvider("addresses","http://jabber.org/protocol/address",new MultipleAddressesProvider());
        ProviderManager.addIQProvider("si", "http://jabber.org/protocol/si",new StreamInitiationProvider());
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/bytestreams",new BytestreamsProvider());
        ProviderManager.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        ProviderManager.addIQProvider("command", "http://jabber.org/protocol/commands",new AdHocCommandDataProvider());
        ProviderManager.addExtensionProvider("malformed-action","http://jabber.org/protocol/commands",new AdHocCommandDataProvider.MalformedActionError());
        ProviderManager.addExtensionProvider("bad-locale","http://jabber.org/protocol/commands",new AdHocCommandDataProvider.BadLocaleError());
        ProviderManager.addExtensionProvider("bad-payload","http://jabber.org/protocol/commands",new AdHocCommandDataProvider.BadPayloadError());
        ProviderManager.addExtensionProvider("bad-sessionid","http://jabber.org/protocol/commands",new AdHocCommandDataProvider.BadSessionIDError());
        ProviderManager.addExtensionProvider("session-expired","http://jabber.org/protocol/commands",new AdHocCommandDataProvider.SessionExpiredError());
    }
}