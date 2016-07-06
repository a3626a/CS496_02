package kaist.cs496_02;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by q on 2016-07-06.
 */
public class NetworkHelper {

    /*
    본 메소드는 네트워크 가용성을 확인하지 않습니다.
     */
    public static InputStream getImage(final String name, final int pos) {
        try {
            // Connect to the server
            URL url = new URL(MainActivity.server_url_gallery + "/" + name + "/" + Integer.toString(pos));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Setup header
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Send the data
            conn.connect();

            // Get response
            int response = conn.getResponseCode();
            InputStream is = conn.getInputStream();
            return is;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getGallerySize(final String name) {
        try {
            // Connect to the server
            URL url = new URL(MainActivity.server_url_gallery + "/" + name + "/length");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Setup header
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Send the data
            conn.connect();

            // Get response
            int response = conn.getResponseCode();
            InputStream is = conn.getInputStream();
            return is.read();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
