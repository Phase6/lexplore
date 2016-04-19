package com.mindsoon.lexplore;

        import android.content.Context;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.AsyncTask;
        import android.os.Handler;
        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentManager;
        import android.support.v4.app.FragmentTransaction;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.text.Html;
        import android.text.method.LinkMovementMethod;
        import android.view.KeyEvent;
        import android.view.View;
        import android.widget.EditText;
        import android.widget.LinearLayout;
        import android.widget.RelativeLayout;
        import android.widget.ScrollView;
        import android.widget.TextView;
        import android.view.View.OnClickListener;
        import org.apache.http.HttpEntity;
        import org.apache.http.HttpResponse;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.methods.HttpGet;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.params.HttpConnectionParams;
        import org.apache.http.params.HttpParams;
        import org.apache.http.util.EntityUtils;
        import org.json.JSONObject;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.Toast;
        import java.io.IOException;
        import java.io.UnsupportedEncodingException;
        import java.net.SocketTimeoutException;
        import java.net.URLEncoder;
        import java.util.ArrayList;
        import java.util.List;

public class MyActivity extends ActionBarActivity implements OnClickListener, View.OnKeyListener {

    FragmentManager fragmentManager;
    LinearLayout fragmentContainer;
    private static int CONTENT_VIEW_ID;

    EditText submitText;
    String currentWord;
    ArrayList<String> searchHistory = new ArrayList<String>();

    private boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        fragmentManager = getSupportFragmentManager();
        fragmentContainer = (LinearLayout) findViewById(R.id.contentLayout);
        CONTENT_VIEW_ID = fragmentContainer.getId();

        submitText = (EditText) findViewById(R.id.submitText);


        if(!isNetworkAvailable()){
            toastUser(getString(R.string.enable_internet));
        }

        addSplashScreenFragment();

