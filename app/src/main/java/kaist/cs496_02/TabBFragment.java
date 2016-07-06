package kaist.cs496_02;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by q on 2016-06-29.
 */
public class TabBFragment extends Fragment implements GridView.OnItemClickListener {

    private ImageAdapter adapter;
    private Bitmap mPlaceHolderBitmap;
    public static ImageView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ImageAdapter(getActivity());
        mPlaceHolderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waiting);
        new AsyncTask<ImageAdapter.IntegerWrapper, Void, Void>() {
            @Override
            protected Void doInBackground(ImageAdapter.IntegerWrapper... params) {
                ImageAdapter.IntegerWrapper wrp = params[0];
                wrp.set(NetworkHelper.getGallerySize("LeeChangHwan"));
                Log.i("LogCat","Gallery Size: "+Integer.toString(wrp.get()));
                return null;
            }
        }.execute(adapter.length);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_gallery, container, false);
        GridView gridView = (GridView) v.findViewById(R.id.GridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);

        view = (ImageView)v.findViewById(R.id.test_view);

        Button btn_upload = (Button) v.findViewById(R.id.upload);
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("LogCat", "[UPLOAD]CLICK");
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Log.i("LogCat", "[UPLOAD]NETWORK AVAILABLE");
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... params) {
                            try {
                                // Get the first Image
                                String[] cols = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.TITLE};
                                Cursor cp = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cols, null, null, null);
                                int dataIndex = cp.getColumnIndex(MediaStore.Images.Media.DATA);
                                int sizeIndex = cp.getColumnIndex(MediaStore.Images.Media.SIZE);
                                int titleIndex = cp.getColumnIndex(MediaStore.Images.Media.TITLE);
                                Log.i("LogCat", "[UPLOAD]DATA INDEX: " + Integer.toString(dataIndex));
                                Log.i("LogCat", "[UPLOAD]SIZE INDEX: " + Integer.toString(sizeIndex));
                                Log.i("LogCat", "[UPLOAD]TITLE INDEX: " + Integer.toString(titleIndex));

                                while (cp.moveToNext()) {
                                    Uri imageUri = Uri.parse("file://"+cp.getString(dataIndex));
                                    int len = cp.getInt(sizeIndex);
                                    String title = cp.getString(titleIndex);
                                    // Connect to the server
                                    URL url = new URL(MainActivity.server_url_gallery+"/"+"LeeChangHwan"+"/"+title);
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    // Setup header
                                    conn.setReadTimeout(5000);
                                    conn.setConnectTimeout(1000);
                                    conn.setRequestMethod("POST");
                                    conn.setDoOutput(true);
                                    conn.setFixedLengthStreamingMode(len);
                                    // Write Data
                                    byte[] buffer = new byte[1024]; // Adjust if you want
                                    int bytesRead;
                                    InputStream is = getActivity().getContentResolver().openInputStream(imageUri);
                                    OutputStream os = conn.getOutputStream();
                                    while ((bytesRead = is.read(buffer)) != -1)
                                    {
                                        os.write(buffer, 0, bytesRead);
                                    }
                                    os.flush();

                                    // Send the data
                                    conn.connect();

                                    // Get response
                                    int response = conn.getResponseCode();
                                    is = conn.getInputStream();
                                    String contentAsString = StreamHelper.readIt(is);
                                    Log.i("LogCat", "[SEND]RESPOND : "+contentAsString);
                                    os.close();
                                    is.close();
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (ProtocolException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);
                        }
                    }.execute();
                } else {
                    Log.i("LogCat", "[UPLOAD]NETWORK ERROR");
                }

            }
        });

        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(getActivity(), ImageActivity.class);
        i.putExtra("selected", position);
        startActivity(i);
    }

    private class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private IntegerWrapper length;

        public ImageAdapter(Context c) {
            mContext = c;
            length = new IntegerWrapper();
        }

        public int getCount() {
            return length.get();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(BitmapHelper.dpToPx(100), BitmapHelper.dpToPx(100)));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(2, 2, 2, 2);
            } else {
                imageView = (ImageView) convertView;
            }

            BitmapHelper.loadBitmap(position, imageView, BitmapHelper.dpToPx(100), BitmapHelper.dpToPx(100), mPlaceHolderBitmap, getResources());

            return imageView;
        }

        private class IntegerWrapper {
            private int length;

            public IntegerWrapper() {
                this(0);
            }

            public IntegerWrapper(int length) {
                this.length=length;
            }

            public int get() {
                return length;
            }

            public void set(int length) {
                this.length=length;
            }
        }

    }
}