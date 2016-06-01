package com.boco.mqapiwrapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQC;
import com.ibm.mq.MQMessage;

/**
 * MQ ��Ϣ�շ��ͻ��� API��װ��<br>
 * 
 * <p>
 * ������Ҫ�ṩ�˸澯ƽ̨����MQ�������շ����ݵȲ���<br>
 * �������ʽ����:
 * 
 * <blockquote>
 * 
 * <pre>
 * MessageClient mc = new MessageClient(&quot;10.21.3.171&quot;, 1414, &quot;WNMS4_QM&quot;, &quot;CH1&quot;,
 * 		&quot;SEND&quot;, &quot;RECV&quot;);
 * mc.connect(false, false);
 * byte[] msgBuf = &quot;12345678&quot;.getBytes();
 * mc.sendTextMessage(msgBuf, 15);
 * msgBuf = mc.recvTextMessage();
 * System.out.println(&quot;���ճɹ���&quot; + &quot;����Ϊ��&quot; + mc.getRecvQueueName() + &quot;����Ϣ����Ϊ&quot;
 * 		+ mc.getMessageType() + &quot;����ϢΪ&quot; + new String(msgBuf));
 * mc.disConnect();
 * </pre>
 * 
 * </blockquote>
 * 
 * �������������һ������MQ���շ����ݣ���ȡ�����Ϣ���Լ��ر����ӵĲ���<br>
 * ������ϢҲ����ͨ������һ�ַ�ʽ��
 * 
 * <blockquote>
 * 
 * <pre>
 * mc.recvMQMsg();
 * byte[] msgBuf = mc.getMessageBuffer();
 * </pre>
 * 
 * </blockquote>
 * 
 * {@link #regAdd(String, String, int)}���ĺ�{@link #regDelete(String, String)}
 * ȡ�����ģ� �÷����£�
 * 
 * <blockquote>
 * 
 * <pre>
 * MessageClient mc = new MessageClient(&quot;10.21.3.171&quot;, 1414, &quot;WNMS4_QM&quot;, &quot;CH1&quot;,
 * 		&quot;CTRL.Q&quot;, &quot;RECV&quot;);
 * mc.connect();
 * mc.regAdd(&quot;1,4,5,6&quot;, &quot;MyTest.app&quot;, 9789);
 * mc.regDelete(&quot;WNMS4_QM&quot;, &quot;RECV&quot;);
 * mc.disConnect();
 * </pre>
 * 
 * </blockquote>
 * 
 * ��ϸ�÷��鿴�÷�����˵��<br>
 * 
 * @version 2.0
 * @author b
 */
public class MessageClient {
	private static Logger log = LoggerFactory.getLogger(MessageClient.class);

	private MQCommonHelper sendQueue;
	private MQCommonHelper recvQueue;
	private String mqMgrName;

	private MQMessage currentMQMsg;
	private int currentMsgLen;
	private byte[] currentMsgBuf;

	private long recvCountor = 0;
	private long perCountor = 0;
	private long recvTimes = 0;
	private long currRecvRate = 0;
	private int intervalOfPrintRecvRate = 1000;

	private final int TIMEOUT_WAIT = 1000;
	private int sendRetrys = 0;
	private int timeout = TIMEOUT_WAIT;
	private int recvMode = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_FAIL_IF_QUIESCING;
	private int testNumber;
	@Deprecated
	private boolean initialized;
	private int clientType;
	private int ccsid;

	@Deprecated
	private final String verInfo = "\r\nBOCO MQ API Wrapper\r\n"
			+ " Version: V2.0\r\n" + " Latest Build: 2016-05-20\r\n"
			+ "CopyRight: BOCO";
	private final int DEFAULT_MESSAGE_PRIORITY = -1;

	/**
	 * ���������<br>
	 * <ul>
	 * <li>
	 * {@linkplain #MessageClient(String, int, String, String, String, String)
	 * MessageClient(String, int, String, String, String, String)}</li>
	 * <li>
	 * {@linkplain #MessageClient(String, int, String, String, String, String, int, int, int, int, boolean)
	 * MessageClient(String, int, String, String, String, String, int, int, int,
	 * int)}</li>
	 * </ul>
	 */
	@Deprecated
	public MessageClient() {
	}

