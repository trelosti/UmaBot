import DTFParser.Parser;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.security.auth.login.LoginException;

public class Main extends ListenerAdapter {
    public static void main(String[] args) {
        JDABuilder jdaBuilder = JDABuilder.createDefault(Info.token);

        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setOwnerId("797050359952572436");
        builder.setPrefix("!");
        builder.setActivity(Activity.watching("Hentai"));
        builder.setHelpWord("help");

        jdaBuilder.addEventListeners(new Parser());

        try {
            jdaBuilder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }
}
