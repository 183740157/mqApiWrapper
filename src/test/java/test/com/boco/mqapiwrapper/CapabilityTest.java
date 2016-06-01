package test.com.boco.mqapiwrapper;

import junit.framework.Assert;

import org.junit.Test;

import com.boco.mqapiwrapper.MessageClient;

public class CapabilityTest {

	/**
	 * ���ԶϿ���������<br>
	 * 
	 * <p>���Թ��̣����в��Գ���������ʱ���Ͽ�����(������)���ȴ�����(20��֮��)��������(������)<br>
	 * �����������շ���Ϣ�����������Ե��շ���Ϣ����ȣ���ô˵���ó����������������</p>
	 */
	@Test
	public void testReconnect() {
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "EXAM", "EXAM");
		mc.connect();
		int sendMsgNum = 0;
		int recvMsgNum = 0;
		boolean condition = false;
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				condition=mc.sendTextMessage(MessageClientTest.TEST_MESSAGE.getBytes(), 15);
				if(condition)
					sendMsgNum++;
			}
			for (int j = 0; j < 100; j++) {
				condition=mc.recvMQMsg();
				if(condition)
					recvMsgNum++;
			}
		}
		System.out.println("sendMsgNum��"+sendMsgNum);
		System.out.println("recvMsgNum��"+recvMsgNum);
		Assert.assertTrue(sendMsgNum == recvMsgNum);
		mc.disConnect();
	}
	
	@Test
	public void testCurrRecvRate() {
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		mc.connect();
		for (int i = 0; i < 500; i++) {
			mc.sendTextMessage(MessageClientTest.TEST_MESSAGE.getBytes(), 15);
		}
		mc.setFrequencyOfPrintRecvRate(100);
		for (int i = 0; i < 500; i++) {
			mc.recvTextMessage();
		}
		System.out.println("�����ٶȣ�" + mc.getCurrRecvRate() + "��/��");
		mc.disConnect();
	}
}
