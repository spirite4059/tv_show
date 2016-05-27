package com.gochinatv.ad.cmd;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.text.TextUtils;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.LogCat;
import com.okhtttp.response.ADDeviceDataResponse;

/**
 * Created by fq_mbp on 16/5/27.
 */
public class CmdReceiver {

    private Activity activity;

    public CmdReceiver(Activity activity){
        this.activity = activity;
    }

    public void open(String action, ADDeviceDataResponse adDeviceDataResponse){
        LogCat.e("push", "执行open命令.........");
        if(!isCanExecute(action)){
            return;
        }

        int adType = getAdType(action);
        if(adType == -1){
            return;
        }

        FragmentManager fm = activity.getFragmentManager();

        Fragment fg = fm.findFragmentByTag(Constants.FRAGMENT_TAG_PRE + adType);
        if(fg != null){
            return;
        }

        BaseFragment fragment = BaseFragment.getInstance(adType);
        if(fragment != null){
            FragmentTransaction ft = fm.beginTransaction();
            if(adDeviceDataResponse != null && adDeviceDataResponse.layout != null && adDeviceDataResponse.layout.size() > adType - 1){
                fragment.setLayoutResponse(adDeviceDataResponse.layout.get(adType - 1));
            }
            ft.add(R.id.root_main, fragment, Constants.FRAGMENT_TAG_PRE + adType);
            ft.commit();
        }

    }

    public void close(String action){
        LogCat.e("push", "执行关闭命令.........");
        if(!isCanExecute(action)){
            return;
        }

        int adType = getAdType(action);
        if(adType == -1){
            return;
        }

        FragmentManager fm = activity.getFragmentManager();
        BaseFragment fragment = (BaseFragment) fm.findFragmentByTag(Constants.FRAGMENT_TAG_PRE + adType);
        if(fragment != null){
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }

    }

    public void fresh(String action){
        if(!isCanExecute(action)){
            return;
        }


    }


    private boolean isCanExecute(String action){
        if(activity == null || TextUtils.isEmpty(action)){
            return false;
        }
        if(!action.contains("ad")){
            return false;
        }
        return true;
    }

    private int getAdType(String action){
        String actionAd = action.substring(2, action.length());
        if(TextUtils.isEmpty(actionAd)){
            return -1;
        }
        return Integer.parseInt(actionAd);
    }

}
