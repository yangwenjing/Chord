package eric.chord.communication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.json.*;


import eric.chord.core.*;
import eric.chord.test.TaskExecutor;

public class Communicatable4Local2 implements ICommunicatable{
	public static int n = 8;
	public static ChordNode[] nodes = new ChordNode[n];
	public static Scanner input = new Scanner(System.in);
	
	public static String readd(String fileName, String encoding) { 
    	File file = new File(fileName);
    	if(file.exists()){
	        StringBuffer fileContent = new StringBuffer(); 
	        try { 
	            FileInputStream fis = new FileInputStream(fileName); 
	            InputStreamReader isr = new InputStreamReader(fis, encoding); 
	            BufferedReader br = new BufferedReader(isr); 
	            String line = null; 
	
	            while ((line = br.readLine()) != null) { 
	                fileContent.append(line); 
//	                fileContent.append(System.getProperty("line.separator")); 
	            } 
	            br.close(); 
	            isr.close(); 
	            fis.close(); 
	        } catch (Exception e) { 
	            e.printStackTrace(); 
	        } 
//	        System.out.println(fileContent.toString());
	        return fileContent.toString(); 
    	}
    	else return null;
    } 
	
	public static void localtest(){
//		for(int i=0;i<n;i++){
//			nodes[i] = new ChordNode( new TaskExecutor(),new Communicatable4Local2("node#" + Integer.toString(i) + "#"));
//		}
		nodes[0] = new ChordNode( new TaskExecutor(),new Communicatable4Local2("ET'node#0#"));
		nodes[1] = new ChordNode( new TaskExecutor(),new Communicatable4Local2("ET'node#1#"));
		nodes[2] = new ChordNode( new TaskExecutor(),new Communicatable4Local2("ET'node#2#"));
		nodes[3] = new ChordNode( new TaskExecutor(),new Communicatable4Local2("ET'node#3#"));
		nodes[4] = new ChordNode( new TaskExecutor(),new Communicatable4Local2("ET'node#4#"));
		nodes[5] = new ChordNode( new TaskExecutor(),new Communicatable4Local2("ET'node#5#"));
		nodes[6] = new ChordNode( new TaskExecutor(),new Communicatable4Local2("ET'node#6#"));
		nodes[7] = new ChordNode( new TaskExecutor(),new Communicatable4Local2("ET'node#7#"));
		
		nodes[0].create();
		nodes[0].communication_start();
		for(int i=1;i<n-1;i++){
			
			try {
				Thread.sleep(1000);
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
					
					for(int i=0;i<n;i++){
						nodes[i].trigerCircle();
						Thread.sleep(1000);
					}
					
					StringBuilder sb = new StringBuilder();
					for(int i=0;i<60;i++){
						sb.append("taskXXX" +"XXX-"+Integer.toString(i));
						if(i!=59)
							sb.append(";");
					}
					
//					for(int i=0;i<n;i++)
//						for(int j=0;j<n;j++)
//							if(i!=j){
//								
//								nodes[i].check(nodes[j].getID());
//								Thread.sleep(700);
//							}
//					String uidList = readd("data/uidList.txt", "UTF-8");
//					if(uidList != null) nodes[0].getTasks(uidList);
					nodes[3].getTasks(sb.toString());
//					nodes[0].getTasks(sb.toString());
//					nodes[1].getTasks(sb.toString());
//					nodes[2].getTasks(sb.toString());
//					nodes[3].getTasks(sb.toString());
//					nodes[4].getTasks(sb.toString());
//					nodes[5].getTasks(sb.toString());
				}
				else if(signal == 2 ){
					ChordNode.log("node node#7# joins");
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
				else if(signal == 5){
					for(int i=0;i<n;i++){
						System.out.println(nodes[i].getID()+" : "+nodes[i].tasknum);
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
	
	public static ChordNode getNodeById(String node_id){
		for(int i=0;i<n;i++)
			if(nodes[i].getID().equals(node_id))
				return nodes[i];
		return null;
	}
	
	public void newMsg(Msg4LocalTest m4t){
		this.myHeard.add(m4t);
	}
	
	private ArrayList<Msg4LocalTest> myHeard = new ArrayList<Msg4LocalTest>();
	
	public String id;
	
		
	public Communicatable4Local2(String id) {
		super();
		this.id = id;
	}
	
	public String getID(){
		return this.id;
	}

	public boolean send(String node_id,JSONObject msg){
		
		Msg4LocalTest m4t = new Msg4LocalTest(node_id,msg);
		m4t.setStatus("delivered");
		((Communicatable4Local2) getNodeById(node_id).getCommunication()).newMsg(m4t);
		return true;
	}
	
	public String heardResponse(String req_id){
		String result = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i).getData();
			
				if(data.getString("method").equals(ChordNode.method.respond) && data.getString("target").equals(req_id) && myHeard.get(i).getStatus().equals("delivered") ){
					JSONObject res = data;
					if(res.has("data"))
						result = res.getString("data");
					else
						result = "";

//					myResponse.get(i).setStatus("read");
					myHeard.remove(i);
					break;
				}
			}
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
			
		
		return result;
	}
	
	public JSONObject heardRequest(){
		JSONObject req = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i).getData();
			
				if(data.getString("method").equals(ChordNode.method.request) && myHeard.get(i).getStatus().equals("delivered")){
					req = data;
//					myHeard.get(i).setStatus("read");
					myHeard.remove(i);
					break;
				}
			}
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
		return req;
	}
	
	public String heardNotify(){
		String node_id = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i).getData();
			
				if(data.getString("method").equals(ChordNode.method.notify) && myHeard.get(i).getStatus().equals("delivered")){
					node_id = data.getString("from");
//					myHeard.get(i).setStatus("read");
					myHeard.remove(i);
					break;
				}
			}
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return node_id;
	}
	
	public String heardJoin(){
		String node_id = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i).getData();
			
				if(data.getString("method").equals(ChordNode.method.join) && myHeard.get(i).getStatus().equals("delivered") ){
					node_id = data.getString("from");
//					myHeard.get(i).setStatus("read");
					myHeard.remove(i);
					break;
				}
			}
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return node_id;
	}
	
	public String heardCheck(){
		String node_id = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i).getData();
			
				if(data.getString("method").equals(ChordNode.method.check) && myHeard.get(i).getStatus().equals("delivered") ){
					JSONObject res = data;
					
					node_id = res.getString("from");

//					myResponse.get(i).setStatus("read");
					myHeard.remove(i);
					break;
				}
			}
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return node_id;
	}
	
	public String heardConfirm(String from_id){
		String node_id = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i).getData();
			
				if(data.getString("method").equals(ChordNode.method.confirm) && myHeard.get(i).getStatus().equals("delivered") ){
					JSONObject res = data;
					
					node_id = res.getString("from");
					if(node_id.equals(from_id)){
//						myResponse.get(i).setStatus("read");
						myHeard.remove(i);
						break;
					}else{
						node_id = null;
					}

				
				}
			}
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return node_id;
	}
	
	@Override
	public JSONObject heardCircle() {
		JSONObject msg = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i).getData();
			
				if(data.getString("method").equals(ChordNode.method.circle) && myHeard.get(i).getStatus().equals("delivered")){
					msg = data;
//					myHeard.get(i).setStatus("read");
					myHeard.remove(i);
					break;
				}
			}
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return msg;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			try {
				Thread.sleep(16000);
				System.out.println(getID() +" myHeard "+Integer.toString(myHeard.size()));
				
			} catch (InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println(e);
				e.printStackTrace();
			}
			
		}

	}
	
	

}
