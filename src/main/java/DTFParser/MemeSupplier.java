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
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MemeSupplier extends ListenerAdapter {
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

        Duration durationOfClear = Duration.between(ZonedDateTime.now(), tableClearDelay);
        long clearDelay = durationOfClear.getSeconds();

        ChromeOptions options = new ChromeOptions();
        //options.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");

        options.setBinary("/app/.apt/usr/bin/google-chrome");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        Stack<MemePair> links = new Stack<>();

        /*  Check for a new meme once in 60 seconds */
        ScheduledExecutorService schedulerGetMemes = Executors.newScheduledThreadPool(1);
        schedulerGetMemes.scheduleAtFixedRate(() -> {
                    WebDriver driver = new ChromeDriver(options);
                    try {
                        //driver.manage().window().minimize();
                        driver.get("https://dtf.ru/kek/entries/top/day");
                        System.out.println("Driver boot");

                        WebElement page = driver.findElement(By.cssSelector("div.feed__chunk"));
                        List<WebElement> contents = page.findElements(By.cssSelector("div.content-container"));
                        //List<WebElement> memes = page.findElements(By.cssSelector("div.andropov_image"));

                        for (WebElement e : contents) {
                            String memeTitle = "";
                            String memeSrc = "";
                            if (!e.findElements(By.cssSelector("div.content-title")).isEmpty()) {
                                memeTitle = e.findElement(By.cssSelector("div.content-title"))
                                        .getText();
                            }

                            if (!e.findElements(By.cssSelector("div.andropov_image")).isEmpty()) {
                                memeSrc = e.findElement(By.cssSelector("div.andropov_image"))
                                        .getAttribute("data-image-src");
                            }

                            if (!memeSrc.isEmpty()) {
                                if (!databaseWorker.checkIfValueExists("memes", "link", memeSrc)) {
                                    System.out.println(memeSrc);
                                    links.push(MemePair.makeMemePair(memeTitle, memeSrc));
                                }
                            }
                        }

                        EmbedBuilder builder;

                        for (MemePair pair : links) {
                            // Ignore if a new image is the same as a previous one
                            if (!pair.src.isEmpty() && !databaseWorker.checkIfValueExists("memes", "link", pair.src)) {
                                builder = new EmbedBuilder()
                                        .setTitle(pair.title.isEmpty() ? " " : pair.title)
                                        .setImage(pair.src)
                                        .setColor(Color.GREEN);

                                guild.getTextChannelById("800740503914020879").sendMessage(builder.build()).queue();
                                databaseWorker.insertIntoTable("memes", "link", pair.src);
                                break;
                            }
                        }

                        links.clear();
                    } catch (Exception throwables) {
                        throwables.printStackTrace();
                    } finally {
                        try {
                            Thread.sleep(3000);
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
                    if (databaseWorker.getCountOfRows("public", "memes") > 1000) {
                        databaseWorker.deleteAndResetAllRows("memes", "id");
                        System.out.println("CLEARED");
                    }
                },
                clearDelay,
                TimeUnit.DAYS.toSeconds( 14),
                TimeUnit.SECONDS);
    }
}
