package com.youtube.ishtwar;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;


/*To implement
* 1. DB Connection
* 2. Read/Write to admins list
* 3. Read/Write to channels subscription list*/

public class BotDb {
    private static volatile BotDb dataBase;
    private final ArrayList<Integer> botAdmins;
    private final ArrayList<Long> chatsToSpam;


    private BotDb(){
        botAdmins = new ArrayList<>();
        botAdmins.add(372981941); //Мыш
        botAdmins.add(396124623); //Игорь

        chatsToSpam = new ArrayList<>();
        chatsToSpam.add(-1001159168691L); //Тестовый чатик
    }

    public static BotDb getInstance() {
        BotDb localDataBase = dataBase;
        if (localDataBase == null) {
            synchronized (BotDb.class) {
                localDataBase = dataBase;
                if (localDataBase == null) {
                    dataBase = localDataBase = new BotDb();
                }
            }
        }
        return localDataBase;
    }

    public ArrayList<Integer> getBotAdmins() {
        return botAdmins;
    }

    public List<Long> getChatsToSpam() {
        return chatsToSpam;
    }
}
