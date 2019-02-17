import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramBot extends TelegramLongPollingBot {
    static List<Integer> players = new ArrayList<>();
    static Map<String, Boolean> phone = new HashMap<>(); //голосовал?

    public static void main(String[] args) {
        //ADD YOUR PLAYERS HERE
        players.add(0); //игрок 1
        players.add(0); //игрок 2
        players.add(0); //игрок 3

        //ADD YOUR VOTERS HERE
        phone.put("munoon", false);

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new TelegramBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        System.out.println("BOT WORKING");
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            if (message.getText().equals("/start")) {
                sndMsg(message, "Привет! Я бот голосования. Напиши число участника за которого хочешь проголосовать!");
            }
            else {
                newVote(message);
            }
        }
    }

    public void newVote(Message message) {
        if (phone.containsKey(message.getFrom().getUserName())) {     // message.getText().contains("1") &&
            if (phone.get(message.getFrom().getUserName()).equals(false)) {
                phone.replace(message.getFrom().getUserName(), false, true);
                players.set(Integer.parseInt(message.getText()) - 1, players.get(Integer.parseInt(message.getText()) - 1) + 1);
                System.out.println("Был отдан голос игроку " + message.getText() + " от зрителя " + message.getFrom().getUserName());
                getStatus();
                sndMsg(message, "Вы проголосовали за игрока " + message.getText());
            }
            else {
                System.out.println("Зритель " + message.getFrom().getUserName() + " пытаеться проголосвать два раза!");
                sndMsg(message, "Вы уже голосовали!");
            }
        }
        else {
            System.out.println("Найден неизвестный зритель: " + message.getFrom().getUserName());
            sndMsg(message, "Вы не зарегестрированы в голосовании!");
        }
    }

    public void getStatus() {
        System.out.println();
        for (int i = 0; i < players.size(); i++) {
            System.out.println(String.format("Player №%s, votes %s", i, players.get(i)));
        }
        System.out.println();
    }

    private void sndMsg(Message message, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
//        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        System.out.println("Bot send message " + s + " to " + message.getFrom().getUserName());
    }

    @Override
    public String getBotUsername() {
        return "Telegram Bot By Munoon";
    }

    @Override
    public String getBotToken() {
        return "783977786:AAE0kzDPubTe-DaJ2I79gH-3IbscgXYOWII";
    }
}