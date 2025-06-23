package oo.com.iseu.Information;

public class ShareVarInfo {
	//���ڽڵ㶨λ��·�����к�(���ݿ�洢)
	private int lineNumber;
	private String path;
	//���������жϵļ�¼
	private String type;
	//�����ĸ�����
	private String belongMethod;
	//�Ƿ�Ϊԭʼ����
	//ԭʼ��������λ��
	private String belongClass;
	private int variableID;
	
	
	public ShareVarInfo(int lineNumber, String type,String path,String belongMethod,String belongClass,int variableID) {
		super();
		this.lineNumber = lineNumber;
		this.type = type;
		this.path = path;
		this.belongMethod = belongMethod;
		this.belongClass = belongClass;
		this.variableID = variableID;
	}
	
	public String getBelongMethod() {
		return belongMethod;
	}

	public void setBelongMethod(String belongMethod) {
		this.belongMethod = belongMethod;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getBelongClass() {
		return belongClass;
	}

	public void setBelongClass(String belongClass) {
		this.belongClass = belongClass;
	}

	public int getVariableID() {
		return variableID;
	}

	public void setVariableID(int variableID) {
		this.variableID = variableID;
	}

	@Override
	public String toString() {
		return  "PATH:          "+path+
				"\nLINENUMBER:    "+lineNumber+
				"\nTYPE:          "+type+
				"\nBELONGMETHOD:  "+belongMethod+
				"\nBELONGCLASS:   "+belongClass+
				"\nCLASSVARID:     "+variableID+"\n";
	}
	
}
