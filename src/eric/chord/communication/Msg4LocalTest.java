package eric.chord.communication;

import org.json.*;

public class Msg4LocalTest {
	
	private String target_node_id;
	private String status;
	private JSONObject data;
	private int time_to_live;
	
	public Msg4LocalTest(String target_node_id, JSONObject data) {
		this.target_node_id = target_node_id;
		this.status = "created";
		this.data = data;
		this.time_to_live =5;
	}
	public Msg4LocalTest(Msg4LocalTest m4t) {
		this.target_node_id = m4t.getTarget_node_id();
		this.status = m4t.getStatus();
		this.data = m4t.getData();
		this.time_to_live =5;
	}
	
	public String getTarget_node_id() {
		return target_node_id;
	}
	public void setTarget_node_id(String target_node_id) {
		this.target_node_id = target_node_id;
	}
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public void setData(JSONObject data) {
		this.data = data;
	}
	public JSONObject getData() {
		return data;
	}
	public boolean hasTime_to_live() {
		return time_to_live>0;
	}
	public void minusTime_to_live() {
		this.time_to_live--;
	}

	@Override
	public String toString() {
		return "Msg4Test [target_node_id=" + target_node_id + ", status="
				+ status + ", data=" + data + "]";
	}
	
}
