package example;

import org.junit.Test;

import com.boco.mqapiwrapper.MessageClient;

/**
 * 获取心跳号<br>
 * 
 * @version 2.0
 * @author b
 */
public class GetTestNumberExample {
	
	@Test
	public void testGetTestNumber(){
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "CTRL.Q", "RECV");
		mc.connect();
		mc.regAdd("1,4,5,6", "MyTest.app", 20);
		// 心跳发送间隔30秒
		for(int i=0;i<60;i++){
			mc.recvTextMessage();
			System.out.println(i);
			if(mc.getMessageType() == 5){
				System.out.println("    心跳号为："+mc.getTest_number());
				break;
			}
		}
		mc.regDelete("WNMS4_QM", "RECV");
		mc.disConnect();
	}
}
