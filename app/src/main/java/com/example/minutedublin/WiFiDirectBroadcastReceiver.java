package com.example.minutedublin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.Tag;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//notifies us about events of wifi
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Wifip2p mActivity;
    public static final String TAG = "NGVL";

    public WiFiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, Wifip2p mActivity) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mActivity = mActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        //indicates whether p2p is enabled
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                Toast.makeText(context, "Wifi Is On", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(context, "Wifi Is Off", Toast.LENGTH_SHORT).show();
            }
        }

        //indicates available peer list change
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "onReceive::WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION");
            if (mManager != null)
            {
                mManager.requestPeers(mChannel,mActivity.peerListListener);
            }


// Call WifiP2pManager.requestPeers() to get a list of current peers
// request available peers from the wifi p2p manager. This is an
// asynchronous call and the calling activity is notified with a
// callback on PeerListListener.onPeersAvailable()
//                Toast.makeText(context, "BBBBBBBBBB", Toast.LENGTH_SHORT).show();
//                WifiP2pDeviceList list = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
//                for (WifiP2pDevice d : list.getDeviceList()) { //...
//                    Toast.makeText(context, d.deviceAddress, Toast.LENGTH_SHORT).show();
//                }

        }

        //state of p2p connectivity change
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null)
            {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected())
            {
                mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
            } else {
                mActivity.ConnectionStatus.setText("Device Disconnected");
            }

        }

        //device configuration details change
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //do something

        }
    }
}

