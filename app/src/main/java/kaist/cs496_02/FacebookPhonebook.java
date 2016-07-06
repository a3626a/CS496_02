package kaist.cs496_02;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by q on 2016-07-07.
 */
public class FacebookPhonebook {

    private final String filename = "phonebook";

    private String name;
    private Activity activity;
    private FacebookAapter adapter;
    private ArrayList<JSONObject> disk;
    private ArrayList<JSONObject> db;
    private ArrayList<JSONObject> fb;

    public FacebookPhonebook(Activity activity, FacebookAapter adapter) {
        this.activity = activity;
        this.adapter = adapter;
        readDataFromDisk();
        readDataFromDB();
    }

    public ArrayList<JSONObject> getPhonebook() {
        if (fb != null) {
            return fb;
        } else if (db != null) {
            return db;
        } else {
            return disk;
        }
    }

    //make new adapter
    private void readDataFromDisk() {
        disk = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(activity.openFileInput(filename)));

            String text;
            String jsonfile = "";
            while ((text = reader.readLine()) != null) {
                jsonfile += text;
            }
            Log.i("LogCat","DATA FROM DISK"+jsonfile);
            JSONObject jobj = new JSONObject(jsonfile);

            name = jobj.getString("name");
            JSONArray arr = jobj.getJSONArray("phonebook");

            for (int i = 0; i < arr.length(); i++) {
                disk.add(arr.getJSONObject(i));
            }

            adapter.notifyDataSetChanged();

            reader.close();
        } catch (FileNotFoundException e) {
            Log.i("LogCat","FILE NOT FOUND");
        } catch (IOException e) {
        } catch (JSONException e) {
            Log.i("LogCat","JSON EXCEPTION");
        }
    }

    private void writeDataToDisk() {
        disk = db;

        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(activity.openFileOutput(filename, Context.MODE_PRIVATE)));
            JSONArray jarr = StreamHelper.getJSONArrayFromArrayList(db);
            JSONObject jobj = new JSONObject();
            jobj.put("name",name);
            jobj.put("phonebook",jarr);
            writer.write(jobj.toString());
            writer.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (JSONException e) {
        }
    }

    private void readDataFromDB() {
        if (NetworkHelper.isConnected(activity) && name != null)
            new GetMSGTask().execute();
    }

    private void writeDataToDB() {
        db = fb;

        if (NetworkHelper.isConnected(activity))
            new SendMSGTask().execute();
    }

    public void readDataFromFB(JSONArray arr, String name) throws JSONException {
        fb = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            fb.add(arr.getJSONObject(i));
        }

        adapter.notifyDataSetChanged();

        this.name = name;

        writeDataToDB();
        writeDataToDisk();
    }

    public class GetMSGTask extends AsyncTask<Void, Void, Void> {

        public GetMSGTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            JSONArray arr = getJSON();
            db = new ArrayList<>();
            try {
                for (int i = 0; i < arr.length(); i++) {
                    db.add(arr.getJSONObject(i));
                }

                adapter.notifyDataSetChanged();
                writeDataToDisk();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public JSONArray getJSON() {

            String rawURL = null;
            try {
                rawURL = MainActivity.server_url_phone + "/" + URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.i("PLACE", "getJSON");
                e.printStackTrace();
            }
            InputStream is = null;

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
                String contentAsString = StreamHelper.readIt(is);
                Log.i("LogCat", "[GET]READ RESPOND: "+contentAsString);
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

    public class SendMSGTask extends AsyncTask<Void, Void, Void> {

        String rawURL;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                rawURL = MainActivity.server_url_phone + "/" + URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            putJSON();

            return null;
        }

        public void putJSON() {
            OutputStream os = null;
            InputStream is = null;

            JSONArray jobj = StreamHelper.getJSONArrayFromArrayList(fb);
            int len = 0;
            try {
                len = jobj.toString().getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(rawURL);
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
                String contentAsString = StreamHelper.readIt(is);
                Log.i("LogCat", "[SEND]RESPOND : contentAsString");
                os.close();
                is.close();
            } catch (MalformedURLException e) {
                Log.i("LogCat", "[SEND]FAILED TO CONNECT by MalformedURLException");
            } catch (IOException e) {
                Log.i("LogCat", "[SEND]FAILED TO CONNECT by IOException");
                Log.i("LogCat", e.toString());
            }
        }

    }

}
