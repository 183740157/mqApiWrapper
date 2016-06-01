package com.boco.mqapiwrapper;

/**
 * 公共类型及常量定义类
 *
 */
class Constants {

	/**
	 * 定义系统消息号
	 */
	public static final int REG_ADD_MSG_NO = 0;
	public static final int REG_ACK_MSG_NO = 1;
	public static final int REG_CHG_MSG_NO = 2;
	public static final int REG_DLT_MSG_NO = 3;
	public static final int TEST_ACK_MSG_NO = 4;
	public static final int TEST_MSG_NO = 5;
	public static final int BROADCAST_ACK_MSG_NO = 6;
	public static final int CLNT_LOGIN_MSG_NO = 7;
	public static final int PLATFORM_QUERY_MSG_NO = 8;
	public static final int PLATFORM_STATUS_MSG_NO = 9;

	public static final short MESSAGE_OK = 0;
	public static final int MSG_TYPE_FIRST = 65536;
	public static final int MSG_TYPE_REAL = 999934463;

	public static final String DEFAULT_CLNT_QUEUE_NAME = "MODEL.Q";
	public static final String DEFAULT_SNDR_QUEUE_NAME = "CTRL.Q";
	public static final String DEFAULT_MANAGER_NAME = "DEFAULT_QM";
	public static final String DEFAULT_MSG_ID = "000000000000000000000000";

	public static final short MAX_QUEUE_BUFFER = 4096;
	public static final short TEST_NUMBER_MESSAGE_BUFFER = 2048;
	public static final int MAX_BULK_MSG_BUFFER = 204800;
	public static final short DEFAULT_WAIT_ACK_TIME = 10;

	public static final short DATA_TYPE_INTEGER = 0;
	public static final short DATA_TYPE_SHORT = 1;
	public static final short DATA_TYPE_LONG = 2;
	public static final short DATA_TYPE_FLOAT = 3;
	public static final short DATA_TYPE_CHAR = 4;

	public static final short MESSAGE_RECEIVE_NONE = -31305;
	public static final short MESSAGE_RECEIVE_INVALID = -31307;

}
