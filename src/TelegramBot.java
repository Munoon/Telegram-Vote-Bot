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
    static Map<Integer, String> playerName = new HashMap<>(); // ID / Name
    static Map<Integer, Integer> playerVotes = new HashMap<>(); // ID / Votes
    static Map<String, Integer> phone = new HashMap<>(); // Voter Name / ID for player voted
    static String BotToken, BotUsername, startMsg, voteMsg, nRegisterMsg, wrongFormatMsg, wrongNumberMsg, statusMsg, revoteMsg;
    static String ruVoteMsg, ruRevoteMsg, ruWrongNumberMsg, runRegisterMsg, ruWrongFormatMsg, ruStartMsg;
    static boolean doLogs;
    static Writer output;

    public static void main(String[] args) {
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
            if (message.getText().equals("/start") && message.getFrom().getLanguageCode().equals("ru")) {
                sndMsg(message, ruStartMsg);
            }
            else if (message.getText().equals("/start")) {
                sndMsg(message, startMsg);
            }
            else if (message.getFrom().getLanguageCode().equals("ru")) {
                newVoteRu(message);
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

        // ADDING PLAYERS
        String players = properties.getProperty("allPlayers");
        int id = 1;
        while (players.contains(",")) {
            playerName.put(id, players.substring(0, players.indexOf(",")));
            players = players.substring(players.indexOf(", ") + 2);
            id++;
        }
        playerName.put(id, players);

        // ADDING USERS
        String users = properties.getProperty("allUsers");
        while (users.contains(",")) {
            phone.put(users.substring(0, users.indexOf(",")), 0);
            users = users.substring(users.indexOf(", ") + 1);
        }
        phone.put(users, 0);

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
        nRegisterMsg = properties.getProperty("NotRegisteredMessage", "You are not registered to vote!");
        wrongFormatMsg = properties.getProperty("WrongFormatMessage", "You have entered the wrong format.");
        wrongNumberMsg = properties.getProperty("WrongNumberMessage", "You have entered the wrong number.");
        statusMsg = properties.getProperty("StatusMessage", "Votes for player %1$d: %2$d");
        revoteMsg = properties.getProperty("ReVoteMessage", "You have revoted for the player %s");

        ruStartMsg = properties.getProperty("RuStartMessage", "Привет! Я бот голосования! Напиши номер игрока за которого хочешь проголосовать.");
        ruVoteMsg = properties.getProperty("RuVoteMessage", "Вы проголосовали за игрока %s");
        runRegisterMsg = properties.getProperty("RuNotRegisteredMessage", "Вы не зарегестрированы в голосовании!");
        ruWrongFormatMsg = properties.getProperty("RuWrongFormatMessage", "Вы ввели неверный формат.");
        ruWrongNumberMsg = properties.getProperty("RuWrongNumberMessage", "Вы ввели неверное число.");
        ruRevoteMsg = properties.getProperty("RuReVoteMessage", "Вы изменили свой голос на игрока %s");

        for (int i = 1; i < playerName.size() + 1; i++) {
            playerVotes.put(i, 0);
        }
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
                if (!(Integer.parseInt(message.getText()) > playerName.size())) {
                    if (phone.get(message.getFrom().getUserName()) == 0) {
                        addVote(Integer.parseInt(message.getText()));
                        updateStatus();
                        sndMsg(message, String.format(voteMsg, getNameById(Integer.parseInt(message.getText())))); // "You voted for the player " + message.getText()
                        phone.put(message.getFrom().getUserName(), Integer.parseInt(message.getText()));
                        log(String.format("A voice was given to the player %s from the user %s", message.getText(), message.getFrom().getUserName()));
                    } else if (phone.get(message.getFrom().getUserName()) != 0) {
                        removeVote(phone.get(message.getFrom().getUserName()));
                        addVote(Integer.parseInt(message.getText()));
                        updateStatus();
                        sndMsg(message, String.format(revoteMsg, getNameById(Integer.parseInt(message.getText()))));
                        phone.put(message.getFrom().getUserName(), Integer.parseInt(message.getText()));
                        log(String.format("A voice was given to the player %s from the user %s (THAT WAS A REVOTE)", message.getText(), message.getFrom().getUserName()));
                    }
                }
                else {
                    log(String.format("%s enter wrong number", message.getFrom().getUserName()));
                    sndMsg(message, wrongNumberMsg);
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

    public void newVoteRu(Message message) {
        try {
            if (phone.containsKey(message.getFrom().getUserName())) {     // message.getText().contains("1") &&
                if (!(Integer.parseInt(message.getText()) > playerName.size())) {
                    if (phone.get(message.getFrom().getUserName()) == 0) {
                        addVote(Integer.parseInt(message.getText()));
                        updateStatus();
                        sndMsg(message, String.format(ruVoteMsg, getNameById(Integer.parseInt(message.getText())))); // "You voted for the player " + message.getText()
                        phone.put(message.getFrom().getUserName(), Integer.parseInt(message.getText()));
                        log(String.format("A voice was given to the player %s from the user %s", message.getText(), message.getFrom().getUserName()));
                    } else if (phone.get(message.getFrom().getUserName()) != 0) {
                        removeVote(phone.get(message.getFrom().getUserName()));
                        addVote(Integer.parseInt(message.getText()));
                        updateStatus();
                        sndMsg(message, String.format(ruRevoteMsg, getNameById(Integer.parseInt(message.getText()))));
                        phone.put(message.getFrom().getUserName(), Integer.parseInt(message.getText()));
                        log(String.format("A voice was given to the player %s from the user %s (THAT WAS A REVOTE)", message.getText(), message.getFrom().getUserName()));
                    }
                }
                else {
                    log(String.format("%s enter wrong number", message.getFrom().getUserName()));
                    sndMsg(message, ruWrongFormatMsg);
                }
            } else {
                log(String.format("Unknown user found: %s", message.getFrom().getUserName()));
                sndMsg(message, runRegisterMsg);
            }
        } catch (NumberFormatException e) {
            log(String.format("%s enter invalid format", message.getFrom().getUserName()));
            sndMsg(message, ruWrongFormatMsg);
        } catch (IndexOutOfBoundsException e) {
            log(String.format("%s enter wrong number", message.getFrom().getUserName()));
            sndMsg(message, ruWrongNumberMsg);
        }
    }

    public void addVote(int id) {
        playerVotes.put(id, playerVotes.get(id) + 1);
    }

    public void removeVote(int id) {
        playerVotes.put(id, playerVotes.get(id) - 1);
    }

    public Integer getVoteById(int id) {
        return playerVotes.get(id);
    }

    public String getNameById(int id) {
        return playerName.get(id);
    }

    public void updateStatus() {
//        System.out.println();
//        for (int i = 0; i < players.size(); i++) {
//            System.out.println(String.format("Player №%s, votes %s", i, players.get(i)));
//        }
//        System.out.println();

        try {
            Writer writer = new BufferedWriter(new FileWriter("vote status.txt", false));
            for (int i = 1; i < playerName.size() + 1; i++) {
                writer.write(String.format(statusMsg + "\n", getNameById(i), i, getVoteById(i)));
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            log("IO Exception");
        }

        log("Status updated");
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