package oo.com.iseu.UI.Basic;

public enum TaskType {
	SDG(0),
	MDG(1),
	DDG(2),
	SLICING(3),
	OTHER(4);
	
	private int type;
	private TaskType(int type) {
		this.type = type;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
