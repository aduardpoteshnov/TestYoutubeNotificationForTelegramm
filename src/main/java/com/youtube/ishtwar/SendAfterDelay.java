package com.youtube.ishtwar;

public class SendAfterDelay extends Thread {

    private final YouTubeNotificationsBot bot;
    private String videoId;


    SendAfterDelay(YouTubeNotificationsBot bot, String videoId) {
        this.bot = bot;
        this.videoId = videoId;
    }

    public void run() {
        System.out.println("SendAfterDelay instance is running");
        try {
            wait(600000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bot.newUpdateReceived(videoId);
    }
}
