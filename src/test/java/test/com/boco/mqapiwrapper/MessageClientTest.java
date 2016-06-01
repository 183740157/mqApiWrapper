package test.com.boco.mqapiwrapper;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.boco.mqapiwrapper.MessageClient;
import com.ibm.mq.MQC;
import com.ibm.mq.MQMessage;

public class MessageClientTest {

	public static final String TEST_MESSAGE = "qwertyuiopasdfghjklzxcvbnm"
			+ "qwertyuiopasdfghjklzxcvbnm" + "qwertyuiopasdfghjklzxcvbnm"
			+ "qwertyuiopasdfghjklzxcvbnm" + "qwertyuiopasdfghjklzxcvbnm"
			+ "qwertyuiopasdfghjklzxcvbnm" + "qwertyuiopasdfghjklzxcvbnm";

	@Test
	public void testInit() {
		MessageClient mc = new MessageClient();
		boolean condition = false;
		condition = mc.init("10.21.3.171", 1414, "WNMS4_QM", "CH1", "TEST",
				"TEST", 1);
		Assert.assertTrue("��ʼ��ʧ��", condition);

		condition = mc.init("10.21.3.171", 1414, "WNMS4_QM", "CH1", "", "", 1);
		Assert.assertFalse("�շ����о�Ϊ�մ�����ʼ��Ӧ��ʧ��", condition);

		condition = mc.init("10.21.3.171", 1414, "WNMS4_QM", "CH1", "TEST",
				null, 1);
		Assert.assertFalse("���ն���Ϊnull����ʼ��Ӧ��ʧ��", condition);

		condition = mc.init("10.21.3.171", 1414, "WNMS4_QM", "CH1", null,
				"TEST", 1);
		Assert.assertFalse("���Ͷ���Ϊnull����ʼ��Ӧ��ʧ��", condition);

		condition = mc.init("10.21.3.171", 1414, null, null, "TEST", "TEST", 1);
		Assert.assertFalse("���й�������ͨ��Ϊnull����ʼ��Ӧ��ʧ��", condition);

		condition = mc.init("10.21.3.171", 1414, "WNMS4_QM", null, "TEST",
				"TEST", 1);
		Assert.assertFalse("ͨ��Ϊnull����ʼ��Ӧ��ʧ��", condition);

		condition = mc
				.init("10.21.3.171", 1414, null, "CH1", "TEST", "TEST", 1);
		Assert.assertFalse("���й�����Ϊnull����ʼ��Ӧ��ʧ��", condition);

		condition = mc.init("10.21.3.171", 1414, "", "", "TEST", "TEST", 1);
		Assert.assertFalse("���й�������ͨ��Ϊ�մ�����ʼ��Ӧ��ʧ��", condition);

		condition = mc.init("10.21.3.171", 1414, "", "CH1", "TEST", "TEST", 1);
		Assert.assertFalse("���й�����Ϊ�մ�����ʼ��Ӧ��ʧ��", condition);

		condition = mc.init("10.21.3.171", 1414, "WNMS4_QM", "", "TEST",
				"TEST", 1);
		Assert.assertFalse("ͨ��Ϊ�մ�����ʼ��Ӧ��ʧ��", condition);
	}

	@Test
	public void testMessageClient() {
		MessageClient mc1 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		Assert.assertTrue("��ʼ��ʧ��", mc1.isInitialized());

		MessageClient mc2 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST", 1, 1383, 3, 200, false);
		Assert.assertTrue("��ʼ��ʧ��", mc2.isInitialized());

		MessageClient mc3 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "", "TEST", 1, 1383, 3, 200, false);
		Assert.assertTrue("��ʼ��ʧ��", mc2.isInitialized());

		MessageClient mc4 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", null, null);
		Assert.assertFalse("�շ�����Ϊnull����ʼ��Ӧ��ʧ��", mc4.isInitialized());

		MessageClient mc5 = new MessageClient("10.21.3.171", 1414, null, null,
				"TEST", "TEST", 1, 1383, 3, 200, false);
		Assert.assertFalse("���й�������ͨ��Ϊnull����ʼ��Ӧ��ʧ��", mc5.isInitialized());

