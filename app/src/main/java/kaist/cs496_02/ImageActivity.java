package kaist.cs496_02;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageActivity extends Activity {

    private Bitmap mPlaceHolderBitmap;
    private PhotoViewAttacher mAttacher;
    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        mPlaceHolderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waiting);
        image = (ImageView)findViewById(R.id.image);

        image.setImageBitmap(mPlaceHolderBitmap);

        Intent intent = getIntent();
        final int rid = intent.getIntExtra("selected",0);
        final String name = intent.getStringExtra("name");
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    Point pSize = new Point();
                    getWindowManager().getDefaultDisplay().getSize(pSize);

                    InputStream is = NetworkHelper.getImage(name, rid);
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[16384];
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    byte[] image = buffer.toByteArray();
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(image,0,image.length,options);
                    int width=options.outWidth;
                    int height=options.outHeight;

                    if ((height/(double)width)*pSize.x>pSize.y) {
                        options.inSampleSize = BitmapHelper.calculateInSampleSize(options, (int)(pSize.y*(width/(double)height)), pSize.y);
                    } else {
                        options.inSampleSize = BitmapHelper.calculateInSampleSize(options, pSize.x, (int)(pSize.x*(height/(double)width)));
                    }

                    options.inJustDecodeBounds = false;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image,0,image.length,options);
                    return bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                image.setImageBitmap(bitmap);
                mAttacher = new PhotoViewAttacher(image);
            }
        }.execute();

        /*
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource();

        int width = options.outWidth;
        int height = options.outHeight;
        if ((height/(double)width)*pSize.x>pSize.y) {
            BitmapHelper.loadBitmap(rid, image, (int)(pSize.y*(width/(double)height)), pSize.y, mPlaceHolderBitmap, getResources());
        } else {
            BitmapHelper.loadBitmap(rid, image, pSize.x, (int)(pSize.x*(height/(double)width)), mPlaceHolderBitmap, getResources());
        }
        */

    }
}
