package com.boco.mqapiwrapper;

/**
 * ��Ϣ����빦�ܷ�װ��<br>
 * 
 * <p>
 * �ṩ�����ͣ������ͣ������ͣ��ַ������������������͵ı�����뷽��<br>
 * �ù������ṩ�ı��뷽ʽ�ǽ���ͬ���͵�����ת�����ֽ����ݣ��Լ��ֽ�����ת��Ϊ��Ӧ����������<br>
 * ע��������ķ�����������ʹ�ã���Ҫ���ڲ�����<br>
 * </p>
 * 
 * <dl>
 * <dt>����������ֽ�����Ϊbyte[] msg<br></dt>
 * 
 * <dt>��msg����һ���ֽ���Ϣ����ô����Ϣ�Ľṹ���£�</dt>
 * <dd>msg[0]��...��msg[3]�ⲿ�ִ洢��Ϣ���ͣ�msg[4]��msg[4]�Ժ���ֽ�<br>
 * ����������һ����Ϣ�ĸ�����Ϣ��Ԫ��Ϣ����Ϊ����Ϣ��ԪΪint�����ݣ�<br>
 * ��ֱ�ӽ�����룬�������msg[4]��...��msg[7]�С��������String�����ݣ�<br>
 * ������루����ʱ�ᴫ��һ��maxLength������������ת�����ֽ����鳤�ȣ���<br>
 * maxLength����int�ͱ��뷽ʽ���浽msg[8]��...��msg[11]�У����Ŷ�String��Ϣ������б��룬<br>
 * ����String���ݱ��������鳤��Ϊlen�����ⲿ�ִ���msg[12]��...��msg[11+len]�У�<br>
 * msg[12+len]��...��msg[12+maxLength-len]��ȫ����ֵΪ0�����ٴ������ݵ�ʱ�򣬴�<br>
 * msg[13+maxLength-len]��ʼ���ֽ�����<br></dd>
 * 
 * <dt>short������{@linkplain #encode_short(byte[], short) ����}
 * {@linkplain #decode_short(byte[]) ����}��</dt>
 * <dd>short�����ݳ���Ϊ16bit��������Ϊ�߰�λ�͵Ͱ�λ�ֱ����msg[0]��msg[1]�У�����ɱ��롣<br>
 * ����һ��short�͵�����0����msg[0]��msg[1]�ֱ���뵽short���ݸ߰�λ�͵Ͱ�λ������ɽ���<br></dd>
 * 
 * <dt>int������{@linkplain #encode_int(byte[], int) ����}
 * {@linkplain #decode_int(byte[]) ����}��</dt>
 * <dd>int�����ݳ���Ϊ32bit����32λ���Ϊ�Ĳ��ִ���msg[0]��msg[1]��msg[2]��msg[3]�����У�����ɱ��롣<br>
 * ���뷽ʽ��short�ͻ�����ͬ<br></dd>
 * 
 * <dt>float������{@linkplain #encode_float(byte[], float) ����}
 * {@linkplain #decode_float(byte[]) ����}��</dt>
 * <dd>����float���������γ�10��ֱ��������С��1����һ��short�����ݼ�¼����10�Ĵ���<br>
 * Ȼ�󽫴�����float���ݳ���10^10ת��Ϊһ��int������<br>
 * ��󽫸�int��������֮ǰ��short������ֽ��������δ�ŵ�msg[0]��msg[1]��...��msg[5]��<br>
 * ������̾��ǽ�����Ĺ��̷��������<br></dd>
 * 
 * <dt>String������{@linkplain #encode_char(byte[], String, int) ����}
 * {@linkplain #decode_char(byte[], int) ����}��</dt>
 * <dd>�ַ�������ʱ���˱�������ݻ����Բ��������int������maxLength�������ַ����������<br>
 * �ɵ��ֽ����鳤�ȣ����Ƚ�maxLength������ֽ�����msg[0]��msg[1]��msg[2]��msg[3]��String<br>
 * �������ݿ�����getBytes()���б��룬�������������ݴ��ֽ������msg[4]��ʼ�洢��ֱ��ȫ��<br>
 * �洢��ϣ�ʣ��δ�����ݵ����鵥Ԫȫ����ֵΪ0������ɱ��뽫�ֽ�������int�ͽ�ǰ��λ���룬<br>
 * �����ֵΪ���ַ�����ĳ��ȣ���msg[4]��msg[5]...���������ʹ��new String(byte[])����<br></dd>
 * 
 * <dt>ϵͳ��Ϣ����������£�</dt>
 * <dd>���Ƚ���Ϣ����msgType��{@linkplain #encode_int(byte[], int) encode_int()}
 * ���б��룬���浽msg[0]...msg[3]<br>
 * Ȼ����ݹ���ϵͳ��Ϣʱ�����ĺܶ���Ϣ��Ԫ���ֱ���ò�ͬ�ı��뷽ʽ�����ϣ����б���<br>
 * ÿ�ν�һ����Ϣ��Ԫ������ֽ����飬����ŵ�֮ǰ�������ݵĺ��棬һ����һ�����<br>
 * ��������Ϣ��Ԫ��������ֽ����鲢���뵽msg�����У��������Ϣ�������<br></dd>
 * 
 * <dt>ϵͳ��Ϣ����������£�</dt>
 * <dd>���Ƚ�msg�ֽ������ǰ��λ��{@linkplain #decode_int(byte[]) decode_int()}
 * ���룬���õ�ֵΪ����Ϣ����Ϣ����msgType <br>
 * Ȼ����ݸ���Ϣ�����жϽ��յĸ�����Ϣ��Ԫ����Ϊ�������ͣ���ʹ�ö�Ӧ�Ľ��뷽�����н���<br></dd>
 * </dl>
 * 
 * @version 2.0
 * @author b
 */
