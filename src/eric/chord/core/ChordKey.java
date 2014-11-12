package eric.chord.core;

import java.security.MessageDigest;

public class ChordKey {
	
	private String identifier;
	private long key;
	
	public ChordKey(long key){
		this.setIdentifier(null);
		this.setKey(key);
	}
	
	public ChordKey(String id){
		this.setIdentifier(id);
		this.setKey(hash_sha1(id));
	}
	
	public ChordKey(ChordKey key) {
		this.setIdentifier(key.getIdentifier());
		this.setKey(key.getKey());
	}
	
	public boolean equals(ChordKey key){
		return (this.getKey()==key.getKey());
	}

	public boolean isBetween(ChordKey fromKey,ChordKey toKey){
		if(fromKey.getKey()<toKey.getKey())
			return ( this.getKey()>fromKey.getKey() && this.getKey()<toKey.getKey() );
		else if(fromKey.getKey()>toKey.getKey())
			return ( this.getKey()>fromKey.getKey() || this.getKey()<toKey.getKey() );
		else{
			return false;
//			if(this.getKey()==fromKey.getKey())
//				return false;
//			else
//				return true;
		}
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public long getKey() {
		return key;
	}

	public void setKey(long key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return "ChordKey [identifier=" + identifier + ", key=" + key + "]";
	}

	public static long hash_sha1(String id){
		if(id==null){
			return -1;
		}
		long value = 0;
		byte[] bytes_value;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.reset();
			byte[] code = md.digest(id.getBytes());
			bytes_value = new byte[ChordNode.m/8];
			int shrink = code.length / bytes_value.length;
			int bitCount = 1;
			for (int j = 0; j < code.length * 8; j++) {
				int currBit = ((code[j / 8] & (1 << (j % 8))) >> j % 8);
				if (currBit == 1)
					bitCount++;
				if (((j + 1) % shrink) == 0) {
					int shrinkBit = (bitCount % 2 == 0) ? 0 : 1;
					bytes_value[j / shrink / 8] |= (shrinkBit << ((j / shrink) % 8));
					bitCount = 1;
				}
			}
			//value = bytes_values;
		} catch (Exception e) {
			e.printStackTrace();
			return -1l;
		}
		for (int i = 0; i < bytes_value.length; i++){
			value += bytes_value[i] << (8*i);
		}
		value = (value + (1<<ChordNode.m))%(1<<ChordNode.m);
		return value;
	}

}
