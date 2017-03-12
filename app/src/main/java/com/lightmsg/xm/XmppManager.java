package com.lightmsg.xm;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Callable;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Authentication;
import org.jivesoftware.smack.packet.HeartBeatMessage;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Profiles;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.lightmsg.R;
import com.lightmsg.service.CoreService;
import com.lightmsg.service.CoreService.Account;
import com.lightmsg.util.MediaFile;

public class XmppManager {
    private static final String TAG = XmppManager.class.getSimpleName();
    private CoreService xs;

    private Handler handler;

    private XMPPConnection connection = null;
    private XmConnectionListener connectiongListener = null;
    private boolean bConn = false;
    private boolean bAuth = false;

    private PacketListener notificationPacketListener;
    private Thread reconnection;

    private Roster roster = null;
    private Collection<RosterEntry> rosterEntries = null;

    private ChatManager chatManager = null;
    //private Chat newChat = null;

    private MultiUserChat muc = null;

    private XmConnectTask   xmConnectTask;
    private XmLoginTask     xmLoginTask;
    private XmRegisterTask  xmRegisterTask;
    private XmProfilesTask  xmProfilesTask;

    private boolean     isConnecting;
    private boolean     isLogining;

    public XmppManager(CoreService xmppSvc) {
        xs = xmppSvc;
        handler = new Handler();
        notificationPacketListener = new NotificationPacketListener(this);
        connectiongListener = new XmConnectionListener();
        reconnection = new ReconnectionThread(this);
    }

    public Context getContext() {
        return xs;
    }

    public PacketListener getNotificationPacketListener() {
        return notificationPacketListener;
    }

    public void connect() {
        Log.d(TAG, "connect()");
        if (!isConnected()) {
            if (isConnecting) {
                // Already request, wait for its finish
                Log.d(TAG, "connect(), already requested, waiting!");
            } else {
                Log.d(TAG, "connect(), request successful!");
                isConnecting = true;
                xs.submmitTaskInSingle(new XmConnectTask());
            }
        } else {
            Intent intent = new Intent();
            intent.setAction(CoreService.ACTION_CONNECT_OK);
            xs.sendBroadcast(intent);
        }
    }

    public void disconnect() {

    }

    public boolean isConnected() {
        bConn = (connection != null && connection.isConnected());
        Log.i(TAG, "isConnected(), bConn = "+bConn);
        return bConn;
    }

    public boolean isAuthenticated() {
        bAuth = (connection != null && connection.isConnected()
                && connection.isAuthenticated());
        Log.i(TAG, "isAuthenticated(), bAuth = "+bAuth);
        return bAuth;
    }
    
    public boolean isLogined() {
        return isAuthenticated();
    }

    public String getJidAfterLogin() {
        if (connection != null && connection.isConnected()
                && connection.isAuthenticated()) {
            return connection.getUser();
        } else {
            return "";
        }
    }

    public void registerUser() {
        Log.d(TAG, "registerUser()");
        connect();
        xs.submmitTaskInSingle(new XmRegisterTask());
    }

    public boolean isRegistered() {
        return false;//xs.getSharedPreferencesReg().contains("username")
                //&& xs.getSharedPreferencesReg().contains("password");
    }

    public void login() {
        Log.d(TAG, "login()");
        connect();

        if (!isAuthenticated()) {
            if (isLogining) {
                // Already request, wait for its finish
                Log.d(TAG, "login(), already requested, waiting!");
            } else {
                Log.d(TAG, "login(), request successful!");
                isLogining = true;
                xs.submmitTaskInSingle(new XmLoginTask());
            }
        } else {
            Intent intent = new Intent();
            intent.setAction(CoreService.ACTION_LOGIN_OK);
            xs.sendBroadcast(intent);
        }
    }

    public void logout() {

    }

    public void setProfiles() {
        Log.d(TAG, "setProfiles()");

        login();
        xs.submmitTaskInSingle(new XmProfilesTask());
    }

    public void updateRoster() {
        Log.v(TAG, "updateRoster()...");
        if (isConnected() && isAuthenticated()) {
            roster = connection.getRoster();
            roster.addRosterListener(new XmRosterListener());

            rosterEntries = roster.getEntries();
            for (RosterEntry entry : rosterEntries) {
                Log.v(TAG, "updateRoster(), entry:"+entry);
                String uid = entry.getUser();
                String name = entry.getName();
                String nick = entry.getName();
                String gender = "";
                String region = "";
                String group = "";
                String threadId = "";
                String portrait = "";
                int state = 0;

                xs.insertOrUpdateRoster(
                        uid, name, nick, 
                        gender, region, group, 
                        threadId, portrait, state);
            }
        } else {
            Log.e(TAG, "updateRoster(), Failed, please login first!!!");
        }
    }

