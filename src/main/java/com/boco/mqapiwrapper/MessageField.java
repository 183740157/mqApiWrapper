package com.boco.mqapiwrapper;

/**
 * 消息单元封装类 
 * 在业务消息中涉及到的最小的单元
 * 提供了对消息单元的设置获取属性和编解码等操作的方法
 * 
 */
class MessageField {
	private int fieldType;
	private int fieldSize;
	private String fieldName;
	private Object fieldValue;
	
	public MessageField(int fieldType,int fieldSize,String fieldName,Object fieldValue){
		this.fieldType=fieldType;
		this.fieldSize=fieldSize;
		this.fieldName=fieldName;
		this.fieldValue=fieldValue;
	}

	/**
	 * 消息单元编码 
	 * 可以根据消息单元的数字类型进行编码，将值转换成字符数组形式
	 * 
	 * @param  msg 
	 *         字符数组类型变量，用于装载编码后的值
	 *         
	 * @return  根据不同的消息单元数字类型，转码后的值不同
	 *          int类型，值为4
	 *          short类型，值为2
	 *          long类型，值为4
	 *          float类型，值为6
	 *          String类型，值为编码时传入的字符串长度加上4
	 */
	public int encode(byte[] msg) {
		int rc = 0; // 转码号，用来确定msg是怎么转码的，以后可以确定怎么进行解码

		switch (fieldType) {
		case Constants.DATA_TYPE_INTEGER:
			rc = CodecHelper.encode_int(msg, (Integer) fieldValue);
			break;
		case Constants.DATA_TYPE_CHAR:
			rc = CodecHelper.encode_char(msg, ((String) fieldValue), fieldSize);
			break;
		default:
			System.out.println("Unknow field type.");
			break;
		}
		return rc;
	}

	/**
	 * 消息单元解码 
	 * 将字符数组转换成对应数字类型的数据
	 * 
	 * @param  msg
	 *         字符数组类型变量，装载了解码前的字符数据
	 *         
	 * @return  根据不同的消息单元数字类型，解码后的值不同
	 *          int类型，值为4
	 *          short类型，值为2
	 *          long类型，值为4
	 *          float类型，值为6
	 *          String类型，值为编码时传入的字符串长度加上4
	 *          解码失败返回 1
	 */
	public int decode(byte[] msg) {
		int rc = 0;

		switch (fieldType) {
		case Constants.DATA_TYPE_INTEGER:
			CodecHelper.DecodeResult di = CodecHelper.decode_int(msg);    		    		
    		setFieldValue((Integer)di.result);
    		rc = di.returnDatalen;
    		break;
		case Constants.DATA_TYPE_CHAR:
			CodecHelper.DecodeResult dc = CodecHelper
					.decode_char(msg, fieldSize);
			setFieldValue((((String) dc.result)).trim());
			rc = dc.returnDatalen;
			break;
		default:
			rc = 1;
			break;
		}
		return rc;
	}

	public int getFieldType() {
		return fieldType;
	}

	public int getFieldSize() {
		return fieldSize;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Object getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(Object fieldValue) {
		this.fieldValue = fieldValue;
	}
}
