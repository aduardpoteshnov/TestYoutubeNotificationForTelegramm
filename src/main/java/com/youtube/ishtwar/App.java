package com.youtube.ishtwar;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;


public class App {
    public static void main(String[] args) {
        //Bot initialization and start
        //Db and youtube listener will be initialize in Bot class
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        YouTubeNotificationsBot bot;
        System.out.println("PORT is: " + args[0]);

        try {
            botsApi.registerBot(bot = new YouTubeNotificationsBot());
            System.out.println("Bot registration completed");
            bot.youTubeListenerStart(Integer.parseInt(args[0]));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
