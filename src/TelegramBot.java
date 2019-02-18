import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TelegramBot extends TelegramLongPollingBot {
    static List<Integer> players = new ArrayList<>();
    static Map<String, Boolean> phone = new HashMap<>(); //голосовал?
    static String BotToken, BotUsername, startMsg, voteMsg, alreadyVotedMsg, nRegisterMsg, wrongFormatMsg, wrongNumberMsg;
    static boolean doLogs;
    static Writer output;

    public static void main(String[] args) {
        // TODO ADD IT ALL IN SETTINGS
        //ADD YOUR PLAYERS HERE
        players.add(0); //player 0
        players.add(0); //player 1
        players.add(0); //player 2

        //ADD YOUR VOTERS HERE
        phone.put("munoon", false);

        settings();
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new TelegramBot());
        } catch (TelegramApiException e) {
            log("ERROR TELEGRAM BOT TOKEN");
            System.exit(0);
        }
        log("BOT WORKING");
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            if (message.getText().equals("/start")) {
                sndMsg(message, startMsg);
            }
            else {
                newVote(message);
            }
        }
    }

    public static void settings() {
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream("config.properties");
            properties.load(in);
        } catch (FileNotFoundException e) {
            log("FILE NOT FOUND EXCEPTION");
        } catch (IOException e) {
            log("IO EXCEPTION");
        }

        BotToken = properties.getProperty("BotToken");
        BotUsername = properties.getProperty("BotUsername", "Unknown Bot");

        if (properties.getProperty("DoLogs").equals("true")) {
            doLogs = true;
            setWriter();
        } else if (properties.getProperty("DoLogs").equals("false")) doLogs = false;
        else {
            System.out.println("Unknown format in DoLogs settings.");
            doLogs = false;
        }

        startMsg = properties.getProperty("StartMessage", "Hi! I'm voting bot. Write the number of the member you want to vote for!");
        voteMsg = properties.getProperty("VoteMessage", "You voted for the player %s");
        alreadyVotedMsg = properties.getProperty("AlreadyVotedMessage", "You have already voted!");
        nRegisterMsg = properties.getProperty("NotRegisteredMessage", "You are not registered to vote!");
        wrongFormatMsg = properties.getProperty("WrongFormatMessage", "You have entered the wrong format.");
        wrongNumberMsg = properties.getProperty("WrongNumberMessage", "You have entered the wrong number.");
    }

    public static void log(String s) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[dd.MM.yyyy][HH:mm:ss] ");

        if (doLogs) {
            try {
                getOutput().write(simpleDateFormat.format(date) + s + "\n");
                getOutput().flush();
//                getOutput().close();
            } catch (IOException e) {
                log("IO EXCEPTION");
            }
        }

        System.out.println(simpleDateFormat.format(date) + s);
    }

    public static void setWriter() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        try {
            output = new BufferedWriter(new FileWriter("log/log-" + simpleDateFormat.format(date) + ".txt", true));
        } catch (IOException e) {
            log("IO EXCEPTION");
        }
    }

    public static Writer getOutput() {
        return output;
    }

    public void newVote(Message message) {
        try {
            if (phone.containsKey(message.getFrom().getUserName())) {     // message.getText().contains("1") &&
                if (phone.get(message.getFrom().getUserName()).equals(false)) {
                    players.set(Integer.parseInt(message.getText()) - 1, players.get(Integer.parseInt(message.getText()) - 1) + 1);
                    log(String.format("A voice was given to the player %s from the user %s", message.getText(), message.getFrom().getUserName()));
                    getStatus();
                    sndMsg(message, String.format(voteMsg, message.getText())); // "You voted for the player " + message.getText()
                    phone.replace(message.getFrom().getUserName(), false, true);
                } else {
                    log(String.format("User %s trying to vote twice!", message.getFrom().getUserName()));
                    sndMsg(message, alreadyVotedMsg); // "You have already voted!"
                }
            } else {
                log(String.format("Unknown user found: %s", message.getFrom().getUserName()));
                sndMsg(message, nRegisterMsg);
            }
        } catch (NumberFormatException e) {
            log(String.format("%s enter invalid format", message.getFrom().getUserName()));
            sndMsg(message, wrongFormatMsg);
        } catch (IndexOutOfBoundsException e) {
            log(String.format("%s enter wrong number", message.getFrom().getUserName()));
            sndMsg(message, wrongNumberMsg);
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
            log("TELEGRAM API EXCEPTION");
        }
//        System.out.println("Bot send message " + s + " to " + message.getFrom().getUserName());
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