package com.youtube.ishtwar;

import com.youtube.ishtwar.db.BotDb;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class YouTubeNotificationsBot extends TelegramLongPollingBot {
    String botName = System.getenv("BOT_NAME");
    String botToken = System.getenv("BOT_TOKEN");
    List<Long> spamChatList = BotDb.getInstance().getChatsToSpamList();
    YouTubeListener youTubeListener;

    {
        if (App.httpPort != null) {
            youTubeListenerStart(Integer.parseInt(App.httpPort));
        }else System.out.println("httpPort is empty");
    }


    public void youTubeListenerStart(int port) {
        try {
            youTubeListener = new YouTubeListener(port);
            youTubeListener.setObserver(this);
        } catch (Exception e) {
            System.out.println("youTubeListener doesn't start");
            e.printStackTrace();
        }
    }


    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();

            if (update.getMessage().getText().equals("/chatId")) {
                String message = "This chatId is: " + "\"" + chatId + "\"";
                sendMessage(chatId, message);
            }

            if (update.getMessage().getText().equals("/info")) {
                String message = "This chatId is: " + "\"" + chatId + "\" \n" +
                        "Your userId is: " + "\"" + update.getMessage().getFrom().getId() + "\"";
                sendMessage(chatId, message);
            }

            if (update.getMessage().getText().equals("/start")) {
                sendMessage(chatId, "I'm alive! \n" +
                        "For run youtubeUpdates listener enter /setPort<PORT> without_spaces \n" +
                        "For start notification to some chat, add bot to chat and execute /spam command (/nospam for stop it) \n" +
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
            if (update.getMessage().getText().equals("/spam")) {
                System.out.println("Someone spam to me!");
                if (!spamChatList.contains(chatId)) {
                    String chatName = update.getMessage().getChat().getTitle();
                    if (BotDb.getInstance().addNewChatToSpamList(chatId, chatName)) {
                        spamChatList = BotDb.getInstance().getChatsToSpamList();
                    }
                    sendMessage(chatId, "Added to my spamlist");
                } else {
                    sendMessage(chatId, "This chat is already in my spamlist");
                }
            }

            if (update.getMessage().getText().equals("/stopspam")){
                System.out.println("I've try to stop spam to chat " + chatId);
                if(spamChatList.contains(chatId)){
                    if(BotDb.getInstance().removeChatFromSpamList(chatId)){
                        spamChatList = BotDb.getInstance().getChatsToSpamList();
                    }
                    sendMessage(chatId, "Removed from my spamlist");
                } else {
                    sendMessage(chatId, "This chat is not in my spamlist now");
                }
            }

            if (update.getMessage().getText().contains("/setPort")) {
                int port = Integer.parseInt(update.getMessage().getText().substring(8));
                youTubeListenerStart(port);
                sendMessage(chatId, "Looks like everything is OK");
            }

            if (update.getMessage().getText().contains("/sub")){
                new PubSubRegistration().subscribe();
                sendMessage(chatId, "Процедура подписки на Ishtvar вроде запустилась. hub.lease_seconds = 432000(5 суток). А получилось или нет узнаем позже");
            }
        }
    }


    void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage
                .builder()
                .chatId(Long.toString(chatId))
                .text(messageText)
                .build();
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

    void sendMessageWithKB(Long chatId, String messageText){
        SendMessage message = SendMessage
                .builder()
                .chatId(Long.toString(chatId))
                .text(messageText)
                .replyMarkup(twoButtonsKB())
                .build();
    }

    private InlineKeyboardMarkup twoButtonsKB(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        InlineKeyboardButton buttonFire = new InlineKeyboardButton();
        InlineKeyboardButton buttonShit = new InlineKeyboardButton();
        buttonFire.setText("ОГОНЬ!");
        buttonShit.setText("НЕ ОГОНЬ!");
        buttons.add(buttonFire);
        buttons.add(buttonShit);
        keyboard.add(buttons);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
        }
    }
}