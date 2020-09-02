package com.github.zigcat;

import com.github.zigcat.exceptions.UnauthorizedException;
import com.github.zigcat.ormlite.models.UserBot;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Map<String, Integer> map = new HashMap<>();
    private Logger l = LoggerFactory.getLogger(Bot.class);
    private Dao<UserBot, Integer> userDao;

    {
        try {
            userDao = DaoManager.createDao(DatabaseConfiguration.source, UserBot.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        Message message = new Message();
        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        try{
            checkUserInBase(update);
        } catch (UnauthorizedException e){
            sendMessage.setChatId(e.getChatId())
                    .setText(e.getMessage());
            try {
                sendMessage(sendMessage);
            } catch (TelegramApiException telegramApiException) {
                telegramApiException.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkUserInBase(Update update) throws SQLException {
        Message message = update.getMessage();
        if(message.getContact() == null){
            throw new UnauthorizedException("Send me your contacts and I check you in my database.", message.getChatId());
        } else {
            for(UserBot u: userDao.queryForAll()){
                if(u.getPhone().equals(message.getContact().getPhoneNumber())){
                    u.setChatId(update.getMessage().getChatId().toString());
                    userDao.update(u);
                    throw new UnauthorizedException("I remember you, what's up", message.getChatId());
                }
            }
            throw new UnauthorizedException("Who you are? What are you doing in my swarm?! Relax, just joke, but you need to contact with my creator :)", message.getChatId());
        }
    }

    public String getBotUsername() {
        return "@handle_testbots_bot";
    }
    public String getBotToken() {
        return "971475765:AAHe2riPKbj9y9gr6gP5c3hhOXd0Ik6dSsE";
    }
}