    public void addFriendInRoster(String user, String name) {
        Log.v(TAG, "addFriendInRoster()..."+user+", "+name);
        if (isConnected() && isAuthenticated()) {
            roster = connection.getRoster();
            roster.addRosterListener(new XmRosterListener());

            try {
                roster.createEntry(user, name, null);
            } catch (XMPPException e) {
                e.printStackTrace();
            }

        } else {
            Log.e(TAG, "addFriendInRoster(), Failed, please login first!!!");
        }
    }

    public Collection<RosterEntry> getRosterEntries() {
        Log.v(TAG, "getRosterEntries()...");
        if (isConnected() && isAuthenticated()) {
            roster = connection.getRoster();
            roster.addRosterListener(new XmRosterListener());

            rosterEntries = roster.getEntries();
            for (RosterEntry entry : rosterEntries) {
                Log.v(TAG, "getRoster(), entry:"+entry);
            }
            return rosterEntries;
        } else {
            Log.e(TAG, "getRoster(), Failed, please login first!!!");
        }
        return null;
    }

    public String getUserName(String jid) {
        //if (isConnected() && isAuthenticated()) {
        //    roster = connection.getRoster();
        //    roster.addRosterListener(new XmRosterListener());
        //}

        jid = xs.guaranteeJidNoResource(jid);
        Log.d(TAG, "getUserName(), jid="+jid);

        if (roster == null)
            //return "anonymous";
            return xs.guaranteeUid(jid);

        RosterEntry entry = roster.getEntry(jid);

        if (entry == null)
            //return "anonymous";
            return xs.guaranteeUid(jid);

        Log.d(TAG, "getUserName(), entry=" + entry);
        return entry.getName();
    }

    private class XmRosterListener implements RosterListener {

        @Override
        public void entriesAdded(Collection<String> arg0) {
            Log.v(TAG, "XmRosterListener.entriesAdded()...");
            updateRoster();
        }

        @Override
        public void entriesDeleted(Collection<String> arg0) {
            Log.v(TAG, "XmRosterListener.entriesDeleted()...");
            updateRoster();
        }

        @Override
        public void entriesUpdated(Collection<String> arg0) {
            Log.v(TAG, "XmRosterListener.entriesUpdated()...");
            //updateRoster();
        }

        @Override
        public void presenceChanged(Presence arg0) {
            Log.v(TAG, "XmRosterListener.presenceChanged()...");
        }

    }

    private class XmConnectionListener implements ConnectionListener {

        @Override
        public void connectionClosed() {
            // TODO Auto-generated method stub
            Log.i(TAG, "XmConnectionListener.connectionClosed()...");
            connection = null;
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            // TODO Auto-generated method stub
            Log.i(TAG, "XmConnectionListener.connectionClosedOnError()..."+e);

            //if (connection != null
            //		&& connection.isConnected()) {
            //	connection.disconnect();
            //    connection = null;
            //}
            connection = null;

            xs.testIfReconnct();
        }

        @Override
        public void reconnectingIn(int e) {
            // TODO Auto-generated method stub
            Log.i(TAG, "XmConnectionListener.reconnectingIn()..."+e);
        }

        @Override
        public void reconnectionFailed(Exception e) {
            // TODO Auto-generated method stub
            Log.i(TAG, "XmConnectionListener.reconnectionFailed()..."+e);
        }

        @Override
        public void reconnectionSuccessful() {
            // TODO Auto-generated method stub
            Log.i(TAG, "XmConnectionListener.reconnectionSuccessful()...");
        }

    }

    public void startReconnectionThread() {
        //if (reconnection != null) {
        //	reconnection.interrupt();
        //}
        //reconnection = new ReconnectionThread(this);

        synchronized (ReconnectionThread.class) {
            Log.d(TAG, "startReconnectionThread(), "+reconnection.isAlive()+", " + reconnection.getState());
            if (!reconnection.isAlive()) {
                if (reconnection.getState() == Thread.State.TERMINATED) {
                    //reconnection.run();
                    reconnection = new ReconnectionThread(this);
                    reconnection.setName("Xmpp Reconnection Thread");
                    reconnection.start();
                } else if (reconnection.getState() == Thread.State.NEW) {
                    reconnection.setName("Xmpp Reconnection Thread");
                    reconnection.start();
                } else {
                    Log.e(TAG, "startReconnectionThread(), SHOULD NOT run here!!!");
                }
            } else {
                Log.d(TAG, "startReconnectionThread(), no more reqeust, just waiting");
            }
        }
    }

    public void stopReconnectionThread() {
        synchronized (ReconnectionThread.class) {
            if (reconnection.isAlive()) {
                reconnection.interrupt();
            }
        }
    }

    private static class ReconnectionThread extends Thread {
        private static final String TAG = ReconnectionThread.class.getSimpleName();

        private final XmppManager xmppManager;
        private int times;

