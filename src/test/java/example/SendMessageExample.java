package example;

import org.junit.After;
import org.junit.Test;

import test.com.boco.mqapiwrapper.MessageClientTest;

import com.boco.mqapiwrapper.MessageClient;

/**
 * ·¢ËÍÏûÏ¢<br>
 * 
 * @version 2.0
 * @author b
 */
public class SendMessageExample {

	@After
	public void recvTextMessage(){
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "", "EXAM");
		mc.connect();
		mc.recvTextMessage();
		mc.disConnect();
	}
	
	@Test
	public void sendTextMessageExample1(){
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "EXAM", "");
		mc.connect();
		mc.sendTextMessage(MessageClientTest.TEST_MESSAGE.getBytes(), 15);
		mc.disConnect();
	}
	
	@Test
	public void sendTextMessageExample2(){
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "EXAM", "");
		mc.connect();
		mc.sendTextMessage(MessageClientTest.TEST_MESSAGE.getBytes(), 15, 5);
		mc.disConnect();
	}

	@Test
	public void sendTextMessageExample3(){
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "EXAM", "");
		mc.connect();
		mc.sendTextMessage(MessageClientTest.TEST_MESSAGE.getBytes(), 15, "boco12344321");
		mc.disConnect();
	}
	
	@Test
	public void sendTextMessageExample4(){
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "EXAM", "");
		mc.connect();
		mc.sendTextMessage(MessageClientTest.TEST_MESSAGE.getBytes(), 15, 5, "boco12344321");
		mc.disConnect();
	}
}
