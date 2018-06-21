package sbbj_tpg;

public class Pair<K, V> {
	private K first;
	private V second;

	// Create a Pair object which holds a K-type and V-type value
	public Pair(K first, V second) {
		this.first = first;
		this.second = second;
	}

	// Return this Pair's first value, which is type K
	public K getFirst() {
		return first;
	}

	// Return this Pair's second value, which is type V
	public V getSecond() {
		return second;
	}

	// Set this Pair's first value, which is type K
	public void setFirst(K first) {
		this.first = first;
	}

	// Set this Pair's second value, which is type V
	public void setSecond(V second) {
		this.second = second;
	}
}