import DTFParser.MemeSupplier;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import minesweeper.Game;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import weather.ForecastSender;

import javax.security.auth.login.LoginException;

public class Main extends ListenerAdapter {
    public static void main(String[] args) {
        JDABuilder jdaBuilder = JDABuilder.createDefault(Info.token);

        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setOwnerId("797050359952572436");
        builder.setPrefix("!");
        builder.setActivity(Activity.watching("Anime"));
        builder.setHelpWord("help");

        builder.addCommand(new ForecastSender());

        CommandClient client = builder.build();


        jdaBuilder.addEventListeners(new MemeSupplier());
        jdaBuilder.addEventListeners(client);
        jdaBuilder.addEventListeners(new Game());

        try {
            jdaBuilder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }

    }
}
