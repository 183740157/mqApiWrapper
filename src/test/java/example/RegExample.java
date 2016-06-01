package example;

import org.junit.Test;

import com.boco.mqapiwrapper.MessageClient;

/**
 * ����Ϣƽ̨����ָ������Ϣ���Լ�����Ϣƽ̨ע���Ѷ��ĵ���Ϣ<br>
 * 
 * @version 2.0
 * @author b
 */
public class RegExample {

	@Test
	public void regAddAndRegDeleteExample(){
		MessageClient mc = new MessageClient("10.21.3.171", 1414, "WNMS4_QM",
				"CH1", "CTRL.Q", "RECV");
		mc.connect();
		mc.regAdd("1,4,5,6", "MyTest.app", 20);
		mc.regDelete("WNMS4_QM", "RECV");
		mc.disConnect();
	}
}
