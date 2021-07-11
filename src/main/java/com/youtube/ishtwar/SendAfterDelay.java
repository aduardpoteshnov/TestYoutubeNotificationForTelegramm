package com.youtube.ishtwar;

public class SendAfterDelay extends Thread {

    private final YouTubeListener listener;
    private final String videoId;


    SendAfterDelay(YouTubeListener listener, String videoId) {
        this.listener = listener;
        this.videoId = videoId;
    }

    public void run() {
        System.out.println("SendAfterDelay instance is running");
        try {
            sleep(600000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        listener.sendItemToBot(videoId);
    }
}
