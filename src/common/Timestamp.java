package common;

import java.io.Serializable;

public class Timestamp implements Comparable<Timestamp>, Serializable {
	private static final int MAX_CLIENTS = 1000;
	private static final long serialVersionUID = -175054147849229102L;
	private int id;
	private long time;
	
	public Timestamp(int id) {
		this(id, System.currentTimeMillis());
	}
	public Timestamp(int id, long time) {
		this.id = id;
		this.time = time;
	}
	public int getId() {
		return this.id;
	}
	public long getTime() {
		return this.time;
	}
	
	@Override
	public int compareTo(Timestamp t) {
		if (this.time == t.time) {
			return Integer.compare(this.id, t.id);
		} else {
			return Long.compare(this.time, t.time);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return o == null ? false : compareTo((Timestamp)o) == 0;
	}
	
	@Override
	public int hashCode() {
		return (int) time*MAX_CLIENTS + id;
	}
}
