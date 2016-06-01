package com.boco.mqapiwrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

/**
 * MQ 基本操作封装类<br>
 * 
 * <p>
 * 主要提供了一些对MQ 的基本操作，例如：<br>
 * 连接MQ，断开与MQ连接，接收消息，发送消息等等<br>
 * 该类大部分方法，主要目的是提供MessageClient中的方法调用，不推荐使用<br>
 * </p>
 * 
 * @version 2.0
 * @author b
 */
public class MQCommonHelper {
	private static Logger log = LoggerFactory.getLogger(MQCommonHelper.class);

	private MQQueue msgQueue;
	private MQQueueManager qMgr;

	private int port;
	private String hostname;
	private String qName;
	private String channel;
	private String qMgrName;
	private int ccsid;
	private int qOptions;
	private Hashtable properties = new Hashtable();

	private boolean useDynamic;
	// 服务器标识，true代表服务器模式，false代表客户端模式
	private boolean bindingFlg = false;
	private boolean IsConnected;

	private int msgLen;
	private int msgType;
	private int msgPriority;

	private static ArrayList<Integer> reasonCodeList;

	private static final int MQRC_NO_MSG_AVAILABLE = 2033;
	private static final int MQRC_Q_FULL = 2053;
	private static final int MSG_RECV_TIMEOUT = 3;
	private static final int MSG_SEND_TIMEOUT = 20;
	private static final int DEFAULT_MQ_CCSID = 1383;
	private static final int DEFAULT_MQ_PORT = 1414;
	private static final int RECV_MQ_OPTIONS = MQC.MQGMO_WAIT | MQC.MQMO_NONE
			| MQC.MQGMO_FAIL_IF_QUIESCING | MQC.MQGMO_COMPLETE_MSG;

	static {
		reasonCodeList = new ArrayList<Integer>();
		reasonCodeList.add(2009); // MQRC_CONNECTION_BROKEN
		reasonCodeList.add(2018); // MQRC_HCONN_ERROR
		reasonCodeList.add(2019); // MQRC_HOBJ_ERROR
		reasonCodeList.add(2025); // MQRC_MAX_CONNS_LIMIT_REACHED
		reasonCodeList.add(2059); // MQRC_Q_MGR_NOT_AVAILABLE
		reasonCodeList.add(2161); // MQRC_Q_MGR_QUIESCING
		reasonCodeList.add(2162); // MQRC_Q_MGR_STOPPING
		reasonCodeList.add(2202); // MQRC_CONNECTION_QUIESCING
		reasonCodeList.add(2203); // MQRC_CONNECTION_STOPPING
		reasonCodeList.add(2223); // MQRC_Q_MGR_NOT_ACTIVE
		reasonCodeList.add(2273); // MQRC_CONNECTION_ERROR
		reasonCodeList.add(2283); // MQRC_CHANNEL_STOPPED
		reasonCodeList.add(6124); // MQRC_NOT_CONNECTED
		reasonCodeList.add(6125); // MQRC_NOT_OPEN
	}

	public MQCommonHelper(String mqHost, int port, String qMgrName,
			String channelName, String qName, int qopts) {
		hostname = mqHost;
		this.port = port;
		this.qMgrName = qMgrName;
		this.qName = qName.trim();
		channel = channelName;
		qOptions = qopts;
	}

	/**
	 * 建立与MQ的连接<br>
	 * 
	 * <p>
	 * 注：该方法不建议使用，主要供内部调用
	 * </p>
	 * 
	 * @return 连接成功返回true， 否则返回false<br>
	 */
	public boolean connect() {
		if (!bindingFlg) {
			properties.put(MQConstants.HOST_NAME_PROPERTY, hostname);
			properties.put(MQConstants.CHANNEL_PROPERTY, channel);
			properties.put(MQConstants.PORT_PROPERTY, port);
			properties.put(MQConstants.CCSID_PROPERTY, ccsid);
			MQException.log = null;
		}

		try {
			if (qMgr == null) {
				qMgr = new MQQueueManager(qMgrName, properties);
			}

			if (!(qMgr == null)) {
				msgQueue = qMgr.accessQueue(qName, qOptions);
				IsConnected = true;
			}
		} catch (MQException me) {
			log.error("connect with MQException , reason:{}", me.getMessage());
		}

		return IsConnected;
	}

