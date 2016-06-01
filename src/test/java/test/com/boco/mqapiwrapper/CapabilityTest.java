package test.com.boco.mqapiwrapper;

import junit.framework.Assert;

import org.junit.Test;

import com.boco.mqapiwrapper.MessageClient;

public class CapabilityTest {

	/**
	 * 测试断开重连性能<br>
	 * 
	 * <p>测试过程：运行测试程序，在运行时，断开连接(拔网线)，等待几秒(20秒之内)重新连接(插网线)<br>
	 * 如果程序继续收发消息，并且最后测试的收发消息数相等，那么说明该程序的重连功能正常</p>
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
		System.out.println("sendMsgNum："+sendMsgNum);
		System.out.println("recvMsgNum："+recvMsgNum);
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
		System.out.println("接收速度：" + mc.getCurrRecvRate() + "条/秒");
		mc.disConnect();
	}
}
