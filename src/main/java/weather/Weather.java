package weather;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class Weather extends ListenerAdapter {
    public void onReady(ReadyEvent event) {
        BufferedReader reader;
        String line;
        StringBuffer response = new StringBuffer();
        String key = "afef806ca4262c3ff14053d67e5f33a9";
        String city = "Bishkek";
        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            //System.out.println(status);

            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }
            else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }
            System.out.println(response.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    static String parseJson(String content) {
//        JSONArray weatherSpecifications = new JSONArray(content);
//
//    }
}