        ReconnectionThread(XmppManager xmppManager) {
            this.xmppManager = xmppManager;
            this.times = 0;
        }

        public void run() {
            try {
                while (!(isInterrupted() || (xmppManager.isConnected() && xmppManager.isAuthenticated()))) {
                    Log.d(TAG, "Trying to reconnect in " + waiting() + " seconds");
                    Thread.sleep((long) waiting() * 1000L);

                    xmppManager.login();
                    times++;
                }
                Log.e(TAG, "Not trying to reconnect any more, interrupted!!(1)");
            } catch (final InterruptedException e) {
                Log.e(TAG, "Not trying to reconnect any more, interrupted!!(2)");
            } finally {
                this.xmppManager.handler.post(new Runnable() {
                    public void run() {
                        if (xmppManager.isLogined()) {
                            xmppManager.connectiongListener.reconnectionSuccessful();
                        } else {
                            xmppManager.connectiongListener.reconnectionFailed(new Exception("Interrupted!!!(But not logined in by now)"));
                        }
                    }
                });
                this.times = 0;
            }
        }

        /**
         * To calculate the time to wait.
         *     If times = 0, waiting 3s;
         *     If 5 >= times >= 1, waiting 5s;
         *     If 15 >= times >= 6, waiting 10s;
         *     If 25 >= times >= 16, waiting 60s;
         *     If times > 25, waiting 300s;
         *
         * @return the time to wait in Sec.
         */
        private int waiting() {
            if (times > 20) { // times > 20
                return 300;
            } else if (times > 15) { // 20 >= times >= 16
                return 60;
            } else if (times == 0) { // times == 0
                return 3;
            } else { // 15 >= times > 0
                return times <= 5 ? 5 : 10;
            }
        }
    }

    /**
     * A runnable task to connect the server. 
     */
    private class XmConnectTask implements Callable<Integer> {

        final XmppManager xm;

        private XmConnectTask() {
            this.xm = XmppManager.this;
        }

        public Integer call() {
            Log.i(TAG, "XmConnectTask.call()...");

            if (!xm.isConnected()) {
                // Create the configuration for this new connection
                ConnectionConfiguration connConfig = new ConnectionConfiguration(
                        CoreService.getServer(), CoreService.getPort());

                
                //connConfig.setSecurityMode(SecurityMode.required);

                connConfig.setSecurityMode(SecurityMode.disabled);
                connConfig.setSASLAuthenticationEnabled(false);
                connConfig.setCompressionEnabled(false);

                connConfig.setDebuggerEnabled(true);

                connConfig.setRosterLoadedAtLogin(bLoadRosterAtLogin);

                connection = new XMPPConnection(connConfig);

                try {
                    // connection listener
                    connection.addConnectionListener(connectiongListener);
                    // Connect to the server
                    connection.connect();

                    // packet provider
                    ProviderManager.getInstance().addIQProvider("notification",
                            "jabber:iq:notification",
                            new NotificationIQProvider());

                    xs.setServerHost(xm.connection.getHost());
                    // packet provider
                    //ProviderManager.getInstance().addIQProvider("notification",
                    //		"SmartCommunity:iq:notification",
                    //		new XmppManagerIQProvider());

                } catch (XMPPException e) {
                    Log.e(TAG, "XMPP connection failed!!!", e);

                    if (false) {
                        Intent intent = new Intent();
                        intent.setAction(CoreService.ACTION_CONNECT_FAIL);
                        intent.putExtra("connect_error", e.getMessage());
                        xs.sendBroadcast(intent);
                    } else {
                        xs.testIfReconnct();
                    }

                    isConnecting = false;
                    return -1;
                }

                Log.i(TAG, "XMPP connected successfully!!! "+connection);
                Intent intent = new Intent();
                intent.setAction(CoreService.ACTION_CONNECT_OK);
                xs.sendBroadcast(intent);
            } else {
                Log.i(TAG, "XMPP connected already!!!");
            }

            isConnecting = false;
            return 0;
        }
    }

    /**
     * A runnable task to register a new user onto the server. 
     */
    private class XmRegisterTask implements Callable<Integer> {

        final XmppManager xm;

        private XmRegisterTask() {
            xm = XmppManager.this;
        }

