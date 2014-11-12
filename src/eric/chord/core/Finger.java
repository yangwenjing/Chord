package eric.chord.core;

public class Finger {
	
	private long start = -1l;
	private ChordKey nodeKey = null;

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public ChordKey getNodeKey() {
		return nodeKey;
	}

	public void setNodeKey(ChordKey key) {
		this.nodeKey = key;
	}

	@Override
	public String toString() {
		return "ºó¼Ì½áµã [start=" + start + ", nodeKey=" + nodeKey + "]";
	}
	
}
