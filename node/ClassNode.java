package oo.com.iseu.node;

import java.util.ArrayList;

import javax.servlet.ServletContextAttributeListener;

/**
 * @author Jason
 * @param
 * ��������: ������
		����ClassNode�ڵ�
		ʵ�ֵĽӿڵ�List<InterfaceNode>
		�����ķ���List<MethodNode>
		�����ĳ�Ա����List<FieldNode>
		����List<ClassNode>
		�Ƿ������
		�Ƿ�������
		FilePath
		startLine
 *
 */
public class ClassNode {
	public String strClassName;

	/**
	 * @return the className
	 */
	public String getClassName() {
		return strClassName;
	}

	/**
	 * @param className
	 *            the className to set
	 */
	public void setClassName(String className) {
		this.strClassName = className;
	}

	public ClassNode superClass = null;
	public ClassNode declaringClass = null;
	public ArrayList<ClassNode> listInnerClass = new ArrayList<ClassNode>();
	public ArrayList<InterfaceNode> listInterface = new ArrayList<InterfaceNode>();
	public ArrayList<MethodNode> listMethod = new ArrayList<MethodNode>();
	public ArrayList<FieldNode> listField = new ArrayList<FieldNode>();

	public ArrayList<ClassNode> listSonClass = new ArrayList<ClassNode>();
	public boolean isAnonymous = false;
	public boolean isAbstract = false;
	
	public String strFilePath = null;
	public int startLine = -1;
	
	public ArrayList<GenericityNode> listGenericityNodes = new ArrayList<>();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	// @Override
	// public String toString() {
	// // TODO Auto-generated method stub
	// StringBuffer strBuffer = new StringBuffer();
	// strBuffer.append("[Class] " + strClassName + "\r\n");
	//
	// if (superClass == null)
	// strBuffer.append("-->[SuperClass] null\r\n");
	// else
	// strBuffer.append("-->[SuperClass] " + superClass.getClassName() +
	// "\r\n");
	//
	// if(listSonClass.isEmpty())
	// strBuffer.append("-->[SonClass] null\r\n");
	// else{
	// for (ClassNode classNode : listSonClass) {
	// strBuffer.append("-->[SonClass] " + classNode.getClassName() + "\r\n");
	// }
	// }
	//
	// strBuffer.append("-->[Implements Interface] \r\n");
	//
	// if (listInterface.isEmpty())
	// strBuffer.append("----> 0 interface implemented\r\n");
	// else {
	// for (InterfaceNode interfaceNode : listInterface) {
	// strBuffer.append("----> " + interfaceNode.getInterfaceName() + "\r\n");
	// }
	// }
	//
	// strBuffer.append("-->[Declaring Method] \r\n");
	// if (listMethod.isEmpty())
	// strBuffer.append("----> 0 method declared in this class\r\n");
	// else {
	// for (MethodNode methodNode : listMethod) {
	// strBuffer.append(methodNode);
	// }
	// }
	//
	// strBuffer.append("-->[Declaring Field] \r\n");
	// if (listField.isEmpty())
	// strBuffer.append("----> 0 field declared in this class\r\n");
	// else {
	// for (FieldNode fieldNode : listField) {
	// strBuffer.append(fieldNode);
	// }
	// }
	// return strBuffer.toString();
	// }
}