	/**
	 * ��ʼ������<br>
	 * 
	 * <p>
	 * ���÷��Ͷ��н��ն��е������Ϣ���������MQ����֮ǰ��׼������<br>
	 * ����Ĭ���ظ�����Ϣ����Ϊ0���ͻ�������Ϊ1��ccsidΪ1383���ȴ���ʱΪ1000���룬ȡֵ��ʽĬ�Ϲ���ȡֵ<br>
	 * �ȴ���ʱ��������Ϣʱ�������Ϣ����û�����ݣ�����շ�����ȴ�����ʱ�䣬Ȼ���ٽ�����Ϣ<br>
	 * ע�����Ͷ������ͽ��ն�����������һ��Ϊ�մ�""�������ڽ��ջ��߷�����Ϣ����������������Ϊ�մ�<br>
	 * ���಻֧�ֶ��̣߳���Ҫÿһ���߳̽���һ������<br>
	 * </p>
	 * 
	 * @param mqHost
	 *            ���й�������������IP<br>
	 * 
	 * @param port
	 *            ���й������ļ����˿ں�<br>
	 * 
	 * @param qMgrName
	 *            ���й���������<br>
	 * 
	 * @param channelName
	 *            ���й�����ͨ������<br>
	 * 
	 * @param sendQName
	 *            ���ڷ�����Ϣ�Ķ�������<br>
	 *            <ol>
	 *            <li>����Ҫע��澯��Ϣƽ̨�����ض���Ϣ�ģ��˶������ƹ̶�Ϊ "CTRL.Q"</li>
	 *            <li>ֻ������ͨ���з�����Ϣ�İ�ʵ�ʶ���������д�����ú͸澯��Ϣƽ̨ר�ö���������<br>
	 *            ��ʱrecvQName��Ҫ��д��Ϊ�մ�""���ɣ����ǲ�����Ϊnull</li>
	 *            </ol>
	 * 
	 * @param recvQName
	 *            ���ڽ�����Ϣ�Ķ�������<br>
	 *            <ol>
	 *            <li>����Ҫע��澯��Ϣƽ̨�����ض���Ϣ�ģ���ʵ�ʶ���������д�����ú͸澯��Ϣƽ̨ר�ö���������</li>
	 *            <li>ֻ�Ǵ���ͨ���н�����Ϣ�İ�ʵ�ʶ���������д�����ú͸澯��Ϣƽ̨ר�ö���������<br>
	 *            ��ʱsendQName��Ҫ��д��Ϊ�մ�""���ɣ����ǲ�����Ϊnull</li>
	 *            </ol>
	 */
	public MessageClient(String mqHost, int port, String qMgrName,
			String channelName, String sendQName, String recvQName) {
		init(mqHost, port, qMgrName, channelName, sendQName, recvQName, 1);
	}

	/**
	 * ��ʼ������<br>
	 * 
	 * <p>
	 * ���÷��Ͷ��н��ն��е������Ϣ���������MQ����֮ǰ��׼������<br>
	 * ע�����Ͷ������ͽ��ն�����������һ��Ϊ�մ�""��������������Ϊ�մ�<br>
	 * ���಻֧�ֶ��̣߳���Ҫÿһ���߳̽���һ������<br>
	 * </p>
	 * 
	 * @param mqHost
	 *            ���й�������������IP<br>
	 * 
	 * @param port
	 *            ���й������ļ����˿ں�<br>
	 * 
	 * @param qMgrName
	 *            ���й���������<br>
	 * 
	 * @param channelName
	 *            ���й�����ͨ������<br>
	 * 
	 * @param sendQName
	 *            ���ڷ�����Ϣ�Ķ�������<br>
	 *            <ol>
	 *            <li>����Ҫע��澯��Ϣƽ̨�����ض���Ϣ�ģ��˶������ƹ̶�Ϊ "CTRL.Q"</li>
	 *            <li>ֻ������ͨ���з�����Ϣ�İ�ʵ�ʶ���������д�����ú͸澯��Ϣƽ̨ר�ö���������<br>
	 *            ��ʱrecvQName��Ҫ��д��Ϊ�մ�""���ɣ����ǲ�����Ϊnull</li>
	 *            </ol>
	 * 
	 * @param recvQName
	 *            ���ڽ�����Ϣ�Ķ�������<br>
	 *            <ol>
	 *            <li>����Ҫע��澯��Ϣƽ̨�����ض���Ϣ�ģ���ʵ�ʶ���������д�����ú͸澯��Ϣƽ̨ר�ö���������</li>
	 *            <li>ֻ�Ǵ���ͨ���н�����Ϣ�İ�ʵ�ʶ���������д�����ú͸澯��Ϣƽ̨ר�ö���������<br>
	 *            ��ʱsendQName��Ҫ��д��Ϊ�մ�""���ɣ����ǲ�����Ϊnull</li>
	 *            </ol>
	 * 
	 * @param clientType
	 *            �ͻ������ͣ�ͨ��Ϊ1 <br>
	 * 
	 * @param ccsid
	 *            �ַ�����ʶ��<br>
	 * 
	 * @param sendRetrys
	 *            ��Ϣ����ʧ�ܺ����·�����Ϣ�Ĵ���<br>
	 *            ע����sendRetrysС��0��������sendRetrys����0<br>
	 * 
	 * @param timeout
	 *            �ȴ���ʱ����λ����<br>
	 *            ע����timeoutС��0��������timeout����0<br>
	 * 
	 * @param isExclusive
	 *            ���ܷ����ӽ��ն���ȡֵ��ʽ�ǹ���ȡֵ���Ƕ�ռȡֵ<br>
	 *            true ��ռȡֵ��false ����ȡֵ<br>
	 *            <ul>
	 *            <li>��ռȡֵ:һ������ӽ��ն���ȡֵʱ��������������ӽ��ն�����ȡֵ</li>
	 *            <li>����ȡֵ:һ������ӽ��ն���ȡֵʱ������������Դӽ��ն�����ȡֵ</li>
	 *            </ul>
	 */
	public MessageClient(String mqHost, int port, String qMgrName,
			String channelName, String sendQName, String recvQName,
			int clientType, int ccsid, int sendRetrys, int timeout,
			boolean isExclusive) {
		if (isExclusive) {
			recvMode = MQC.MQOO_INPUT_EXCLUSIVE;
		} else {
			recvMode = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_FAIL_IF_QUIESCING;
		}
		if (init(mqHost, port, qMgrName, channelName, sendQName, recvQName,
				clientType)) {
			this.timeout = timeout;
			this.sendRetrys = sendRetrys;
			setCcsid(ccsid);
		}
	}

