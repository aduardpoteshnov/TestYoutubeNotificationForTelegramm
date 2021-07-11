package com.youtube.ishtwar.db;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*To implement
 * 1. DB Connection
 * 2. Read/Write to admins list
 * 3. Read/Write to channels subscription list*/

public class BotDb {
    private static BotDb dataBase;
    private final String dbUser;
    private final String dbPwd;
    private final String dbUrl;
    private ArrayList<Long> chatsToSpam;
    private Map<String, Integer> alreadySentItems;
    private URI dbUri;

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
        System.out.println("DB Initialized");
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

    private void fillChatToSpamListFromDb() {
        chatsToSpam = new ArrayList<>();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM chatsId");
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                chatsToSpam.add(Long.parseLong(result.getString("chatId")));
            }
        } catch (SQLException e) {
            System.out.println("fillChatsToSpamListFromDb problem");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Long> getChatsToSpamList() {
        fillChatToSpamListFromDb();
        return chatsToSpam;
    }

    public boolean addNewChatToSpamList(Long chatId, String chatName) {
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
        } catch (SQLException e) {
            System.out.println("addNewChatToSpamList problem");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public boolean removeChatFromSpamList(Long chatId) {
        Connection connection = null;
        boolean result = true;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM chatsid WHERE chatid = (?)");
            statement.setString(1, String.valueOf(chatId));
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("removeChatFromSpamList problem");
            result = false;
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private void fillAlreadySentItemsListFromDb() {
        alreadySentItems = new HashMap<>();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM sentItems");
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                alreadySentItems.put(result.getString("videoId"), result.getInt("isSent"));
            }
        } catch (SQLException e) {
            System.out.println("fillAlreadySentItemsListFromDb problem");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Map<String, Integer> getSentItemsList() {
        fillAlreadySentItemsListFromDb();
        return alreadySentItems;
    }

    public void markAsSendById (String videoId){
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE sentitems SET issent = 1 WHERE videoid = (?)");
            statement.setString(1, videoId);
            statement.executeUpdate();

        }catch (SQLException e){
            System.out.println("Problems in SQL markAsSendById");
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

    public void addNewSentItem(HashMap<String, String> newSentItem) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO sentItems (id, videoId, published, updated, channelTitle, channelUri) " +
                            "values (DEFAULT, (?), (?), (?), (?), (?)) RETURNING id");
            statement.setString(1, newSentItem.get("videoId"));
            statement.setString(2, newSentItem.get("published"));
            statement.setString(3, newSentItem.get("updated"));
            statement.setString(4, newSentItem.get("channelTitle"));
            statement.setString(5, newSentItem.get("channelUri"));
            statement.executeQuery();
        } catch (SQLException e) {
            System.out.println("fill addNewSentItem problem");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}