        submitText.setOnClickListener(this);
        submitText.setOnKeyListener(this);

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != 0) {
            return false;
        } else if (!isNetworkAvailable()) {
            toastUser(getString(R.string.enable_internet));
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_ENTER && submitText.length() > 0) {
            currentWord = submitText.getText().toString();
            if (!isWordValid(currentWord)) {
                toastUser(getString(R.string.invalid_user_input_response));
                return true;
            }
            hideKeyboard();
            searchHistory.add(currentWord);
            clearFragmentContainer();
            getResultsForEntry(currentWord);

            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onBackPressed(){

        if(searchHistory.size() > 1){
            searchHistory.remove(searchHistory.size() - 1);
            submitText.setText(searchHistory.get(searchHistory.size() - 1));
            clearFragmentContainer();
            getResultsForEntry(searchHistory.get(searchHistory.size() - 1));
            hideKeyboard();
        }else if(searchHistory.size() == 1&& !doubleBackToExitPressedOnce){
            this.doubleBackToExitPressedOnce = true;
            toastUser(getString(R.string.tap_back));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }else{
            super.onBackPressed();
        }

    }

    public void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    boolean isWordValid(String word) {
        return !word.equals("");
    }

    public void onClick(View v) {
        if (submitText.length() > 0) {
            submitText.getText().clear();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void toastUser(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void clearFragmentContainer(){
        List<Fragment> showingFragments = fragmentManager.getFragments();
        if (showingFragments != null) {
            for (Fragment frag : showingFragments){
                getSupportFragmentManager().beginTransaction().remove(frag).commit();
            }
        }


    }

    public void addSplashScreenFragment(){
        Fragment splashScreenFragment = new SplashScreenFragment();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(CONTENT_VIEW_ID, splashScreenFragment, "splashScreenFragment");
        ft.commitAllowingStateLoss();
    }

    public void getResultsForEntry(String word){

        //TODO: Change the names of the sources
        //TODO: Add correct url for your API

        String source1 = "MW Dictionary";
        String api1 = getString(R.string.mw_dictionary_api_url);
        addEntryResultFragment(source1, api1, word);

        String source2 = "Urban Dictionary";
        String api2 = getString(R.string.urban_dictionary_api_url);
        addEntryResultFragment(source2, api2, word);

        String source3 = "Thesaurus";
        String api3 = getString(R.string.thesaurus_api_url);
        addEntryResultFragment(source3, api3, word);

    }


    public void addEntryResultFragment(String source, String url, String word){

        Fragment entryResultFragment = new EntryResultFragment();

        Bundle bundle = new Bundle();
        bundle.putString("source", source);
        bundle.putString("url", url);
        bundle.putString("word", word);
        entryResultFragment.setArguments(bundle);

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(CONTENT_VIEW_ID, entryResultFragment, "entryResultFragment");
        ft.commitAllowingStateLoss();
    }



    /*void hydrateEntryContent(final View v, final Entry.Verb verb){

        class HttpGetAsyncTask extends AsyncTask<String, Void, JSONObject> {
            @Override
            protected void onPreExecute() {
                showSpinner(verb);
            }

            @Override
            protected JSONObject doInBackground(String... strings) {
                int code = 0;
                String url = getString(R.string.url) + verb + "/";
                try {
                    url += URLEncoder.encode(entry.word, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    url += entry.word.replaceAll("\\s+","");
                }
                JSONObject returnedData = new JSONObject();
                try {
                    HttpClient client = new DefaultHttpClient();
                    final HttpParams httpParameters = client.getParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
                    HttpConnectionParams.setSoTimeout(httpParameters, 10000);
                    HttpGet get = new HttpGet(url);
                    HttpResponse responseGet = client.execute(get);
                    HttpEntity resEntityGet = responseGet.getEntity();
                    code = responseGet.getStatusLine().getStatusCode();
                    if (resEntityGet != null) {
                        String response = EntityUtils.toString(resEntityGet);
                        returnedData = new JSONObject(response);
                    }
                } catch (SocketTimeoutException e) {
                    showToast(getString(R.string.server_timeout_response));
                } catch (IOException e) {
                    showToast(getString(R.string.server_error_response));
                } catch (Exception e) {
                    showToast(code == 404
                            ? getString(R.string.server_error_response)
                            : getString(R.string.app_error_response));
                }
                return returnedData;
            }

            protected void onPostExecute(JSONObject jsonObject) {
                hideSpinner(verb);
                if (jsonObject.length() > 0)  {
                    hideAllWidgets(false);
                    entry.hydrateContentFromJson(jsonObject, verb, getBaseContext());
                    if (entry.hasAnyContent()) {
                        insertEntryIntoFields(entry);
                        if (entry.hasAllContent()) {
                            searchHistory.add(entry);
                        }
                    }
                }
            }
        }

        HttpGetAsyncTask aTask = new HttpGetAsyncTask();
        aTask.execute(entry.word);
    }*/



    /*void setFields() {
        dictionary_name = (TextView) findViewById(R.id.dictionary_name);
        slang_name = (TextView) findViewById(R.id.slang_name);
        synonym_name = (TextView) findViewById(R.id.synonym_name);
        dictionary_textview = (TextView) findViewById(R.id.dictionary_textview);
        slang_textview = (TextView) findViewById(R.id.slang_textview);
        synonym_textview = (TextView) findViewById(R.id.synonym_textview);
        submitText = (EditText) findViewById(R.id.submitText);
        dictionary_spinner = (RelativeLayout) findViewById(R.id.dictionary_spinner);
        slang_spinner = (RelativeLayout) findViewById(R.id.slang_spinner);
        synonym_spinner = (RelativeLayout) findViewById(R.id.synonym_spinner);
        box_layout = (LinearLayout) findViewById(R.id.box_layout);
        splash_layout = (LinearLayout) findViewById(R.id.splash_layout);
        scroll_layout = (ScrollView) findViewById(R.id.scroll_layout);
        about_layout = (LinearLayout) findViewById(R.id.about_layout);
        TextView about_contact = (TextView) findViewById(R.id.about_contact);
        dictionary_textview.setMovementMethod(LinkMovementMethod.getInstance());
        dictionary_textview.setLinksClickable(true);
        slang_textview.setMovementMethod(LinkMovementMethod.getInstance());
        slang_textview.setLinksClickable(true);
        synonym_textview.setMovementMethod(LinkMovementMethod.getInstance());
        synonym_textview.setLinksClickable(true);
        about_contact.setMovementMethod(LinkMovementMethod.getInstance());
        about_contact.setLinksClickable(true);
    }*/

    /*void insertEntryIntoFields(Entry entry) {
        if (entry.dictionary != null) {
            dictionary_name.setText(Html.fromHtml(entry.dictionary.name));
            dictionary_textview.setText(Html.fromHtml(entry.dictionary.content));
        }
        if (entry.synonym != null) {
            synonym_name.setText(Html.fromHtml(entry.synonym.name));
            synonym_textview.setText(Html.fromHtml(entry.synonym.content));
        }
        if (entry.slang != null) {
            slang_name.setText(Html.fromHtml(entry.slang.name));
            slang_textview.setText(Html.fromHtml(entry.slang.content));
        }
        scroll_layout.scrollTo(0, 0);
    }*/



    /*void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }*/

    /*public void hideAllWidgets(boolean hideBox){
        box_layout.setVisibility(hideBox ? View.GONE : View.VISIBLE);
        about_layout.setVisibility(View.GONE);
        splash_layout.setVisibility(View.GONE);
    }*/

    /*public void showAbout(View v) {
        hideAllWidgets(true);
        about_layout.setVisibility(View.VISIBLE);
    }*/

    /*void showSpinner(Entry.Verb verb) {
        hideAllWidgets(false);
        if (verb == Entry.Verb.Dictionary) {
            dictionary_spinner.setVisibility(View.VISIBLE);
            dictionary_textview.setVisibility(View.GONE);
        } else if (verb == Entry.Verb.Slang) {
            slang_spinner.setVisibility(View.VISIBLE);
            slang_textview.setVisibility(View.GONE);
        } else if (verb == Entry.Verb.Synonym) {
            synonym_spinner.setVisibility(View.VISIBLE);
            synonym_textview.setVisibility(View.GONE);
        }
    }*/

    /*void hideSpinner(Entry.Verb verb) {
        if (verb == Entry.Verb.Dictionary) {
            dictionary_spinner.setVisibility(View.GONE);
            dictionary_textview.setVisibility(View.VISIBLE);
        } else if (verb == Entry.Verb.Slang) {
            slang_spinner.setVisibility(View.GONE);
            slang_textview.setVisibility(View.VISIBLE);
        } else if (verb == Entry.Verb.Synonym) {
            synonym_spinner.setVisibility(View.GONE);
            synonym_textview.setVisibility(View.VISIBLE);
        }
    }*/

    /*public void showSplash(View v) {
        hideAllWidgets(true);
        splash_layout.setVisibility(View.VISIBLE);
    }*/


}