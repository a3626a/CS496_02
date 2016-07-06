package kaist.cs496_02;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by q on 2016-07-06.
 */
public class JSONAdapter  extends ArrayAdapter {
    public JSONAdapter(Context context, @NonNull List<PhonePerson> objects) {
        super(context, R.layout.phone_person_entry, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        PhonePerson person = (PhonePerson) getItem(position);

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate( R.layout.phone_person_entry, parent, false);
        } else {
            view = convertView;
        }

        person.write(view);

        return view;
    }
}
