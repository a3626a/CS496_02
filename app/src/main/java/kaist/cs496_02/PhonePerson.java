package kaist.cs496_02;

import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;



public class PhonePerson {
    private String name;
    private String number;

    public PhonePerson(JSONObject jobj) {
        try {
            this.name = jobj.getString("name");
            this.number = jobj.getString("number");
        } catch (JSONException e) {
        }
    }

    public void write(View view) {
        TextView nametext = (TextView) view.findViewById(R.id.name);
        nametext.setText(this.name);
        TextView numbertext = (TextView) view.findViewById(R.id.number);
        numbertext.setText(this.number);
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("number", number);
        } catch (JSONException e) {}
        return obj;
    }
}
