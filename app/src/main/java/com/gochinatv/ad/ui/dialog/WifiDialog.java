package com.gochinatv.ad.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.gochinatv.ad.R;
import com.gochinatv.ad.interfaces.OnWifiConnectListener;
import com.gochinatv.ad.tools.AlertUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.WifiAutoConnectManager;

import java.util.ArrayList;


/**
 * Created by zfy on 2015/11/19.
 */
public class WifiDialog extends Dialog {


    private EditText etPwd;
    private Button btnConnect;
    private Button btnCancel;
    private Spinner spinner;
    WifiAutoConnectManager wifiAutoConnectManager;
    ArrayList<ScanResult> wifiList;
    WifiAutoConnectManager.WifiCipherType security;
    ArrayList<WifiInfos> wifoResults;
    private String wifiName;

    public WifiDialog(Context context, WifiAutoConnectManager wifiAutoConnectManager, ArrayList<ScanResult> wifiList) {
        super(context, R.style.wifiDialog);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_wifi);
        setCanceledOnTouchOutside(false);

        this.wifiAutoConnectManager = wifiAutoConnectManager;
        this.wifiList = wifiList;

        initView();
        init();
        bindEvent();
    }


    private void initView() {
        etPwd = (EditText) findViewById(R.id.et_wifi_pwd);
        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        spinner = (Spinner) findViewById(R.id.spinner);
    }


    public void init() {
        // 获取扫描的wifi列表
        wifoResults = new ArrayList<>();
        WifiInfos wifis = new WifiInfos();
        wifoResults.add(0, wifis);
        wifis.ssid = "请选择wifi";
        if (wifiList != null && wifiList.size() != 0) {
            for (ScanResult scanResult : wifiList) {
                WifiInfos wifi = new WifiInfos();
                wifi.ssid = scanResult.SSID;
                if (!TextUtils.isEmpty(scanResult.capabilities) && scanResult.capabilities.contains("WPA")) {
                    wifi.capabilities = WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA;

                } else if (!TextUtils.isEmpty(scanResult.capabilities) && scanResult.capabilities.contains("WEP")) {
                    wifi.capabilities = WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WEP;
                } else {
                    wifi.capabilities = WifiAutoConnectManager.WifiCipherType.WIFICIPHER_NOPASS;
                }
                wifoResults.add(wifi);
            }
        }

        WifiListAdapter wifiListAdapter = new WifiListAdapter(wifoResults);
        spinner.setAdapter(wifiListAdapter);
    }

    boolean isFirstSelection = true;
    private void bindEvent() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                if(isFirstSelection){
                    isFirstSelection = false;
                    return;
                }

                etPwd.setText("");

                WifiInfos wifi = wifoResults.get(pos);
                if(wifi != null){
                    wifiName = wifi.ssid;
                    security = wifi.capabilities;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pwd = etPwd.getText().toString();
                if(security != WifiAutoConnectManager.WifiCipherType.WIFICIPHER_NOPASS && TextUtils.isEmpty(pwd)){
                    AlertUtils.alert(getContext(), "请输入wifi密码");
                    etPwd.requestFocus();
                    return;
                }
                LogCat.e("wifi", "wifiName: " + wifiName);
                LogCat.e("wifi", "pwd: " + pwd);
                LogCat.e("wifi", "security: " + security);
                wifiAutoConnectManager.connect(wifiName, pwd, security, new OnWifiConnectListener() {
                    @Override
                    public void onWifiConnect(final boolean isConnect) {
                        LogCat.e("wifi", "wifi状态: " + isConnect);
                        btnConnect.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!isConnect) {
                                    AlertUtils.alert(getContext(), "热点链接失败,请重试!");
                                    spinner.setSelection(0);
                                    etPwd.setText("");

                                } else {
                                    AlertUtils.alert(getContext(), "正在链接热点!");
                                    dismiss();
                                }

                            }
                        });
                    }
                });


            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }


    static WifiAutoConnectManager.WifiCipherType getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WEP;
        }
        return (config.wepKeys[0] != null) ? WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WEP : WifiAutoConnectManager.WifiCipherType.WIFICIPHER_NOPASS;
    }


    public class WifiInfos {
        public String ssid;
        public WifiAutoConnectManager.WifiCipherType capabilities;

    }


    private class WifiListAdapter extends BaseAdapter {

        private ArrayList<WifiInfos> wifoResults;

        public WifiListAdapter(ArrayList<WifiInfos> wifoResults) {
            this.wifoResults = wifoResults;
        }

        @Override
        public int getCount() {
            return wifoResults.size();
        }

        @Override
        public Object getItem(int i) {
            return wifoResults.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.item_wifi, null);
            }
            if(i == 0){
                view.setClickable(true);
            }else {
                view.setClickable(false);
            }
            WifiInfos wifiInfos = wifoResults.get(i);
            ((TextView) view).setText(wifiInfos.ssid);
            return view;
        }

    }

}


