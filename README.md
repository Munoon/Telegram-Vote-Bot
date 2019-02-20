# Telegram Vote Bot
This is telegram bot that counts votes. It accepts votes only from certain members and prohibits re-voting. All statistic write in file `vote status.txt`.

# Installing
1. First of all you need JDK. [Download it you can here](https://www.oracle.com/technetwork/java/javase/downloads/index.html).
2. You need to download Java library for Telegram. [Download it you can here](https://github.com/rubenlagus/TelegramBots/releases). I personally use and develop on version 4.1. I recommend you to download `telegrambots-4.1-jar-with-dependencies.jar
`.
3. Set-up.
    * Write in Telegram to `@BotFather`. Write `/start` and then `/newbot`.
    * Follow the instructions to get token.
    * Open `config.properties` and set the parameters according to the instructions.
    * There you need to type your token and bot name in the appropriate fields.
    * In `src/TelegramBot.java` class `main` add players for which will vote users by command `players.add(0);`.
    * Add users who can vote by command `phone.put("username", false);` (you should type them usernames from small letter).
4. Profit.

# TODO
1. Make normal settings.
3. Make voting by stickers.
4. Add support multi-languages.
5. Add live voting on website.