	/**
	 * 动态连接MQ<br>
	 * 
	 * <p>
	 * 注：该方法不建议使用，主要供内部调用
	 * </p>
	 * 
	 * @return 连接成功返回true， 否则返回false<br>
	 */
	public boolean connectWithDynamic() {
		useDynamic = true;
		if (!bindingFlg) {
			properties.put(MQConstants.HOST_NAME_PROPERTY, hostname);
			properties.put(MQConstants.CHANNEL_PROPERTY, channel);
			properties.put(MQConstants.PORT_PROPERTY, port);
			properties.put(MQConstants.CCSID_PROPERTY, ccsid);
			properties.put(MQConstants.TRANSPORT_PROPERTY,
					MQConstants.TRANSPORT_MQSERIES);
			MQException.log = null;
		}
		try {
			if (qMgr == null) {
				qMgr = new MQQueueManager(qMgrName, properties);
			}
			if (!(qMgr == null)) {
				msgQueue = qMgr.accessQueue(qName, qOptions, "", "*", "");

				log.info("Connect to " + msgQueue.name.trim() + " " + "Success");

				IsConnected = true;
			}
		} catch (MQException me) {
			log.error("connectWithDynamic with MQException :" + me.getMessage());
		}

		return IsConnected;
	}

	public void disConnect() {
		if (IsConnected) {
			try {
				if (!(msgQueue == null)) {
					msgQueue.close();
				}
			} catch (MQException me) {
				log.error("Close " + this.qName + " with MQException :"
						+ me.getMessage());
			}
			msgQueue = null;

			try {
				if (!(qMgr == null)) {
					qMgr.disconnect();
				}
			} catch (MQException e) {
				log.error("disConnect from QueueManager with MQException :"
						+ e.getMessage());
			}
			qMgr = null;

			IsConnected = false;
		}
	}

