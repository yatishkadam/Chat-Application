package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.IMConnection;
import edu.northeastern.ccs.im.Message;

public class RandomUserTest {

	private static final String POSSIBLE_SET = "abcdefghijklmnopqrstuvwxyz";
	
	
	private Thread server;
	private ExecutorService exec = Executors.newFixedThreadPool(10);

	// Listen on this port until ...
	private IMConnection connect;
	
	@BeforeEach
	void setUp() throws Exception {
		
		server = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try {
					Prattle.main(new String[1]);
				} catch (IOException e) {
				}
			}
		});
	
		exec.submit(server);
	}

	@Test
	void test() throws IOException, InterruptedException {
		String[] args = new String[3];
		args[0] = "localhost";
		args[1] = "8613";
		args[2] = randomUserGenerator();
		
		do
		{
			System.out.println("connecting......");
			connect = new IMConnection(args[0],ServerConstants.getPort(), args[2]);
		} while(!connect.connect());
		
		if(connect.connectionActive())
		{
			connect.sendMessage("hello there");
		}
		
		Thread.sleep(200);
				
//		Message groupMsg = Message.makeGroupMessage("alice", ">MSD hello everyone in MSD group");
		//Prattle.sendGroupMessage(groupMsg);
		
		Message privateMsgFailure = Message.makePrivateMessage("madmax", "@bob Bob is not registered on the server");
		Prattle.sendPrivateMessasge(privateMsgFailure);

		Thread.sleep(200);

		Prattle.removeClient(null);	
	}
	
	private String randomUserGenerator()
	{
		StringBuilder str = new StringBuilder();
		int wordLength = ThreadLocalRandom.current().nextInt(3, 10);
		
		while(wordLength > 0)
		{
			int position = (int)(Math.random()*POSSIBLE_SET.length());
			str.append(POSSIBLE_SET.charAt(position));
			wordLength--;
		}
		
		return str.toString();
	}
}
