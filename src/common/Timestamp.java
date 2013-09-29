package common;

public class Timestamp implements Comparable<Timestamp> {
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
}
