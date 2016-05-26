package com.example.alphacom.xmppchat.xmpp;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * Created by alphacom on 5/6/2016.
 */
public class XMPPOperate {

    /**
     * Create a new group
     */
    public static boolean addGroup(Roster roster,String groupName)
    {
        try {
            roster.createGroup(groupName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Add user to friend list without group name
     */
    public static boolean addUser(Roster roster, String userName, String name)
    {
        try {
            roster.createEntry(userName, name, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Add user to group
     * @param roster
     * @param userName
     * @param name
     * @return
     */
    public static boolean addUsers(Roster roster, String userName, String name, String groupName)
    {
        try {
            roster.createEntry(userName, name, new String[]{ groupName});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Remove friend from group
     * @param userJid
     * @param groupName
     */
    public static void removeUserFromGroup(final String userJid,final String groupName, final XMPPTCPConnection connection) {
        Roster roster = Roster.getInstanceFor(connection);
        RosterGroup group = roster.getGroup(groupName);
        if (group != null) {
            try {
                RosterEntry entry = roster.getEntry(userJid);
                if (entry != null)
                    try {
                        group.removeEntry(entry);
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Disconnect
     * @param connection
     * @return
     */
    public static boolean disconnectAccount(XMPPTCPConnection connection)
    {
        try {
            /*connection.getAccountManager().deleteAccount();  */
            connection.disconnect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Remove a friend
     * @param roster
     * @param userJid
     * @return
     */
    public static boolean removeUser(Roster roster,String userJid)
    {
        try {
            RosterEntry entry = roster.getEntry(userJid);
            roster.removeEntry(entry);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Add a friend to a group
     * @param userJid
     * @param groupName
     */
    public static void addUserToGroup(final String userJid, final String groupName,
                                      final XMPPTCPConnection connection) {
        Roster roster = Roster.getInstanceFor(connection);
        if (groupName == null) return;
        RosterGroup group = roster.getGroup(groupName);
        // If group already exists, add entry to the group.
        // create a group if it doesn't exist.
        RosterEntry entry = roster.getEntry(userJid);
        try {
            if (group != null) {
                if (entry != null)
                    group.addEntry(entry);
            } else {
                RosterGroup newGroup = roster.createGroup("My Friend");
                if (entry != null)
                    newGroup.addEntry(entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Modify mood
     * @param connection
     * @param status
     */
    public static void changeStateMessage(final XMPPTCPConnection connection, final String status)
    {
        Presence presence = new Presence(Presence.Type.available);
        try {
            presence.setStatus(status);
            connection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            //TODO connection failed. please try it again.
            e.printStackTrace();
        }
    }
}
