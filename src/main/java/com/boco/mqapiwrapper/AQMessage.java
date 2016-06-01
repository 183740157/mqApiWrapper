package com.boco.mqapiwrapper;

import java.util.*;

/**
 * 业务消息封装类类
 * 
 * 提供了对业务消息的基本操作
 * 1.初始化相应的业务消息
 * 2.对业务消息进行编解码
 * 3.操作业务消息的值
 * 4.设置获取业务消息的属性
 */
class AQMessage {
	private byte[] messageBuffer;
	private int messageType;
	private int messagePriority;
	private int messageLength;
	private static final String defaultValue = "NO NAME";
	// 装载一条消息的所有消息单元
	private List<MessageField> fieldList;
	
	private Map<Integer,List> fieldListPackage;
	
	{
		fieldListPackage = new HashMap<Integer, List>();
		
		fieldList = new ArrayList<MessageField>();
		addMsgCell("machine_name", Constants.DATA_TYPE_CHAR, 32, (Object) defaultValue);
		addMsgCell("user_name", Constants.DATA_TYPE_CHAR, 32, (Object) defaultValue);
		addMsgCell("application_name", Constants.DATA_TYPE_CHAR, 32,
				(Object) defaultValue);
		addMsgCell("manager_name", Constants.DATA_TYPE_CHAR, 32, (Object) defaultValue);
		addMsgCell("queue_name", Constants.DATA_TYPE_CHAR, 48, (Object) defaultValue);
		addMsgCell("msg_type", Constants.DATA_TYPE_CHAR, 256, (Object) defaultValue);
		addMsgCell("pid", Constants.DATA_TYPE_INTEGER, 4, 0);
		addMsgCell("clnt_type", Constants.DATA_TYPE_INTEGER, 4, 0);
		addMsgCell("unique_inst", Constants.DATA_TYPE_INTEGER, 4, 0);
		fieldListPackage.put(Constants.REG_ADD_MSG_NO, fieldList);
		
		fieldList = new ArrayList<MessageField>();
		addMsgCell("result", Constants.DATA_TYPE_INTEGER, 4, 0);
		addMsgCell("reason", Constants.DATA_TYPE_CHAR, 256, (Object) defaultValue);
		fieldListPackage.put(Constants.REG_ACK_MSG_NO, fieldList);
		
		fieldList = new ArrayList<MessageField>();
		addMsgCell("manager_name", Constants.DATA_TYPE_CHAR, 32, (Object) defaultValue);
		addMsgCell("queue_name", Constants.DATA_TYPE_CHAR, 48, (Object) defaultValue);
		addMsgCell("msg_type", Constants.DATA_TYPE_CHAR, 256, (Object) defaultValue);
		fieldListPackage.put(Constants.REG_CHG_MSG_NO, fieldList);
		
		fieldList = new ArrayList<MessageField>();
		addMsgCell("manager_name", Constants.DATA_TYPE_CHAR, 32, (Object) defaultValue);
		addMsgCell("queue_name", Constants.DATA_TYPE_CHAR, 48, (Object) defaultValue);
		fieldListPackage.put(Constants.REG_DLT_MSG_NO, fieldList);
		
		fieldList = new ArrayList<MessageField>();
		addMsgCell("queue_name", Constants.DATA_TYPE_CHAR, 48, (Object) defaultValue);
		addMsgCell("test_numbr", Constants.DATA_TYPE_INTEGER, 4, 0);
		addMsgCell("status", Constants.DATA_TYPE_CHAR, 128, (Object) defaultValue);
		fieldListPackage.put(Constants.TEST_ACK_MSG_NO, fieldList);
		
		fieldList = new ArrayList<MessageField>();
		addMsgCell("queue_name", Constants.DATA_TYPE_CHAR, 48, (Object) defaultValue);
		addMsgCell("test_numbr", Constants.DATA_TYPE_INTEGER, 4, 0);
		addMsgCell("status", Constants.DATA_TYPE_CHAR, 128, (Object) defaultValue);
		fieldListPackage.put(Constants.TEST_MSG_NO, fieldList);
		
		fieldList = new ArrayList<MessageField>();
		addMsgCell("queue_name", Constants.DATA_TYPE_CHAR, 48, (Object) defaultValue);
		addMsgCell("related_msg_type", Constants.DATA_TYPE_INTEGER, 4, 0);
		fieldListPackage.put(Constants.BROADCAST_ACK_MSG_NO, fieldList);
		
		fieldList = new ArrayList<MessageField>();
		addMsgCell("type", Constants.DATA_TYPE_INTEGER, 4, 0);
		addMsgCell("machine_name", Constants.DATA_TYPE_CHAR, 32, (Object) defaultValue);
		addMsgCell("user_name", Constants.DATA_TYPE_CHAR, 32, (Object) defaultValue);
		addMsgCell("application_name", Constants.DATA_TYPE_CHAR, 32,
				(Object) defaultValue);
		addMsgCell("login_time", Constants.DATA_TYPE_CHAR, 20, (Object) defaultValue);
		fieldListPackage.put(Constants.CLNT_LOGIN_MSG_NO, fieldList);
		
		fieldList = new ArrayList<MessageField>();
		addMsgCell("flag", Constants.DATA_TYPE_INTEGER, 4, 0);
		fieldListPackage.put(Constants.PLATFORM_QUERY_MSG_NO, fieldList);
		
		fieldList = new ArrayList<MessageField>();
		addMsgCell("app_name", Constants.DATA_TYPE_CHAR, 32, (Object) defaultValue);
		addMsgCell("queue_name", Constants.DATA_TYPE_CHAR, 48, (Object) defaultValue);
		addMsgCell("time_out", Constants.DATA_TYPE_INTEGER, 4, 0);
		fieldListPackage.put(Constants.PLATFORM_STATUS_MSG_NO, fieldList);
	}
	