	/**
	 * ��ʼ������<br>
	 * 
	 * <p>
	 * ע���÷��������ʽΪ�ڹ�������ʱ��ֱ����ɳ�ʼ��<br>
	 * ���������<br>
	 * <ul>
	 * <li>
	 * {@linkplain #MessageClient(String, int, String, String, String, String)
	 * MessageClient(String, int, String, String, String, String)}</li>
	 * <li>
	 * {@linkplain #MessageClient(String, int, String, String, String, String, int, int, int, int, boolean)
	 * MessageClient(String, int, String, String, String, String, int, int, int,
	 * int)}</li>
	 * </ul>
	 */
	@Deprecated
	public boolean init(String mqHost, int port, String qMgrName,
			String channelName, String sendQName, String recvQName,
			int clientType) {
		if (!initCheckParam(qMgrName, channelName, sendQName, recvQName)) {
			initialized = false;
			log.error("Failed to initialize");
			return false;
		}
		if (mqHost == null) {
			initialized = false;
			log.error("The mqHost cannot be NULL");
			log.error("Failed to initialize");
			return false;
		}
		if (!sendQName.trim().equals("")) {
			sendQueue = new MQCommonHelper(mqHost, port, qMgrName, channelName,
					sendQName, MQC.MQOO_OUTPUT | MQC.MQOO_FAIL_IF_QUIESCING);
		}
		if ((!recvQName.trim().equals(""))) {
			recvQueue = new MQCommonHelper(mqHost, port, qMgrName, channelName,
					recvQName, recvMode);
		}
		this.clientType = clientType;
		mqMgrName = qMgrName;
		initialized = true;

		return true;
	}

	private boolean initCheckParam(String qMgrName, String channelName,
			String sendQName, String recvQName) {
		if (!checkParamNotNull(qMgrName, channelName)) {
			return false;
		}
		if (sendQName == null || recvQName == null) {
			log.error("Neither the sendQName and the recvQName cannot be NULL");
			return false;
		}
		if (sendQName.trim().equals("") && recvQName.trim().equals("")) {
			log.error("SendQueue and receQueue can not both be empty");
			return false;
		}
		return true;
	}

	private boolean checkParamNotNull(String param1, String param2) {
		if (param1 == null || param2 == null) {
			log.error("Neither the qMgrName and the channelName cannot be NULL");
			return false;
		}
		if (param1.trim().equals("") || param2.trim().equals("")) {
			log.error("Neither the qMgrName and the channelName cannot be empty");
			return false;
		}
		return true;
	}

	/**
	 * ��ָ����ʽ���ӵ����й�����<br>
	 * 
	 * <p>
	 * �������MQ��ͬһ̨��������Ҫͨ��������ʽ�������ӣ�����ȫ����true<br>
	 * �������MQ����ͬһ̨��������Ҫʹ��TCPЭ��������ӣ�����ȫ����false<br>
	 * ע���÷�����Ҫ�ڳ�ʼ���ɹ���ʹ��<br>
	 * </p>
	 * 
	 * @param recvFlg
	 * 
	 * @param sendFlg
	 * 
	 * @return ���ӳɹ�����true�����򷵻�false<br>
	 */
	public boolean connect(boolean recvFlg, boolean sendFlg) {
		boolean recvOk = true;
		boolean sendOk = true;
		if (sendQueue != null) {
			sendQueue.setBindingFlg(sendFlg);
			sendOk = sendQueue.connect();
		}
		if (recvQueue != null) {
			recvQueue.setBindingFlg(recvFlg);
			recvOk = recvQueue.connect();
		}
		return sendOk && recvOk;
	}

