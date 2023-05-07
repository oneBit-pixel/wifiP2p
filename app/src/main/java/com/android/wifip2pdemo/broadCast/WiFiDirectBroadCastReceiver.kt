package com.android.wifip2pdemo.broadCast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.wifip2pdemo.viewModel.ChooseState
import com.android.wifip2pdemo.viewModel.WifiP2pViewModel
import com.android.wifip2pdemo.viewModel.WifiState
import com.blankj.utilcode.util.LogUtils


/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val viewModel: WifiP2pViewModel
) : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION -> {
                LogUtils.i("搜索状态改变了...")
                val state = intent.getIntExtra(
                    WifiP2pManager.EXTRA_DISCOVERY_STATE,
                    WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED
                )
                when (state) {
                    WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED -> {
                        LogUtils.i("开始搜索...")
                        viewModel.setState(WifiState.WIFI_P2P_DISCOVERY_STARTED)
                    }
                    WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED -> {
                        LogUtils.i("搜索停止...")
                        viewModel.setState(WifiState.WIFI_P2P_DISCOVERY_STOPPED)
                    }
                    else -> {}
                }
            }
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                LogUtils.i("123状态改变...")
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                viewModel.setState(WifiState.WIFI_P2P_PEERS_CHANGED_ACTION)
                manager.apply {
                    requestPeers(channel) { peers ->
                        viewModel.addPeer(peers.deviceList.toList())
                    }
                }

            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Respond to new connection or disconnections
                viewModel.setState(WifiState.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                LogUtils.i("连接状态改变...")
                val networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo?

                if (networkInfo != null) {
                    viewModel.setConnectState(networkInfo.isConnected)
                }

                manager?.requestConnectionInfo(channel) { info ->
                    val address = info.groupOwnerAddress
                    val groupOwner = info.isGroupOwner
                    val formed = info.groupFormed
                    println("是否形成组==>$formed")
                    println("是否为组长==>${groupOwner}")
                    println("连接信息==>${address}")
                    if (!groupOwner && formed) {
                        val host = address.hostAddress
                        viewModel.connectSocket(host)
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    manager.requestDeviceInfo(
//                        channel
//                    ) { device ->
//                        LogUtils.i("本机状态改变..."+device?.isGroupOwner)
//                        viewModel.mDevice = device
//                    }
//                } else {
//                    val device =
//                        intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
//                    LogUtils.i("本机状态改变..."+device?.isGroupOwner)
//                    viewModel.mDevice = device
//                }

            }
            else -> {

            }

        }
    }
}