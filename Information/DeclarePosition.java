package oo.com.iseu.Information;

public enum DeclarePosition {
	INMEMBER(0),
	INPARAMETER(1),
	INMEMBERPRIMITIVE(2),
	INMETHODMEM(3),
	INMETHOD(-1);
	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	private DeclarePosition(int type) {
		this.type = type;
	}

}