        public Integer call() {
            Log.i(TAG, "XmRegisterTask.run()...");

            if (!xm.isConnected()) {
                Log.i(TAG, "XmRegisterTask.run(), NOT connected, return.");
                //Intent intent = new Intent();
                //intent.setAction(CoreService.ACTION_REGISTER_FAIL);
                //xs.sendBroadcast(intent);
                return -1;
            }

            //if (!xm.isRegistered()) {
            /*Account acc = xs.getAccount();
            final String newUsername = acc.account;
            final String newName = acc.nick;
            final String newPassword = acc.pwd;*/
            //final String newUsername = xs.getSharedPreferencesReg().getString("username", "");
            //final String newName = xs.getSharedPreferencesReg().getString("name", "");
            //final String newPassword = xs.getSharedPreferencesReg().getString("password", "");

            final String newUsername = xs.getRegUser();
            final String newName = xs.getRegName();
            final String newPassword = xs.getRegPwd();
            if (newUsername == null || newUsername.isEmpty()
                    || newName == null || newName.isEmpty()
                    || newPassword == null || newPassword.isEmpty()) {
                Toast.makeText(xs, R.string.account_register_should_not_empty, Toast.LENGTH_LONG).show();
                return -1;
            }

            Registration registration = new Registration();

            PacketFilter packetFilter = new AndFilter(new PacketIDFilter(
                    registration.getPacketID()), new PacketTypeFilter(
                            IQ.class));

            PacketListener packetListener = new PacketListener() {

                public void processPacket(Packet packet) {
                    Log.d(TAG, "XmRegisterTask.PacketListener, "+
                            "processPacket()...");
                    Log.d(TAG, "XmRegisterTask.PacketListener, "+"packet="
                            + packet.toXML());

                    if (packet instanceof IQ) {
                        IQ response = (IQ) packet;
                        if (response.getType() == IQ.Type.ERROR) {
                            if (!response.getError().toString().contains(
                                    "409")) {
                                Log.e(TAG,
                                        "Unknown error while registering XMPP account! (1)"
                                                + response.getError()
                                                .getCondition());
                            }
                            else {
                                Log.e(TAG,
                                        "Unknown error while registering XMPP account! (2)"
                                                + response.getError()
                                                .getCondition());
                            }

                            //Toast.makeText(xs, "Register failed: "+response.getError().getCondition(), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent();
                            intent.setAction(CoreService.ACTION_REGISTER_FAIL);
                            intent.putExtra("register_error", response.getError().getCondition());
                            xs.sendBroadcast(intent);

                        } else if (response.getType() == IQ.Type.RESULT) {

                            Log.d(TAG, "username=" + newUsername);
                            Log.d(TAG, "password=" + newPassword);

                            //Editor editor = xs.getSharedPreferences().edit();
                            //editor.putString("username",
                            //		newUsername);
                            //editor.putString("password",
                            //		newPassword);
                            //editor.commit();
                            Intent intent = new Intent();
                            intent.setAction(CoreService.ACTION_REGISTER_OK);
                            xs.sendBroadcast(intent);
                            Log.i(TAG, "Account registered successfully!!!");
                        }
                    }
                }
            };

            connection.addPacketListener(packetListener, packetFilter);

            registration.setType(IQ.Type.SET);
            registration.addAttribute("username", newUsername);
            registration.addAttribute("name", newName);
            registration.addAttribute("password", newPassword);
            connection.sendPacket(registration);

            //} else {
            //	Log.i(TAG, "Account registered already!!!");
            //}
            return 0;
        }
    }

    /**
     * A runnable task to register a new user onto the server. 
     */
    private class XmProfilesTask implements Callable<Integer> {

        final XmppManager xm;

        private XmProfilesTask() {
            xm = XmppManager.this;
        }

