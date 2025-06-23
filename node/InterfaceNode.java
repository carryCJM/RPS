package oo.com.iseu.node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason ��������: �ӿ����� ʵ�֣���չ���Ľӿڵ�List<InterfaceNode> �����ķ���List<MethodNode>
 *         �����ĳ�Ա����List<FieldNode> ʵ�ָýӿڵ���List<ClassNode> FilePath startLine
 *
 */
public class InterfaceNode {
	public String strinterfaceName;

	/**
	 * @return the interfaceName
	 */
	public String getInterfaceName() {
		return strinterfaceName;
	}

	/**
	 * @param interfaceName
	 *            the interfaceName to set
	 */
	public void setInterfaceName(String interfaceName) {
		this.strinterfaceName = interfaceName;
	}

	public ArrayList<InterfaceNode> listSuperInterface = new ArrayList<InterfaceNode>();
	public ArrayList<InterfaceNode> listSonInterface = new ArrayList<InterfaceNode>();
	public List<MethodNode> listMethodDeclared = new ArrayList<MethodNode>();
	public List<FieldNode> listField = new ArrayList<FieldNode>();
	public List<ClassNode> listClassImplements = new ArrayList<ClassNode>();
	public List<EnumNode> listEnumImplements = new ArrayList<EnumNode>();

	public String strFilePath = null;
	public int startLine = -1;
	
	public ArrayList<GenericityNode> listGenericityNodes = new ArrayList<>();

	// public InterfaceNode(String interfaceNames, List<InterfaceNode>
	// interfaceLists,
	// List<MethodNode> methodLists, List<FieldNode> fieldLists ) {
	// interfaceName = interfaceNames;
	// interfaceList = interfaceLists;
	// methodList = methodLists;
	// fieldList = fieldLists;
	// }
	public void printSuperInterfaceInfo() {
		if (listSuperInterface.isEmpty()) {
			System.out.println("[SuperInterface]: null");
		} else {
			for (InterfaceNode interfaceNode : listSuperInterface) {
				System.out.println(interfaceNode.getInterfaceName());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("[Interface] " + strinterfaceName + "\r\n");

		strBuffer.append("-->[SuperInterface] \r\n");

		if (listSuperInterface.isEmpty()) {
			strBuffer.append("----> 0 super interfaces\r\n");
		} else {
			for (InterfaceNode interfaceNode : listSuperInterface) {
				strBuffer.append("----> " + interfaceNode.getInterfaceName() + "\r\n");
			}
		}

		strBuffer.append("-->[SonInterface] \r\n");

		if (listSonInterface.isEmpty()) {
			strBuffer.append("----> 0 son interfaces\r\n");
		} else {
			for (InterfaceNode interfaceNode : listSonInterface) {
				strBuffer.append("----> " + interfaceNode.getInterfaceName() + "\r\n");
			}
		}

		strBuffer.append("-->[Declaring Method] \r\n");
		if (listMethodDeclared.isEmpty()) {
			strBuffer.append("----> 0 method declared in this interface\r\n");
		} else {
			for (MethodNode methodNode : listMethodDeclared) {
				strBuffer.append(methodNode);
			}
		}

		strBuffer.append("-->[Declaring Field] \r\n");
		if (listField.isEmpty()) {
			strBuffer.append("----> 0 field declared in this interface\r\n");
		} else {
			for (FieldNode fieldNode : listField) {
				strBuffer.append(fieldNode);
			}
		}

		strBuffer.append("-->[Implement Class]\r\n");
		if (listClassImplements.isEmpty()) {
			strBuffer.append("----> 0 class implements this interface\r\n");
		} else {
			for (ClassNode clsNode : listClassImplements) {
				strBuffer.append("------>" + clsNode.getClassName() + "\r\n");
			}
		}
		return strBuffer.toString();
	}

}
