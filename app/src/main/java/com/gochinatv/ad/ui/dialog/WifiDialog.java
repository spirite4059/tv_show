/*
package com.gochinatv.ad.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.gochinatv.ad.MainActivity;
import com.gochinatv.ad.R;
import com.gochinatv.ad.interfaces.OnWifiConnectListener;
import com.gochinatv.ad.tools.AlertUtils;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.WifiAutoConnectManager;

import java.util.ArrayList;


*/
/**
 * Created by zfy on 2015/11/19.
 *//*

public class WifiDialog extends Dialog {
    private MainActivity mainActivity;
    private Context context;
    private CheckBox checkBox;
    private TextView tvCountTime;//显示30s秒倒计时
    private EditText etPwd;
    private Button btnConnect;
    private Button btnCancel;
    private Spinner spinner;
    WifiAutoConnectManager wifiAutoConnectManager;
    ArrayList<ScanResult> wifiList;
    WifiAutoConnectManager.WifiCipherType security;
    ArrayList<WifiInfos> wifoResults;
    private String wifiName;
    private Handler handler;

    public WifiDialog(Context context, WifiAutoConnectManager wifiAutoConnectManager, ArrayList<ScanResult> wifiList) {
        super(context, R.style.wifiDialog);
        mainActivity = (MainActivity) context;
        this.context = context;
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
        tvCountTime = (TextView) findViewById(R.id.tv_count_time);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
    }


    public void init() {
        // 获取扫描的wifi列表
        wifoResults = new ArrayList<>();
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
                                    etPwd.setText("");

                                } else {
                                    AlertUtils.alert(getContext(), "正在链接热点!");
                                    */
/**
                                     * 隐藏NavigationBar
                                     *//*

                                    DataUtils.hideNavigationBar((Activity) context);
                                    dismiss();
                                    if(handler == null){
                                        handler = new Handler(Looper.getMainLooper());
                                    }

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(!DataUtils.isNetworkConnected(getContext())){
                                                // 5秒后仍然没有网络,就继续显示dialog
                                                AlertUtils.alert(getContext(), "热点链接失败,请重试!");
                                                etPwd.setText("");
                                                show();
                                            }
                                        }
                                    }, 15000);

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
                */
/**
                 * 隐藏NavigationBar
                 *//*

                DataUtils.hideNavigationBar((Activity) context);
                dismiss();
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(etPwd != null){
                    if(b){
                        //显示密码
                        etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }else{
                        //隐藏密码
                        etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }

                    //下面两行代码实现: 输入框光标一直在输入文本后面
                    Editable etable = etPwd.getText();
                    Selection.setSelection(etable, etable.length());
                }
            }
        });

        etPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if( mainActivity != null){
                    if(charSequence.length() > 0){
                        //停止倒计时
                        mainActivity.cancelDialogCountTimer();
                        tvCountTime.setText("30s");
                    }else{
                        //开始倒计时
                        mainActivity.startDialogCountTimer();
                    }
                }else{
                    LogCat.e("WifiDialog", "mainActivity == null........");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

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
            WifiInfos wifiInfos = wifoResults.get(i);
            ((TextView) view).setText(wifiInfos.ssid);
            return view;
        }

    }


    */
/**
     * 显示30s倒计时
     *//*

    public void showCountTime(String time){
        if(tvCountTime != null){
            tvCountTime.setText(time+"s");
        }

    }







}


*/
