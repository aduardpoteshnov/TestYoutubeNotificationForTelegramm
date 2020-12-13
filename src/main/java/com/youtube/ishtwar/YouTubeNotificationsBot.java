package com.youtube.ishtwar;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.List;

public class YouTubeNotificationsBot extends TelegramLongPollingBot {
    List<Integer> adminsList = BotDb.getInstance().getBotAdmins();
    List<Long> spamChatList = BotDb.getInstance().getChatsToSpam();
    YouTubeListener youTubeListener;

    public void youTubeListenerStart(int port) {
        try {
            youTubeListener = new YouTubeListener(port);
            youTubeListener.setObserver(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getBotUsername() {
        return "IshtvarBot";
    }

    @Override
    public String getBotToken() {
        return "1499712264:AAHZiYm-S3ww8dZmFSMf8QWdSQ0DovCkYJY";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();

            if (update.getMessage().getText().equals("/chatId")) {
                String message = "This chatId is: " + "\"" + chatId + "\"";
                sendMessage(chatId, message);
            }

            if (update.getMessage().getText().equals("/start")) {
                if (adminsList.contains(update.getMessage().getFrom().getId())) {
                    sendMessage(chatId, "I'm alive! \n" +
                            "For run youtubeUpdates listener enter /setPort<PORT> without_spaces \n" +
                            "For start notification to some chat, add bot to chat and execute /spam command \n" +
                            "Cause it early-test you should do previous steps every time after restart bot service on server \n" +
                            "For point youtube notification to your server instance do next \n" +
                            "1. Go to http://pubsubhubbub.appspot.com/subscribe \n" +
                            "2. Enter address of your server to <Callback URL> field \n" +
                            "3. Enter https://www.youtube.com/xml/feeds/videos.xml?channel_id=<YOUR CHANNEL ID> in Topic URL \n" +
                            "4. Set <Asynchronous> Verify type and set Mode to <Subscribe> \n" +
                            "5. Click Do It! \n\n" +
                            "You will see white page, it's ok \n" +
                            "Go back to http://pubsubhubbub.appspot.com/subscribe and fill diagnostics fields with previously used URLs \n" +
                            "Check <State> row on Subscription Details page, it should be <Verified> \n" +
                            "If you faced some problems contact https://t.me/EduardPoteshnov  @EduardPoteshnove");
                }
            }
            if (update.getMessage().getText().equals("/spam")) {
                if (adminsList.contains(update.getMessage().getFrom().getId())) {
                    if (!spamChatList.contains(chatId)) {
                        spamChatList.add(chatId);
                        sendMessage(chatId, "Added to my spamlist");
                    } else {
                        sendMessage(chatId, "This chat is already in my spamlist");
                    }
                }
            }
            if (update.getMessage().getText().contains("/setPort")){
                if(adminsList.contains(update.getMessage().getFrom().getId())){
                    int port = Integer.parseInt(update.getMessage().getText().substring(8));
                    youTubeListenerStart(port);
                    sendMessage(chatId, "Looks like everything is OK");
                }
            }
        }
    }

    void sendMessage(long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void newUpdateReceived(String urlToPost) {
        for (Long aLong : spamChatList) {
            sendMessage(aLong, urlToPost);
        }
    }

}