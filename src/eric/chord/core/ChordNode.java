package eric.chord.core;

import eric.chord.communication.*;

import org.json.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ChordNode {
	
	public static int m = 16;
	
	public static final String msg_recieved = "MSG_RECIEVED";
	public static class req_target{
		public static final String successor = "successor";
		public static final String predecessor = "predecessor";
		public static final String findsuccessor = "findsuccessor";
		//public static final String findpredecessor = "findpredecessor";
		public static final String closestprecedingnode = "closestprecedingnode";
		public static final String task = "task";
		public static final String SpeakTo = "speakTo";
	}
	
	public static class method{
		public static final String notify = "notify";
		public static final String request = "request";
		public static final String respond = "respond";
		public static final String join = "join";
		public static final String check = "check";
		public static final String confirm = "confirm";
		public static final String circle = "circle";
	}
	
	private ITaskExecutable taskExecutor;
	private ICommunicatable communication;
	private boolean commStarted;
	
	private ChordKey nodeKey;
	private ChordKey predecessorKey;
	private ChordKey successorKey;
	
	private Finger[] fingers;
	
	public String getIdByfinger(int fid)
	{
		if(fid<fingers.length)
		{
			Finger f = fingers[fid];
		  return f.getNodeKey().getIdentifier();
		}
		else
			return this.nodeKey.getIdentifier();
	}
	
	public String getPreID(){
		if(predecessorKey==null)
			return null;
		else
			return predecessorKey.getIdentifier();
	}
	
	public String getSucID(){
		if(successorKey==null)
			return null;
		else
			return successorKey.getIdentifier();
	}
	
	public void getTasks(String list) {
		String[] tasklist = list.split(";");
		for(int i=0;i<tasklist.length;i++){
			String task=tasklist[i];
			try {
				Thread.sleep(1500);
				ChordKey taskkey = new ChordKey(task);
				ChordKey node = this.find_successor(taskkey);
				String node_id = node.getIdentifier();
				System.out.println("task " + taskkey + " to node " + node);
				request_not_waiting( node_id, req_target.task + "_" +  task );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			
			
		}
	}
	
	public String sendTask(String task) throws Exception{
		String node_id = this.find_successor(new ChordKey(task)).getIdentifier(); 
		String result = request( node_id, req_target.task + "_" +  task );
		return result;
	}
	
	public String sendMsgTo(String node_id,String message) throws Exception{
		//String node_id = this.find_successor(new ChordKey(task)).getIdentifier(); 
		String result = request( node_id, req_target.SpeakTo + "_" +  message );
		return result;
	}
	
	public int tasknum = 0;
	
	public String execTask(String task) throws Exception{
		ChordNode.log(communication.getID() + " executing task " + task);
		tasknum++;
		System.out.println(tasknum);
		//System.out.println(communication.getID() + " executing task " + task);
		return taskExecutor.execTask(task);
	}
	
	ArrayList<String> targets = new ArrayList<String>();
	
	public void newTask(String target){
		targets.add(target);
	}
	
	public class NoWaitingRequestHandler extends Thread{
		
		public void run(){
			while(true){
				
				try {
					Thread.sleep(100);
					if(targets.size()>0){
						String target = targets.remove(0);
						String data = ChordNode.this.getTarget(target);
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	public class MyRequestListener extends Thread{
		public void run(){
			
			while(true){
				try {
					Thread.sleep(10);
					
					String node_id = null;
					String target = null;
					JSONObject req = communication.heardRequest();
					if(req!=null){
//						System.out.println(ChordNode.this.communication.getID() + " heard request " + req);
						node_id = req.getString("from");
						ChordNode.this.fix_fingers_for_existing(new ChordKey(node_id));
						String req_id = req.getString("req_id");
						String data = null;
						target = req.getString("target");
						
						if(req.getBoolean("waiting")){
							data = ChordNode.this.getTarget(target);
							if(data==msg_recieved)
								System.out.println("Node"+node_id+":"+target);
							ChordNode.this.respond(node_id, req_id, data);
						}
						else{
							ChordNode.this.newTask(target);
							
						}
					}
					
					//ChordNode.this.fix_fingers();
					
				} catch (InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				} catch (JSONException e) {
					System.err.println(e);
					e.printStackTrace();
				} catch (Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}
				
			}
		}
	}
	
	public class MyListener extends Thread{
		
		public void run(){
			String target;
			while(true){
				try {
					Thread.sleep(10);
					
					String node_id = communication.heardNotify();
					if(node_id!=null){
//						System.out.println( ChordNode.this.communication.getID() + " heard notification from " + node_id);
						ChordKey pre = new ChordKey(node_id);
						ChordNode.this.be_notified(pre);
						ChordNode.this.fix_fingers_for_existing(pre);
					}
					node_id = communication.heardJoin();
					if(node_id!=null){
//						System.out.println(ChordNode.this.communication.getID() + " heard " + node_id + " wants to join");
						ChordKey key = new ChordKey(node_id);
						ChordNode.this.fix_fingers_for_existing(key);
					}
					node_id = communication.heardCheck();
					if(node_id!=null){
//						System.out.println(ChordNode.this.communication.getID() + " heard " + node_id + " check me ");
						ChordKey key = new ChordKey(node_id);
						ChordNode.this.fix_fingers_for_existing(key);
						ChordNode.this.confirm(node_id);
					}
					
					JSONObject circle = communication.heardCircle();
					if(circle!=null){
						if(circle.getInt("jump")<m && !circle.getString("from").equals(this.getId())){
							node_id = circle.getString("from");
							ChordNode.this.fix_fingers_for_existing(new ChordKey(node_id));
							int jump = circle.getInt("jump")+1;
							ChordNode.this.forward_circle(node_id, jump);
						}
					}
					
					//ChordNode.this.fix_fingers();
					
				} catch (InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				} catch (JSONException e) {
					System.err.println(e);
					e.printStackTrace();
				} catch (Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}
				
			}
		}
	}
	
	public class MyStabilizator extends Thread{
		public void run(){
			while(true){
				try {
					Thread.sleep(300);
					ChordNode.this.stabilize();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
	
	public class MyMaintainer extends Thread{
		
		public void run(){
			while(true){
				try {
					Thread.sleep(500);			
					ChordNode.this.checkPredecessor();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
	
	public class MyFingerFixer extends Thread{
		//public int finger_pointer = 0;
//		public void run(){
//			while(true){
//				try {
//					Thread.sleep(1000);
//					ChordNode.this.init_circle();
////					fingers[finger_pointer].setNodeKey(ChordNode.this.find_successor(new ChordKey(fingers[finger_pointer].getStart())));
////					finger_pointer = (finger_pointer+1)%ChordNode.m;
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//		}
		public void run(){
			ChordNode.this.init_circle();
		}
	}
	
	MyRequestListener req_listener;
	MyListener listener;
	MyStabilizator stabilizator;
	MyMaintainer maintainer;
	//MyFingerFixer fixer;
	NoWaitingRequestHandler nwhandler;
	Thread mycomm;
	
	public boolean communication_start(){
		if(!commStarted){
			mycomm = new Thread(getCommunication());
			mycomm.start();
			commStarted=true;
			return true;
		}else{
			return false;
		}
		
		
	}
	
	public String getTarget(String target) throws Exception{
		String data = null;
		if(target.equals(req_target.successor)){
			data = this.getSucID();
		}else if(target.equals(req_target.predecessor)){
			data = this.getPreID();
		}else{
			String[] ts = target.split("_");
			if(ts[0].equals(req_target.closestprecedingnode) ){
				ChordKey key = new ChordKey(ts[1]);
				data = this.closest_proceding_node(key).getIdentifier();
			}else if(ts[0].equals(req_target.task)){
				data = this.execTask(ts[1]);
			}else if(ts[0].equals(req_target.findsuccessor)){
				ChordKey key = new ChordKey(ts[1]);
				data = this.find_successor(key).getIdentifier();
			}else if(ts[0].equals(req_target.SpeakTo))
			{
				if(ts.length==2)
				{
					SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date d = new Date();
					System.out.println("***Message from: "+this.communication.getID()+"***");
					System.out.println(sdf.format(d)+":"+ts[1]);
				}
				data = "已阅";
				//System.out.println(ts[1]);
			}
			else{//not defined
				throw new Exception("undefined request");
			}
		}
		return data;
	}
	
	

	public ChordNode( ITaskExecutable ite, ICommunicatable comm){
		this.setTaskExecutor(ite);
		this.setCommunication(comm);
		
		this.nodeKey = new ChordKey(this.communication.getID());
		this.fingers = new Finger[m];
		for(int i=0;i<m;i++){
			fingers[i] = new Finger();
			fingers[i].setStart((this.nodeKey.getKey()+(1<<i))%(1<<m));
			fingers[i].setNodeKey(this.nodeKey);
		}
		
		req_listener = new MyRequestListener();
		listener = new MyListener();
		stabilizator = new MyStabilizator();
		maintainer = new MyMaintainer();
		//fixer = new MyFingerFixer();
		nwhandler = new NoWaitingRequestHandler();
		
		req_listener.start();
		listener.start();
		stabilizator.start();
		maintainer.start();
		//fixer.start();
		nwhandler.start();
	}
	
	public void trigerCircle(){
		MyFingerFixer fixer = new MyFingerFixer();
		fixer.start();
	}
	
	@SuppressWarnings("deprecation")
	public void withdraw(){
		listener.stop();
		stabilizator.stop();
		maintainer.stop();
		mycomm.stop();
		
	}
	
	public ChordKey find_successor(ChordKey key) throws Exception{
		//System.out.println("find_successor"+key.getIdentifier());
		if(key.isBetween(this.nodeKey, this.successorKey) || this.nodeKey.equals(this.successorKey)){
			return this.successorKey;
		}
		ChordKey closestpreceding = this.closest_proceding_node(key); 
//		ChordKey predecessor = this.find_predecessor(key);
		String suc_id = this.request(closestpreceding.getIdentifier(), req_target.findsuccessor + "_" + key.getIdentifier());
		ChordKey successor = null;
		if(suc_id!=null)
			successor = new ChordKey(suc_id);//need to fetch predecessor's successor
		
		return successor;
	}
	
	public ChordKey closest_proceding_node(ChordKey key){
		for(int i=m-1;i>=0;i--){
			ChordKey node_key = fingers[i].getNodeKey();
			if( node_key.isBetween(this.nodeKey,key) || this.nodeKey.equals(key)){
				return node_key;
			}
		}
		return this.nodeKey;
	}
	
	public void stabilize() throws Exception{
		if(this.getSuccessorKey()!=null){
			String suc_pre_id = this.request(this.getSucID(), req_target.predecessor); 
			ChordKey suc_pre = null;
			if(suc_pre_id!=null)
				suc_pre = new ChordKey(suc_pre_id);
			if(suc_pre!=null && (suc_pre.isBetween(this.nodeKey, this.successorKey) || (this.nodeKey.equals(this.successorKey) && !suc_pre.equals(this.nodeKey)))){
				this.successorKey = new ChordKey(suc_pre);
				ChordNode.log(suc_pre_id + " is successor of "+ this.getID());
			}
			if(this.successorKey.equals(nodeKey)){
				this.be_notified(nodeKey);
			}else{
				this.notify(this.successorKey);
			}
		}else{
			this.setSuccessorKey(new ChordKey(this.communication.getID()));
		}
	}
	
	//refresh finger table entries because of knowing existence of key
	public void fix_fingers_for_existing(ChordKey key) {
		for(int i=0;i<m;i++){
			ChordKey start = new ChordKey(fingers[i].getStart());
			if(key.isBetween(start, fingers[i].getNodeKey()) ){
				fingers[i].setNodeKey(key);
			}
		}
	}
	
	//refresh finger table entries because of assumed failure of key
	public void fix_fingers_for_failed(ChordKey key){
		for(int i=m-1;i>=0;i--){
			if(fingers[i].getNodeKey().equals(key)){
				if(i==m-1){
					fingers[i].setNodeKey(this.getNodeKey());
				}else{
					fingers[i].setNodeKey(fingers[i+1].getNodeKey());
				}
				
			}
		}
		if(this.getSuccessorKey().equals(key)){
			this.setSuccessorKey(fingers[0].getNodeKey());
		}
	}
	
	public void fix_fingers() throws Exception{
		for(int i=0;i<m;i++){
			fingers[i].setNodeKey(this.find_successor(new ChordKey(fingers[i].getStart())));
		}
	}
	
	public void create(){
		this.successorKey = new ChordKey(this.communication.getID());
		this.predecessorKey = null;
	}
	
	public void join(String node_id) throws Exception{
		this.predecessorKey = null;
		
		JSONObject msg = new JSONObject();
		ArrayList<String> arrstr = new ArrayList<String>();
		
		try {
			msg.put("method", method.join);
			arrstr.add("method:"+method.join);
			msg.put("from", this.communication.getID());
			arrstr.add("from:"+this.communication.getID());
			
			StringBuilder req_id_builder = new StringBuilder();
			Collections.sort(arrstr);
			for(int i=0;i<arrstr.size();i++){
				req_id_builder.append(arrstr.get(i));
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			req_id_builder.append(df.format(new Date()));
			
			String req_id = MD5(req_id_builder.toString());
			msg.put("req_id", req_id);
			
			try {
				communication.send(node_id, msg);
			} catch (ChordCommunicationException e) {
				this.fix_fingers_for_failed(new ChordKey(node_id));
				e.printStackTrace();
			}
			
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		String suc_id = this.request(node_id, req_target.findsuccessor + "_" + this.communication.getID());
		if(suc_id != null )
			this.successorKey = new ChordKey(suc_id);
		//this.fix_fingers(new ChordKey(node_id));
	}
	
	//suggest to suc that me might be its predecessor
	public void notify(ChordKey suc_key){
		JSONObject msg = new JSONObject();
		ArrayList<String> arrstr = new ArrayList<String>();
		
//		System.out.println(this.communication.getID() + " notifies " + suc_key.getIdentifier());
		
		try {
			msg.put("method", method.notify);
			arrstr.add("method:"+method.notify);
			msg.put("from", this.communication.getID());
			arrstr.add("from:"+this.communication.getID());
			
			StringBuilder req_id_builder = new StringBuilder();
			Collections.sort(arrstr);
			for(int i=0;i<arrstr.size();i++){
				req_id_builder.append(arrstr.get(i));
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			req_id_builder.append(df.format(new Date()));
			
			String req_id = MD5(req_id_builder.toString());
			msg.put("req_id", req_id);
			
			try {
				communication.send(suc_key.getIdentifier(), msg);
			} catch (ChordCommunicationException e) {
				this.fix_fingers_for_failed(new ChordKey(suc_key.getIdentifier()));
				e.printStackTrace();
			}
			
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//pre suggests that it is our predecessor
	public boolean be_notified(ChordKey pre_key){
		boolean result = false;
		if(predecessorKey == null || pre_key.isBetween(predecessorKey, this.nodeKey) || predecessorKey.equals(this.nodeKey)){
			if(!(predecessorKey!=null && predecessorKey.equals(pre_key))){
				this.predecessorKey = new ChordKey(pre_key);
				result = true;
				ChordNode.log(pre_key.getIdentifier() + " is predecessor of " + this.communication.getID());
//				System.out.println(pre_key.getIdentifier() + " is predecessor of " + this.communication.getID());
			}
			
		}
		return result;
	}
	
	public void init_circle(){
		if(this.getSuccessorKey()==null || this.getSuccessorKey().equals(nodeKey)){
			return;
		}
		String suc_id = this.getSucID();
		JSONObject msg = new JSONObject();
		ArrayList<String> arrstr = new ArrayList<String>();
		
		try {
			msg.put("method", method.circle);
			arrstr.add("method:"+method.circle);
			msg.put("from", this.communication.getID());
			arrstr.add("from:"+this.communication.getID());
			msg.put("jump", 0);
			arrstr.add("jump:0");
			
			StringBuilder req_id_builder = new StringBuilder();
			Collections.sort(arrstr);
			for(int i=0;i<arrstr.size();i++){
				req_id_builder.append(arrstr.get(i));
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			req_id_builder.append(df.format(new Date()));
			
			String req_id = MD5(req_id_builder.toString());
			msg.put("req_id", req_id);
			
			try {
				communication.send(suc_id, msg);
			} catch (ChordCommunicationException e) {
				this.fix_fingers_for_failed(new ChordKey(suc_id));
				e.printStackTrace();
			}
			
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void forward_circle(String initial_node, int jump){
		if(this.getSuccessorKey()==null || this.getSuccessorKey().equals(nodeKey)){
			return;
		}
		String suc_id = this.getSucID();
		JSONObject msg = new JSONObject();
		ArrayList<String> arrstr = new ArrayList<String>();
		
		try {
			msg.put("method", method.circle);
			arrstr.add("method:"+method.circle);
			msg.put("from", initial_node);
			arrstr.add("from:"+initial_node);
			msg.put("jump", jump);
			arrstr.add("jump:"+Integer.toString(jump));
			StringBuilder req_id_builder = new StringBuilder();
			Collections.sort(arrstr);
			for(int i=0;i<arrstr.size();i++){
				req_id_builder.append(arrstr.get(i));
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			req_id_builder.append(df.format(new Date()));
			
			String req_id = MD5(req_id_builder.toString());
			msg.put("req_id", req_id);
			
			try {
				communication.send(suc_id, msg);
			} catch (ChordCommunicationException e) {
				this.fix_fingers_for_failed(new ChordKey(suc_id));
				e.printStackTrace();
			}
			
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void checkPredecessor(){
		if(this.getPredecessorKey()==null || this.getPredecessorKey().equals(nodeKey)){
			//when pre is null, no need to check
			//predecessor is self, no need to check
			return;
		}
		String pre_id = this.getPreID();
		if(!this.check(pre_id)){
			this.fix_fingers_for_failed(getPredecessorKey());
			this.setPredecessorKey(null);
			
		}
	}
	
	public boolean check(String node_id){
//		System.out.println(this.getID() + " checking node " + node_id );
		boolean result = false;
		
		JSONObject msg = new JSONObject();
		ArrayList<String> arrstr = new ArrayList<String>();
		
		try {
			msg.put("method", method.check);
			arrstr.add("method:"+method.check);
			msg.put("from", this.communication.getID());
			arrstr.add("from:"+this.communication.getID());
			
			StringBuilder req_id_builder = new StringBuilder();
			Collections.sort(arrstr);
			for(int i=0;i<arrstr.size();i++){
				req_id_builder.append(arrstr.get(i));
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			req_id_builder.append(df.format(new Date()));
			
			String req_id = MD5(req_id_builder.toString());
			msg.put("req_id", req_id);
			
			try {
				communication.send(node_id, msg);
			} catch (ChordCommunicationException e) {
				this.fix_fingers_for_failed(new ChordKey(node_id));
				e.printStackTrace();
				return false;
			}
			
			String from_id = null;
			int timecount = 0;		
			while(timecount <= 10){
				Thread.sleep(200);
				//System.out.println(timecount);
				timecount++;
				from_id = communication.heardConfirm(node_id);
				if( from_id != null ){
//					System.out.println(this.getID() + "heard confirm from " + from_id );
					result = true;
					break;
				}
			}
			
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(result == false){
			ChordNode.log(this.getID() + "checking "+ node_id + "failed");
		}
		return result;
	}
	
	public void confirm(String node_id) {
		// TODO Auto-generated method stub
		JSONObject msg = new JSONObject();
		ArrayList<String> arrstr = new ArrayList<String>();
		
		try {
			msg.put("method", method.confirm);
			arrstr.add("method:"+method.confirm);
			msg.put("from", this.communication.getID());
			arrstr.add("from:"+this.communication.getID());
			
			StringBuilder req_id_builder = new StringBuilder();
			Collections.sort(arrstr);
			for(int i=0;i<arrstr.size();i++){
				req_id_builder.append(arrstr.get(i));
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			req_id_builder.append(df.format(new Date()));
			
			String req_id = MD5(req_id_builder.toString());
			msg.put("req_id", req_id);
			
			try {
				communication.send(node_id, msg);
			} catch (ChordCommunicationException e) {
				this.fix_fingers_for_failed(new ChordKey(node_id));
				e.printStackTrace();
			}
			
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void respond(String node_id, String target, String data){
		
//		System.out.println(this.getID() + " responding to " + node_id + " target for " + target);
		JSONObject msg = new JSONObject();
		ArrayList<String> arrstr = new ArrayList<String>();
		
		
		try {
			msg.put("method", method.respond);
			arrstr.add("method:"+method.respond);
			msg.put("from", this.communication.getID());
			arrstr.add("from:"+this.communication.getID());
			msg.put("target", target);
			arrstr.add("target:"+target);
			msg.put("data",data);
			arrstr.add("data:"+data);
			StringBuilder req_id_builder = new StringBuilder();
			Collections.sort(arrstr);
			for(int i=0;i<arrstr.size();i++){
				req_id_builder.append(arrstr.get(i));
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			req_id_builder.append(df.format(new Date()));
			
			String req_id = MD5(req_id_builder.toString());
			msg.put("req_id", req_id);
			
			try {
				communication.send(node_id, msg);
			} catch (ChordCommunicationException e) {
				this.fix_fingers_for_failed(new ChordKey(node_id));
				e.printStackTrace();
			}
//			System.out.println(this.getID() + " responded to " + node_id + " : " + msg);
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public String request(String node_id,String target) throws Exception{
		if(node_id.equals(this.communication.getID())){
			try {
				return this.getTarget(target);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		JSONObject msg = new JSONObject();
		ArrayList<String> arrstr = new ArrayList<String>();
		String response = null;
		//System.out.println(node_id + " " + target + "request");
		try {
			msg.put("method", method.request);
			arrstr.add("method:"+method.request);
			msg.put("from", this.communication.getID());
			arrstr.add("from:"+this.communication.getID());
			msg.put("target", target);
			arrstr.add("target:"+target);
			msg.put("waiting", true);
			arrstr.add("waiting:true");
			StringBuilder req_id_builder = new StringBuilder();
			Collections.sort(arrstr);
			for(int i=0;i<arrstr.size();i++){
				req_id_builder.append(arrstr.get(i));
			}
			String req_id_base = req_id_builder.toString();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			
			
			response = null;
			String req_id = "";
			int timecount = 0;
			int roundcount = 0;
			while(response == null ){
//				System.out.println(this.communication.getID()+" requests for "+target+" of "+node_id+"##round:"+Integer.toString(roundcount));
				if(roundcount>5){
					Thread.sleep(200);
					if(!this.check(node_id)){
						this.fix_fingers_for_failed(new ChordKey(node_id));
					}
					throw new Exception(this.communication.getID()+" requests for "+target+" of "+node_id+" timed out...");
				}
				req_id = MD5(req_id_base + df.format(new Date()));
				msg.put("req_id", req_id);
				
				try {
					communication.send(node_id, msg);
				} catch (ChordCommunicationException e) {
					this.fix_fingers_for_failed(new ChordKey(node_id));
					e.printStackTrace();
					return null;
				}
				
				timecount = 0;		
				while(timecount <= 10){
					Thread.sleep(200);
					//System.out.println(timecount);
					timecount++;
					response = communication.heardResponse(req_id);
					if(response != null ){
						//System.out.println("heard response from " + node_id + ":" + target + " " + response);
						break;
					}
				}
				
				roundcount++;
				
			}
			
			
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		if(response.equals(""))
			return null;
		return response;
	}
	
	public void request_not_waiting(String node_id,String target) throws Exception{
		if(node_id.equals(this.communication.getID())){
			try {
				this.getTarget(target);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		JSONObject msg = new JSONObject();
		ArrayList<String> arrstr = new ArrayList<String>();
		String response = null;
		//System.out.println(node_id + " " + target + "request");
		try {
			msg.put("method", method.request);
			arrstr.add("method:"+method.request);
			msg.put("from", this.communication.getID());
			arrstr.add("from:"+this.communication.getID());
			msg.put("target", target);
			arrstr.add("target:"+target);
			msg.put("waiting", false);
			arrstr.add("waiting:false");
			StringBuilder req_id_builder = new StringBuilder();
			Collections.sort(arrstr);
			for(int i=0;i<arrstr.size();i++){
				req_id_builder.append(arrstr.get(i));
			}
			String req_id_base = req_id_builder.toString();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			
			String req_id = MD5(req_id_base + df.format(new Date()));
			msg.put("req_id", req_id);
			try {
				communication.send(node_id, msg);
			} catch (ChordCommunicationException e) {
				this.fix_fingers_for_failed(new ChordKey(node_id));
				e.printStackTrace();
				return;
			}
			
			
			
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
	}

	public ChordKey getNodeKey() {
		return nodeKey;
	}

	public void setNodeKey(ChordKey nodeKey) {
		this.nodeKey = nodeKey;
	}

	public ChordKey getPredecessorKey() {
		return predecessorKey;
	}

	public void setPredecessorKey(ChordKey predecessorKey) {
		this.predecessorKey = predecessorKey;
	}

	public ChordKey getSuccessorKey() {
		return successorKey;
	}

	public void setSuccessorKey(ChordKey successorKey) {
		this.successorKey = successorKey;
	}
	
	public ITaskExecutable getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(ITaskExecutable taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public ICommunicatable getCommunication() {
		return communication;
	}
	
	public String getID(){
		return getCommunication().getID();
	}

	public void setCommunication(ICommunicatable communication) {
		this.communication = communication;
	}

	public String fingerTable2Str(){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<m;i++){
			sb.append(Integer.toString(i)+":"+fingers[i].toString()+"\n");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "ChordNode [id=" + this.communication.getID() + ", predecessorKey="
				+ predecessorKey + ", successorKey=" + successorKey + "]\n后继结点表:\n" + this.fingerTable2Str() ;
				
	}
	
	public static void log(String msg){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String now = df.format(new Date());
		//System.out.println(now + " $$ " + msg);
	}
	
	public static String MD5(String s){
		char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
}