public class CodecHelper {

	/**
	 * �Զ���װ�ؽ�������Ϣ��<br>
	 * 
	 * <p>ע���������Ϊ�ڲ����û�д��������ʱʹ��</p>
	 */
	public static class DecodeResult {
		public int returnDatalen;
		public Object result;
	};

	public static int encode_int(byte[] msg, int value) {
		int leadZeros = Integer
				.numberOfLeadingZeros(value < 0 ? ~value : value);

		int byteNum = (40 - leadZeros) / 8;

		for (int n = 0; n < byteNum; n++) {
			msg[3 - n] = (byte) (value >>> (n * 8));
		}

		return 4;
	}

	public static CodecHelper.DecodeResult decode_int(byte[] msg) {
		CodecHelper.DecodeResult dr = new CodecHelper.DecodeResult();
		dr.result = bytes2int(msg);
		dr.returnDatalen = 4;
		return dr;
	}

	public static int encode_short(byte[] msg, short value) {
		int leadZeros = Integer
				.numberOfLeadingZeros(value < 0 ? ~value : value);

		int byteNum = (40 - leadZeros) / 8;
		for (int n = 0; n < byteNum; n++) {
			msg[1 - n] = (byte) (value >>> (n * 8));
		}
		return 2;
	}

	public static CodecHelper.DecodeResult decode_short(byte[] msg) {
		CodecHelper.DecodeResult dr = new CodecHelper.DecodeResult();
		dr.result = bytes2short(msg);
		dr.returnDatalen = 2;
		return dr;
	}

	public static int encode_float(byte[] msg, float value) {
		short dot = 0;
		int tmpValue;
		short token = 1;

		if (value != 0.0) {
			if (value < 0) {
				value = -value;
				token = -1;
			}
			if (value >= 1) {
				while (value >= 1) {
					value /= 10;
					dot++;
				}
			} else {
				while (value * 10 < 1) {
					value *= 10;
					dot++;
				}
				dot = (short) -dot;
			}
		}

		tmpValue = token * ((int) (value * 1000000000));
		byte msg1[] = new byte[4];
		encode_int(msg1, tmpValue);

		byte msg2[] = new byte[2];
		encode_short(msg2, dot);

		for (int i = 0; i < 4; i++) {
			msg[i] = msg1[i];
		}

		for (int i = 0; i < 2; i++) {
			msg[4 + i] = msg2[i];
		}

		return 6;
	}

