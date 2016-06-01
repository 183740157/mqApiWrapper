package test.com.boco.mqapiwrapper;

import junit.framework.Assert;
import org.junit.Test;
import com.boco.mqapiwrapper.CodecHelper;

public class CodecHelperTest {
	
	@Test
	public void encodeDecodeCharTest() {
		System.out.println("Char:");
		String str = "123456";
		System.out.println("str:" + str);
		byte[] msg = new byte[4096]; // Constants.MAX_QUEUE_BUFFER
		
		CodecHelper.encode_char(msg, str, 4096); // Constants.MAX_QUEUE_BUFFER
		System.out.print("encode:");
		for (int i = 0; i < msg.length; i++) {
			if(msg[i]!=0)
				System.out.print(msg[i]);
		}
		
		CodecHelper.DecodeResult dr = CodecHelper.decode_char(msg, str.length());
		System.out.println("\ndecode:" + (String) dr.result);
		Assert.assertEquals(str, (String) dr.result);
	}
	
	@Test
	public void encodeDecodeShortTest(){
		System.out.println("short:");
		short st = -12345;
		System.out.println("st:" + st);
		byte msg[] = new byte[2];
		
		CodecHelper.encode_short(msg, st);
		System.out.print("encode:");
		for (int i = 0; i < msg.length; i++) {
			if(msg[i]!=0)
				System.out.print(msg[i]);
		}
		
		CodecHelper.DecodeResult dr = CodecHelper.decode_short(msg);
		System.out.println("\ndecode:" + dr.result);
		Assert.assertEquals(st, dr.result);
	}
	
	@Test
	public void encodeDecodeFloatTest(){
		System.out.println("float:");
		float ft = 123.789f;
		byte msg[] = new byte[6];
		CodecHelper.encode_float(msg, ft);
		System.out.println("ft:" + ft);
		System.out.print("encode:");
		for (int i = 0; i < msg.length; i++) {
			if(msg[i]!=0)
				System.out.print(msg[i]);
		}
		
		CodecHelper.DecodeResult dr = CodecHelper.decode_float(msg);
		System.out.println("\ndecode:" + dr.result );
		Assert.assertEquals(ft, dr.result);
	}
	
}