	/**
	 * ���ӵ�ָ���Ķ��й������Ľ��ն��кͷ��Ͷ���<br>
	 * 
	 * <p>
	 * �÷���Ϊ��ͬ����ͨ��TCPЭ���������<br>
	 * ע���÷�����Ҫ�ڳ�ʼ���ɹ���ʹ��<br>
	 * </p>
	 * 
	 * @return ���ӳɹ�����true�����򷵻�false<br>
	 */
	public boolean connect() {
		boolean recvOk = true;
		boolean sendOk = true;
		if (sendQueue != null) {
			sendOk = sendQueue.connect();
		}
		if (recvQueue != null) {
			recvOk = recvQueue.connect();
		}
		return sendOk && recvOk;
	}

	/**
	 * ʹ�ö�̬���н�����Ϣ<br>
	 * 
	 * <p>
	 * �ڵ��ó�ʼ������ʱ�����ն�����������дΪ��̬���е�����<br>
	 * ע���÷�����Ҫ�ڳ�ʼ���ɹ���ʹ��<br>
	 * </p>
	 * 
	 * @return ���ӳɹ�����true�����򷵻�false<br>
	 */
	public boolean connectWithDynamic() {
		boolean recvOk = true;
		boolean sendOk = true;
		if (sendQueue != null) {
			sendOk = sendQueue.connect();
			log.info("Connect to Queue " + sendQueue.getQname() + " " + sendOk);
		}
		if (recvQueue != null) {
			recvOk = recvQueue.connectWithDynamic();
			log.info("Connect to Queue recvQueue" + " " + recvOk);
		}
		return sendOk && recvOk;
	}

	/**
	 * �Ͽ���MQ������<br>
	 * 
	 * <p>
	 * ע�����ٽ�����Ϣʱ�����ô˷����Ͽ�����й�����������
	 * </p>
	 * 
	 * @return �Ͽ����ӳɹ�����true�����򷵻�false<br>
	 */
	public boolean disConnect() {
		if (!(null == sendQueue)) {
			sendQueue.disConnect();
		}
		if (!(null == recvQueue)) {
			recvQueue.disConnect();
		}
		return true;
	}

	/**
	 * ������Ϣ<br>
	 * 
	 * <p>
	 * ͨ����getMessageBuffer����һ��ʹ�ã���ʹ�ø÷���������Ϣ��<br>
	 * �ٵ���getMessageBuffer������ø���Ϣ���ֽ�����<br>
	 * ע���ȴ���ʱ��ʱ���ɹ��췽����ʼ����ʱ�򴴽���Ĭ��Ϊ1000����
	 * </p>
	 * 
	 * @return ���յ���Ϣ����true�����򷵻�false<br>
	 */
	public boolean recvMQMsg() {
		return recvMQMsg(timeout);
	}

	/**
	 * ������Ϣ<br>
	 * 
	 * <p>
	 * ͨ����getMessageBuffer����һ��ʹ�ã���ʹ�ø÷���������Ϣ��<br>
	 * �ٵ���getMessageBuffer������ø���Ϣ���ֽ�����<br>
	 * ���������<br>
	 * <ul>
	 * <li>
	 * {@linkplain #recvMQMsg() recvMQMsg()}</li>
	 * </ul>
	 * 
	 * @param timeout
	 *            �ȴ���ʱ����λ����<br>
	 *            ע��������ֵС��0��������Ϊ1000<br>
	 * 
	 * @return ���յ���Ϣ����true�����򷵻�false<br>
	 */
	@Deprecated
	public boolean recvMQMsg(int timeout) {
		currentMQMsg = recvMQMessage(timeout);
		if (null == currentMQMsg)
			return false;

		// �ڷ������ݵ�ʱ����Ϣ���ͻ����BaseDef.MSG_TYPE_FIRST
		int msgType = currentMQMsg.messageType - Constants.MSG_TYPE_FIRST;
		try {
			currentMsgLen = currentMQMsg.getDataLength();
		} catch (IOException ie) {
			log.error("Failed to get the message length, reason:{}",
					ie.getMessage());
			return false;
		}

		if (msgType == Constants.TEST_MSG_NO
				|| msgType > Constants.PLATFORM_STATUS_MSG_NO) {
			currentMsgBuf = new byte[currentMsgLen];
			try {
				currentMQMsg.readFully(currentMsgBuf);
			} catch (IOException ie) {
				log.error("Extracted message failed, reason:{}",
						ie.getMessage());
				return false;
			}

			if (msgType == Constants.TEST_MSG_NO) {
				sendTestAck(msgType, currentMsgBuf);
			}
		}
		return true;
	}

