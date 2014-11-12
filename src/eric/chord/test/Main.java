package eric.chord.test;

import eric.chord.communication.*;
import eric.chord.core.*;

import org.json.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Main {

	public static int n = 4;
	public static ChordNode[] nodes = new ChordNode[n];
	public static Scanner input = new Scanner(System.in);
	
	public static void main(String[] args) {
		
		//Communicatable4Local2.localtest();
		LANtest();
		

	}
	
	public static void LANtest(){
		ChordNode node = new ChordNode( new TaskExecutor(),new Communicatable4LAN());		
		node.communication_start();
		System.out.println("*************************Welcome to our world!**********************");
		
		System.out.println("Input (1 or 2):Create new Chord ring:1, Join an existing Chord ring:2");
		System.out.print("Please input:");
		
		int choice = input.nextInt();
		
		if(choice == 1){
			node.create();
			System.out.println("node_id: "+node.getID());
		}else{
			System.out.println("Type in node_id of one node in the Chord ring: ");
			String node_id = input.next();
			try {
				node.join(node_id);
				System.out.println("Successfully join the Chord ring.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Failed to join the Chord ring.");
				e.printStackTrace();
			}
		}
		

		try {

			Thread.sleep(1000);
			
			System.out.println(node);
			int signal;
			promptMsg();
			while(true){
				signal = input.nextInt();
				
				if(signal == 1){
					System.out.println(node);
				}
				else if(signal == 2){
					node.getTasks("Task_Talk");
					promptMsg();
				}
				else if(signal == 3){
					//node.sendTask("taskalone");
					
					System.out.println(node);
					prompSpeakTo();
					//String node_id = input.nextLine();
					int fid = input.nextInt();
					
					String node_id = node.getIdByfinger(fid);
					String s = input.nextLine();
					
					while((s=input.nextLine())!="-cancel")
					{
						//if(s=="-cancel\n") break;
						//System.out.println(s);
						String data =node.sendMsgTo(node_id, s);
						System.out.println("***"+data+"***");
						s="";
						
					
					}
					promptMsg();
				}
				else if(signal == -1){
					break;
				}
				
				
			}
			
			
		} catch (InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			
			System.err.println(e);
			e.printStackTrace();
		}
//		
	}
	private static void prompSpeakTo() {
		System.out.println("输入对方的key来发送消息~");
	}

	private static void promptMsg()
	{
		System.out.println("Please input(1,2),input -1 to exist.");
		System.out.println("1. 输出你的ip和在线用户.");
		System.out.println("2. 分配任务给其他节点.");
		System.out.println("3. 发消息给id.");
		
	}
	public static void localtest(){
		NodesPrinter np = new NodesPrinter();
		np.start();
		
		nodes[0] = new ChordNode( new TaskExecutor(),new Communicatable4Local("node#0#"));	
		nodes[0].create();
		nodes[0].communication_start();
		for(int i=1;i<n-1;i++){
			
			try {
				Thread.sleep(1000);
				nodes[i] = new ChordNode( new TaskExecutor(),new Communicatable4Local("node#" + Integer.toString(i) + "#"));
				nodes[i].communication_start();
				nodes[i].join(nodes[0].getID());
				
			} catch (InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		
		
		try {
			int signal;
			while(true){
				signal = input.nextInt();
				if(signal == 1){
					ChordNode.log("get tasks");
					nodes[0].getTasks("task1;task2;task3;task4;task5;task6;task7;task8;task9;task10;task11;task12;task13;task14");
				}
				else if(signal == 2 ){
					ChordNode.log("node node#3# joins");
					nodes[n-1] = new ChordNode(new TaskExecutor(),new Communicatable4Local("node#3#"));
					nodes[n-1].communication_start();
					nodes[n-1].join(nodes[0].getID());
				}
				else if(signal == 3){
					ChordNode.log("node node#3# fails");
					nodes[3].withdraw();
				}
				else if(signal == 4){
					for(int i=0;i<n;i++){
						System.out.println("ERIC: " + nodes[i]);
					}
				}
				else if(signal == -1){
					break;
				}
			}
			
			for(int i=0;i<n;i++){
				System.out.println("ERIC: " + nodes[i]);
			}
			
		} catch (InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	public static class NodesPrinter extends Thread{
		
		public void run(){
			
			try {
				FileWriter fout = new FileWriter("localtest.txt");
				while(true){
					Thread.sleep(1000);
					for(int i=0;i<n;i++){
						fout.write("ERIC: " + nodes[i]);
					}
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
	
	public static void functiontest(){
		
		Communicatable4Local c = new Communicatable4Local("1");
		
		Communicatable4LAN c4l = new Communicatable4LAN();
		try {
			byte[] b = c4l.ipaddr2bytes("127.0.0.1");
			for(int i=0;i<b.length;i++)
				System.out.println(b[i]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ChordKey key=new ChordKey("dafasasdfavbs>>,dfgasddasdfc");
		System.out.println(key.getIdentifier());
		System.out.println(key.getKey());
		JSONObject jsonObj = new JSONObject();				
		try {
			jsonObj.append("key1", "adsdfas");
			jsonObj.put("key3", "asdfas");
			jsonObj.append("key2", "asdf");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(jsonObj);
		try {
			JSONObject job = new JSONObject("{\"key3\":\"asdfas\",\"key2\":[\"asdf\"],\"key1\":[\"adsdfas\"]}");
			System.out.println(job.get("key1").getClass());
			System.out.println(job.get("key2"));
			System.out.println(job.get("key3"));
			System.out.println(job.getClass());
			if(job.getClass().equals(job.getClass())){
				System.out.println("YEAH");
			}
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			JSONArray array = new JSONArray("[{\"key3\":\"asdfas\",\"key2\":[\"asdf\"],\"key1\":[\"adsdfas\"]}]");
			for(int i=0;i<array.length();i++){
				System.out.println(array.get(i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(ChordNode.MD5("abdfasdfascd"));
				
		ArrayList<String> arrstr = new ArrayList<String>();
		arrstr.add("abda");
		arrstr.add("dafd");
		arrstr.add("bdfas");
		Collections.sort(arrstr);
		for(int i=0;i<arrstr.size();i++){
			System.out.println(arrstr.get(i));
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
	}

}
