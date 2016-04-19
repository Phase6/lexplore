package com.mindsoon.lexplore;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by john on 4/19/16.
 */
public class SplashScreenFragment extends Fragment {

    String LOG_TAG = "SplashScreenFragment";

    Context parentContext;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.parentContext = activity.getApplicationContext();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if(bundle != null){

        }else{

        }

        View rootView = inflater.inflate(R.layout.fragment_splash_screen, container, false);

        return rootView;
    }

}
