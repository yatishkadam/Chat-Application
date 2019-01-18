package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.server.ServerConstants;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is used to test the serverConstants class
 *
 * @author team-207-F18
 */
class ServerConstantsTest {

    /** Message to find the date. */
    protected static final String DATE_COMMAND = "What is the date?";

    /** Message to find the time. */
    protected static final String TIME_COMMAND = "What time is it?";

    /** Command for impatient users */
    protected static final String IMPATIENT_COMMAND = "What time is it Mr. Fox?";

    /** Name of the private user who broadcasts interesting responses. */
    protected static final String SERVER_NAME = "Prattle";

    protected static final String COOL_COMMAND = "OMG ROFL TTYL";

	/** Private user who responds to interesting queries. */
	protected static final String NIST_ID = "NIST";
	
    /**
     * Used to test the Response for date
     */
    @Test
    void getBroadcastResponsesForDate() {
        GregorianCalendar cal = new GregorianCalendar();
        Message dateMessage = Message.makeBroadcastMessage(NIST_ID,
                (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DATE) + "/" + cal.get(Calendar.YEAR));
        List<Message> messageList;
        messageList = ServerConstants.getBroadcastResponses(DATE_COMMAND);
        assertEquals(dateMessage.toString(), messageList.get(0).toString());
    }
     /**
      * Used to test the Response for Time
      */
      @Test
      void getBroadcastResponsesForTime() {
        GregorianCalendar cal = new GregorianCalendar();
        Message timeMessage = Message.makeBroadcastMessage(NIST_ID,
                cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));
        List<Message> messageList;
        messageList = ServerConstants.getBroadcastResponses(TIME_COMMAND);
        assertEquals(timeMessage.toString(), messageList.get(0).toString());
    }

     /**
      * Used to test the Response for Impatient
      */
    @Test
    void getBroadcastResponsesForImpatient() {
        GregorianCalendar cal = new GregorianCalendar();
        Message timeMessage = Message.makeBroadcastMessage("Mr. Fox",
                cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));
        List<Message> messageList;
        messageList = ServerConstants.getBroadcastResponses(IMPATIENT_COMMAND);
        assertEquals(timeMessage.toString(), messageList.get(1).toString());
    }


     /**
      *
      * Used to test the Response for other
      *
      */
     @Test
    void getBroadcastResponsesForOther() {
        Message message = Message.makeBroadcastMessage(SERVER_NAME, COOL_COMMAND);
        List<Message> messageList;
        messageList = ServerConstants.getBroadcastResponses("WTF");

       assertEquals(message.toString(),messageList.get(0).toString());
    }

     /**
      * 
      * Used to test the Response for null
      * 
      */
     @Test
    void getBroadcastResponsesForNull(){
        List<Message> messageList;
        messageList = ServerConstants.getBroadcastResponses("BOB");
        assertNull(messageList);
    }
     @Test
     void testServerName() {
    	 assertEquals("Prattle",ServerConstants.getServerName());
    	 
     }
}