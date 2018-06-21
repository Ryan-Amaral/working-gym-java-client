package sbbj_tpg;

public class OpenBoolean {
	protected boolean value;

	// Create a new OpenBoolean object initialized to val
	public OpenBoolean(boolean val) {
		value = val;
	}

	// Set this OpenBoolean to val
	public void setValue(boolean val) {
		value = val;
	}

	// Return the current boolean value of this OpenBoolean
	public boolean getValue() {
		return value;
	}

	// Return the string representation of this OpenBoolean
	public String toString() {
		return "" + value;
	}
}
