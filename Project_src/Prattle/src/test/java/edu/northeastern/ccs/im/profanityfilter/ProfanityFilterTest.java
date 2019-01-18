package edu.northeastern.ccs.im.profanityfilter;

import edu.northeastern.ccs.im.Message;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfanityFilterTest {

    @Test
    void getCensoredText() {
        String s = "lol fUck y@u humping";
        assertEquals("lol **** y@u *******", ProfanityFilter.getFilteredText(s));
    }

    @Test
    void getCensoredMessage(){
        String s = "@alice lol fUck y@u humping";
        String expected = "lol **** y@u *******";
        Message testPrivateMessage = Message.makePrivateMessage("Kyle", s);
        assertEquals(expected, ProfanityFilter.filterMessageOnParentalControlledWords(testPrivateMessage).getText());
    }

    @Test
    void getCensoredMessageEmpty(){
        String s = "";
        assertEquals("", ProfanityFilter.getFilteredText(s));
    }

    @Test
    void getCensoredMessageNULL(){
        String s = null;
        assertEquals("", ProfanityFilter.getFilteredText(s));
    }

    
    @Test
    void addWordToHashMap(){
        String s ="";
        ProfanityFilter.addWordToHashMap(s);
    }
}