	/**
	 * ����MQMessage��ʽ����Ϣ<br>
	 * 
	 * <p>
	 * ע��MQMessageΪMQ��API���ṩ��һ����Ϣ��
	 * </p>
	 * 
	 * @param timeout
	 *            �ȴ���ʱ����λ����<br>
	 * 
	 * @return ����һ�� MQMessage�������û���յ���Ч����Ϣ���򷵻�null<br>
	 */
	public MQMessage recvMQMessage(int timeout) {
		if (null == recvQueue) {
			log.error("If the recvQueue is empty, the method can not be used");
			return null;
		}
		if (recvCountor == Long.MAX_VALUE) {
			recvCountor = 0;
			recvTimes = 0;
			log.info("recvCountor is Long.Max, recvCountor set 0, recvTimes=0");
		}

		long sTime = System.currentTimeMillis(); // ��ȡ��ǰʱ��
		MQMessage recvMessage = recvQueue.receiveMQMsg(timeout);
		if (recvMessage != null) {
			countRecvRate(sTime);
		}
		return recvMessage;
	}

	private void countRecvRate(long sTime) {
		recvCountor++;
		perCountor++;
		// �����������˶೤ʱ�䣬��λ����
		recvTimes = recvTimes + System.currentTimeMillis() - sTime;
		if (recvTimes > 0)
			currRecvRate = perCountor * 1000 / recvTimes;
		// ÿintervalOfPrintRecvRate�Σ�����־�д�ӡһ�Σ�����ʱ�䣬�ٶ�
		if (perCountor == intervalOfPrintRecvRate) {
			log.info(
					"receive queue[{}], all count: {}, 1000~time: {}ms, speed: {}/s",
					new Object[] { recvQueue.getQname(), recvCountor,
							recvTimes, currRecvRate });
			perCountor = 0;
			recvTimes = 0;
		}
	}

	/**
	 * ���ô�ӡһ�ν�����Ϣ�ٶȵļ��<br>
	 * 
	 * <p>
	 * �����ý�����Ϣ����ʱ�����Զ����������Ϣ���ٶȣ�ÿ����һ����Ϣ���ͻ��ӡһ�ν�����Ϣ���ٶ�<br>
	 * ע��Ĭ��ֵΪ1000���÷�����Ҫ�ڵ��ý�����Ϣ�ķ���֮ǰ����<br>
	 * </p>
	 * 
	 * @param intervalOfPrintRecvRate
	 *            ÿ���ն�������Ϣ����ӡһ�ν�����Ϣ�ٶ�
	 */
	public void setFrequencyOfPrintRecvRate(int intervalOfPrintRecvRate) {
		if (intervalOfPrintRecvRate <= 0) {
			log.error("The frequency of printing the RecvRate must be greater than 0");
		} else {
			this.intervalOfPrintRecvRate = intervalOfPrintRecvRate;
		}
	}

	/**
	 * ��ȡ���ݽ��յ��ٶ�<br>
	 * 
	 * @return ÿ�������Ϣ����������λ����/��<br>
	 */
	public long getCurrRecvRate() {
		return currRecvRate;
	}

	/**
	 * �����ı���Ϣ<br>
	 * 
	 * <p>
	 * ʹ�������������й�������ͬ�Ĵ��뼯��byte[]ת��ΪString��ʽ<br>
	 * ע���ȴ���ʱ��ʱ���ɹ��췽����ʼ����ʱ�򴴽���Ĭ��Ϊ1000����
	 * </p>
	 * 
	 * @return �����ֽ�����Ϣ����<br>
	 *         ע���������ʧ�ܻ�ǰ��ϢΪ��Ϣƽ̨ϵͳ��Ϣ��������������Ϣ���򷵻�null<br>
	 */
	public byte[] recvTextMessage() {
		return recvTextMessage(timeout);
	}

	/**
	 * �����ı���Ϣ<br>
	 * 
	 * ���������<br>
	 * <ul>
	 * <li>
	 * {@linkplain #recvMQMsg() recvMQMsg()}</li>
	 * </ul>
	 * 
	 * @param timeout
	 *            �ȴ���ʱ����λ����<br>
	 *            ע��������ֵС��0��������Ϊ1000<br>
	 * 
	 * @return �����ֽ�����Ϣ����<br>
	 *         ע���������ʧ�ܻ�ǰ��ϢΪ��Ϣƽ̨ϵͳ��Ϣ��������������Ϣ���򷵻�null<br>
	 */
	@Deprecated
	public byte[] recvTextMessage(int timeout) {
		currentMQMsg = recvMQMessage(timeout);
		if (null == currentMQMsg)
			return null;

		int msgType = currentMQMsg.messageType - Constants.MSG_TYPE_FIRST;
		int msgLen;
		try {
			msgLen = currentMQMsg.getDataLength();
		} catch (IOException ie) {
			log.error("Failed to get the message length, reason:{}",
					ie.getMessage());
			return null;
		}

		byte[] chArray = new byte[msgLen];
		try {
			currentMQMsg.readFully(chArray);
		} catch (IOException ie) {
			log.error("Extracted message failed, reason:{}", ie.getMessage());
			return null;
		}

		if (msgType == Constants.TEST_MSG_NO) {
			sendTestAck(msgType, chArray);
		}

		return chArray;
	}

