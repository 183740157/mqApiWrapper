package com.boco.mqapiwrapper;

/**
 * 消息编解码功能封装类<br>
 * 
 * <p>
 * 提供了整型，短整型，浮点型，字符串，这四种数据类型的编码解码方法<br>
 * 该工具类提供的编码方式是将不同类型的数据转化成字节数据，以及字节数据转换为对应的数据类型<br>
 * 注：该类里的方法均不建议使用，主要供内部调用<br>
 * </p>
 * 
 * <dl>
 * <dt>假设编码后的字节数组为byte[] msg<br></dt>
 * 
 * <dt>若msg代表一条字节消息，那么该消息的结构如下：</dt>
 * <dd>msg[0]，...，msg[3]这部分存储消息类型，msg[4]及msg[4]以后的字节<br>
 * 用来存放组成一条消息的各个消息单元信息。若为该消息单元为int型数据，<br>
 * 则直接将其编码，并存放在msg[4]，...，msg[7]中。若其后是String型数据，<br>
 * 则将其编码（编码时会传入一个maxLength参数，代表其转码后的字节数组长度），<br>
 * maxLength会用int型编码方式，存到msg[8]，...，msg[11]中，接着对String消息主体进行编码，<br>
 * 假设String数据编码后的数组长度为len，则将这部分存入msg[12]，...，msg[11+len]中，<br>
 * msg[12+len]，...，msg[12+maxLength-len]中全部赋值为0。若再存入数据的时候，从<br>
 * msg[13+maxLength-len]开始存字节数据<br></dd>
 * 
 * <dt>short型数据{@linkplain #encode_short(byte[], short) 编码}
 * {@linkplain #decode_short(byte[]) 解码}：</dt>
 * <dd>short型数据长度为16bit，将其拆分为高八位和低八位分别存入msg[0]和msg[1]中，即完成编码。<br>
 * 构造一个short型的数据0，将msg[0]和msg[1]分别存入到short数据高八位和低八位，即完成解码<br></dd>
 * 
 * <dt>int型数据{@linkplain #encode_int(byte[], int) 编码}
 * {@linkplain #decode_int(byte[]) 解码}：</dt>
 * <dd>int型数据长度为32bit，将32位拆分为四部分存入msg[0]，msg[1]，msg[2]，msg[3]数据中，即完成编码。<br>
 * 解码方式与short型基本相同<br></dd>
 * 
 * <dt>float型数据{@linkplain #encode_float(byte[], float) 编码}
 * {@linkplain #decode_float(byte[]) 解码}：</dt>
 * <dd>首先float型数据依次除10，直到该数据小于1，用一个short型数据记录除以10的次数<br>
 * 然后将处理后的float数据乘以10^10转化为一个int型数据<br>
 * 最后将该int型数据与之前的short编码成字节数组依次存放到msg[0]，msg[1]，...，msg[5]中<br>
 * 解码过程就是将编码的过程反过来求出<br></dd>
 * 
 * <dt>String型数据{@linkplain #encode_char(byte[], String, int) 编码}
 * {@linkplain #decode_char(byte[], int) 解码}：</dt>
 * <dd>字符串编码时除了本身的数据还有以参数传入的int型数据maxLength（代表字符串编码后形<br>
 * 成的字节数组长度）首先将maxLength编码成字节数组msg[0]，msg[1]，msg[2]，msg[3]。String<br>
 * 类型数据可以用getBytes()进行编码，并将编码后的数据从字节数组的msg[4]开始存储，直到全部<br>
 * 存储完毕，剩余未存数据的数组单元全部赋值为0。即完成编码将字节数组用int型将前四位解码，<br>
 * 解码的值为该字符数组的长度，再msg[4]，msg[5]...非零的数据使用new String(byte[])解码<br></dd>
 * 
 * <dt>系统消息编码过程如下：</dt>
 * <dd>首先将消息类型msgType用{@linkplain #encode_int(byte[], int) encode_int()}
 * 进行编码，并存到msg[0]...msg[3]<br>
 * 然后根据构建系统消息时创建的很多消息单元，分别采用不同的编码方式（如上）进行编码<br>
 * 每次将一个消息单元编码成字节数组，并存放到之前传入数据的后面，一个接一个存放<br>
 * 当所有消息单元均编码成字节数组并传入到msg数组中，则该条信息编码完成<br></dd>
 * 
 * <dt>系统消息解码过程如下：</dt>
 * <dd>首先将msg字节数组的前四位用{@linkplain #decode_int(byte[]) decode_int()}
 * 解码，所得的值为该消息的消息类型msgType <br>
 * 然后根据该消息类型判断接收的各个消息单元依次为哪种类型，并使用对应的解码方法进行解码<br></dd>
 * </dl>
 * 
 * @version 2.0
 * @author b
 */
public class CodecHelper {

	/**
	 * 自定义装载解码后的信息类<br>
	 * 
	 * <p>注：该类仅作为内部调用或写测试用例时使用</p>
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
	 * 字符串编码<br>
	 * 
	 * @param msg
	 *            装载字符串编码后的字节数据<br>
	 * 
	 * @param value
	 *            需要进行编码的字符串对象<br>
	 * 
	 * @param maxLength
	 *            设置编码后字节数组的长度<br>
	 * 
	 * @return 该消息单元编码成字节数据后的数组长度<br>
	 */
	public static int encode_char(byte[] msg, String value, int maxLength) {
		int rc = encode_int(msg, maxLength);

		byte bv[] = value.getBytes();
		int realLen = value.length();
		// 在msg尾部接着存放字符串信息
		for (int i = 0; i < realLen; i++) {
			msg[rc + i] = bv[i];
		}
		// 补0
		for (int i = (realLen + rc); i < maxLength; i++) {
			msg[i] = 0;
		}

		rc += maxLength;
		return rc; // 4+maxlength
	}

	/**
	 * 字符串解码<br>
	 * 
	 * @param msg
	 *            需要解码的字节数组<br>
	 * 
	 * @param length
	 * <br>
	 *            解码后的字符串长度<br>
	 * 
	 * @return CodecHelper.DecodeResult类对象，其属性值装载转码后的信息<br>
	 */
	public static CodecHelper.DecodeResult decode_char(byte[] msg, int length) {
		CodecHelper.DecodeResult di = decode_int(msg);
		int len = (Integer) di.result; // 为编码时候的maxlength变量
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
	 * 字节数据转换成16进制的字符串<br>
	 *
	 * @param data
	 *            要转换的字节数据<br>
	 * 
	 * @return 转换后的字符串<br>
	 */
	public static String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return buf.toString();
	}

	/**
	 * 字节数组转换为16进制的字符串<br>
	 *
	 * @param data
	 *            要转换的字节数组<br>
	 * 
	 * @return 转换后的字符串<br>
	 */
	public static String bytesToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			buf.append(byteToHex(data[i]) + ",");
		}
		return buf.toString();
	}

	/**
	 * 整型数据转化成16进制字符<br>
	 *
	 * @param i
	 *            要转换的整型数据<br>
	 * 
	 * @return 转换后的字符数据<br>
	 */
	public static char toHexChar(int i) {
		if ((0 <= i) && (i <= 9))
			return (char) ('0' + i);
		else
			return (char) ('a' + (i - 10));
	}
}
