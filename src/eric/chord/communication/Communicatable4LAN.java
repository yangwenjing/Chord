package eric.chord.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import eric.chord.core.ChordNode;
import eric.chord.core.ICommunicatable;
import eric.chord.core.ChordCommunicationException;

public class Communicatable4LAN implements ICommunicatable {

	public String id;//using ipaddress
	public static int port = 3008;
	
	private ServerSocket sRecvMsg=null;
	
	private boolean worthRunning;
	
	private ArrayList<JSONObject> myHeard = new ArrayList<JSONObject>();
	
	public static class MsgSender {
		
		private static final int largestMsgLength=20000;
		
		public static void sendStrMsg(String originalMsg, byte[] ip, int port) throws ChordCommunicationException
	    {

	        try
	        {
	        	String sendMsg=modifyStrMsg(originalMsg);
	        	InetAddress client = InetAddress.getByAddress(ip);
	        	Socket sendSocket = new Socket(client,port);
	        	
	        	try
	        	{
	        		//Console.WriteLine("Connecting...");
	                //sendSocket.connect(new InetSocketAddress(client,sendPort));
	                //Console.WriteLine("Successfully connected!");
	                byte[] sendContents = sendMsg.getBytes("UTF-8");
	            
	                OutputStream sendMsgStream=sendSocket.getOutputStream();
	                sendMsgStream.write(sendContents, 0, sendContents.length);//发送二进制数据
	        	}
	        	catch (Exception ex)
	            {
	            	System.err.println("Exception occurs: "+ex);
	    			ex.printStackTrace();
	            }
	        	finally
	            {
	                sendSocket.close();
	            }
	        }
	        catch (ConnectException ex)
	        {
	        	throw new ChordCommunicationException();
	        }
	        catch (Exception e){
	        	System.err.println("Exception occurs: "+e);
    			e.printStackTrace();
	        }
	        
	    }

	    private static String modifyStrMsg(String originalMsg) throws Exception
	    {
	        StringBuffer strbf = new StringBuffer();
	        int length=originalMsg.length();

	        if (length > largestMsgLength)
	            throw (new Exception("The message is too long for one time to send..."));

	        if(originalMsg.lastIndexOf(';')!=-1)
	            throw (new Exception("The message contains invalid character \';\'..."));

	        strbf.append(length);
	        strbf.append(';');
	        strbf.append(originalMsg);
	        strbf.append(';');

	        String sendMsg = strbf.toString();
	        return sendMsg;
	    }

	}
	
	
	public Communicatable4LAN() {
		super();
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			this.id = addr.getHostAddress().toString();//获得本机IP
//			String address=addr.getHostName().toString();//获得本机名称
			this.setWorthRunning(true);
			
			sRecvMsg =new ServerSocket(port);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public byte[] ipaddr2bytes(String ipaddress) throws ChordCommunicationException{
		byte[] ip=new byte[4];
		String[] ipnums = ipaddress.split("\\.");
		if(ipnums.length!=4)
			throw new ChordCommunicationException("invalid ipaddress");
		for(int i=0;i<4;i++){
			ip[i] = (byte) Integer.parseInt(ipnums[i]);
		}
		return ip;
	}

	@Override
	public String getID() {		
		return this.id;
	}

	@Override
	public boolean send(String node_id, JSONObject msg) throws ChordCommunicationException  {
		String strMsg = msg.toString();
//		if(strMsg.length()>=3000){
//			throw new ChordCommunicationException("The communication does not support so long a message to be sent.");
//		}
		byte[] ip = ipaddr2bytes(node_id);
		MsgSender.sendStrMsg(strMsg, ip, port);
		return true;
	}

	@Override
	public String heardCheck() {
		String node_id = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i);
			
				if(data.getString("method").equals(ChordNode.method.check) ){
					JSONObject res = data;
					
					node_id = res.getString("from");

//					myResponse.get(i).setStatus("read");
					myHeard.remove(i);
					break;
				}
			}
		} catch (JSONException e){
			System.err.println(e);
			e.printStackTrace();
		}
		return node_id;
	}

	@Override
	public String heardConfirm(String from_id) {
		String node_id = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i);
			
				if(data.getString("method").equals(ChordNode.method.confirm) ){
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
	public String heardJoin() {
		String node_id = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i);
			
				if(data.getString("method").equals(ChordNode.method.join) ){
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

	@Override
	public String heardNotify() {
		String node_id = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i);
			
				if(data.getString("method").equals(ChordNode.method.notify) ){
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

	@Override
	public JSONObject heardRequest() {
		JSONObject req = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i);
			
				if(data.getString("method").equals(ChordNode.method.request) ){
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

	@Override
	public String heardResponse(String req_id) {
		String result = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i);
			
				if(data.getString("method").equals(ChordNode.method.respond) && data.getString("target").equals(req_id) ){
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
	
	@Override
	public JSONObject heardCircle() {
		JSONObject msg = null;
		try{
			for(int i=0;i<myHeard.size();i++){
				JSONObject data = myHeard.get(i);
			
				if(data.getString("method").equals(ChordNode.method.circle) ){
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
		while(isWorthRunning())
		{
			try {
				Thread.sleep(51);
				//counter++;
				//System.out.println(counter);
				Socket listener=sRecvMsg.accept();
				listener.setReceiveBufferSize(4096);
				
				
				byte[] recvMsgBytes = new byte[1024];
				InputStream recvMsgStream=listener.getInputStream();
				recvMsgStream.read(recvMsgBytes,0,recvMsgBytes.length);
				String recvStr = new String(recvMsgBytes,"UTF-8");
				
				String[] strArray = recvStr.split(";");
				//System.out.println(strArray[0]);
                int msgLength = Integer.parseInt(strArray[0]);
                int acuLength = strArray[1].length();
                StringBuffer recvStrBf=new StringBuffer(strArray[1]);
                
                while (acuLength < msgLength)
                {
                	recvMsgStream.read(recvMsgBytes,0,recvMsgBytes.length);
                	recvStr = new String(recvMsgBytes,"UTF-8");
                	strArray = recvStr.split(";");
                    acuLength += strArray[0].length();
                    recvStrBf.append(strArray[0]);
                }
                
                String recvMsg = recvStrBf.toString();
                //System.out.println(recvMsg);
                handleMsg(recvMsg);
				
			} catch (Exception e) {
				System.err.println("Exception occurs: "+e);
				e.printStackTrace();
			}
		}
		System.out.println("Listener is going to close..");
		try {
			sRecvMsg.close();
		} catch (Exception e) {
			System.err.println("Exception occurs: "+e);
			e.printStackTrace();
		}

	}
	
	private void handleMsg(String recvMsg) throws JSONException {
		JSONObject msg = new JSONObject(recvMsg);
		myHeard.add(msg);
	}

	public boolean isWorthRunning() {
		return worthRunning;
	}

	public void setWorthRunning(boolean worthRunning) {
		this.worthRunning = worthRunning;
	}

	

}
