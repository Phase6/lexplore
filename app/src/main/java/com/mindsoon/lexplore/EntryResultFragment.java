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
public class EntryResultFragment extends Fragment {

    String LOG_TAG = "EntryResultFragment";

    Context parentContext;
    String source, url, word = "";

    TextView sourceTextView;
    RelativeLayout progressSpinnerRelativeLayout;
    TextView contentTextView;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.parentContext = activity.getApplicationContext();
    }

    /*@Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.parentContext = context;
    }*/

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if(bundle != null){
            source = bundle.getString("source");
            url = bundle.getString("url");
            word = bundle.getString("word");
        }else{

        }

        View rootView = inflater.inflate(R.layout.fragment_entry_result, container, false);

        sourceTextView = (TextView) rootView.findViewById(R.id.resultSourceTextView);
        sourceTextView.setText(source);

        progressSpinnerRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.resultProgressRelativeLayout);

        contentTextView = (TextView) rootView.findViewById(R.id.resultContentTextView);

        getContent();

        return rootView;
    }


    public void getContent(){

        //TODO: THIS IS WHERE YOU WOULD USE A ASYNC TASK TO RETRIEVE DATA FROM THE PROVIDED URL
        //TODO: THIS APPROACH WOULD REQUIRE A CHANGE TO YOUR BACKEND

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                contentTextView.setText(url + " for word: " + word);
                progressSpinnerRelativeLayout.setVisibility(View.GONE);
                contentTextView.setVisibility(View.VISIBLE);
            }
        }, 2000);



    }


}
