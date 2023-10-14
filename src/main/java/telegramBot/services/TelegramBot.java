package telegramBot.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegramBot.config.BotConfig;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    //Конфигурационный файл
    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
        list();
    }

    //создание меню
    public void list() {
        List<BotCommand> listofCommands = new ArrayList<>();

        listofCommands.add(new BotCommand("/infoaboutshelter", " Узнать информацию о приюте "));
        listofCommands.add(new BotCommand("/howgetpet", "Как взять животное из приюта "));
        listofCommands.add(new BotCommand("/getreportaboutpet", "Прислать отчет о питомце"));
        listofCommands.add(new BotCommand("/callvolontee", "озвать волонтера"));

        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
        }

    }


    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            long chatId = update.getMessage().getChatId();

            //вывел в отдельную переменную
            String firstName = update.getMessage().getChat().getFirstName();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, firstName);
                    break;
                default:
                    sendMessage(chatId, "Sorry, command was not recogrized");
            }
            //если приходит нам не строка, а другие данные
        } else if (update.hasCallbackQuery()) {
            //эти самые данные
            String callBackData = update.getCallbackQuery().getData();
            //получаем id сообшения и id чата
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            //проверка на какую кнопку нажал
            if(callBackData.equalsIgnoreCase("CAT_SHELTER")){
                sendMessage(chatId, "анкеты от приюта кошек");
            } else if (callBackData.equalsIgnoreCase("DOG_SHELTER")) {
                sendMessage(chatId, "анкеты от приюта собак");
            } else if (callBackData.equalsIgnoreCase("VOLONTEER")) {
                sendMessage(chatId, "Скоро ответит волонтер");
            }
        }
    }
    //Метод, который отправляет сообшение приветствие
    private void startCommandReceived(long chatId, String firstName) {
        String answer = "Привет " + firstName + ", мы приют из Астаны, помогаем вам приютить животное.";

        sendMessage(chatId, answer);
        //метод для кнопок
        register(chatId);
    }

    //метод для отправки сообщений
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    //создание самих кнопок
    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите, какой приют хотите посмотреть: ");

        //
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        //
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        //
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        //Создание кнопок: текс кнопок, некий идентификатор
        var catBtd = new InlineKeyboardButton();
        catBtd.setText("Приют для кошек");
        catBtd.setCallbackData("CAT_SHELTER");

        var dogBtn = new InlineKeyboardButton();
        dogBtn.setText("Приют для собак");
        dogBtn.setCallbackData("DOG_SHELTER");

        var volonteerBtn = new InlineKeyboardButton();
        volonteerBtn.setText("Вызвать волонтера");
        volonteerBtn.setCallbackData("VOLONTEER");

        //добавляем кнопки в коллекцию
        rowInLine.add(catBtd);

        rowInLine.add(dogBtn);

        rowInLine.add(volonteerBtn);

        //
        rowsInLine.add(rowInLine);

        //
        markupInLine.setKeyboard(rowsInLine);


        //
        message.setReplyMarkup(markupInLine);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
}
