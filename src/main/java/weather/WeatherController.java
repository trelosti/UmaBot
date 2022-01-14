package weather;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WeatherController extends ListenerAdapter {
    public StringBuffer getWeather(String aKey, String aCity) {
        StringBuffer shortForecast = null;
        BufferedReader reader;
        String line;
        StringBuffer response = new StringBuffer();
        String key = "afef806ca4262c3ff14053d67e5f33a9";
        String city = "Bishkek";
        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

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
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }
            //System.out.println(response.toString());
            shortForecast = parseWeatherJson(response.toString());


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (shortForecast != null) {
            return shortForecast;
        } else {
            return new StringBuffer("Failed to get weather info");
        }
    }

    static StringBuffer parseWeatherJson(String content) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        StringBuffer result = new StringBuffer();
        try {
            Forecast forecast = mapper.readValue(content, Forecast.class);
            String description = forecast.getWeather().get(0).getDescription();
            String temp = (int)forecast.getMain().getTemp() + "°C";
            String humidity = forecast.getMain().getHumidity() + "%";
            result.append(description).append("\n").append(temp).append("\n").append(humidity).append("\n");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;
    }
}
