package example;

import org.junit.Before;
import org.junit.Test;

import test.com.boco.mqapiwrapper.MessageClientTest;

import com.boco.mqapiwrapper.MessageClient;

/**
 * 接收消息<br>
 * 
 * @version 2.0
 * @author b
 */
public class RecvMessageExample {

	@Before
	public void sendTextMessage(){
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "EXAM", "");
		mc.connect();
		mc.sendTextMessage(MessageClientTest.TEST_MESSAGE.getBytes(), 15);
		mc.sendTextMessage(MessageClientTest.TEST_MESSAGE.getBytes(), 15);
		mc.disConnect();
	}
	
	@Test
	public void recvMQMsgExample(){
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "", "EXAM");
		mc.connect();
		mc.recvMQMsg();
		byte[] msg = mc.getMessageBuffer();
		System.out.println(new String(msg));
		mc.recvMQMsg(100);
		msg= mc.getMessageBuffer();
		System.out.println(new String(msg));
		mc.disConnect();
	}
	
	@Test
	public void recvTextMessageExample(){
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "", "EXAM");
		mc.connect();
		byte[] msg = mc.recvTextMessage();
		System.out.println(new String(msg));
		msg = mc.recvTextMessage(100);
		System.out.println(new String(msg));
		mc.disConnect();
	}
}
