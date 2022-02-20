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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Time;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Parser extends ListenerAdapter {
    String filePath = System.getProperty("/app") + File.separator + "resources" + File.separator + "usedMemes.txt";
    String lineSeparator = System.lineSeparator();

    public void onReady(ReadyEvent event) {
        System.out.println(System.getProperty("user.dir"));
        System.out.println(filePath);
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

        StringBuffer buffer = new StringBuffer();
        try {
            readFileWithMemes(filePath, buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*  Check for a new meme once in 20 seconds */
        ScheduledExecutorService schedulerGetMemes = Executors.newScheduledThreadPool(2);
        schedulerGetMemes.scheduleAtFixedRate(() -> {
                    WebDriver driver = new ChromeDriver(options);
                    try {
                        driver.get("https://dtf.ru/kek/entries/top/day");
                        System.out.println("Driver boot");


                        WebElement page = driver.findElement(By.cssSelector("div.feed__chunk"));
                        ArrayList<WebElement> memes = (ArrayList<WebElement>) page.findElements(By.cssSelector("div.andropov_image"));
                        String memeSrc = "";
                        for (WebElement el : memes)
                        {
                            if (buffer.indexOf(el.getAttribute("data-image-src")) == -1)
                            {
                                memeSrc = el.getAttribute("data-image-src");
                                break;
                            }
                        }
                        //WebElement meme = page.findElement(By.cssSelector("div.andropov_image"));
                        //String memSrc = meme.getAttribute("data-image-src");

                        EmbedBuilder builder;

                        // Ignore if a new image is the same as a previous one
                        if (!memeSrc.isEmpty()) {
                            builder = new EmbedBuilder()
                                    .setImage(memeSrc)
                                    .setColor(Color.GREEN);

                            guild.getTextChannelById("800740503914020879").sendMessage(builder.build()).queue();
                            writeToFileWithMemes(filePath, memeSrc);
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
                    try {
                        clearFileWithMemes(filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                clearDelay,
                TimeUnit.DAYS.toSeconds(2),
                TimeUnit.SECONDS);
    }

    private void readFileWithMemes(String path, StringBuffer buffer) throws IOException {
        String line;
        FileReader fileReader = new FileReader(path);
        BufferedReader br = new BufferedReader(fileReader);
        while ((line = br.readLine()) != null) {
            buffer.append(line).append(lineSeparator);
        }
    }

    private void writeToFileWithMemes(String pathToFile, String src) {
        Path path = Paths.get(pathToFile);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            writer.write(src + lineSeparator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFileWithMemes(String pathToFile) throws IOException {
        Path path = Paths.get(pathToFile);
        Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING);
    }
}