        public Integer call() {
            Log.i(TAG, "XmProfilesTask.run()...");

            if (!xm.isConnected()) {
                Log.i(TAG, "XmProfilesTask.run(), NOT connected, return.");
                return -1;
            }

            if (!xm.isAuthenticated()) {
                Log.i(TAG, "XmProfilesTask.run(), NOT authenticated, return.");
                return -1;
            }

            final String username = xs.getChangeUser(); 
            final String name = xs.getChangeName();
            final String password = xs.getChangeCurrentPwd();
            final String newpwd = xs.getChangeNewPwd();

            /*final String username = xs.getSharedPreferencesLogin().getString("username", "");
            final String name = xs.getSharedPreferencesLogin().getString("name", "");
            final String password = xs.getSharedPreferencesLogin().getString("password", "");
            final String newpwd = xs.getSharedPreferencesLogin().getString("newpwd", "");
            final String newEmail = xs.getSharedPreferencesLogin().getString("email", "q.h.zhang@samsung.com");
            final String newRelation = xs.getSharedPreferencesLogin().getString("relation", "");
            final String newGroup = xs.getSharedPreferencesLogin().getString("group", "");
            final String newPortrait = xs.getSharedPreferencesLogin().getString("protrait", "");*/
            if (username == null || username.isEmpty()
                    || password == null || password.isEmpty()
                    //|| newpwd == null || newpwd.isEmpty()
                    //|| name == null || name.isEmpty()
                    //|| newRelation == null || newRelation.isEmpty()
                    //|| newGroup == null || newGroup.isEmpty()
                    //|| newPortrait == null || newPortrait.isEmpty()
                    ) {
                Toast.makeText(xs, R.string.account_register_should_not_empty, Toast.LENGTH_LONG).show();
                return -1;
            }

            Profiles profile = new Profiles();
            PacketFilter packetFilter = new AndFilter(new PacketIDFilter(
                    profile.getPacketID()), new PacketTypeFilter(
                            IQ.class));

            PacketListener packetListener = new PacketListener() {

                public void processPacket(Packet packet) {
                    Log.d(TAG, "XmProfilesTask.PacketListener, "+
                            "processPacket()...");
                    Log.d(TAG, "XmProfilesTask.PacketListener, "+"packet="
                            + packet.toXML());

                    if (packet instanceof IQ) {
                        IQ response = (IQ) packet;
                        if (response.getType() == IQ.Type.ERROR) {
                            if (!response.getError().toString().contains(
                                    "409")) {
                                Log.e(TAG,
                                        "Unknown error while set profiles for XMPP account! (1)"
                                                + response.getError()
                                                .getCondition());
                            }
                            else {
                                Log.e(TAG,
                                        "Unknown error while set profiles for XMPP account! (2)"
                                                + response.getError()
                                                .getCondition());
                            }

                            //Toast.makeText(xs, "Register failed: "+response.getError().getCondition(), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent();
                            intent.setAction(CoreService.ACTION_PROFILE_FAIL);
                            intent.putExtra("profiles_error", response.getError().getCondition());
                            xs.sendBroadcast(intent);

                        } else if (response.getType() == IQ.Type.RESULT) {

                            Log.d(TAG, "username=" + username);
                            Log.d(TAG, "password=" + password);

                            Intent intent = new Intent();
                            intent.setAction(CoreService.ACTION_PROFILE_OK);
                            xs.sendBroadcast(intent);
                            Log.i(TAG, "Account set profiles successfully!!!");
                        }
                    }
                }
            };
            connection.addPacketListener(packetListener, packetFilter);

            String digest;
            Authentication auth = new Authentication();
            auth.setDigest(connection.getConnectionID(), password);
            digest = auth.getDigest();

            profile.setType(IQ.Type.SET);
            profile.addAttribute("username", username);
            profile.addAttribute("name", name);
            if (digest != null && !digest.isEmpty()) {
                profile.addAttribute("digest", digest);
            } else {
                profile.addAttribute("password", password);
            }
            profile.addAttribute("newpwd", newpwd);
            //profile.addAttribute("email", newEmail);
            //profile.addAttribute("portrait", newPortrait);
            //profile.addAttribute("relation", newRelation);
            //profile.addAttribute("group", newGroup);

            Log.d(TAG, "XmProfilesTask.run(), profile=" + profile.toXML());
            connection.sendPacket(profile);

            return 0;
        }
    }

    /**
     * A runnable task to log into the server. 
     */
    private String user;
    private class XmLoginTask implements Callable<Integer> {

        final XmppManager xm;

        private XmLoginTask() {
            this.xm = XmppManager.this;
        }

