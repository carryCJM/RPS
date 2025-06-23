package oo.com.iseu.UI.Basic;

public enum SliceType {
	OOSLICING(0),
	CSLICING(1),
	DDSLICING(2);
	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	private  SliceType(int type) {
		this.type = type;
	}

}