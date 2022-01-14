package weather;

import java.util.ArrayList;

public class Forecast {
    private Coord coord;
    private ArrayList<Weather> weather;
    private String base;
    private Main main;
    private int visibility;
    private Wind wind;
    private Clouds clouds;
    private int dt;
    private Sys sys;
    private int timezone;
    private int id;
    private String name;
    private int cod;

    ArrayList<Weather> getWeather() {
        return weather;
    }

    void setWeather(ArrayList<Weather> weather) {
        this.weather = weather;
    }

    Main getMain() {
        return main;
    }

    void setMain(Main main) {
        this.main = main;
    }
}

class Coord {
    private double lon;
    private double lat;
}

class Weather {
    private int id;
    private String main;
    private String description;
    private String icon;

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }
}

class Main {
    private double temp;
    private double feels_like;
    private double temp_min;
    private double temp_max;
    private int pressure;
    private int humidity;
    private int sea_level;
    private int grnd_level;

    double getTemp() {
        return temp;
    }

    void setTemp(double temp) {
        this.temp = temp;
    }

    int getHumidity() {
        return humidity;
    }

    void setHumidity(int humidity) {
        this.humidity = humidity;
    }
}

class Wind {
    private double speed;
    private int deg;
    private double gust;
}

class Clouds {
    private int all;
}

class Sys {
    private int type;
    private int id;
    private String country;
    private int sunrise;
    private int sunset;
}