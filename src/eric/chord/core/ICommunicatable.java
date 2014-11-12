package eric.chord.core;

import org.json.JSONObject;

public interface ICommunicatable extends Runnable{
	
	public String getID();
	
	public boolean send(String node_id,JSONObject msg) throws ChordCommunicationException;
	public String heardCheck();
	public String heardConfirm(String from_id);
	public String heardJoin();
	public String heardNotify();
	public JSONObject heardRequest();
	public String heardResponse(String req_id);
	public JSONObject heardCircle();
	

}
