package edu.northeastern.ccs.im.profanityfilter;

import edu.northeastern.ccs.db.services.UserService;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.server.ClientRunnable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProfanityFilter {
    /*
Logger
 */
    private static final Logger logger = LogManager.getLogger(ClientRunnable.class);

    private static String filePath = System.getProperty("user.dir") + "/Word_Filter - Sheet1.csv";
    private static Map<String, String> allBadWords = new HashMap<>();
    private static Map<String, String> userBadWords = new HashMap<>();
    ProfanityFilter(){}
    /**
     * this is used to filter the words which we had designated as needs to be under parental control
     * @param message - message - the message
     * @return - string with replaced "desginated words"
     */
    public static Message filterMessageOnParentalControlledWords(Message message) {
        addWordOnlyFromUser(message.getRecName());
        String text = getFilteredText(message.getText());
        message.setText(text);
        return message;
    }

    /**
     * this is used to filter the words which we had designated as needs to be under parental control
     * @param messageText - String - the message text
     * @return - string with replaced "desginated words"
     */
    public static String getFilteredText(final String messageText){
        loadWordsToFilter();
        if (messageText == null || messageText.equalsIgnoreCase("")){
            return "";
        }
        ArrayList<String> wordToBeFiltered = new ArrayList<>();
        String filteredtext = messageText;

        //ReplaceAll similar looking charaters in the text.
        filteredtext = filteredtext.replaceAll("1", "i")
                .replaceAll("!", "i")
                .replaceAll("3", "e")
                .replaceAll("4", "a")
                .replaceAll("@", "a")
                .replaceAll("5", "s")
                .replaceAll("7", "t")
                .replaceAll("0", "o")
                .replaceAll("9", "g")
                .replaceAll("[0-9]", "");

        for (String s: filteredtext.split(" ")) {
            if (allBadWords.get(s.toLowerCase()) != null){
                wordToBeFiltered.add(s);
            }
        }
        for (String s: filteredtext.split(" ")) {
            if (userBadWords.get(s.toLowerCase()) != null){
                wordToBeFiltered.add(s);
            }
        }

        String censoredText = messageText;
        for (String wToChange: wordToBeFiltered) {
            char[] stars = new char[wToChange.length()];
            Arrays.fill(stars, '*');
            censoredText = censoredText.replaceAll(wToChange, new String(stars));
        }
        return censoredText;
    }

    /**
     * This is used to load the words to be filtered into memory
     */
    private static void loadWordsToFilter() {
        try (FileReader fr = new FileReader(filePath)){
            try (BufferedReader reader = new BufferedReader(fr)){
                String currentLine = "";
                while ((currentLine = reader.readLine()) != null) {
                    addWordToHashMap(currentLine);
                }
            }
        } catch (Exception e) {
            logger.info(e);
        }
    }
    public static void addWordToHashMap(String currentLine){
        String[] content = null;
        try {
            content = currentLine.split(",");
            if (content.length == 0) {
                //do nothing
            }
            for(int i =0;i<content.length;i++){
                final String word = content[i];
                // Make sure there are no capital letters in the spreadsheet
                allBadWords.put(word.replaceAll(" ", "").toLowerCase(), "dummy");
            }
        } catch (Exception e) {
            logger.info(e);
        }
    }


    public static void addWordOnlyFromUser(String userName){
        String wordList = UserService.getCensoredWordsForUser(userName);
        String[] content = null;
        userBadWords.clear();
        try {
            content = wordList.split(" |,");
            if (content.length == 0) {
                //do nothing
            }
            for(int i =0;i<content.length;i++){
                final String word = content[i];
                // Make sure there are no capital letters in the spreadsheet
                userBadWords.put(word.replaceAll(" ", "").toLowerCase(), "dummy");
            }
        } catch (Exception e) {
            logger.info(e);
        };
    }
}