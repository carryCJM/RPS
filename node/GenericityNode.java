package oo.com.iseu.node;

import java.util.ArrayList;

public class GenericityNode {
	private String genericityName;
	private ArrayList<String> TypeBound;
	public ArrayList<GenericityNode> listGenericityNodes = new ArrayList<>();
	
	public String getGenericityName() {
		return genericityName;
	}
	public void setGenericityName(String genericityName) {
		this.genericityName = genericityName;
	}
	public ArrayList<String> getTypeBound() {
		return TypeBound;
	}
	public void setTypeBound(ArrayList<String> typeBound) {
		this.TypeBound = typeBound;
	}
	public GenericityNode(String genericityName, ArrayList<String> typeBound) {
		this.genericityName = genericityName;
		this.TypeBound = typeBound;
	}
	
	
	
}
