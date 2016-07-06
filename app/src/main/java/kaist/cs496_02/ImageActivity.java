package kaist.cs496_02;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageActivity extends Activity {

    private Bitmap mPlaceHolderBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        mPlaceHolderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waiting);

        ImageView image = (ImageView)findViewById(R.id.image);

        Point pSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(pSize);

        Intent intent = getIntent();
        int rid = intent.getIntExtra("selected",0);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), rid, options);

        int width = options.outWidth;
        int height = options.outHeight;
        if ((height/(double)width)*pSize.x>pSize.y) {
            BitmapHelper.loadBitmap(rid, image, (int)(pSize.y*(width/(double)height)), pSize.y, mPlaceHolderBitmap, getResources());
        } else {
            BitmapHelper.loadBitmap(rid, image, pSize.x, (int)(pSize.x*(height/(double)width)), mPlaceHolderBitmap, getResources());
        }


    }
}
