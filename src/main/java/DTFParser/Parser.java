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
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Parser extends ListenerAdapter {
    ArrayList<String> usedMemes;

    public void onReady(ReadyEvent event) {
        usedMemes = new ArrayList<>();

        System.setProperty("GOOGLE_CHROME_BIN", "/app/.apt/usr/bin/google-chrome");
        System.setProperty("CHROMEDRIVER_PATH", "/app/.chromedriver/bin/chromedriver");
        JDA jda = event.getJDA();
        Guild guild = jda.getGuildById("800740503914020875");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        Duration duration = Duration.between(now, ZonedDateTime.now());
        long initDelay = duration.getSeconds();

        ZonedDateTime fileClearDelay = now.withHour(0).withMinute(0).withSecond(0);

        if (now.compareTo(fileClearDelay) > 0) {
            fileClearDelay = fileClearDelay.plusDays(1);
        }

        Duration durationOfClear = Duration.between(now, fileClearDelay);
        long clearDelay = durationOfClear.getSeconds();

        ChromeOptions options = new ChromeOptions();
        options.setBinary("/app/.apt/usr/bin/google-chrome");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        /*  Check for a new meme once in 20 seconds */
        ScheduledExecutorService schedulerGetMemes = Executors.newScheduledThreadPool(1);
        schedulerGetMemes.scheduleAtFixedRate(() -> {
                    WebDriver driver = new ChromeDriver(options);
                    try {
                        driver.get("https://dtf.ru/kek/entries/top/day");
                        System.out.println("Driver boot");

                        WebElement page = driver.findElement(By.cssSelector("div.feed__chunk"));
                        WebElement meme = page.findElement(By.cssSelector("div.andropov_image"));
                        String memSrc = meme.getAttribute("data-image-src");

                        EmbedBuilder builder;

                        // Ignore if a new image is the same as a previous one
                        if (!memSrc.isEmpty() && !usedMemes.contains(memSrc)) {
                            builder = new EmbedBuilder()
                                    .setImage(memSrc)
                                    .setColor(Color.GREEN);

                            guild.getTextChannelById("800740503914020879").sendMessage(builder.build()).queue();
                            usedMemes.add(memSrc);
                        }
                    } finally {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        driver.quit();
                    }
                },
                initDelay,
                60,
                TimeUnit.SECONDS
        );

        schedulerGetMemes.scheduleAtFixedRate(() -> {
                    usedMemes.clear();
                },
                clearDelay,
                TimeUnit.DAYS.toSeconds(2),
                TimeUnit.SECONDS);
    }
}