	public AQMessage(int len) {
		messageLength = len;
		fieldList = null;
	}

	public AQMessage(int msgType, int len) {
		fieldList = new ArrayList<MessageField>();
		messageType = msgType;
		messageLength = 100;
		messagePriority = 8;
		fieldList = fieldListPackage.get(msgType);
		messageLength = len;
	}
	
	/**
	 * 设置消息单元信息，并添加到消息单元集合中
	 * 
	 * @param  name
	 *         消息单元名称
	 *         
	 * @param  type
	 *         消息单元的数据类型，int，String，float等
	 *         
	 * @param  size
	 *         消息单元数据长度，仅对String类型的数据有用，其他类型用不到
	 *         
	 * @param value
	 *        消息单元的值，跟消息单元的名字对应
	 */
	public void addMsgCell(String name, int type, int size, Object value) {
		MessageField msgField = new MessageField(type, size, name, value);
		fieldList.add(msgField);
	}

	/**
	 * 根据一条消息中消息单元的名称，设置对应的值
	 * 
	 * @param  fName
	 *         消息单元的名称
	 *            
	 * @param  fValue
	 *         消息单元的值
	 *            
	 * @return  0代表设置成功，1代表设置失败
	 */
	public int setFieldValue(String fieldName, Object fieldValue) {
		for (MessageField mField : fieldList) {
			if (mField.getFieldName().equalsIgnoreCase(fieldName)) {
				mField.setFieldValue(fieldValue);
				return 0;
			}
		}
		return 1;
	}

	public Object getFieldValue(String fName) {
		for (MessageField mField : fieldList) {
			if (mField.getFieldName().equalsIgnoreCase(fName)) {
				Object v = mField.getFieldValue();
				return v;
			}
		}
		return null;
	}

	public int setMessageBuffer(byte[] buffer) {
		if (null == buffer) {
			return 1;
		}

		messageLength = buffer.length;
		messageBuffer = new byte[messageLength];
		for (int i = 0; i < messageLength; i++) {
			messageBuffer[i] = buffer[i];
		}

		int tp = decodeMsgType();
		AQMessage aMsg = new AQMessage(tp, Constants.MAX_QUEUE_BUFFER);
		if ((tp >= 0) && (aMsg != null)) {
			this.fieldList = aMsg.fieldList;
		}
		
		return 0;
	}

