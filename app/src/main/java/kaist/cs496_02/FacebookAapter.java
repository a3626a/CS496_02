package kaist.cs496_02;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONException;

/**
 * Created by q on 2016-07-06.
 */
public class FacebookAapter extends BaseAdapter {

    private Activity activity;

    public FacebookAapter(Activity activity) {
        this.activity=activity;
    }

    @Override
    public int getCount() {
        return TabAFragment.fbpb.getPhonebook().size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.phone_person_entry, parent, false);
        } else {
            view = convertView;
        }

        try {
            TextView nametext = (TextView) view.findViewById(R.id.name);
            nametext.setText(TabAFragment.fbpb.getPhonebook().get(position).getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*
        TextView numbertext = (TextView) view.findViewById(R.id.number);
        numbertext.setText(this.number);
        */

        return view;
    }
}
