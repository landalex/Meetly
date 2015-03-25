package com.cmpt276.meetly;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by AlexLand on 15-03-25.
 */
public class WifiP2pHelper {
    private final String TAG = "WifiP2pHelper";
    private IntentFilter intentFilter;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private boolean wifiP2pEnabled;
    private WifiBroadcastReceiver mReceiver;
    private WifiP2pDeviceList mPeers;

    public WifiP2pHelper(MainActivity activity, Context context, IntentFilter intentFilter) {
        mManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, activity.getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, activity);

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        this.intentFilter = intentFilter;
    }

    protected void getWifiPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Wifi discoverPeers success");
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Log.i(TAG, "Wifi requestPeers success");
                        mPeers = peers;
                    }
                });
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    protected void connectToPeer(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        final String deviceName = device.deviceName;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG, "Connected successfully to " + deviceName);
            }

            @Override
            public void onFailure(int reason) {
                Log.i(TAG, "Connection failed to " + deviceName);
            }
        });
    }

    public WifiP2pDeviceList getPeersList() {
        return mPeers;
    }

    public BroadcastReceiver getReceiver() {
        return mReceiver;
    }
}
