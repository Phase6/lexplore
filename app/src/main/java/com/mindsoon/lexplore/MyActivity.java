package com.mindsoon.lexplore;

        import android.content.Context;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.AsyncTask;
        import android.os.Handler;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.text.Html;
        import android.text.method.LinkMovementMethod;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.View;
        import android.widget.EditText;
        import android.widget.LinearLayout;
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

public class MyActivity extends ActionBarActivity implements OnClickListener {
    private LinearLayout layoutProgress, box_layout, splash_layout, about_layout;
    private TextView dictionary_name, slang_name, thesaurus_name, dictionary_box, slang_box, thesaurus_box;
    private EditText submitText;
    private ScrollView scroll_layout;
    private ArrayList<Entry> searchHistory = new ArrayList<Entry>();
    private boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getSupportActionBar().hide();
        toastIfNoInternet();
        setFields();

        submitText.setOnClickListener(this);
        submitText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != 0) {
                    return false;
                } else if (!isNetworkAvailable()) {
                    toastIfNoInternet();
                } else if ( (keyCode == KeyEvent.KEYCODE_BACK) && (searchHistory.size() > 1) ) {
                    searchHistory.remove(searchHistory.size() - 1);
                    submitText.setText(searchHistory.get(searchHistory.size() - 1).getWord());
                    insertEntryIntoFields(searchHistory.get(searchHistory.size() - 1));
                    hideKeyboard(v);
                } else if ( (keyCode == KeyEvent.KEYCODE_BACK) ||
                        (keyCode == KeyEvent.KEYCODE_MENU) ) {
                    checkForDoubleBackPress();
                    showSplash(v);
                } else if ( (keyCode == KeyEvent.KEYCODE_ENTER) && (submitText.length() > 0) ) {
                    Log.d("word", "getting definition of " + submitText.getText().toString());
                    getDefinitions(v, submitText.getText().toString());
                    hideKeyboard(v);
                }
                return true;
            }
        });
    }

    void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MyActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void checkForDoubleBackPress() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        showToast(getString(R.string.tap_back));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000); }

    void setFields() {
        dictionary_name = (TextView) findViewById(R.id.dictionary_name);
        slang_name = (TextView) findViewById(R.id.slang_name);
        thesaurus_name = (TextView) findViewById(R.id.thesaurus_name);
        dictionary_box = (TextView) findViewById(R.id.dictionary_box);
        slang_box = (TextView) findViewById(R.id.slang_box);
        thesaurus_box = (TextView) findViewById(R.id.thesaurus_box);
        submitText = (EditText) findViewById(R.id.submitText);
        layoutProgress = (LinearLayout) findViewById(R.id.progress_spinner_layout);
        box_layout = (LinearLayout) findViewById(R.id.box_layout);
        splash_layout = (LinearLayout) findViewById(R.id.splash_layout);
        scroll_layout = (ScrollView) findViewById(R.id.scroll_layout);
        about_layout = (LinearLayout) findViewById(R.id.about_layout);
        TextView about_contact = (TextView) findViewById(R.id.about_contact);
        dictionary_box.setMovementMethod(LinkMovementMethod.getInstance());
        dictionary_box.setLinksClickable(true);
        slang_box.setMovementMethod(LinkMovementMethod.getInstance());
        slang_box.setLinksClickable(true);
        thesaurus_box.setMovementMethod(LinkMovementMethod.getInstance());
        thesaurus_box.setLinksClickable(true);
        about_contact.setMovementMethod(LinkMovementMethod.getInstance());
        about_contact.setLinksClickable(true);
    }

    void insertEntryIntoFields(Entry entry) {
        dictionary_name.setText(Html.fromHtml(entry.getDictionaryName()));
        slang_name.setText(Html.fromHtml(entry.getSlangName()));
        thesaurus_name.setText(Html.fromHtml(entry.getThesaurusName()));
        dictionary_box.setText(Html.fromHtml(entry.getDictionaryContent()));
        slang_box.setText(Html.fromHtml(entry.getSlangContent()));
        thesaurus_box.setText(Html.fromHtml(entry.getThesaurusContent()));
        scroll_layout.scrollTo(0, 0);
    }

    public void onClick(View v) {
        if (submitText.length() > 0) {
            submitText.getText().clear();
        }
    }

    void toastIfNoInternet() {
        if (!isNetworkAvailable()) {
            showToast(getString(R.string.enable_internet));
        }
    }

    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void hideAllWidgets(){
        about_layout.setVisibility(View.GONE);
        box_layout.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.GONE);
        splash_layout.setVisibility(View.GONE);
    }

    public void showAbout(View v) {
        hideAllWidgets();
        about_layout.setVisibility(View.VISIBLE);
    }

    void showResults(View v) {
        hideAllWidgets();
        box_layout.setVisibility(View.VISIBLE);
    }

    void showSpinner(View v) {
        hideAllWidgets();
        layoutProgress.setVisibility(View.VISIBLE);
    }

    public void showSplash(View v) {
        hideAllWidgets();
        splash_layout.setVisibility(View.VISIBLE);
    }

    void getDefinitions(final View v, final String submittedWord){

        class HttpGetAsyncTask extends AsyncTask<String, Void, JSONObject> {

            @Override
            protected void onPreExecute() {
                showSpinner(v);
            }

            @Override
            protected JSONObject doInBackground(String... strings) {
                String url;
                try {
                    url = getString(R.string.url) + URLEncoder.encode(submittedWord, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    url = getString(R.string.url) + submittedWord.replaceAll("\\s+","");
                }
                JSONObject returnedData = new JSONObject();
                try {
                    HttpClient client = new DefaultHttpClient();
                    final HttpParams httpParameters = client.getParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
                    HttpConnectionParams.setSoTimeout        (httpParameters, 10000);
                    HttpGet get = new HttpGet(url);
                    HttpResponse responseGet = client.execute(get);
                    HttpEntity resEntityGet = responseGet.getEntity();
                    if (resEntityGet != null) {
                        String response = EntityUtils.toString(resEntityGet);
                        returnedData = new JSONObject(response);
                    }
                } catch (SocketTimeoutException e) {
                    showToast(getString(R.string.no_server_response));
                } catch (IOException e) {
                    showToast(getString(R.string.no_server_response));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return returnedData;
            }

            protected void onPostExecute(JSONObject definitionObject) {
                showResults(v);
                Entry thisEntry = new Entry(submittedWord);
                thisEntry.insertContent(definitionObject, getBaseContext());
                insertEntryIntoFields(thisEntry);
                searchHistory.add(thisEntry);
                Log.d("word", searchHistory.get(searchHistory.size()-1).word +
                        " added to history of " + searchHistory.size() + " words");
            }
        }

        HttpGetAsyncTask aTask = new HttpGetAsyncTask();
        aTask.execute(submittedWord);
    }
}