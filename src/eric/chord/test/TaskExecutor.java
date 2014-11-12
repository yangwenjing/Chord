package eric.chord.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import eric.chord.core.ITaskExecutable;

public class TaskExecutor implements ITaskExecutable {

	@Override
	public String execTask(String task) {
//		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//		String msg = "executing task \"" + task +"\"";
//		System.out.println("start: " + df.format(new Date()) + " " + msg);
		
		/*try {
			Thread.sleep(100);
			System.out.println("ET's Test!");
		} catch (InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}*/
//		System.out.println("end: " + df.format(new Date()) + " " + msg);
		return "it is done";
	}

}
