package kaist.cs496_02;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by q on 2016-06-29.
 */
public class TabBFragment extends Fragment implements GridView.OnItemClickListener{

    public static final Integer[] mThumbIds = {
            R.drawable.sample_1, R.drawable.sample_2, R.drawable.sample_3, R.drawable.sample_4,
            R.drawable.sample_5, R.drawable.sample_6, R.drawable.sample_7, R.drawable.sample_8,
            R.drawable.sample_9, R.drawable.sample_10, R.drawable.sample_11, R.drawable.sample_12,
            R.drawable.sample_13, R.drawable.sample_14, R.drawable.sample_15, R.drawable.sample_16,
            R.drawable.sample_17, R.drawable.sample_18, R.drawable.sample_19, R.drawable.sample_20,
            R.drawable.sample_21
    };
    private ImageAdapter adapter;
    private Bitmap mPlaceHolderBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ImageAdapter(getActivity());
        mPlaceHolderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.waiting);
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_gallery, container, false);
        GridView gridView = (GridView) v.findViewById(R.id.GridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);

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

        public ImageAdapter(Context c){
            mContext = c;
        }
        public int getCount(){
            return mThumbIds.length;
        }
        public Object getItem(int position){
            return null;
        }
        public long getItemId(int position){
            return 0;
        }
        public View getView(int position, View convertView, ViewGroup parent){
            ImageView imageView;

            if(convertView == null){
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(BitmapHelper.dpToPx(100), BitmapHelper.dpToPx(100)));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(2, 2, 2, 2);
            }
            else{
                imageView = (ImageView) convertView;
            }

            BitmapHelper.loadBitmap(mThumbIds[position],imageView,BitmapHelper.dpToPx(100),BitmapHelper.dpToPx(100),mPlaceHolderBitmap,getResources());

            return imageView;
        }
    }
}