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
import org.openqa.selenium.interactions.Actions;

import java.awt.*;
import java.io.File;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Parser extends ListenerAdapter {
    //    ArrayDeque<String> images;
    //    ArrayDeque<String> usedImages;
    String previousMemeSrc = "";
    String path = System.getProperty("user.dir");


    public void onReady(ReadyEvent event) {
        System.setProperty("webdriver.chrome.driver", path + File.separator + "driver" + File.separator + "chromedriver.exe");
        JDA jda = event.getJDA();
        Guild guild = jda.getGuildById("800740503914020875");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        Duration duration = Duration.between(now, ZonedDateTime.now());
        long initDelay = duration.getSeconds();


        ScheduledExecutorService schedulerGetMemes = Executors.newScheduledThreadPool(1);
        schedulerGetMemes.scheduleAtFixedRate(() -> {
                    WebDriver driver = new ChromeDriver();
                    try {
                        driver.get("https://dtf.ru/kek");
                        System.out.println(driver.getPageSource());

                        WebElement button = driver.findElement(By.cssSelector("div.ui-rounded-button__link"));
                        //WebElement target = button.findElement(By.("Популярное"));
                        Actions action = new Actions(driver);
                        // Perform click-and-hold action on the element
                        action.clickAndHold(button).release().build().perform();
                        WebElement todayButton = driver.findElement(By.linkText("За день"));
                        action.clickAndHold(todayButton).release().build().perform();

                        WebElement page = driver.findElement(By.cssSelector("div.feed__chunk"));
                        WebElement meme = page.findElement(By.cssSelector("div.andropov_image"));
                        String memSrc = meme.getAttribute("data-image-src");

                        EmbedBuilder builder;

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
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        driver.close();
                    }
                },
                initDelay,
                10,
                TimeUnit.SECONDS
        );
//        images = new ArrayDeque<>();
//        usedImages = new ArrayDeque<>();
//
//        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Almaty"));
//
//        ZonedDateTime getMemesForDay = now.withHour(15).withMinute(4).withSecond(0);
//        ZonedDateTime startOfPosting = now.withHour(15).withMinute(5).withSecond(0);
//        ZonedDateTime nightTimePause = now.withHour(23).withMinute(0).withSecond(0);
//
//        if (now.compareTo(getMemesForDay) > 0) {
//            getMemesForDay = getMemesForDay.plusDays(1);
//        }
//        if (now.compareTo(startOfPosting) > 0) {
//            startOfPosting = startOfPosting.plusDays(1);
//        }
//        if (now.compareTo(nightTimePause) > 0) {
//            nightTimePause = nightTimePause.plusDays(1);
//        }
//
//        // duration between now and the beginning of the next first lesson
//        Duration durationUntilMemeFetching = Duration.between(now, getMemesForDay);
//        // in seconds
//        long initialDelayFetching = durationUntilMemeFetching.getSeconds();
//
//        Duration durationUntilFirstPost = Duration.between(now, startOfPosting);
//        long initialDelayFirstPost = durationUntilFirstPost.getSeconds();
//
//        Duration durationUntilNightRelease = Duration.between(now, nightTimePause);
//        long initialDelayNightRelease = durationUntilNightRelease.getSeconds();
//
//        ScheduledExecutorService schedulerGetMemes = Executors.newScheduledThreadPool(1);
//
//
//        System.setProperty("webdriver.chrome.driver", "D:\\java_projects\\UmaBot\\driver\\chromedriver.exe");
//
//        WebDriver driver = new ChromeDriver();
//
//        schedulerGetMemes.scheduleAtFixedRate(() -> {
//                    System.out.println("fetch");
//                    try {
//                        driver.get("https://dtf.ru/kek");
//
//                        JDA jda = event.getJDA();
//                        WebElement image = driver.findElement(By.cssSelector("div.feed__chunk"));
//                        List<WebElement> foundImages = image.findElements(By.cssSelector("div.andropov_image"));
//                        for (WebElement el : foundImages) {
//                            String src = el.getAttribute("data-image-src");
//                            if (!src.isEmpty()) {
//                                images.addLast(src);
//                            }
//                        }
//                    } finally {
//                        driver.quit();
//                    }
//                },
//                initialDelayFetching,
//                TimeUnit.DAYS.toSeconds(1),
//                TimeUnit.SECONDS);
//
//        ScheduledExecutorService schedulerFirstPost = Executors.newScheduledThreadPool(1);
//
//        schedulerFirstPost.scheduleAtFixedRate(() -> {
//                    JDA jda = event.getJDA();
//                    System.out.println("left" + images);
//
//                    /* Here specify the Guild and the Channel */
//
//                    Guild guild = jda.getGuildById("800740503914020875");
//                    if (images.size() > 0) {
//                        if (usedImages.size() > 20) {
//                            usedImages.clear();
//                        }
//
//                        String usedImage = images.peekFirst() == null ? "" : images.peekFirst();
//
//                        EmbedBuilder builder;
//
//
//                        builder = new EmbedBuilder()
//                                .setImage(images.pollFirst())
//                                .setColor(Color.GREEN);
//
//                        if (!usedImages.contains(usedImage)) {
//                            guild.getTextChannelById("800740503914020879").sendMessage(builder.build()).queue();
//                        }
//
//
//                        usedImages.addLast(usedImage);
//                    }
//                },
//
//                initialDelayFirstPost,
//                5,
//                TimeUnit.SECONDS);
//
//        ScheduledExecutorService schedulerNightRelease = Executors.newScheduledThreadPool(1);
//
//        schedulerNightRelease.scheduleAtFixedRate(() -> {
//                    System.out.println("clear");
//                    images.clear();
//                },
//
//                initialDelayNightRelease,
//                TimeUnit.DAYS.toSeconds(1),
//                TimeUnit.SECONDS);
    }
}

