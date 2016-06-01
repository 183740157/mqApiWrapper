package example;

import org.junit.Test;

import test.com.boco.mqapiwrapper.MessageClientTest;

import com.boco.mqapiwrapper.MessageClient;

/**
 * 获取消息的相关属性<br>
 * 
 * @version 2.0
 * @author b
 */
public class GetMessagePropertyExample {

	/**
	 * 获取消息属性<br>
	 * 
	 * <p>
	 * 注：获取消息属性时，必须先接收消息，然后再获取所接收消息的对应属性
	 * </p>
	 */
	@Test
	public void getMessagePropertyExample() {
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "EXAM", "EXAM");
		mc.connect();
		mc.sendTextMessage(MessageClientTest.TEST_MESSAGE.getBytes(), 15, "boco12344321");
		mc.recvTextMessage();
		
		int messageType = mc.getMessageType();
		String msgCorrelationId = mc.getCurMsgCorrelationId();
		String recvQueueName = mc.getRecvQueueName();
		
		System.out.println("消息类型：" + messageType + "，" + "告警流水号："
				+ msgCorrelationId.trim() + "，" + "接收队列名：" + recvQueueName);
		mc.disConnect();
	}
}