		MessageClient mc6 = new MessageClient(null, 1414, "WNMS4_QM", "CH1",
				"TEST", "TEST");
		Assert.assertFalse("����IPΪ�գ���ʼ��Ӧ��ʧ��", mc6.isInitialized());
	}

	@Test
	public void testConnection() {
		boolean condition = false;
		MessageClient mc1 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		condition = mc1.connect();
		Assert.assertTrue("����ʧ��", condition);
		condition = mc1.disConnect();
		Assert.assertTrue("�ر�����ʧ��", condition);
		condition = mc1.connect(false, false);
		Assert.assertTrue("��������ʧ��", condition);
		condition = mc1.disConnect();
		Assert.assertTrue("�ر�����ʧ��", condition);
		condition = mc1.connectWithDynamic();
		Assert.assertTrue("��̬����ʧ��", condition);
		condition = mc1.disConnect();
		Assert.assertTrue("�ر�����ʧ��", condition);

		MessageClient mc2 = new MessageClient("88.88.88.88", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		condition = mc2.connect();
		Assert.assertFalse("����IP�����������Ӧ��ʧ��", condition);

		MessageClient mc3 = new MessageClient("10.21.3.171", 1, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		condition = mc3.connect();
		Assert.assertFalse("�����˿������������Ӧ��ʧ��", condition);

		MessageClient mc4 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "");
		condition = mc4.connect();
		Assert.assertTrue("����ʧ��", condition);
		condition = mc4.disConnect();
		Assert.assertTrue("�ر�����ʧ��", condition);

		MessageClient mc5 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "", "TEST");
		condition = mc5.connect();
		Assert.assertTrue("����ʧ��", condition);
		condition = mc5.disConnect();
		Assert.assertTrue("�ر�����ʧ��", condition);
	}

	@Test
	public void testConnectionRepeat() {
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		boolean condition = false;
		byte[] msgBuf = null;
		mc.sendTextMessage(TEST_MESSAGE.getBytes(), 15);
		msgBuf = mc.recvTextMessage();
		Assert.assertEquals(TEST_MESSAGE, new String(msgBuf));
		condition = mc.disConnect();
		Assert.assertTrue("�ر�����ʧ��", condition);
	}

	@Test
	public void testSendMsg() {
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		byte[] msgBuf = TEST_MESSAGE.getBytes();
		boolean condition = false;
		mc.connect();

		condition = mc.sendTextMessage(msgBuf, 15);
		Assert.assertTrue("������Ϣʧ��", condition);

		condition = mc.sendTextMessage(msgBuf, 15, 4);
		Assert.assertTrue("������Ϣʧ��", condition);
		condition = mc.sendTextMessage(msgBuf, 15, 11);
		Assert.assertFalse("������ϢӦ��ʧ��", condition);
		condition = mc.sendTextMessage(msgBuf, 15, -11);
		Assert.assertFalse("������ϢӦ��ʧ��", condition);

		condition = mc.sendTextMessage(msgBuf, 15, "boco12344321");
		Assert.assertTrue("������Ϣʧ��", condition);
		condition = mc.sendTextMessage(msgBuf, 15, null);
		Assert.assertTrue("������Ϣʧ��", condition);

		for (int i = 0; i < 6; i++) {
			mc.recvTextMessage();
		}

		mc.disConnect();
	}

	@Test
	public void testRecvMsg() {
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		byte[] msgBuf = null;
		boolean condition = false;
		mc.connect();

		for (int i = 0; i < 6; i++) {
			condition = mc.sendTextMessage(TEST_MESSAGE.getBytes(), 15);
		}
		msgBuf = mc.recvTextMessage();
		Assert.assertEquals(TEST_MESSAGE, new String(msgBuf));
		msgBuf = mc.recvTextMessage(100);
		Assert.assertEquals(TEST_MESSAGE, new String(msgBuf));
		msgBuf = mc.recvTextMessage(-100);
		Assert.assertEquals(TEST_MESSAGE, new String(msgBuf));

		condition = mc.recvMQMsg();
		Assert.assertTrue("��Ϣ����ʧ��", condition);
		msgBuf = mc.getMessageBuffer();
		Assert.assertEquals(TEST_MESSAGE, new String(msgBuf));
		condition = mc.recvMQMsg(100);
		Assert.assertTrue("��Ϣ����ʧ��", condition);
		msgBuf = mc.getMessageBuffer();
		Assert.assertEquals(TEST_MESSAGE, new String(msgBuf));
		condition = mc.recvMQMsg(-100);
		Assert.assertTrue("��Ϣ����ʧ��", condition);
		msgBuf = mc.getMessageBuffer();
		Assert.assertEquals(TEST_MESSAGE, new String(msgBuf));
		// ����6����Ϣ������7�Σ����һ��Ӧ��ʧ��
		condition = mc.recvMQMsg();
		Assert.assertFalse("���ն���Ϊ�գ���Ϣ����Ӧ��ʧ��", condition);
		msgBuf = mc.getMessageBuffer();
		Assert.assertEquals(null, msgBuf);

		mc.disConnect();
	}

	@Test
	public void testGetMessageType() {
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		mc.connect();
		mc.sendTextMessage(TEST_MESSAGE.getBytes(), 15);
		mc.recvTextMessage();
		int msgType = mc.getMessageType();
		Assert.assertEquals(15, msgType);
		mc.disConnect();
	}

	@Test
	public void testGetCurMsgCorrelationId() {
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		mc.connect();
		mc.sendTextMessage(TEST_MESSAGE.getBytes(), 15, "boco12344321");
		mc.recvTextMessage();
		String msgCorrelationId = mc.getCurMsgCorrelationId();
		Assert.assertEquals("boco12344321", msgCorrelationId.trim());
		mc.disConnect();
	}

	@Test
	public void testRegAddAndRegDelete() {
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "CTRL.Q", "RECV");

		mc.connect();

		boolean condition = mc.regAdd("1,4,5,6", "MyTest.app", 20);
		Assert.assertTrue("����ʧ��", condition);

		condition = mc.regDelete("WNMS4_QM", "RECV");
		Assert.assertTrue("ȡ������ʧ��", condition);

		condition = mc.regAdd(null, null, 20);
		Assert.assertFalse("����Ӧ��ʧ��", condition);
		condition = mc.regDelete(null, null);
		Assert.assertFalse("ȡ������Ӧ��ʧ��", condition);

		condition = mc.regAdd("", "", 20);
		Assert.assertFalse("����Ӧ��ʧ��", condition);
		condition = mc.regDelete("", "");
		Assert.assertFalse("ȡ������Ӧ��ʧ��", condition);

		mc.disConnect();
	}

	@Test
	public void testGetTestNumber() {
		MessageClient mc = new MessageClient();
		mc.init("10.21.3.171", 1414, "WNMS4_QM", "CH1", "CTRL.Q", "RECVT", 1);
		boolean condition = false;
		mc.connect();
		mc.regAdd("1,4,5,6", "MyTest.app", 20);
		// �������ͼ��30��
		for (int i = 0; i < 60; i++) {
			mc.recvTextMessage(1000);
			if (mc.getMessageType() == 5) {
				condition = true;
				System.out.println("������Ϊ��" + mc.getTest_number());
				Assert.assertTrue("������Ӧ�ô���0", mc.getTest_number() > 0);
				break;
			}
		}
		mc.regDelete("WNMS4_QM", "RECVT");
		mc.disConnect();
		Assert.assertTrue(condition);
	}

	@Test
	public void testRecvMsgByUTF() {
		MQMessage msg = new MQMessage();
		// ����MQMD ��ʽ�ֶ�
		msg.format = MQC.MQFMT_NONE;
		msg.messageType = 15 + 65536;
		try {
			msg.writeUTF(TEST_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST", 1, 1383, 3, 200, false);
		mc.connect();
		mc.sendMQMessage(msg);
		Assert.assertEquals(TEST_MESSAGE,
				new String(mc.recvTextMessageByUTF(1000)));
		mc.disConnect();
	}

	@Test
	public void testGetRecvQueueName() {
		MessageClient mc1 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "");
		mc1.connect();
		Assert.assertEquals("", mc1.getRecvQueueName());
		mc1.disConnect();
		MessageClient mc2 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "", "TEST");
		mc2.connect();
		Assert.assertEquals("TEST", mc2.getRecvQueueName());
		mc2.disConnect();
	}

	@Test
	public void testExclusive() {
		MessageClient mc1 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST", 1, 1383, 3, 200, true);
		MessageClient mc2 = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "TEST", "TEST");
		boolean condition = false;
		mc1.connect();
		condition = mc2.connect();
		Assert.assertFalse("��Ϊmc1��ռ���ն��е����ӣ�Ӧ������ʧ��", condition);
		mc1.disConnect();
		mc2.disConnect();
	}
}