	private void sendTestAck(int msgType, byte[] chArray) {
		AQMessage aqMsg = getAQMessage(msgType, chArray);
		testNumber = aqMsg.getTestNumber();
		sendQueue.sendTestAck(chArray, recvQueue.getQname());
	}

	private AQMessage getAQMessage(int msgType, byte[] msgBuf) {
		AQMessage aqMsg = new AQMessage(msgType,
				Constants.TEST_NUMBER_MESSAGE_BUFFER);
		aqMsg.setMessageBuffer(msgBuf);
		aqMsg.decode();

		return aqMsg;
	}

	/**
	 * ����UTF-8��ʽ���ı���Ϣ<br>
	 * 
	 * <p>
	 * ע�������̲��ṩUTF-8��ʽ����Ϣ���ͷ������÷�����Ҫ��Ϊ�˼���������˾��Ŀ
	 * </p>
	 * 
	 * @param timeout
	 *            �ȴ���ʱ����λ����<br>
	 *            ע��������ֵС��0��������Ϊ1000<br>
	 * 
	 * @return �����ֽ�����Ϣ����<br>
	 *         ע���������ʧ�ܻ�ǰ��ϢΪ��Ϣƽ̨ϵͳ��Ϣ��������������Ϣ���򷵻�null<br>
	 */
	public byte[] recvTextMessageByUTF(int timeout) {
		currentMQMsg = recvMQMessage(timeout);
		if (!(null == currentMQMsg)) {
			String recv = "";
			try {
				recv = currentMQMsg.readUTF();
			} catch (IOException ie) {
				log.error("Extracted message failed, reason:{}",
						ie.getMessage());
				return null;
			}
			return recv.getBytes();
		}
		return null;
	}

	/**
	 * �����ı���Ϣ<br>
	 * 
	 * @param msgBuf
	 *            ��Ϣ�ľ�������<br>
	 * 
	 * @param msgType
	 *            ��Ϣ����<br>
	 * 
	 * @return ���ͳɹ�����true������ʧ�ܷ���false<br>
	 */
	public boolean sendTextMessage(byte[] msgBuf, int msgType) {
		return sendTextMessage(msgBuf, msgType, DEFAULT_MESSAGE_PRIORITY);
	}

	/**
	 * �����ı���Ϣ<br>
	 * 
	 * @param msgBuf
	 *            ��Ϣ�ľ�������<br>
	 * 
	 * @param msgType
	 *            ��Ϣ����<br>
	 * 
	 * @param priority
	 *            ��ǰ��Ϣ�����ȼ�<br>
	 *            ע�����ȼ�����Ϊ0-9����-1������֮����ᱨ������-1���ȼ�Ĭ��Ϊ0<br>
	 * 
	 * @return ���ͳɹ�����true������ʧ�ܷ���false<br>
	 */
	public boolean sendTextMessage(byte[] msgBuf, int msgType, int priority) {
		return sendTextMessage(msgBuf, msgType, priority, "");
	}

	/**
	 * �����ı���Ϣ<br>
	 * 
	 * @param msgBuf
	 *            ��Ϣ�ľ�������<br>
	 * 
	 * @param msgType
	 *            ��Ϣ����<br>
	 * 
	 * @param correlationId
	 *            �澯��ˮ�ţ�ÿһ���澯��Ϣ��һ��Ψһ�ĸ澯��ˮ�ţ������ڲ��Ҷ�Ӧ�ĸ澯��Ϣ<br>
	 * 
	 * @return ���ͳɹ�����true������ʧ�ܷ���false<br>
	 */
	public boolean sendTextMessage(byte[] msgBuf, int msgType,
			String correlationId) {
		return sendTextMessage(msgBuf, msgType, DEFAULT_MESSAGE_PRIORITY,
				correlationId);
	}

	/**
	 * �����ı���Ϣ<br>
	 * 
	 * @param msgBuf
	 *            ��Ϣ�ľ�������<br>
	 * 
	 * @param msgType
	 *            ��Ϣ����<br>
	 * 
	 * @param priority
	 *            ��ǰ��Ϣ�����ȼ�<br>
	 * 
	 * @param correlationId
	 *            �澯��ˮ�ţ�ÿһ���澯��Ϣ��һ��Ψһ�ĸ澯��ˮ�ţ������ڲ��Ҷ�Ӧ�ĸ澯��Ϣ<br>
	 *            ע���澯��ˮ�ſ���Ϊnull<br>
	 * 
	 * @return ���ͳɹ�����true������ʧ�ܷ���false<br>
	 */
	public boolean sendTextMessage(byte[] msgBuf, int msgType, int priority,
			String correlationId) {
		MQMessage msg = new MQMessage();
		// ����MQMD ��ʽ�ֶ�
		msg.format = MQC.MQFMT_NONE;
		msg.messageType = msgType + Constants.MSG_TYPE_FIRST;
		if (priority != DEFAULT_MESSAGE_PRIORITY) {
			msg.priority = priority;
		}
		if (correlationId != null && !correlationId.equals("")) {
			msg.correlationId = correlationId.getBytes();
		}

		try {
			msg.write(msgBuf);
		} catch (IOException ie) {
			log.error("Failed to Store the Message , reason:{}",
					ie.getMessage());
			return false;
		}

		return this.sendMQMessage(msg);
	}

