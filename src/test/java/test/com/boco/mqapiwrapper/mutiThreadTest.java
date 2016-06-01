package test.com.boco.mqapiwrapper;

import junit.framework.Assert;

import org.junit.Test;

import com.boco.mqapiwrapper.MessageClient;

class multiThread implements Runnable {

	public MessageClient mc;

	public multiThread(String mqHost, int port, String qMgrName,
			String channelName, String sendQName, String recvQName) {
		mc = new MessageClient(mqHost, port, qMgrName, channelName, sendQName,
				recvQName);
	}

	@Override
	public void run() {
		boolean condition = false;
		condition = mc.connect();
		Assert.assertTrue("连接失败", condition);
		condition = mc.disConnect();
		Assert.assertTrue("关闭连接失败", condition);
	}
}

public class mutiThreadTest {

	@Test
	public void testMultiThreadConnection() {
		for (int i = 0; i < 100; i++) {
			multiThread mtt1 = new multiThread("10.21.3.171", 1414,
					"WNMS4_QM", "CH1", "TEST", "TEST");
			new Thread(mtt1).start();
			multiThread mtt2 = new multiThread("10.10.1.150", 6666,
					"MyTest", "MYCONN", "LQ", "LQ");
			new Thread(mtt2).start();
			multiThread mtt3 = new multiThread("10.10.1.150", 6666,
					"MyTest", "MYCONN", "LQ", "LQ");
			new Thread(mtt3).start();
			multiThread mtt4 = new multiThread("10.21.3.171", 1414,
					"WNMS4_QM", "CH1", "TEST", "TEST");
			new Thread(mtt4).start();
			multiThread mtt5 = new multiThread("10.21.3.171", 1414,
					"WNMS4_QM", "CH1", "TEST", "TEST");
			new Thread(mtt5).start();
			multiThread mtt6 = new multiThread("10.10.1.150", 6666,
					"MyTest", "MYCONN", "LQ", "LQ");
			new Thread(mtt6).start();
		}
	}
}
