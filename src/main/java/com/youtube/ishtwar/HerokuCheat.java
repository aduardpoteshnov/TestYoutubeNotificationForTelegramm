package com.youtube.ishtwar;

import java.time.LocalDate;
import java.util.Random;

public class HerokuCheat extends Thread {
    private YouTubeNotificationsBot bot;
    private final long chatToSpamStatusMessages = -1001159168691L;
    private int spamCounter = 1;
    private final String startDate = LocalDate.now().toString();
    private final String spamMessage = "I'm alive and spam to you " + spamCounter + " times. I have spammed since " + startDate;

    public void setBot(YouTubeNotificationsBot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        do {
            bot.cheatMessage(spamMessage, chatToSpamStatusMessages);
            spamCounter++;
            try {
                sleep(calculateSleepTime(10, 25));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (spamCounter != 600);
        bot.cheatMessage("Cheat spam stopped, use /cheat again if needed",chatToSpamStatusMessages);
    }

    private static long calculateSleepTime(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min * 60000L;
    }
}
