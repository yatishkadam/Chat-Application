package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.IMConnection;
import edu.northeastern.ccs.im.Message;

public class TestLoginWorkFlow {

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
		args[2] = "lopez";
		
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
		
		Message msg = Message.makeBroadcastMessage("zpfi", "Die");
		//Prattle.broadcastMessage(msg);
		Thread.sleep(200);
		
		Message privateMsg = Message.makePrivateMessage("alice", "@madmax Hello");
		//Prattle.sendPrivateMessasge(privateMsg);
		
		Message privateMsgFailure = Message.makePrivateMessage("prasad", "@bob Bob is not registered on the server");
		Prattle.sendPrivateMessasge(privateMsgFailure);

		Thread.sleep(200);

		Prattle.removeClient(null);	
	}
}
