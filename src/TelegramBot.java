import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramBot extends TelegramLongPollingBot {
    static List<Integer> players = new ArrayList<>();
    static Map<String, Boolean> phone = new HashMap<>(); //голосовал?
    static String BotToken = "";
    static String BotUsername = "";

    public static void main(String[] args) {
        /*
        WRITE YOUR SETTINGS HERE.

        FIRSTLY YOU NEED TO SEND /start TO @BotFather
        THEN TYPE /newbot AND FOLLOW THE INSTRUCTION
        AT THE END YOU WILL GET TOKEN
         */
        BotToken = "WRITE BOT TOKEN HERE";
        BotUsername = "WRITE BOT USERNAME HERE";

        //ADD YOUR PLAYERS HERE
        players.add(0); //player 0
        players.add(0); //player 1
        players.add(0); //player 2

        //ADD YOUR VOTERS HERE
        phone.put("username", false);

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
                sndMsg(message, "Hi! I'm voting bot. Write the number of the member you want to vote for!");
            }
            else {
                newVote(message);
            }
        }
    }

    public void newVote(Message message) {
        try {
            if (phone.containsKey(message.getFrom().getUserName())) {     // message.getText().contains("1") &&
                if (phone.get(message.getFrom().getUserName()).equals(false)) {
                    phone.replace(message.getFrom().getUserName(), false, true);
                    players.set(Integer.parseInt(message.getText()) - 1, players.get(Integer.parseInt(message.getText()) - 1) + 1);
                    System.out.println("A voice was given to the player " + message.getText() + " from the user " + message.getFrom().getUserName());
                    getStatus();
                    sndMsg(message, "You voted for the player " + message.getText());
                } else {
                    System.out.println("User " + message.getFrom().getUserName() + " trying to vote twice!");
                    sndMsg(message, "You have already voted!");
                }
            } else {
                System.out.println("Unknown user found: " + message.getFrom().getUserName());
                sndMsg(message, "You are not registered to vote!");
            }
        } catch (NumberFormatException e) {
            System.out.println(message.getFrom().getUserName() + " enter invalid format");
            sndMsg(message, "You have entered the wrong format.");
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
        return BotUsername;
    }

    @Override
    public String getBotToken() {
        return BotToken;
    }
}