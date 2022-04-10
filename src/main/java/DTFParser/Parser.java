package DTFParser;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.openqa.selenium.chrome.ChromeOptions;

import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Parser extends ListenerAdapter {
    DatabaseWorker databaseWorker = new DatabaseWorker();
    //String path = System.getProperty("user.dir");

    public void onReady(ReadyEvent event) {

        System.setProperty("GOOGLE_CHROME_BIN", "/app/.apt/usr/bin/google-chrome");
        System.setProperty("CHROMEDRIVER_PATH", "/app/.chromedriver/bin/chromedriver");
        //System.setProperty("webdriver.chrome.driver", path + File.separator + "driver" + File.separator + "chromedriver.exe");
        JDA jda = event.getJDA();
        Guild guild = jda.getGuildById("800740503914020875");

        ZonedDateTime tableClearDelay = ZonedDateTime.now(ZoneId.systemDefault());

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        Duration duration = Duration.between(now, ZonedDateTime.now());
        long initDelay = duration.getSeconds();

        if (now.compareTo(tableClearDelay) > 0) {
            tableClearDelay = tableClearDelay.plusDays(1);
        }

        Duration durationOfClear = Duration.between(now, tableClearDelay);
        long clearDelay = durationOfClear.getSeconds();

        ChromeOptions options = new ChromeOptions();
        //options.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");

        options.setBinary("/app/.apt/usr/bin/google-chrome");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        Stack<String> links = new Stack<>();

        /*  Check for a new meme once in 60 seconds */
        ScheduledExecutorService schedulerGetMemes = Executors.newScheduledThreadPool(1);
        schedulerGetMemes.scheduleAtFixedRate(() -> {
                    WebDriver driver = new ChromeDriver(options);
                    try {
                        driver.get("https://dtf.ru/kek/entries/top/day");
                        System.out.println("Driver boot");

                        WebElement page = driver.findElement(By.cssSelector("div.feed__chunk"));
                        List<WebElement> memes = page.findElements(By.cssSelector("div.andropov_image"));

                        for (WebElement e : memes) {
                            String memSrc = e.getAttribute("data-image-src");
                            if (!databaseWorker.checkIfValueExists("memes", "link", memSrc)) {
                                //System.out.println(memSrc);
                                links.push(memSrc);
                            }
                        }

                        EmbedBuilder builder;

                        for (String memSrc : links) {
                            // Ignore if a new image is the same as a previous one
                            if (!memSrc.isEmpty() && !databaseWorker.checkIfValueExists("memes", "link", memSrc)) {
                                builder = new EmbedBuilder()
                                        .setImage(memSrc)
                                        .setColor(Color.GREEN);

                                guild.getTextChannelById("800740503914020879").sendMessage(builder.build()).queue();
                                databaseWorker.insertIntoTable("memes", "link", memSrc);
                                break;
                            }
                        }

                        links.clear();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    } finally {
                        driver.quit();
                    }
                },
                initDelay,
                60,
                TimeUnit.SECONDS
        );

        schedulerGetMemes.scheduleAtFixedRate(() -> {
                    if (databaseWorker.getCountOfRows("public", "memes") > 1) {
                        databaseWorker.deleteAndResetAllRows("memes", "id");
                        System.out.println("CLEARED");
                    }
                },
                clearDelay,
                TimeUnit.DAYS.toSeconds( 14),
                TimeUnit.SECONDS);
    }
}