	/**
	 * ����MQMessage���͵���Ϣ<br>
	 * 
	 * <p>
	 * ע��MQMessageΪMQ��API���ṩ��һ����Ϣ��
	 * </p>
	 * 
	 * @param msg
	 *            MQMessage����<br>
	 * 
	 * @return ���ͳɹ�����true�����򷵻�false<br>
	 */
	@Deprecated
	public boolean sendMQMessage(MQMessage msg) {
		if (null == sendQueue) {
			log.error("If the sendQueue is empty, the method can not be used");
			return false;
		}

		boolean sendOk = sendQueue.sendMQMsg(msg);
		if (sendOk) {
			return sendOk;
		}

		int retryTimes = sendRetrys;
		// �ظ�����з�����Ϣ��retry_times��
		while (retryTimes > 0) {
			sendOk = sendQueue.sendMQMsg(msg);
			if (sendOk) {
				return sendOk;
			}
			retryTimes--;
		}
		return false;
	}

	/**
	 * ����Ϣƽ̨����ָ������Ϣ<br>
	 * 
	 * <p>
	 * ���ģ��澯ϵͳ����Ϣ��������������ɣ�һ����Ϊ�ͻ��ˣ�һ����Ϊ�ۺϼ��ƽ̨<br>
	 * ���пͻ�����Ҫ���澯ƽ̨����һ��������Ϣ����Ϣ����Ϊ0������֪�ۺϼ��ƽ̨���ͻ�����Ҫʲô��Ϣ��<br>
	 * ��׼�����ĸ������н��ո���Ϣ���Ӷ�ʵ�ֿͻ������ۺϼ��ƽ̨����Ϣ����
	 * </p>
	 * 
	 * @param msgTypes
	 *            �Զ��ŷָ�����Ϣ�����ַ�������"1,4,5,6"����ʵ�ʶ��ĸ澯��Ϣʱ���������1��5����Ϣ<br>
	 * 
	 * @param appName
	 *            Ӧ�����ƣ�Ӧ�����Ƶĳ���Ҫ����8���ֽ�<br>
	 * 
	 * @param currentPid
	 *            ��ǰ����ID<br>
	 * 
	 * @return ע��ɹ�����true�� ���򷵻�false<br>
	 */
	public boolean regAdd(String msgTypes, String appName, int currentPid) {
		if (!checkParamNotNull(msgTypes, appName)) {
			log.error("Neither the msgTypes and the appName cannot be empty");
			return false;
		}
		AQMessage aqMsg = new AQMessage(Constants.REG_ADD_MSG_NO,
				Constants.MAX_QUEUE_BUFFER);

		String hostname = getHostName();
		// ���ö�����Ϣ�ĸ���ֵ
		aqMsg.setFieldValue("machine_name", hostname);
		aqMsg.setFieldValue("user_name", System.getProperty("user.name"));
		aqMsg.setFieldValue("application_name", appName);
		aqMsg.setFieldValue("manager_name", mqMgrName);
		aqMsg.setFieldValue("queue_name", recvQueue.getQname().trim());
		aqMsg.setFieldValue("msg_type", msgTypes);
		aqMsg.setFieldValue("pid", currentPid);
		aqMsg.setFieldValue("clnt_type", clientType);

		aqMsg.encode();
		if (sendQueue.sendAQMessage(aqMsg) != 0) {
			log.error("Failed to send the REGADD message");
			return false;
		}

		return recvRegAdd();
	}

	private boolean recvRegAdd() {
		AQMessage aqMsg = new AQMessage(Constants.MAX_QUEUE_BUFFER);
		short iLoop = 1;
		int returnCode = 0;
		// ���շ�����Ϣ
		while (iLoop > 0) {
			// ������Ϣ����return_code<0������ʧ�ܣ�������0���ɹ�
			returnCode = recvQueue.receiveMsg31(aqMsg,
					Constants.DEFAULT_WAIT_ACK_TIME);
			if (returnCode == Constants.MESSAGE_OK) {
				returnCode = aqMsg.decode();
				if (returnCode == 0
						&& aqMsg.getMessageType() == Constants.REG_ACK_MSG_NO) {
					return true;
				}
			}
			if (iLoop > 100) {
				log.error("Failed to recvive the REGADD Feedback message");
				return false;
			}
			iLoop++;
		}
		return false;
	}

