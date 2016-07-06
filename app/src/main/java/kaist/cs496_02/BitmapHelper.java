package kaist.cs496_02;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Network;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by q on 2016-06-30.
 */
public class BitmapHelper {

    public static Bitmap decodeSampledBitmapFromResource(int pos, int reqWidth, int reqHeight) {
        try {
            InputStream is = NetworkHelper.getImage("LeeChangHwan",pos);
            if (is != null) {
                Log.i("LogCat","[BITMAP]GETTING INPUT STREAM FROM NETWORK SUCCESS");
            } else {
                Log.i("LogCat","[BITMAP]GETTING INPUT STREAM FROM NETWORK FAILED");
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            byte[] image = buffer.toByteArray();

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(image,0,image.length,options);
            int orgWidth=options.outWidth;
            int orgHeight=options.outHeight;
            Log.i("LogCat","[BITMAP]IMAGE WIDTH: "+Integer.toString(orgWidth));
            Log.i("LogCat","[BITMAP]IMAGE HEIGHT: "+Integer.toString(orgHeight));
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            Log.i("LogCat","[BITMAP]IMAGE SAMPLING: "+ Integer.toString(options.inSampleSize));
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            BitmapRegionDecoder regionDecoder = BitmapRegionDecoder.newInstance(image,0,image.length, false);
            Log.i("LogCat","[BITMAP]DECODER CREATED");
            if (reqHeight/(double)reqWidth>orgHeight/(double)orgWidth) {
                int tWidth = (int)(orgHeight*(reqWidth/(double)reqHeight));
                Bitmap ret = regionDecoder.decodeRegion(new Rect((orgWidth-tWidth)/2,0,(orgWidth+tWidth)/2,orgHeight),options);
                Log.i("LogCat","[BITMAP]DECODE FAILED? : " + Boolean.toString(ret==null));
                return ret;
            } else {
                int tHeight = (int)(orgWidth*(reqHeight/(double)reqWidth));
                Bitmap ret =regionDecoder.decodeRegion(new Rect(0,(orgHeight-tHeight)/2,orgWidth,(orgHeight+tHeight)/2),options);
                Log.i("LogCat","[BITMAP]DECODE FAILED? : " + Boolean.toString(ret==null));
                return ret;
            }
        } catch(Exception e) {
            Log.i("LogCat","[BITMAP]DECODER CREATION FAILED BY SOME REASON");
            e.printStackTrace();
        }
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
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
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

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }


        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            int data = (Integer)params[0];
            int x = (Integer)params[1];
            int y = (Integer)params[2];

            Bitmap bitmap = BitmapHelper.decodeSampledBitmapFromResource(data,x,y);

            return bitmap;
        }


        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.i("LogCat","[BITMAP]BITMAP PREPARED");
            Log.i("LogCat","[BITMAP]IS BITMAP NULL? : " + Boolean.toString(bitmap==null));
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    Log.i("LogCat","[BITMAP]SET BITMAP");
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

}
