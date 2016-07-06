package kaist.cs496_02;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by q on 2016-07-05.
 */
public class TabAFragment extends Fragment {

    TextView viewText;
    CallbackManager callbackManager;
    FacebookAapter adapter;

    JSONArray jsonArray = new JSONArray(); //for Contacts
    static FacebookPhonebook fbpb;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new FacebookAapter(getActivity());
        if (fbpb == null)
            fbpb = new FacebookPhonebook(getActivity(), adapter);

        //getDataFromContact();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        View v = inflater.inflate(R.layout.tab_phonebook, container, false);
        viewText = (TextView) v.findViewById(R.id.textView);
        ListView lv = (ListView) v.findViewById(R.id.list);
        lv.setAdapter(adapter);

        if (NetworkHelper.isConnected(getActivity())) {
            //----------------------Start Facebook Part--------------------------------------------------------------------------------
            callbackManager = CallbackManager.Factory.create();
            LoginButton loginButton = (LoginButton) v.findViewById(R.id.login_button);
            loginButton.setReadPermissions(Arrays.asList("user_friends", "public_profile", "email")); //access additional profile or post contents
            loginButton.setFragment(this);
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {

                    final FacbookLoginWrapper wrp = new FacbookLoginWrapper();

                    AccessToken accessToken = loginResult.getAccessToken();

                    GraphRequest request = new GraphRequest(
                            accessToken,
                            "/me/taggable_friends",
                            null,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                @Override
                                public void onCompleted(final GraphResponse response) {
                                    try {
                                        wrp.setArr(response.getJSONObject().getJSONArray("data"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    );
                    request.executeAsync();


                    GraphRequest request2 = GraphRequest.newMeRequest(
                            accessToken,
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    try {
                                        wrp.setName(object.getString("name"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,link");
                    request2.setParameters(parameters);
                    request2.executeAsync();
                    //----------------------End Facebook login success Part--------------------------------------------------------------------------------

                    viewText.setText("login maintained");
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
        }
        //--------------End Facebook Part---------------------------------------------------------

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

/*
    //use existing adapter
    private void getDataFromContact() {
        Dialog dialog = new Dialog(getContext());
        Uri uri = ContactsContract.Contacts.CONTENT_URI;

        Cursor cursor = dialog.getContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cursor.getInt(cursor.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = dialog.getContext().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("name", name);
                            jsonObject.put("number", phoneNo);
                            jsonArray.put(jsonObject);
                            adapter.add(new PhonePerson(jsonObject));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    pCur.close();
                }
            }
        }
    }
*/

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

}
