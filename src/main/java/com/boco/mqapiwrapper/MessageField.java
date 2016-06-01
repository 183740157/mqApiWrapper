package com.boco.mqapiwrapper;

/**
 * ��Ϣ��Ԫ��װ�� 
 * ��ҵ����Ϣ���漰������С�ĵ�Ԫ
 * �ṩ�˶���Ϣ��Ԫ�����û�ȡ���Ժͱ����Ȳ����ķ���
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
	 * ��Ϣ��Ԫ���� 
	 * ���Ը�����Ϣ��Ԫ���������ͽ��б��룬��ֵת�����ַ�������ʽ
	 * 
	 * @param  msg 
	 *         �ַ��������ͱ���������װ�ر�����ֵ
	 *         
	 * @return  ���ݲ�ͬ����Ϣ��Ԫ�������ͣ�ת����ֵ��ͬ
	 *          int���ͣ�ֵΪ4
	 *          short���ͣ�ֵΪ2
	 *          long���ͣ�ֵΪ4
	 *          float���ͣ�ֵΪ6
	 *          String���ͣ�ֵΪ����ʱ������ַ������ȼ���4
	 */
	public int encode(byte[] msg) {
		int rc = 0; // ת��ţ�����ȷ��msg����ôת��ģ��Ժ����ȷ����ô���н���

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
	 * ��Ϣ��Ԫ���� 
	 * ���ַ�����ת���ɶ�Ӧ�������͵�����
	 * 
	 * @param  msg
	 *         �ַ��������ͱ�����װ���˽���ǰ���ַ�����
	 *         
	 * @return  ���ݲ�ͬ����Ϣ��Ԫ�������ͣ�������ֵ��ͬ
	 *          int���ͣ�ֵΪ4
	 *          short���ͣ�ֵΪ2
	 *          long���ͣ�ֵΪ4
	 *          float���ͣ�ֵΪ6
	 *          String���ͣ�ֵΪ����ʱ������ַ������ȼ���4
	 *          ����ʧ�ܷ��� 1
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
