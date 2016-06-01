package example;

import org.junit.Test;

import test.com.boco.mqapiwrapper.MessageClientTest;

import com.boco.mqapiwrapper.MessageClient;

/**
 * ��ȡ��Ϣ���������<br>
 * 
 * @version 2.0
 * @author b
 */
public class GetMessagePropertyExample {

	/**
	 * ��ȡ��Ϣ����<br>
	 * 
	 * <p>
	 * ע����ȡ��Ϣ����ʱ�������Ƚ�����Ϣ��Ȼ���ٻ�ȡ��������Ϣ�Ķ�Ӧ����
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
		
		System.out.println("��Ϣ���ͣ�" + messageType + "��" + "�澯��ˮ�ţ�"
				+ msgCorrelationId.trim() + "��" + "���ն�������" + recvQueueName);
		mc.disConnect();
	}
}
