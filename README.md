# Telegram Vote Bot
This is telegram bot that counts votes. It accepts votes only from certain members and prohibits re-voting.

# Installing
1. You need to download Java library for Telegram. [Download it you can here](https://github.com/rubenlagus/TelegramBots/releases). I personally use and develop on version 4.1. I recommend you to download `telegrambots-4.1-jar-with-dependencies.jar
`.
2. Set-up.
    * Write in Telegram to `@BotFather`. Write `/start` and then `/newbot`.
    * Follow the instructions to get token.
    * Open `src/TelegramBot` and find `main` class.
    * There you need to type your token and bot name in the appropriate fields.
    * Add players for which will vote users by command `players.add(0);`.
    * Add users who can vote by command `phone.put("username", false);` (you should type them usernames from small letter).
3. Profit.

# TODO
1. Extend functionality.
2. Make normal settings.
3. Add live voting on website.