        public Integer call() {
            Log.i(TAG, "XmLoginTask.run()...");

            if (!xm.isConnected()) {
                Log.e(TAG, "XmLoginTask.run(), to log in before connected, failed!!!");
                isLogining = false;
                return -1;
            } else if (!xm.isAuthenticated()) {
                Account account = xs.getAccount();
                if (account == null) {
                    Log.e(TAG, "XmLoginTask.run(), Account in DB should not be null!!!");

                    Intent intent = new Intent();
                    intent.setAction(CoreService.ACTION_LOGIN_FAIL);
                    intent.putExtra("login_error", xs.getString(R.string.login_failed_null));
                    xs.sendBroadcast(intent);
                    isLogining = false;
                    return -1;
                }
                
                final String username = account.account;
                final String password = account.pwd;

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Log.e(TAG, "XmLoginTask.run(), Username and password should not be null!!!");

                    Intent intent = new Intent();
                    intent.setAction(CoreService.ACTION_LOGIN_FAIL);
                    intent.putExtra("login_error", xs.getString(R.string.login_failed_null));
                    xs.sendBroadcast(intent);
                    isLogining = false;
                    return -1;
                }

                user = username;

                Log.d(TAG, "XmLoginTask.run(), username=" + username);
                Log.d(TAG, "XmLoginTask.run(), password=" + password);

                try {
                    xm.connection.login(
                            username,
                            password,
                            /*Locator.getInstance(xs).getMessageGroupId()*/CoreService.SC_RESOURCE_ID);
                    Log.d(TAG, "XmLoginTask.run(), Logged in successfully!!!");

                    // packet filter
                    PacketFilter packetFilter = new PacketTypeFilter(
                            NotificationIQ.class);
                    // packet listener
                    PacketListener packetListener = xm
                            .getNotificationPacketListener();
                    connection.addPacketListener(packetListener, packetFilter);

                } catch (XMPPException e) {
                    Log.e(TAG, "XmLoginTask.run()... xmpp error");
                    Log.e(TAG, "XmLoginTask.run(), Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    String INVALID_CREDENTIALS_ERROR_CODE = "401";
                    String errorMessage = e.getMessage();
                    if (errorMessage != null
                            && errorMessage
                            .contains(INVALID_CREDENTIALS_ERROR_CODE)) {
                        //xm.reregisterAccount();
                    } else {
                        //xm.startReconnect();
                    }

                    Intent intent = new Intent();
                    intent.setAction(CoreService.ACTION_LOGIN_FAIL);
                    intent.putExtra("login_error", e.getXMPPError().getCondition()/*e.getMessage()*/);
                    xs.sendBroadcast(intent);
                    isLogining = false;
                    return -1;
                } catch (Exception e) {
                    Log.e(TAG, "XmLoginTask.run()... other error");
                    Log.e(TAG, "XmLoginTask.run(), Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    //xm.startReconnect();

                    Intent intent = new Intent();
                    intent.setAction(CoreService.ACTION_LOGIN_FAIL);
                    intent.putExtra("login_error", e.getMessage());
                    xs.sendBroadcast(intent);
                    isLogining = false;
                    return -1;
                }

                Log.d(TAG, "XmLoginTask.run(), XMPP login successfully!!!");
                synchronized (reconnection) {
                    if (reconnection.isAlive()) {
                        Log.d(TAG, "XmLoginTask.run(), Interrupt reconnection thread!");
                        reconnection.interrupt();
                    }
                }
                
                Intent intent = new Intent();
                intent.setAction(CoreService.ACTION_LOGIN_OK);
                xs.sendBroadcast(intent);
                //xs.setAccount();

                if (chatManager == null)
                    chatManager = connection.getChatManager();
                chatManager.addChatListener(new XmChatManagerListener());

            } else {
                Log.d(TAG, "XmLoginTask.run(), Logged in already!!!");
            }

            isLogining = false;
            return 0;
        }
    }

    public void chatWith(String jid, String content) {
        chatWith(jid, null, null, content, null);
    }

    private final HashMap<String, PendingIntent> mPi = new HashMap<String, PendingIntent>();
    public String chatWith(String jid, String thread, File file, String content, PendingIntent pi) {
        Log.v(TAG, "chatWith()...jid="+jid+", thread="+thread+", contents="+content);
        Chat newChat = null;
        Message msg = null;
        if (isConnected() && isAuthenticated()) {
            Log.v(TAG, "chatWith()..."+connection+", "+chatManager);

            //Always to fetch current ChatManager from Connection.
            //if (chatManager == null)
                chatManager = connection.getChatManager();

            if (thread != null) {
                newChat = chatManager.getThreadChat(thread);
                Log.v(TAG, "chatWith()...getThreadChat="+newChat);
            }
            if (newChat == null) {
                newChat = chatManager.createChat(jid, new XmMessageListener());
                Log.v(TAG, "chatWith()...createChat="+newChat);
            }

            try {
                if (file != null) {
                    msg = new Message(jid, Message.Type.file);

                    //EX. content-type: text/plain
                    String mime = MediaFile.getMimeType(file.getAbsolutePath());
                    Message.File mf = new Message.File(file.getName(), 
                            String.valueOf(file.length()), 
                            String.valueOf(file.hashCode()), 
                            mime);
                    
                    msg.setFile(mf);
                    msg.setFileMethod(Message.FileMethod.upload);
                    msg.setBody(content);
                    
                    mPi.put(msg.getPacketID(), pi);
                    
                    Log.v(TAG, "chatWith()...to send file, "+mPi);
                } else {
                    //msg = newChat.createMessage();
                    msg = new Message(jid, Message.Type.chat);
                    msg.setBody(content);
                }
                newChat.sendMessage(msg);

                String threadId = newChat.getThreadID();
                //String name = newChat.getParticipant();
                String name = getUserName(jid);
                String xml = msg.toXML();
                Log.i(TAG, "participant="+jid+", threadId="+threadId+", name="+name);
                Log.i(TAG, "xml="+xml);

                xs.updateThread(jid, threadId, name, msg.getBody(), -1, true);
                xs.storeRawMessage(msg.getPacketID(), msg.getTo(), msg.getFrom(), 
                        msg.getType().toString(), msg.getBody(), msg.getThread(), xml, true);

                Intent intent = new Intent();
                intent.putExtra("MSG_CONTENT", msg.getBody());
                intent.setAction(CoreService.ACTION_MSG_STATE_CHANGE);
                xs.sendBroadcast(intent);
            }
            catch (XMPPException e) {
                Log.e(TAG, "chatWith(), Error: delivering blocked!!!");
            }
        } else {
            Log.e(TAG, "chatWith(), Failed, please check login status!!!");
        }
        
        String ret = "";
        if (msg != null) {
            ret = msg.getPacketID();
        }
        return ret;
    }

