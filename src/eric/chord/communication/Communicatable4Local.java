package eric.chord.communication;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.*;

import eric.chord.core.*;

public class Communicatable4Local extends Thread implements ICommunicatable{
	
	public static ArrayList<Msg4LocalTest> heard = new ArrayList<Msg4LocalTest>();
	public static boolean locked = false;
	private ArrayList<Msg4LocalTest> myHeard = new ArrayList<Msg4LocalTest>();
	//private ArrayList<Msg4Test> myResponse = new ArrayList<Msg4Test>();
	
	public String id;
	
		
	public Communicatable4Local(String id) {
		super();
		this.id = id;
	}
	
	public String getID(){
		return this.id;
	}

	public boolean send(String node_id,JSONObject msg){
		
		Msg4LocalTest m4t = new Msg4LocalTest(node_id,msg);
		heard.add(m4t);
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
				int x;
//				while(locked){
//					//x = (int)(Math.random()*99)+100;
//					Thread.sleep(13);
//				}
//				locked = true;
				Thread.sleep(260);
				//listening
				//hear request, send response immediately
				//hear response, save it to the list
				//hear functional method, do as what is bid
				if(heard.size()>0){
					//System.out.println(heard.size());
					for(int i=heard.size()-1;i>=0;i--){
						if(heard.get(i).getStatus().equals("delivered")){
							heard.remove(i);
							break;
						}
						if(heard.get(i).getTarget_node_id().equals(this.id) && heard.get(i).getStatus().equals("created") ){
							
//							this.myHeard.add(new Msg4Test(heard.get(i)));
							Msg4LocalTest msg = heard.get(i);
							msg.setStatus("delivered");
							
							this.myHeard.add(new Msg4LocalTest(msg));
							
							heard.remove(i);
							break;
						}
						//if((heard.get(i).getData()).getString("req_id")==this.id)
					}
				}

				
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
