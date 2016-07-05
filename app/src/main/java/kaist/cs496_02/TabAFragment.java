package kaist.cs496_02;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by q on 2016-07-05.
 */
public class TabAFragment extends Fragment {

    public static String server_url = "http://ec2-52-78-73-98.ap-northeast-2.compute.amazonaws.com:8080";
    TextView viewText;
    EditText editText;
    CallbackManager callbackManager;
    static JSONAdapter adapter;
    private final String filename = "phonebook";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getActivity().openFileInput(filename)));
            String text;
            String jsonfile="";
            while ((text=reader.readLine())!=null) {
                jsonfile+=text;
            }
            JSONArray jarray = new JSONArray(jsonfile);
            ArrayList<PhonePerson> values = new ArrayList<>();
            for (int i = 0 ; i < jarray.length(); i++) {
                values.add(new PhonePerson((JSONObject) jarray.get(i)));
            }
            adapter = new JSONAdapter(getActivity(), values);

            reader.close();
        } catch (FileNotFoundException e) {
            File fPhoneBook = new File(getContext().getFilesDir(), filename);
            ArrayList<PhonePerson> values = new ArrayList<>();
            adapter = new JSONAdapter(getActivity(), values);
        } catch (IOException e) {}
        catch (JSONException e) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        View v = inflater.inflate(R.layout.tab_phonebook, container, false);
        viewText = (TextView) v.findViewById(R.id.textview);
        editText = (EditText) v.findViewById(R.id.edit_text);
        Button button_send = (Button) v.findViewById(R.id.button_send);
        Button button_get = (Button) v.findViewById(R.id.button_get);

        //Facebook
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) v.findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_friends","public_profile","email")); //access additional profile or post contents
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();


                GraphRequest request = new GraphRequest(
                        accessToken,
                        "/me/taggable_friends",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback(){
                            @Override
                            public void onCompleted(final GraphResponse response){
                                //Application code for users friends
                                new Thread(){
                                    public void run(){
                                        //get data and POST data to server
                                        try{
                                            JSONArray jsonArray = response.getJSONObject().getJSONArray("data");
                                            if(jsonArray != null){
                                                for(int i=0;i<jsonArray.length();i++){
                                                    String name = jsonArray.getJSONObject(i).getString("name");
                                                    String id = jsonArray.getJSONObject(i).getString("id");
                                                    new SendMSGTask().execute(name, id);
                                                }
                                            }
                                        } catch (JSONException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        }
                );
                request.executeAsync();


                //viewText.setText("login maintained");
            }

            @Override
            public void onCancel() {
                viewText.setText("login canceled");
            }

            @Override
            public void onError(FacebookException error) {
                viewText.setText("login error");
            }
        });

        /*
        @Override
        public void onResume(int requestCode, int resultCode, Intent data){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        */


        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("LogCat", "[SEND]CLICK");
                ConnectivityManager connMgr = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Log.i("LogCat", "[SEND]NETWORK AVAILABLE");
                    // fetch data
                    new SendMSGTask().execute(editText.getText().toString());
                } else {
                    Log.i("LogCat", "[SEND]NETWORK ERROR");
                    // display error
                }
            }
        });

        button_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("LogCat", "[GET]Click");
                ConnectivityManager connMgr = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Log.i("LogCat", "[GET]NETWORK AVAILABLE");
                    // fetch data
                    new GetMSGTask().execute(editText.getText().toString());
                } else {
                    Log.i("LogCat", "[GET]NETWORK ERROR");
                    // display error
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public class SendMSGTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String name = params[0];
            String id = params[1];

            try {
                JSONObject obj = new JSONObject();
                obj.put("name", name);
                obj.put("number", "010-" + random4digit() + "-" + random4digit());
                obj.put("id", id);
                return putJSON(obj);
            } catch (JSONException e) {
            }

            return null;
        }

        private String random4digit() {
            int num = (int) (Math.random() * 10000);
            if (num < 10) {
                return "000" + Integer.toString(num);
            } else if (num < 100) {
                return "00" + Integer.toString(num);
            } else if (num < 1000) {
                return "0" + Integer.toString(num);
            } else {
                return Integer.toString(num);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            viewText.setText(s);
        }

        public String putJSON(JSONObject jobj) {
            OutputStream os = null;
            InputStream is = null;

            int len = 0;
            try {
                len = jobj.toString().getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(server_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000 /* milliseconds */);
                conn.setConnectTimeout(1000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(len);
                os = conn.getOutputStream();
                OutputStreamWriter wrt = new OutputStreamWriter(os, "UTF-8");
                Log.i("LogCat", "[SEND]ENCODING: " + wrt.getEncoding());
                Log.i("LogCat", "[SEND]JSON RAW: " + jobj.toString());
                wrt.write(jobj.toString());
                wrt.flush();
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();
                String contentAsString = readIt(is, len);
                Log.i("LogCat", "[SEND]RESPOND : contentAsString");
                os.close();
                is.close();
                return contentAsString;
            } catch (MalformedURLException e) {
                Log.i("LogCat", "[SEND]FAILED TO CONNECT by MalformedURLException");
            } catch (IOException e) {
                Log.i("LogCat", "[SEND]FAILED TO CONNECT by IOException");
                Log.i("LogCat", e.toString());
            }
            return null;
        }


    }

    public class GetMSGTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String name = params[0];
            JSONArray jobj = getJSON(name);
            if (jobj != null) {
                return jobj.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            viewText.setText(s);
        }

        public JSONArray getJSON(String name) {

            String rawURL = null;
            try {
                rawURL = server_url + "/" + URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            InputStream is = null;

            int len = 1000;

            try {
                URL url = new URL(rawURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000 /* milliseconds */);
                conn.setConnectTimeout(1000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                Log.i("LogCat", "[GET]CONNECTION ESATABLISHED (" + rawURL + ")");
                int response = conn.getResponseCode();
                is = conn.getInputStream();
                Log.i("LogCat", "[GET]GET RESPOND");
                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                Log.i("LogCat", "[GET]READ RESPOND");
                is.close();
                return new JSONArray(contentAsString);
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            } catch (JSONException e) {
            }

            Log.i("LogCat", "[GET]FAILED TO CONNECT");
            return null;
        }
    }
}