    private class XmChatManagerListener implements ChatManagerListener {
        @Override
        public void chatCreated(Chat chat, boolean createdLocally)
        {
            Log.i(TAG, "XmChatManagerListener.chatCreated()...");
            if (!createdLocally) {
                chat.addMessageListener(new XmMessageListener());
            }
        }
    }

    private class XmMessageListener implements MessageListener {
        @Override
        public void processMessage(Chat chat, Message msg) {
            Log.v(TAG, "XmMessageListener.processMessage(), Received message: xml="+msg.toXML());
            if (msg.getType() == Type.file) {
                try {
                    Log.v(TAG, "XmMessageListener.processMessage(), \""+msg.getPacketID()+"\" matched "+mPi.containsKey(msg.getPacketID()));
                    if (msg.getFileMethod() == Message.FileMethod.uploading
                            && mPi.containsKey(msg.getPacketID())) {
                        mPi.get(msg.getPacketID()).send();
                        mPi.remove(msg.getPacketID());
                        return;
                    } else if (msg.getFileMethod() == Message.FileMethod.download) {
                        Message.File file = msg.getFile();
                        xs.receiveFile(msg.getPacketID(), file.getName(), file.getSize(), file.getType(), file.getHash());
                    }
                } catch (CanceledException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //return;
            }
            
            //<message id="74f4fd0d" to="zhqh" from="zhqh@catination.com/guangdong" type="groupchat"><body>Hello</body><thread>0K14q0</thread></message>
            String participant1 = msg.getFrom();
            String xml = msg.toXML();
            String participant2 = chat.getParticipant();
            Log.v(TAG, "XmMessageListener.processMessage(), p1="+participant1+", p2="+participant2);
            String threadId = chat.getThreadID();
            //String name = newChat.getParticipant();
            String name = getUserName(participant1);

            /*
             * 
            Message.Type:
            chat 
                      Typically short text message used in line-by-line chat interfaces. 
            error 
                      indicates a messaging error. 
            groupchat 
                      Chat message sent to a groupchat server for group chats. 
            headline 
                      Text message to be displayed in scrolling marquee displays. 
            normal 
                      (Default) a normal text message used in email like interface. 
             * 
             */
            String group = xs.getMessageGroupId();//msg.getFrom().substring(msg.getFrom().indexOf("/")+1);
            if (msg.getType() == Type.groupchat) {
                xs.storeGroupThread(participant1, group, name, msg.getBody(), false);
                xs.storeGroupRawMessage(msg.getPacketID(), group, msg.getFrom(), 
                        msg.getType().toString(), msg.getBody(), msg.getThread(), xml, false);
            } else {//if (msg.getType() == Type.chat) {
                xs.updateThread(participant1, threadId, name, msg.getBody(), -1, false);
                xs.storeRawMessage(msg.getPacketID(), msg.getTo(), msg.getFrom(), 
                        msg.getType().toString(), msg.getBody(), msg.getThread(), xml, false);
            }

            Intent intent = new Intent();
            //intent.putExtra("MSG_TYPE", msg.getType());
            if (msg.getType() == Type.groupchat) {
                intent.putExtra("MSG_TYPE", "groupchat");
                intent.putExtra("MSG_GROUP", group);
                intent.putExtra("MSG_GROUPNAME", name);
            } else {
                intent.putExtra("MSG_TYPE", "chat");
                intent.putExtra("MSG_USER", participant1);
                intent.putExtra("MSG_NAME", name);
            }
            intent.putExtra("MSG_CONTENT", msg.getBody());
            intent.setAction(CoreService.ACTION_RECEIVE_NEW_MSG);
            xs.sendBroadcast(intent);
            xs.updateBadge();
        }
    }

