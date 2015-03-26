package com.cmpt276.meetly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by AlexLand on 15-03-25.
 */
public class WifiP2pHelper {
    private final String TAG = "WifiP2pHelper";
    private static final int SERVER_PORT = 1337;
    private IntentFilter intentFilter;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private boolean wifiP2pEnabled;
    private WifiBroadcastReceiver mReceiver;
    private WifiP2pDeviceList mPeers;
    private MainActivity mActivity;
    private HashMap<String, String> buddies;
    private ArrayList<WifiP2pDevice> devices;

    public WifiP2pHelper(MainActivity activity, Context context, IntentFilter intentFilter) {
        mActivity = activity;
        mManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, activity.getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, mActivity);
//
//        //  Indicates a change in the Wi-Fi P2P status.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        // Indicates a change in the list of available peers.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        // Indicates the state of Wi-Fi P2P connectivity has changed.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        // Indicates this device's details have changed.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//
//        this.intentFilter = intentFilter;

        registerWifiService();
        setResponseListeners();
        addServiceRequest();
        discoverServices();
    }


    private void registerWifiService() {
        Map record = new HashMap();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", mActivity.getString(R.string.app_name) + (int) (Math.random() * 1000));

        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("Meetly Event Discovery",
                "_presence._tcp", record);

        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Service registered");
            }

            @Override
            public void onFailure(int reason) {
                if (reason == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.i(TAG, "Service failed to register because it is unsupported");
                }
                else if (reason == WifiP2pManager.BUSY) {
                    Log.i(TAG, "Service failed to register because it is busy");
                }
                else if (reason == WifiP2pManager.ERROR) {
                    Log.i(TAG, "Service failed to register due to an error");
                }
            }
        });
    }

    private void setResponseListeners() {
        WifiP2pManager.DnsSdTxtRecordListener txtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                Log.d(TAG, txtRecordMap.toString());
                buddies.put(srcDevice.deviceAddress, txtRecordMap.get("buddyname"));
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                srcDevice.deviceName = buddies
                        .containsKey(srcDevice.deviceAddress) ? buddies
                        .get(srcDevice.deviceAddress) : srcDevice.deviceName;

                devices.add(srcDevice);
                Log.d(TAG, "Added " + srcDevice.deviceName + " to devices");
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, serviceResponseListener, txtRecordListener);
    }


    private void addServiceRequest() {
        mManager.addServiceRequest(mChannel, WifiP2pDnsSdServiceRequest.newInstance(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Service request added");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Failed to add service request");
            }
        });
    }

    private void discoverServices() {
        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Discovered service");
            }

            @Override
            public void onFailure(int reason) {
                if (reason == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(TAG, "P2P isn't supported on this device.");
                }
            }
        });
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

    public WifiP2pManager getManager() {
        return mManager;
    }
}
