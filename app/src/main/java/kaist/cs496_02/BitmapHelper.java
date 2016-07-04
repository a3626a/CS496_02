package kaist.cs496_02;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by q on 2016-06-30.
 */
public class BitmapHelper {

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        int orgWidth=options.outWidth;
        int orgHeight=options.outHeight;
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        try {
            InputStream is = res.openRawResource(resId);
            BitmapRegionDecoder regionDecoder = BitmapRegionDecoder.newInstance(is, false);
            if (reqHeight/(double)reqWidth>orgHeight/(double)orgWidth) {
                int tWidth = (int)(orgHeight*(reqWidth/(double)reqHeight));
                return regionDecoder.decodeRegion(new Rect((orgWidth-tWidth)/2,0,(orgWidth+tWidth)/2,orgHeight),options);
            } else {
                int tHeight = (int)(orgWidth*(reqHeight/(double)reqWidth));
                return regionDecoder.decodeRegion(new Rect(0,(orgHeight-tHeight)/2,orgWidth,(orgHeight+tHeight)/2),options);
            }
            //return BitmapFactory.decodeResource(res, resId, options);
        } catch(Exception e) {}
        return null;

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.i("LogCat",Integer.toString(inSampleSize));

        return inSampleSize;
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static void loadBitmap(int resId, ImageView imageView, int xpx, int ypx, Bitmap placeHolder, Resources res) {
        if (cancelPotentialWork(resId, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, res);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(res, placeHolder, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(resId, xpx, ypx);
        }
    }

    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            if (bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    static class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;
        private Resources resources;

        public BitmapWorkerTask(ImageView imageView, Resources res) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            resources=res;
        }


        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            int data = (Integer)params[0];
            int x = (Integer)params[1];
            int y = (Integer)params[2];

            // Crop image before setting to image view to reduce memory usage
            Bitmap bitmap = BitmapHelper.decodeSampledBitmapFromResource(resources,data,x,y);
            /*
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if ((y/(double)x)*width>height) {
                double k = (y*width-x*height)/(2*(double)y);
                Bitmap retBitmap =  Bitmap.createBitmap(bitmap,(int)k,0,(int)(width-2*k),height);
                if (retBitmap!=bitmap) {
                    bitmap.recycle();
                }
                return retBitmap;
            } else {
                double k = (x*height-y*width)/(2*(double)x);
                Bitmap retBitmap = Bitmap.createBitmap(bitmap,0, (int)k, width, (int)(height-2*k));
                if (retBitmap!=bitmap) {
                    bitmap.recycle();
                }
                return retBitmap;
            }
            */
            return bitmap;
        }


        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

}
