package kaist.cs496_02;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by q on 2016-07-07.
 */
public class FacbookLoginWrapper {

    private JSONArray arr;
    private String name;

    public FacbookLoginWrapper() {
    }

    public void setName(String name) throws JSONException {
        this.name = name;
        alert();
    }

    public void setArr(JSONArray arr) throws JSONException {
        this.arr = arr;
        alert();
    }

    private void alert() throws JSONException {
        if (name != null && arr != null)
            TabAFragment.fbpb.readDataFromFB(arr, name);
    }

}