	/**
	 * 延迟连接MQ<br>
	 * 
	 * <p>
	 * 延迟second秒后连接MQ
	 * </p>
	 * 
	 * @param second
	 *            时间（单位：秒）<br>
	 * 
	 * @return 连接成功返回true，失败返回false<br>
	 */
	private boolean connectRepeat(int second) {
		try {
			Thread.sleep(second * 1000);
		} catch (InterruptedException e) {
			log.error("connectRepeat with InterruptedException :"
					+ e.getMessage());
		}

		if (useDynamic) {
			if (!connectWithDynamic()) {
				return false;
			}
		} else {
			if (!connect()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 接收业务消息<br>
	 * 
	 * <p>
	 * 注：该方法不建议使用，主要供内部调用
	 * </p>
	 * 
	 * @param aqMsg
	 *            保存接收到的消息<br>
	 * 
	 * @param timeout
	 *            等待超时时间，单位为毫秒<br>
	 * 
	 * @return 接收消息成功返回0，接收失败返回小于0的值<br>
	 */
	public int receiveMsg31(AQMessage aqMsg, int timeout) {
		byte[] recvBuf = null;

		int recvLen = -1;
		MQMessage mqMsg = receiveMQMsg(timeout);
		if (null == mqMsg)
			return -1;

		if (!mqMsg.format.equalsIgnoreCase(MQC.MQFMT_NONE))
			return Constants.MESSAGE_RECEIVE_NONE;

		try {
			msgLen = mqMsg.getDataLength();
			recvLen = msgLen;
			recvBuf = new byte[msgLen];
			mqMsg.readFully(recvBuf);
		} catch (IOException ie) {
			log.error("Extracted message failed, reason:{}", ie.getMessage());
			return -1;
		}

		msgType = mqMsg.messageType;
		msgType -= Constants.MSG_TYPE_FIRST;
		msgPriority = mqMsg.priority;

		aqMsg.setMessageType(msgType);
		aqMsg.setMessagePriority(msgPriority);
		aqMsg.setMessageBuffer(recvBuf);

		return Constants.MESSAGE_OK;
	}

	/**
	 * 接收MQMessage格式的消息<br>
	 * 
	 * <p>
	 * 注：MQMessage为MQ的API中提供的一种消息类
	 * </p>
	 * 
	 * @param timeout
	 *            等待超时时间，单位为毫秒<br>
	 * 
	 * @return 返回MQMessage类型的消息，如果没收到有效的消息则返回null<br>
	 */
	public MQMessage receiveMQMsg(int timeout) {
		if (!IsConnected) {
			if (!connectRepeat(MSG_RECV_TIMEOUT)) {
				msgType = -1;
				return null;
			}
		}
		timeout = receiveMQMsgCheckParam(timeout);
		MQMessage rmsg = new MQMessage();
		MQGetMessageOptions gmo = new MQGetMessageOptions();

		gmo.options = RECV_MQ_OPTIONS;
		gmo.waitInterval = timeout;

		try {
			msgQueue.get(rmsg, gmo);
			msgType = rmsg.messageType - Constants.MSG_TYPE_FIRST;
			return rmsg;
		} catch (MQException me) {
			msgType = -1;
			int rs = me.reasonCode;

			if (rs == MQRC_NO_MSG_AVAILABLE) {
				return null;
			} else if (reasonCodeList.contains(rs)) {
				log.error("receiveMQMsg with MQException : " + me);
				disConnect();
			} else {
				log.error("receiveMQMsg with MQException : " + me);
			}

			return null;
		}
	}

	private int receiveMQMsgCheckParam(int timeout) {
		if (timeout < 0) {
			timeout = 1000;
		}
		return timeout;
	}

	/**
	 * 发送心跳消息<br>
	 * 
	 * <p>
	 * 注：该方法不建议使用，主要供内部调用<br>
	 * 心跳消息主要是为了判断发送队列和接收队列是否可以正常收发消息<br>
	 * 每隔一段时间，客户端会向平台发送心跳消息，平台收到会根据该消息作回应<br>
	 * 再发出一条回复消息，若在一定时间内，客户端接收到该消息，这说明消息收发正常
	 * </p>
	 * 
	 * @param buf
	 *            要发送的字节型消息<br>
	 * 
	 * @param qName
	 *            接收队列名字<br>
	 * 
	 * @return 发送成功返回0，失败返回非0值<br>
	 */
	public int sendTestAck(byte[] buf, String qName) {
		AQMessage msg = new AQMessage(Constants.TEST_MSG_NO,
				Constants.TEST_NUMBER_MESSAGE_BUFFER);
		msg.setMessageBuffer(buf);

		int rt = msg.decode();
		if (rt == 1) {
			return rt;
		}

		AQMessage smsg = new AQMessage(Constants.TEST_ACK_MSG_NO, 2048);
		rt = smsg.setFieldValue("queue_name", new String(qName));
		if (rt == 1) {
			return rt;
		}

		Object obj = new Integer((Integer) msg.getFieldValue("test_numbr"));
		rt = smsg.setFieldValue("test_numbr", (Integer) obj);
		if (rt == 1) {
			return rt;
		}

		smsg.encode();
		return sendAQMessage(smsg);
	}

	public int sendAQMessage(AQMessage aqMsg) {
		int msgLen = aqMsg.getMessageLength();
		int msgType = aqMsg.getMessageType();
		int prio = aqMsg.getMessagePriority();

		return setMsgProperty(aqMsg.getMessageBuf(), msgLen, msgType, prio,
				MQC.MQEI_UNLIMITED) ? 0 : 1;
	}

	private boolean setMsgProperty(byte[] buff, int bufLen, int msgType,
			int priority, int expiryTime) {
		MQMessage msg = new MQMessage();
		msg.format = MQC.MQFMT_NONE;
		int msgPrio = priority;
		msgPrio = setPriority(msgType, msgPrio);
		msg.priority = msgPrio;
		if (msgType < 0 || msgType >= Constants.MSG_TYPE_REAL)
			return false;

		try {
			msg.write(buff);
		} catch (IOException ie) {
			log.error("Failed to Store the Message, reason:{}", ie.getMessage());
		}

		msg.messageType = msgType + Constants.MSG_TYPE_FIRST;
		msg.expiry = expiryTime;

		return sendMQMsg(msg);
	}

	private int setPriority(int msgType, int msgPrio) {
		if (msgType > 100) {
			if (msgPrio > 7) {
				msgPrio = 7;
			} else if (msgPrio < 0) {
				msgPrio = 0;
			}
		} else {
			if (msgPrio > 9) {
				msgPrio = 9;
			} else if (msgPrio < 0) {
				msgPrio = 0;
			}
		}
		return msgPrio;
	}

	/**
	 * 发送MQMessage消息<br>
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
	public boolean sendMQMsg(MQMessage msg) {
		if (!IsConnected) {
			if (!connectRepeat(MSG_SEND_TIMEOUT))
				return false;
		}

		MQPutMessageOptions pmo = new MQPutMessageOptions();
		pmo.options = MQC.MQPMO_LOGICAL_ORDER;
		try {
			msgQueue.put(msg, pmo); // 将消息传入队列
		} catch (MQException me) {
			if (me.reasonCode == MQRC_Q_FULL) { // MQRC_Q_FULL
				return false;
			} else if (reasonCodeList.contains(me.reasonCode)) {
				log.error("sendMQMsg with MQException :" + me.getMessage());
				disConnect();
			} else {
				log.error("sendMQMsg with MQException :" + me.getMessage());
			}

			return false;
		}

		return true;
	}

	public int getPort() {
		return port;
	}

	/**
	 * 获取当前所设置MQ服务所在主机（主机名或IP）<br>
	 * 
	 * @return 当前所设置MQ服务所在主机（主机名或IP）<br>
	 */
	public String getHostname() {
		return hostname;
	}

	public String getQname() {
		if (!(msgQueue == null)) {
			return msgQueue.name.trim();
		} else {
			return "";
		}
	}

	public String getChannel() {
		return channel.trim();
	}

	public String getQMgrName() {
		return qMgrName;
	}

	public boolean isBindingFlg() {
		return bindingFlg;
	}

	public void setBindingFlg(boolean bindingFlg) {
		this.bindingFlg = bindingFlg;
	}

	public int getQOptions() {
		return qOptions;
	}

	public int getCcsid() {
		return ccsid;
	}

	public void setCcsid(int ccsid) {
		this.ccsid = ccsid;
	}
}
