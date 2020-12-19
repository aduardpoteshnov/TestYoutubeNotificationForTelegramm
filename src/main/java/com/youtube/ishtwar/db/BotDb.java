package com.youtube.ishtwar.db;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/*To implement
 * 1. DB Connection
 * 2. Read/Write to admins list
 * 3. Read/Write to channels subscription list*/

public class BotDb {
    private static BotDb dataBase;
    private ArrayList<Integer> botAdmins;
    private final ArrayList<Long> chatsToSpam;
    private URI dbUri;
    private final String dbUser;
    private final String dbPwd;
    private final String dbUrl;


    private BotDb() {
        try {
            dbUri = new URI(System.getenv("DATABASE_URL"));
        } catch (URISyntaxException e) {
            System.out.println("Something went wrong with DB URI");
            e.printStackTrace();
        }

        dbUser = dbUri.getUserInfo().split(":")[0];
        dbPwd = dbUri.getUserInfo().split(":")[1];
        dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

        botAdmins = new ArrayList<>();
        fillBotAdminsListFromDb();

        chatsToSpam = new ArrayList<>();
        fillChatToSpamListFromDb();
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

    private void fillBotAdminsListFromDb() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM botadmin");
            ResultSet result = statement.executeQuery();
            while (result.next()){
                botAdmins.add(Integer.parseInt(result.getString("userId")));
            }
        }catch (SQLException e){
            System.out.println("fillBotAdminsListFromDb problem");
            e.printStackTrace();
        } finally {
            if(connection != null){
                try {
                    connection.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void fillChatToSpamListFromDb() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM chatsId");
            ResultSet result = statement.executeQuery();
            while (result.next()){
                chatsToSpam.add(Long.parseLong(result.getString("chatId")));
            }
        }catch (SQLException e){
            System.out.println("fillChatsToSpamListFromDb problem");
            e.printStackTrace();
        } finally {
            if(connection != null){
                try {
                    connection.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean addNewChatToSpamList(Long chatId, String chatName){
        Connection connection = null;
        boolean result = false;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO chatsid (id, chatid, chatname) " +
                            "values (DEFAULT, (?), (?)) RETURNING id");
            statement.setString(1, String.valueOf(chatId));
            statement.setString(2, chatName);
            result = statement.executeQuery().next();
            if(result) fillChatToSpamListFromDb();
        }catch (SQLException e){
            System.out.println("fillBotAdminsListFromDb problem");
            e.printStackTrace();
        } finally {
            if(connection != null){
                try {
                    connection.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
