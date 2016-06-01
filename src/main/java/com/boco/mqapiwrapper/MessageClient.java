package com.boco.mqapiwrapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQC;
import com.ibm.mq.MQMessage;

/**
 * MQ 消息收发客户端 API封装类<br>
 * 
 * <p>
 * 该类主要提供了告警平台连接MQ并进行收发数据等操作<br>
 * 其操作方式如下:
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
 * System.out.println(&quot;接收成功！&quot; + &quot;队列为：&quot; + mc.getRecvQueueName() + &quot;，消息类型为&quot;
 * 		+ mc.getMessageType() + &quot;，消息为&quot; + new String(msgBuf));
 * mc.disConnect();
 * </pre>
 * 
 * </blockquote>
 * 
 * 如上例就完成了一次连接MQ，收发数据，获取相关信息，以及关闭连接的操作<br>
 * 接收消息也可以通过另外一种方式：
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
 * {@link #regAdd(String, String, int)}订阅和{@link #regDelete(String, String)}
 * 取消订阅， 用法如下：
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
 * 详细用法查看该方法的说明<br>
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
	 * 替代方法：<br>
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
	 * 初始化方法<br>
	 * 
	 * <p>
	 * 设置发送队列接收队列的相关信息，完成连接MQ队列之前的准备工作<br>
	 * 其中默认重复发消息次数为0，客户端类型为1，ccsid为1383，等待超时为1000毫秒，取值方式默认共享取值<br>
	 * 等待超时：接收消息时，如果消息队列没有数据，则接收方法会等待若干时间，然后再接收消息<br>
	 * 注：发送队列名和接收队列名可以有一个为空串""，仅用于接收或者发送消息，但不可以两个都为空串<br>
	 * 该类不支持多线程，需要每一个线程建立一个对象<br>
	 * </p>
	 * 
	 * @param mqHost
	 *            队列管理器所在主机IP<br>
	 * 
	 * @param port
	 *            队列管理器的监听端口号<br>
	 * 
	 * @param qMgrName
	 *            队列管理器名称<br>
	 * 
	 * @param channelName
	 *            队列管理器通道名称<br>
	 * 
	 * @param sendQName
	 *            用于发送消息的队列名称<br>
	 *            <ol>
	 *            <li>对于要注册告警消息平台订阅特定消息的，此队列名称固定为 "CTRL.Q"</li>
	 *            <li>只是向普通队列发送消息的按实际队列名称填写（不得和告警消息平台专用队列重名）<br>
	 *            此时recvQName不要填写，为空串""即可，但是不可以为null</li>
	 *            </ol>
	 * 
	 * @param recvQName
	 *            用于接收消息的队列名称<br>
	 *            <ol>
	 *            <li>对于要注册告警消息平台订阅特定消息的，按实际队列名称填写（不得和告警消息平台专用队列重名）</li>
	 *            <li>只是从普通队列接收消息的按实际队列名称填写（不得和告警消息平台专用队列重名）<br>
	 *            此时sendQName不要填写，为空串""即可，但是不可以为null</li>
	 *            </ol>
	 */
	public MessageClient(String mqHost, int port, String qMgrName,
			String channelName, String sendQName, String recvQName) {
		init(mqHost, port, qMgrName, channelName, sendQName, recvQName, 1);
	}

	/**
	 * 初始化方法<br>
	 * 
	 * <p>
	 * 设置发送队列接收队列的相关信息，完成连接MQ队列之前的准备工作<br>
	 * 注：发送队列名和接收队列名可以有一个为空串""，不可以两个都为空串<br>
	 * 该类不支持多线程，需要每一个线程建立一个对象<br>
	 * </p>
	 * 
	 * @param mqHost
	 *            队列管理器所在主机IP<br>
	 * 
	 * @param port
	 *            队列管理器的监听端口号<br>
	 * 
	 * @param qMgrName
	 *            队列管理器名称<br>
	 * 
	 * @param channelName
	 *            队列管理器通道名称<br>
	 * 
	 * @param sendQName
	 *            用于发送消息的队列名称<br>
	 *            <ol>
	 *            <li>对于要注册告警消息平台订阅特定消息的，此队列名称固定为 "CTRL.Q"</li>
	 *            <li>只是向普通队列发送消息的按实际队列名称填写（不得和告警消息平台专用队列重名）<br>
	 *            此时recvQName不要填写，为空串""即可，但是不可以为null</li>
	 *            </ol>
	 * 
	 * @param recvQName
	 *            用于接收消息的队列名称<br>
	 *            <ol>
	 *            <li>对于要注册告警消息平台订阅特定消息的，按实际队列名称填写（不得和告警消息平台专用队列重名）</li>
	 *            <li>只是从普通队列接收消息的按实际队列名称填写（不得和告警消息平台专用队列重名）<br>
	 *            此时sendQName不要填写，为空串""即可，但是不可以为null</li>
	 *            </ol>
	 * 
	 * @param clientType
	 *            客户端类型，通常为1 <br>
	 * 
	 * @param ccsid
	 *            字符集标识符<br>
	 * 
	 * @param sendRetrys
	 *            消息发送失败后，重新发送消息的次数<br>
	 *            注：若sendRetrys小于0，则设置sendRetrys等于0<br>
	 * 
	 * @param timeout
	 *            等待超时，单位毫秒<br>
	 *            注：若timeout小于0，则设置timeout等于0<br>
	 * 
	 * @param isExclusive
	 *            接受方法从接收队列取值方式是共享取值还是独占取值<br>
	 *            true 独占取值，false 共享取值<br>
	 *            <ul>
	 *            <li>独占取值:一个程序从接收队列取值时，其他程序不允许从接收队列中取值</li>
	 *            <li>共享取值:一个程序从接收队列取值时，其他程序可以从接收队列中取值</li>
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
	 * 初始化方法<br>
	 * 
	 * <p>
	 * 注：该方法替代方式为在构造对象的时候直接完成初始化<br>
	 * 替代方法：<br>
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
	 * 以指定方式连接到队列管理器<br>
	 * 
	 * <p>
	 * 若软件与MQ在同一台机器，需要通过本机方式进行连接，参数全部填true<br>
	 * 若软件与MQ不在同一台机器，需要使用TCP协议进行连接，参数全部填false<br>
	 * 注：该方法需要在初始化成功后使用<br>
	 * </p>
	 * 
	 * @param recvFlg
	 * 
	 * @param sendFlg
	 * 
	 * @return 连接成功返回true，否则返回false<br>
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
	 * 连接到指定的队列管理器的接收队列和发送队列<br>
	 * 
	 * <p>
	 * 该方法为不同主机通过TCP协议进行连接<br>
	 * 注：该方法需要在初始化成功后使用<br>
	 * </p>
	 * 
	 * @return 连接成功返回true，否则返回false<br>
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
	 * 使用动态队列接收消息<br>
	 * 
	 * <p>
	 * 在调用初始化方法时，接收队列名称需填写为动态队列的名称<br>
	 * 注：该方法需要在初始化成功后使用<br>
	 * </p>
	 * 
	 * @return 连接成功返回true，否则返回false<br>
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
	 * 断开与MQ的连接<br>
	 * 
	 * <p>
	 * 注：不再接收消息时，调用此方法断开与队列管理器的连接
	 * </p>
	 * 
	 * @return 断开连接成功返回true，否则返回false<br>
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
	 * 接收消息<br>
	 * 
	 * <p>
	 * 通常与getMessageBuffer方法一起使用，先使用该方法接收消息，<br>
	 * 再调用getMessageBuffer方法获得该消息的字节数据<br>
	 * 注：等待超时的时间由构造方法初始化的时候创建，默认为1000毫秒
	 * </p>
	 * 
	 * @return 接收到消息返回true，否则返回false<br>
	 */
	public boolean recvMQMsg() {
		return recvMQMsg(timeout);
	}

	/**
	 * 接收消息<br>
	 * 
	 * <p>
	 * 通常与getMessageBuffer方法一起使用，先使用该方法接收消息，<br>
	 * 再调用getMessageBuffer方法获得该消息的字节数据<br>
	 * 替代方法：<br>
	 * <ul>
	 * <li>
	 * {@linkplain #recvMQMsg() recvMQMsg()}</li>
	 * </ul>
	 * 
	 * @param timeout
	 *            等待超时，单位毫秒<br>
	 *            注：若输入值小于0，则设置为1000<br>
	 * 
	 * @return 接收到消息返回true，否则返回false<br>
	 */
	@Deprecated
	public boolean recvMQMsg(int timeout) {
		currentMQMsg = recvMQMessage(timeout);
		if (null == currentMQMsg)
			return false;

		// 在发送数据的时候，消息类型会加上BaseDef.MSG_TYPE_FIRST
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
	 * 接收MQMessage格式的消息<br>
	 * 
	 * <p>
	 * 注：MQMessage为MQ的API中提供的一种消息类
	 * </p>
	 * 
	 * @param timeout
	 *            等待超时，单位毫秒<br>
	 * 
	 * @return 返回一个 MQMessage对象，如果没有收到有效的消息，则返回null<br>
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

		long sTime = System.currentTimeMillis(); // 获取当前时间
		MQMessage recvMessage = recvQueue.receiveMQMsg(timeout);
		if (recvMessage != null) {
			countRecvRate(sTime);
		}
		return recvMessage;
	}

	private void countRecvRate(long sTime) {
		recvCountor++;
		perCountor++;
		// 接收数据用了多长时间，单位毫秒
		recvTimes = recvTimes + System.currentTimeMillis() - sTime;
		if (recvTimes > 0)
			currRecvRate = perCountor * 1000 / recvTimes;
		// 每intervalOfPrintRecvRate次，在日志中打印一次，所用时间，速度
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
	 * 设置打印一次接收消息速度的间隔<br>
	 * 
	 * <p>
	 * 当调用接收消息方法时，会自动计算接收消息的速度，每接收一定消息数就会打印一次接收消息的速度<br>
	 * 注：默认值为1000，该方法需要在调用接收消息的方法之前调用<br>
	 * </p>
	 * 
	 * @param intervalOfPrintRecvRate
	 *            每接收多少条消息，打印一次接收消息速度
	 */
	public void setFrequencyOfPrintRecvRate(int intervalOfPrintRecvRate) {
		if (intervalOfPrintRecvRate <= 0) {
			log.error("The frequency of printing the RecvRate must be greater than 0");
		} else {
			this.intervalOfPrintRecvRate = intervalOfPrintRecvRate;
		}
	}

	/**
	 * 获取数据接收的速度<br>
	 * 
	 * @return 每秒接收消息的数量，单位：条/秒<br>
	 */
	public long getCurrRecvRate() {
		return currRecvRate;
	}

	/**
	 * 接收文本消息<br>
	 * 
	 * <p>
	 * 使用者需根据与队列管理器相同的代码集将byte[]转换为String格式<br>
	 * 注：等待超时的时间由构造方法初始化的时候创建，默认为1000毫秒
	 * </p>
	 * 
	 * @return 返回字节型消息数据<br>
	 *         注：如果接收失败或当前消息为消息平台系统消息（不包括心跳消息）则返回null<br>
	 */
	public byte[] recvTextMessage() {
		return recvTextMessage(timeout);
	}

	/**
	 * 接收文本消息<br>
	 * 
	 * 替代方法：<br>
	 * <ul>
	 * <li>
	 * {@linkplain #recvMQMsg() recvMQMsg()}</li>
	 * </ul>
	 * 
	 * @param timeout
	 *            等待超时，单位毫秒<br>
	 *            注：若输入值小于0，则设置为1000<br>
	 * 
	 * @return 返回字节型消息数据<br>
	 *         注：如果接收失败或当前消息为消息平台系统消息（不包括心跳消息）则返回null<br>
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
	 * 接收UTF-8格式的文本消息<br>
	 * 
	 * <p>
	 * 注：本工程不提供UTF-8格式的消息发送方法，该方法主要是为了兼容其他公司项目
	 * </p>
	 * 
	 * @param timeout
	 *            等待超时，单位毫秒<br>
	 *            注：若输入值小于0，则设置为1000<br>
	 * 
	 * @return 返回字节型消息数据<br>
	 *         注：如果接收失败或当前消息为消息平台系统消息（不包括心跳消息）则返回null<br>
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
	 * 发送文本消息<br>
	 * 
	 * @param msgBuf
	 *            消息的具体内容<br>
	 * 
	 * @param msgType
	 *            消息类型<br>
	 * 
	 * @return 发送成功返回true，发送失败返回false<br>
	 */
	public boolean sendTextMessage(byte[] msgBuf, int msgType) {
		return sendTextMessage(msgBuf, msgType, DEFAULT_MESSAGE_PRIORITY);
	}

	/**
	 * 发送文本消息<br>
	 * 
	 * @param msgBuf
	 *            消息的具体内容<br>
	 * 
	 * @param msgType
	 *            消息类型<br>
	 * 
	 * @param priority
	 *            当前消息的优先级<br>
	 *            注：优先级必须为0-9或者-1，除此之外均会报错，输入-1优先级默认为0<br>
	 * 
	 * @return 发送成功返回true，发送失败返回false<br>
	 */
	public boolean sendTextMessage(byte[] msgBuf, int msgType, int priority) {
		return sendTextMessage(msgBuf, msgType, priority, "");
	}

	/**
	 * 发送文本消息<br>
	 * 
	 * @param msgBuf
	 *            消息的具体内容<br>
	 * 
	 * @param msgType
	 *            消息类型<br>
	 * 
	 * @param correlationId
	 *            告警流水号，每一条告警消息有一个唯一的告警流水号，可用于查找对应的告警消息<br>
	 * 
	 * @return 发送成功返回true，发送失败返回false<br>
	 */
	public boolean sendTextMessage(byte[] msgBuf, int msgType,
			String correlationId) {
		return sendTextMessage(msgBuf, msgType, DEFAULT_MESSAGE_PRIORITY,
				correlationId);
	}

	/**
	 * 发送文本消息<br>
	 * 
	 * @param msgBuf
	 *            消息的具体内容<br>
	 * 
	 * @param msgType
	 *            消息类型<br>
	 * 
	 * @param priority
	 *            当前消息的优先级<br>
	 * 
	 * @param correlationId
	 *            告警流水号，每一条告警消息有一个唯一的告警流水号，可用于查找对应的告警消息<br>
	 *            注：告警流水号可以为null<br>
	 * 
	 * @return 发送成功返回true，发送失败返回false<br>
	 */
	public boolean sendTextMessage(byte[] msgBuf, int msgType, int priority,
			String correlationId) {
		MQMessage msg = new MQMessage();
		// 设置MQMD 格式字段
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
	 * 发送MQMessage类型的消息<br>
	 * 
	 * <p>
	 * 注：MQMessage为MQ的API中提供的一种消息类
	 * </p>
	 * 
	 * @param msg
	 *            MQMessage对象<br>
	 * 
	 * @return 发送成功返回true，否则返回false<br>
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
		// 重复向队列发送消息，retry_times次
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
	 * 向消息平台订阅指定的消息<br>
	 * 
	 * <p>
	 * 订阅：告警系统的消息处理由两部分完成，一部分为客户端，一部分为综合监控平台<br>
	 * 其中客户端需要往告警平台发送一条订阅消息（消息类型为0），告知综合监控平台，客户端需要什么消息，<br>
	 * 并准备在哪个队列中接收该消息，从而实现客户端与综合监控平台的消息传输
	 * </p>
	 * 
	 * @param msgTypes
	 *            以逗号分隔的消息类型字符串，如"1,4,5,6"，在实际订阅告警消息时，必须包含1、5号消息<br>
	 * 
	 * @param appName
	 *            应用名称，应用名称的长度要大于8个字节<br>
	 * 
	 * @param currentPid
	 *            当前进程ID<br>
	 * 
	 * @return 注册成功返回true， 否则返回false<br>
	 */
	public boolean regAdd(String msgTypes, String appName, int currentPid) {
		if (!checkParamNotNull(msgTypes, appName)) {
			log.error("Neither the msgTypes and the appName cannot be empty");
			return false;
		}
		AQMessage aqMsg = new AQMessage(Constants.REG_ADD_MSG_NO,
				Constants.MAX_QUEUE_BUFFER);

		String hostname = getHostName();
		// 设置订阅消息的各个值
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
		// 接收返回消息
		while (iLoop > 0) {
			// 接受消息，若return_code<0，接受失败，若等于0，成功
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
	 * 从消息平台注销已订阅的消息<br>
	 * 
	 * <p>
	 * 取消订阅：客户端往告警平台发送一条取消订阅消息（消息类型为3）<br>
	 * 告知综合监控平台，客户端要断开与消息平台的消息传输
	 * </p>
	 * 
	 * @param queueMgrName
	 *            订阅的队列管理器名称<br>
	 * 
	 * @param queueName
	 *            订阅时曾设置的接收队列名称<br>
	 * 
	 * @return 注销消息发送成功返回true，否则返回false<br>
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
	 * 获取当前收到的消息类型<br>
	 * 
	 * <p>
	 * 注：在调用recv方法返回不为空的情况下调用此方法<br>
	 * 在向消息平台订阅消息的情况下，客户端大致可根据当前系统时间与最近一次<br>
	 * 收到心跳消息的时间差(具体超时时间需要根据现场情况设定)判定是否已被消息平台注销，<br>
	 * 如果已被注销，需要调用regAdd()方法重新订阅<br>
	 * 本方法返回值为 5 代表当前消息是心跳消息
	 * </p>
	 * 
	 * @return 当前收到的消息类型<br>
	 */
	public int getMessageType() {
		if (currentMQMsg != null)
			return this.currentMQMsg.messageType - Constants.MSG_TYPE_FIRST;
		return -1;
	}

	/**
	 * 获取当前消息的告警流水号<br>
	 * 
	 * <p>
	 * 注：在调用recv方法返回不为空的情况下调用此方法
	 * </p>
	 * 
	 * @return 当前消息的告警流水号<br>
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
	 * 获取当前消息内容的字节型数据<br>
	 * 
	 * <p>
	 * 注：一般在recvMQMsg方法后调用此方法
	 * </p>
	 * 
	 * @return 当前消息的字节型数据<br>
	 */
	public byte[] getMessageBuffer() {
		if (!(null == currentMQMsg)) {
			return currentMsgBuf;
		}
		return null;
	}

	/**
	 * 获取接收队列的名称<br>
	 * 
	 * @return 接收队列的名称
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
	 * 获取当前消息平台心跳消息的序号<br>
	 * 
	 * <p>
	 * 在调用recv方法及getMessageType方法后调用。<br>
	 * 如果当前消息类型号是 5 的情况下，调用此方法，否则返回值没有实际意义<br>
	 * 正常情况，心跳序号是连续增长的，若消息平台程序重起，心跳序号可能重新从1开始
	 * </p>
	 * 
	 * @return 整形数值，当前心跳序号<br>
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