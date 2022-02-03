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
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.awt.*;
import java.io.File;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Parser extends ListenerAdapter {
    String previousMemeSrc = "";
    String path = System.getProperty("user.dir");

    public void onReady(ReadyEvent event) {
        System.setProperty("GOOGLE_CHROME_BIN", "/app/.apt/usr/bin/google-chrome");
        System.setProperty("CHROMEDRIVER_PATH", "/app/.chromedriver/bin/chromedriver");
        JDA jda = event.getJDA();
        System.out.println(path);
        Guild guild = jda.getGuildById("800740503914020875");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        Duration duration = Duration.between(now, ZonedDateTime.now());
        long initDelay = duration.getSeconds();

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
                        driver.get("https://dtf.ru/kek");

                        //System.out.println(driver.getPageSource());

                        WebElement button = driver.findElement(By.cssSelector("div.ui-rounded-button__link"));
                        Actions action = new Actions(driver);
                        action.clickAndHold(button).release().build().perform();
                        WebElement todayButton = driver.findElement(By.linkText("За день"));
                        action.clickAndHold(todayButton).release().build().perform();

                        WebElement page = driver.findElement(By.cssSelector("div.feed__chunk"));
                        WebElement meme = page.findElement(By.cssSelector("div.andropov_image"));
                        String memSrc = meme.getAttribute("data-image-src");

                        EmbedBuilder builder;

                        // Ignore if a new image is the same as a previous one
                        if (!memSrc.isEmpty() && !memSrc.equals(previousMemeSrc)) {
                            builder = new EmbedBuilder()
                                    .setImage(memSrc)
                                    .setColor(Color.GREEN);

                            guild.getTextChannelById("800740503914020879").sendMessage(builder.build()).queue();
                            previousMemeSrc = memSrc;
                        } else {
                            guild.getTextChannelById("800740503914020879").sendMessage("copy").queue();
                        }
                    } finally {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        driver.quit();
                    }
                },
                initDelay,
                20,
                TimeUnit.SECONDS
        );
    }
}

