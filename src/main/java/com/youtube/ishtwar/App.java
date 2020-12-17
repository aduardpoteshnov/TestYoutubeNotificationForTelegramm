package com.youtube.ishtwar;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;


public class App {
    public static String httpPort;
    public static void main(String[] args) {
        if(args.length > 0){
            httpPort = args[0];
            System.out.println("Received HTTP port is: " + args[0]);
        }else System.out.println("HTTP port didn't receive, setup it by /setPort<PORT> command in telegramm interface");
        //Bot initialization and start
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new YouTubeNotificationsBot());
            System.out.println("Bot registration completed");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