	/**
	 * ����Ϣƽ̨ע���Ѷ��ĵ���Ϣ<br>
	 * 
	 * <p>
	 * ȡ�����ģ��ͻ������澯ƽ̨����һ��ȡ��������Ϣ����Ϣ����Ϊ3��<br>
	 * ��֪�ۺϼ��ƽ̨���ͻ���Ҫ�Ͽ�����Ϣƽ̨����Ϣ����
	 * </p>
	 * 
	 * @param queueMgrName
	 *            ���ĵĶ��й���������<br>
	 * 
	 * @param queueName
	 *            ����ʱ�����õĽ��ն�������<br>
	 * 
	 * @return ע����Ϣ���ͳɹ�����true�����򷵻�false<br>
	 */
	public boolean regDelete(String queueMgrName, String queueName) {
		if (!checkParamNotNull(queueMgrName, queueName)) {
			log.error("Neither the queueMgrName and the queueName cannot be empty");
			return false;
		}
		AQMessage aqMsg = new AQMessage(Constants.REG_DLT_MSG_NO,
				Constants.MAX_QUEUE_BUFFER);

		aqMsg.setFieldValue("manager_name", queueMgrName);
		aqMsg.setFieldValue("queue_name", queueName);

		aqMsg.encode();

		if (sendQueue.sendAQMessage(aqMsg) != 0) {
			log.error("Failed to send the REGDELETE message");
			return false;
		}
		return true;
	}

	private String getHostName() {
		String hostname;
		try {
			InetAddress iAddr = InetAddress.getLocalHost();
			hostname = iAddr.getHostName();
		} catch (UnknownHostException he) {
			hostname = "";
		}
		return hostname;
	}

	@Deprecated
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * ��ȡ��ǰ�յ�����Ϣ����<br>
	 * 
	 * <p>
	 * ע���ڵ���recv�������ز�Ϊ�յ�����µ��ô˷���<br>
	 * ������Ϣƽ̨������Ϣ������£��ͻ��˴��¿ɸ��ݵ�ǰϵͳʱ�������һ��<br>
	 * �յ�������Ϣ��ʱ���(���峬ʱʱ����Ҫ�����ֳ�����趨)�ж��Ƿ��ѱ���Ϣƽ̨ע����<br>
	 * ����ѱ�ע������Ҫ����regAdd()�������¶���<br>
	 * ����������ֵΪ 5 ����ǰ��Ϣ��������Ϣ
	 * </p>
	 * 
	 * @return ��ǰ�յ�����Ϣ����<br>
	 */
	public int getMessageType() {
		if (currentMQMsg != null)
			return this.currentMQMsg.messageType - Constants.MSG_TYPE_FIRST;
		return -1;
	}

	/**
	 * ��ȡ��ǰ��Ϣ�ĸ澯��ˮ��<br>
	 * 
	 * <p>
	 * ע���ڵ���recv�������ز�Ϊ�յ�����µ��ô˷���
	 * </p>
	 * 
	 * @return ��ǰ��Ϣ�ĸ澯��ˮ��<br>
	 */
	public String getCurMsgCorrelationId() {
		if (currentMQMsg == null) {
			return "";
		}
		byte[] correlationIds = currentMQMsg.correlationId;
		if (correlationIds != null && correlationIds.length > 0) {
			return new String(correlationIds);
		}
		return "";
	}

	/**
	 * ��ȡ��ǰ��Ϣ���ݵ��ֽ�������<br>
	 * 
	 * <p>
	 * ע��һ����recvMQMsg��������ô˷���
	 * </p>
	 * 
	 * @return ��ǰ��Ϣ���ֽ�������<br>
	 */
	public byte[] getMessageBuffer() {
		if (!(null == currentMQMsg)) {
			return currentMsgBuf;
		}
		return null;
	}

	/**
	 * ��ȡ���ն��е�����<br>
	 * 
	 * @return ���ն��е�����
	 */
	public String getRecvQueueName() {
		if (null != recvQueue)
			return recvQueue.getQname();
		return "";
	}

	@Deprecated
	public void setCcsid(int ccsid) {
		this.ccsid = ccsid;
		if (null != sendQueue)
			sendQueue.setCcsid(ccsid);
		if (null != recvQueue)
			recvQueue.setCcsid(ccsid);
	}

	/**
	 * ��ȡ��ǰ��Ϣƽ̨������Ϣ�����<br>
	 * 
	 * <p>
	 * �ڵ���recv������getMessageType��������á�<br>
	 * �����ǰ��Ϣ���ͺ��� 5 ������£����ô˷��������򷵻�ֵû��ʵ������<br>
	 * ���������������������������ģ�����Ϣƽ̨��������������ſ������´�1��ʼ
	 * </p>
	 * 
	 * @return ������ֵ����ǰ�������<br>
	 */
	public int getTest_number() {
		return testNumber;
	}

	@Deprecated
	public String getVer_info() {
		return verInfo;
	}

	@Deprecated
	public MQCommonHelper getSendQueue() {
		return sendQueue;
	}

	@Deprecated
	public MQCommonHelper getRecvQueue() {
		return recvQueue;
	}
}