    public void chatInRoom(String room, String content) {
        if (chatManager == null)
            chatManager = connection.getChatManager();

        //if (muc == null) { // Allow to change the room.
        muc = new MultiUserChat(connection, room);
        //}
        muc.addMessageListener(multiUserChatPacketListener);
        Message msg = muc.createMessage();
        msg.addBody(null, content);

        try {
            Log.d(TAG, "chatInRoom(), msg="+msg.toXML());
            muc.sendMessage(msg);
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //<message id="14t78-4" to="beijing" type="groupchat"><body>[生气]</body></message>
        xs.storeGroupThread(connection.getUser(), msg.getTo(), getUserName(msg.getTo()), msg.getBody(), true);
        xs.storeGroupRawMessage(msg.getPacketID(), msg.getTo(), connection.getUser(), 
                msg.getType().toString(), msg.getBody(), msg.getThread(), msg.toXML(), true);

        Intent intent = new Intent();
        intent.putExtra("MSG_CONTENT", msg.getBody());
        intent.setAction(CoreService.ACTION_MSG_STATE_CHANGE);
        xs.sendBroadcast(intent);
    }

    PacketListener multiUserChatPacketListener = new PacketListener() {

        public void processPacket(Packet packet) {
            Log.d(TAG, "MultiUserChat.PacketListener, "+
                    "processPacket()...");
            Log.d(TAG, "MultiUserChat.PacketListener, "+"packet="
                    + packet.toXML());

            if (packet instanceof IQ) {
                IQ response = (IQ) packet;
                if (response.getType() == IQ.Type.ERROR) {
                    Log.e(TAG,
                            "MultiUserChat: Unknown error while..."
                                    + response.getError()
                                    .getCondition());
                } else if (response.getType() == IQ.Type.RESULT) {
                    /*Intent intent = new Intent();
                    intent.setAction(CoreService.ACTION_REGISTER_OK);
                    xs.sendBroadcast(intent);*/
                    Log.i(TAG, "MultiUserChat: OK...");
                }
            }
        }
    };

    FileTransferManager ftm;
    public void sendFile(File file, String fileName) {
        if (connection == null) {
            Log.e(TAG, "sendFile(), connection == null!!!");
            return;
        }

        if (ftm == null) {
            ftm = new FileTransferManager(connection);
        }

        Log.i(TAG, "sendFile(), File:"+file+", Desc="+fileName);

        String test = "test1@"+ CoreService.getServer()+"/"+ CoreService.SC_RESOURCE_ID;
        OutgoingFileTransfer oft = ftm.createOutgoingFileTransfer(test);
        Log.i(TAG, "sendFile(), oft="+oft+", test="+test);
        try {
            oft.sendFile(file, fileName);

            Log.i(TAG, "sendFile(), oft="+oft+", "+oft.getStatus()+", 进度: "+oft.getProgress());
            while (!oft.isDone()){
                if (oft.getStatus().equals(Status.error)){
                    Log.i(TAG, "ERROR!!! " + oft.getError());
                } else {
                    Log.i(TAG, oft.getStatus()+"进度: "+oft.getProgress());
                }
                Thread.sleep(100);
            }
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (oft.getStatus().equals(Status.error)) {
            } else if (oft.getStatus().equals(Status.complete)) {
            } else if (oft.getStatus().equals(Status.cancelled)) {
            }
            Log.i(TAG, "sendFile(), status:"+oft.getStatus());
        }
    }

    
    public void setReceiveFile(){
        if (connection == null) {
            Log.e(TAG, "setReceiveFile(), connection == null!!!");
            return;
        }

        if (ftm == null) {
            ftm = new FileTransferManager(connection);
        }

        Log.i(TAG, "setReceiveFile(), ftm:"+ftm);
        ftm.addFileTransferListener(new FileTransferListener(){
            @Override
            public void fileTransferRequest(FileTransferRequest request) {
                Log.i(TAG, "fileTransferRequest(), request="+request);
                int respone = 0; //Just accept.
                if (respone == 0){
                    try {
                        // Accept it
                        IncomingFileTransfer ift = request.accept();
                        File file = new File(request.getFileName());
                        ift.recieveFile(file);
                        
                        Log.i(TAG, "receiveFile(), ift="+ift+", "+ift.getStatus()+", 进度: "+ift.getProgress());
                        while (!ift.isDone()){
                            if (ift.getStatus().equals(Status.error)){
                                Log.i(TAG, "ERROR!!! " + ift.getError());
                            } else {
                                Log.i(TAG, ift.getStatus()+"进度: "+ift.getProgress());
                            }
                            Thread.sleep(100);
                        }
                    } catch (XMPPException ex) {
                        Log.e(TAG, "fileTransferRequest(), request="+request);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    request.reject();
                }
            }
        });
    }
    
    public void loadRoster() {
        if (connection == null) {
            Log.e(TAG, "loadRoster() failed, connection="+connection);
        }
        
        connection.getRoster().reload();
    }
    
    private boolean bLoadRosterAtLogin;
    public void setLoadRosterAtLogin(boolean load) {
        if (connection != null) {
            Log.e(TAG, "setLoadRosterAtLogin() failed, connection="+connection);
        }
        
        bLoadRosterAtLogin = load;
        Log.d(TAG, "setLoadRosterAtLogin(), bLoadRosterAtLogin="+bLoadRosterAtLogin);
    }
    
    public void sendHeartBeat(String childXml) {
        if (!isConnected()) {
            Log.e(TAG, "sendHeartBeat() failed, connection="+connection);
            return;
        }
        
        /*try {
            connection.getSocket().sendUrgentData(0xFF);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        HeartBeatMessage pkt = new HeartBeatMessage(childXml);
        
        connection.sendPacket(pkt);
        Log.e(TAG, "sendHeartBeat() OK!");
    }
}
