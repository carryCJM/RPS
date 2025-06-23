package oo.com.iseu.node;

import java.util.ArrayList;

public class ParameterNode {
	
	/**
	 * @author ������
	 * ����������
	 	��������
	 */	
	public String parameterName;
	public String parameterType;
	private boolean isGeneric;
	private ArrayList<String> superBounds;
	
//	public ParameterNode(String parameterName, String parameterType) {
//		this.parameterName = parameterName;
//		this.parameterType = parameterType;
//	}
	
	public ParameterNode(String parameterName, String parameterType, boolean isGeneric, ArrayList<String> superBounds) {
		this.parameterName = parameterName;
		this.parameterType = parameterType;
		this.isGeneric = isGeneric;
		this.superBounds = superBounds;
	}
	/*
	 * ���ز�����
	 */
	public String getParameterName() {
		return parameterName;
	}
	
	/*
	 * ���ز�������
	 */
	public String getParameterType() {
		return parameterType;
	}
	public boolean isGeneric() {
		return isGeneric;
	}
	public void setGeneric(boolean isGeneric) {
		this.isGeneric = isGeneric;
	}
	public ArrayList<String> getSuperBounds() {
		return superBounds;
	}
	public void setSuperBounds(ArrayList<String> superBounds) {
		this.superBounds = superBounds;
	}
}
