package oo.com.iseu.UI.Basic;

public enum SliceDirection {
	FORWARD(0),
	BACKWARD(1),
	ALL(2);
	private int type;

	private SliceDirection(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