	/**
	 * 对messageBuffer的消息数据类型进行解密
	 * 
	 * @return 解密后的值
	 */
	private int decodeMsgType() {
		int msgtype = -1;

		if (null == this.messageBuffer)
			return msgtype;

		CodecHelper.DecodeResult dr = CodecHelper.decode_int(messageBuffer);
		msgtype = (Integer) dr.result;

		if (msgtype < 0) {
			return Constants.MESSAGE_RECEIVE_INVALID;
		}

		return msgtype;
	}

	public byte[] getMessageBuf() {
		return messageBuffer;
	}

	/**
	 * 消息加密
	 * 
	 * @return 编码成功返回0 ，编码失败返回1
	 */
	public void encode() {
		byte[] reten = new byte[Constants.MAX_QUEUE_BUFFER];
		messageLength = encode(reten);
		messageBuffer = new byte[messageLength];

		for (int i = 0; i < messageLength; i++) {
			messageBuffer[i] = reten[i];
		}
	}
	
	/**
	 * 将该条消息中的所有消息单元进行编码
	 * 
	 * @param  msg
	 *         接收经过编码后的消息
	 * 
	 * @return  msg字节数组的长度
	 */
	private int encode(byte[] msg) {

		byte bType[] = new byte[4];
		int rc = CodecHelper.encode_int(bType, messageType); // 将消息类型进行加密
		for (int i = 0; i < rc; i++) {
			msg[i] = bType[i];
		}

		Iterator<MessageField> it = fieldList.iterator();
		while (it.hasNext()) {
			MessageField mf = it.next();
			byte[] bMsg = new byte[Constants.MAX_QUEUE_BUFFER];
			int rc1 = mf.encode(bMsg);
			for (int i = 0; i < rc1; i++) {
				msg[i + rc] = bMsg[i];
			}
			rc += rc1;
		}

		this.messageLength = rc;
		return rc;
	}


	/**
	 * 消息解码
	 * 
	 * @return 0 解密成功，1 解密失败
	 */
	public int decode() {
		messageLength = decode(messageBuffer);
		if (messageLength > 0)
			return 0;
		else
			return 1;
	}
	
	/**
	 * 将字节消息解码成各个消息单元
	 * 
	 * @param  msg
	 *         字节消息
	 * 
	 * @return  字节消息数组的长度
	 */
	private int decode(byte[] msg) {
		int msgtype = -1;
		CodecHelper.DecodeResult di = CodecHelper.decode_int(msg);
		int rc = di.returnDatalen;
		msgtype = (Integer) di.result;

		if (msgtype < 0 || (messageType != msgtype)) {
			return Constants.MESSAGE_RECEIVE_INVALID;
		}

		Iterator<MessageField> it = fieldList.iterator();
		while (it.hasNext()) {
			MessageField mField = it.next();
			int bufLen;
			if (mField.getFieldType() == Constants.DATA_TYPE_CHAR) {
				bufLen = mField.getFieldSize() + 4;
			} else {
				bufLen = mField.getFieldSize();
			}

			byte tmpMsg[] = new byte[bufLen];

			if (bufLen > (rc + mField.getFieldSize() + 1))
				return -1;

			for (int i = 0; i < mField.getFieldSize(); i++) {
				tmpMsg[i] = msg[rc + i];
			}

			int rc1 = mField.decode(tmpMsg);
			rc += rc1;
		}

		return rc;
	}

	public int getTestNumber() {
		int testNum = -1;
		if (getMessageType() == Constants.TEST_MSG_NO) {
			List<MessageField> fList = fieldList;
			for (MessageField mField : fList) {
				if (mField.getFieldName().trim().equalsIgnoreCase("test_numbr")) {
					testNum = (Integer) mField.getFieldValue();
					break;
				}
			}
		}

		return testNum;
	}
	
	public void setMessageType(int msgType) {
		messageType = msgType;
	}
	
	public int getMessageType() {
		return messageType;
	}
	
	public void setMessagePriority(int messagePriority) {
		this.messagePriority = messagePriority;
	}
	
	public int getMessagePriority() {
		return messagePriority;
	}
	
	public int getMessageLength() {
		return messageLength;
	}
}