package sbbj_tpg;

public class OpenDouble {
	private double value;

	// Create an OpenDouble object set to val
	public OpenDouble(double val) {
		value = val;
	}

	// Set the value of this OpenDouble to val
	public void setValue(double val) {
		value = val;
	}

	// Return the current value of this OpenDouble
	public double getValue() {
		return value;
	}

	// Return the string representation of this OpenDouble
	public String toString() {
		return "" + value;
	}
}
