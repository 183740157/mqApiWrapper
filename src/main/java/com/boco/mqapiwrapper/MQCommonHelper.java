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
 * MQ ����������װ��<br>
 * 
 * <p>
 * ��Ҫ�ṩ��һЩ��MQ �Ļ������������磺<br>
 * ����MQ���Ͽ���MQ���ӣ�������Ϣ��������Ϣ�ȵ�<br>
 * ����󲿷ַ�������ҪĿ�����ṩMessageClient�еķ������ã����Ƽ�ʹ��<br>
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
	// ��������ʶ��true���������ģʽ��false����ͻ���ģʽ
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
	 * ������MQ������<br>
	 * 
	 * <p>
	 * ע���÷���������ʹ�ã���Ҫ���ڲ�����
	 * </p>
	 * 
	 * @return ���ӳɹ�����true�� ���򷵻�false<br>
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
	 * ��̬����MQ<br>
	 * 
	 * <p>
	 * ע���÷���������ʹ�ã���Ҫ���ڲ�����
	 * </p>
	 * 
	 * @return ���ӳɹ�����true�� ���򷵻�false<br>
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
	 * �ӳ�����MQ<br>
	 * 
	 * <p>
	 * �ӳ�second�������MQ
	 * </p>
	 * 
	 * @param second
	 *            ʱ�䣨��λ���룩<br>
	 * 
	 * @return ���ӳɹ�����true��ʧ�ܷ���false<br>
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
	 * ����ҵ����Ϣ<br>
	 * 
	 * <p>
	 * ע���÷���������ʹ�ã���Ҫ���ڲ�����
	 * </p>
	 * 
	 * @param aqMsg
	 *            ������յ�����Ϣ<br>
	 * 
	 * @param timeout
	 *            �ȴ���ʱʱ�䣬��λΪ����<br>
	 * 
	 * @return ������Ϣ�ɹ�����0������ʧ�ܷ���С��0��ֵ<br>
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
	 * ����MQMessage��ʽ����Ϣ<br>
	 * 
	 * <p>
	 * ע��MQMessageΪMQ��API���ṩ��һ����Ϣ��
	 * </p>
	 * 
	 * @param timeout
	 *            �ȴ���ʱʱ�䣬��λΪ����<br>
	 * 
	 * @return ����MQMessage���͵���Ϣ�����û�յ���Ч����Ϣ�򷵻�null<br>
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
	 * ����������Ϣ<br>
	 * 
	 * <p>
	 * ע���÷���������ʹ�ã���Ҫ���ڲ�����<br>
	 * ������Ϣ��Ҫ��Ϊ���жϷ��Ͷ��кͽ��ն����Ƿ���������շ���Ϣ<br>
	 * ÿ��һ��ʱ�䣬�ͻ��˻���ƽ̨����������Ϣ��ƽ̨�յ�����ݸ���Ϣ����Ӧ<br>
	 * �ٷ���һ���ظ���Ϣ������һ��ʱ���ڣ��ͻ��˽��յ�����Ϣ����˵����Ϣ�շ�����
	 * </p>
	 * 
	 * @param buf
	 *            Ҫ���͵��ֽ�����Ϣ<br>
	 * 
	 * @param qName
	 *            ���ն�������<br>
	 * 
	 * @return ���ͳɹ�����0��ʧ�ܷ��ط�0ֵ<br>
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
	 * ����MQMessage��Ϣ<br>
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
	public boolean sendMQMsg(MQMessage msg) {
		if (!IsConnected) {
			if (!connectRepeat(MSG_SEND_TIMEOUT))
				return false;
		}

		MQPutMessageOptions pmo = new MQPutMessageOptions();
		pmo.options = MQC.MQPMO_LOGICAL_ORDER;
		try {
			msgQueue.put(msg, pmo); // ����Ϣ�������
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
	 * ��ȡ��ǰ������MQ����������������������IP��<br>
	 * 
	 * @return ��ǰ������MQ����������������������IP��<br>
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