	public static CodecHelper.DecodeResult decode_float(byte[] msg) {
		float value;

		byte msg1[] = new byte[4];
		for (int i = 0; i < 4; i++) {
			msg1[i] = msg[i];
		}
		CodecHelper.DecodeResult dr_iv = decode_int(msg1);

		byte msg2[] = new byte[2];
		for (int i = 0; i < 2; i++) {
			msg2[i] = msg[4 + i];
		}
		CodecHelper.DecodeResult dr_dv = decode_short(msg2);

		value = ((Integer) dr_iv.result) / (float) 1000000000.0;

		if (((Short) dr_dv.result) >= 0) {
			for (int i = 0; i < ((Short) dr_dv.result); i++) {
				value *= 10;
			}
		} else {
			for (int i = 0; i < -((Short) dr_dv.result); i++) {
				value /= 10;
			}
		}

		CodecHelper.DecodeResult dr_f = new CodecHelper.DecodeResult();
		dr_f.returnDatalen = 6;
		dr_f.result = (Float) value;
		return dr_f;
	}

	/**
	 * �ַ�������<br>
	 * 
	 * @param msg
	 *            װ���ַ����������ֽ�����<br>
	 * 
	 * @param value
	 *            ��Ҫ���б�����ַ�������<br>
	 * 
	 * @param maxLength
	 *            ���ñ�����ֽ�����ĳ���<br>
	 * 
	 * @return ����Ϣ��Ԫ������ֽ����ݺ�����鳤��<br>
	 */
	public static int encode_char(byte[] msg, String value, int maxLength) {
		int rc = encode_int(msg, maxLength);

		byte bv[] = value.getBytes();
		int realLen = value.length();
		// ��msgβ�����Ŵ���ַ�����Ϣ
		for (int i = 0; i < realLen; i++) {
			msg[rc + i] = bv[i];
		}
		// ��0
		for (int i = (realLen + rc); i < maxLength; i++) {
			msg[i] = 0;
		}

		rc += maxLength;
		return rc; // 4+maxlength
	}

	/**
	 * �ַ�������<br>
	 * 
	 * @param msg
	 *            ��Ҫ������ֽ�����<br>
	 * 
	 * @param length
	 * <br>
	 *            �������ַ�������<br>
	 * 
	 * @return CodecHelper.DecodeResult�����������ֵװ��ת������Ϣ<br>
	 */
	public static CodecHelper.DecodeResult decode_char(byte[] msg, int length) {
		CodecHelper.DecodeResult di = decode_int(msg);
		int len = (Integer) di.result; // Ϊ����ʱ���maxlength����
		int rc = di.returnDatalen;

		byte out[] = new byte[length];

		boolean nulFlag = false;
		if (length > len) {
			for (int i = 0; i < len; i++) {
				if (msg[4 + i] == 0) {
					nulFlag = true;
				}

				if (nulFlag) {
					out[i] = 0;
				} else {
					out[i] = msg[4 + i];
				}
			}

			for (int i = len; i < length; i++) {
				out[i] = 0;
			}
		} else {
			for (int i = 0; i < length; i++) {
				if (msg[4 + i] == 0) {
					nulFlag = true;
				}

				if (nulFlag) {
					out[i] = 0;
				} else {
					out[i] = msg[4 + i];
				}
			}
		}

		CodecHelper.DecodeResult dc = new CodecHelper.DecodeResult();
		dc.returnDatalen = rc + len;
		dc.result = (String) (new String(out));
		return dc;
	}

	public static int bytes2int(byte[] b) {
		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 4; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	public static short bytes2short(byte[] b) {
		short mask = 0xff;
		short temp = 0;
		short res = 0;
		for (int i = 0; i < 2; i++) {
			res <<= 8;
			temp = (short) (b[i] & mask);
			res |= temp;
		}
		return res;
	}

	/**
	 * �ֽ�����ת����16���Ƶ��ַ���<br>
	 *
	 * @param data
	 *            Ҫת�����ֽ�����<br>
	 * 
	 * @return ת������ַ���<br>
	 */
	public static String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return buf.toString();
	}

	/**
	 * �ֽ�����ת��Ϊ16���Ƶ��ַ���<br>
	 *
	 * @param data
	 *            Ҫת�����ֽ�����<br>
	 * 
	 * @return ת������ַ���<br>
	 */
	public static String bytesToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			buf.append(byteToHex(data[i]) + ",");
		}
		return buf.toString();
	}

	/**
	 * ��������ת����16�����ַ�<br>
	 *
	 * @param i
	 *            Ҫת������������<br>
	 * 
	 * @return ת������ַ�����<br>
	 */
	public static char toHexChar(int i) {
		if ((0 <= i) && (i <= 9))
			return (char) ('0' + i);
		else
			return (char) ('a' + (i - 10));
	}
}
