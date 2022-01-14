package weather;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ForecastSender extends Command {
    public ForecastSender() {
        super.name = "forecast";
        super.help = "forecast-help";
        super.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        //TODO: translate info to Russian, add fancy stuff, create real forecast utility (not current weather)
        String[] parameters = commandEvent.getMessage().getContentRaw().split(" ");
        if (parameters.length == 2) {
            String city = parameters[1];
            WeatherController controller = new WeatherController(city);
            StringBuffer weatherInfo = controller.getWeather();
            //String[] output = (weatherInfo.toString()).split("\n");
            commandEvent.getChannel().sendMessage(weatherInfo.toString()).queue();
        } else if (parameters.length == 3) {
            String city = parameters[1] + "+" + parameters[2];
            WeatherController controller = new WeatherController(city);
            StringBuffer weatherInfo = controller.getWeather();
            //String[] output = (weatherInfo.toString()).split("\n");
            commandEvent.getChannel().sendMessage(weatherInfo.toString()).queue();
        } else {
            commandEvent.getChannel().sendMessage("Incorrect input, please check the name of a city\n" +
                    "Неправильная команда, пожалуйста, проверьте название города").queue();
        }

    }
}
