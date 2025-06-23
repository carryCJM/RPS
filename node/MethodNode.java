package oo.com.iseu.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;


/**
 * @author Jason
 * @param ��������: �������� ������������ �������η� �������������б�<String> �Ƿ���󷽷�
 *        ClassNode/InterfaceNode interMethod CFG PrDG FilePath startLine
 *        LastModifyTime:2015-10-11
 * 
 *        * @author Duan Yulong
 * @date 2015-10-22 ��ӳ�Ա����listInstantiationNodes
 */
public class MethodNode {
	public String strRetType;
	public String strMethodName;
	public String strModifier;
	public List<ParameterNode> listParameterTypes = new ArrayList<ParameterNode>();
	public boolean isAbstract = false;
	public ClassNode clsNode = null;
	public EnumNode enumNode = null;
	public InterfaceNode itfNode = null;
	public MethodDeclaration methodDeclaration;     //��ʼ����ʱΪnull

	public ArrayList<InstantiationNode> listInstantiationNodes = new ArrayList<>();

	public ArrayList<MethodIvkNode> listMtdIvkNodes = new ArrayList<>();
	
	public ArrayList<ClassInstanceCreationNode> listClsInsCreationNodes = new ArrayList<>();


	public String strFilePath = null;
	public int startLine = -1;

	public MethodNode(String retType, String mtdName, String modifier, List<ParameterNode> listParas,MethodDeclaration mDeclaration) {
		strRetType = retType;
		strMethodName = mtdName;
		strModifier = modifier;
		listParameterTypes = listParas;
		isAbstract = modifier.contains("abstract");
		methodDeclaration = mDeclaration;
	}

	public List<MethodIvkNode> getIvkNodesWithoutInsName() {
		List<MethodIvkNode> result = new ArrayList<>();
		for (MethodIvkNode methodIvkNode : listMtdIvkNodes) {
			if (methodIvkNode.getInstanceName().equals("this") || methodIvkNode.getInstanceName().equals("super"))
				result.add(methodIvkNode);
		}
		return result;
	}

	public List<MethodIvkNode> getStaticIvkNodes() {
		List<MethodIvkNode> result = new ArrayList<>();
		for (MethodIvkNode methodIvkNode : listMtdIvkNodes) {
			if (methodIvkNode.getIsStaticIvk() && !methodIvkNode.getInstanceName().equals("this")) {
				result.add(methodIvkNode);
			}
		}
		return result;
	}

	/**
	 * @return the strRetType
	 */
	public String getStrRetType() {
		return strRetType;
	}

	/**
	 * @return the strMethodName
	 */
	public String getStrMethodName() {
		return strMethodName;
	}

	/**
	 * @return the strModifier
	 */
	public String getStrModifier() {
		return strModifier;
	}

	public void printMethodInfo() {
		System.out.println("------MethodInfomationPrint------");
		System.out.println("[Name] " + strMethodName);
		System.out.println("[Return Type] " + strRetType);
		System.out.println("[Modifier] " + strModifier);
		System.out.println("[ParameterTypes]:");
		if (!listParameterTypes.isEmpty()) {
			for (ParameterNode string : listParameterTypes) {
				System.out.println("--->" + string);
			}
		} else {
			System.out.println("void");
		}
		System.out.println("------EndOfMethodInfoPrint------");
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
		strBuffer.append("---->[Method] " + strMethodName + "\r\n");
		strBuffer.append("------>[Return Type] " + strRetType + "\r\n");
		strBuffer.append("------>[Modifier] " + strModifier + "\r\n");
		strBuffer.append("------>[ParameterTypes]:\r\n");
		if (listParameterTypes != null) {
			for (ParameterNode string : listParameterTypes) {
				strBuffer.append("-------->" + string.parameterName + "\r\n");
				strBuffer.append("-------->" + string.parameterType + "\r\n");
			}
		} else {
			strBuffer.append("void\r\n");
		}
		strBuffer.append("------>[Abstract]:" + isAbstract + "\r\n");

		if (!listMtdIvkNodes.isEmpty()) {
			strBuffer.append("------>[MethodIvkIncluded:]:\r\n");
			for (MethodIvkNode methodIvkNode : listMtdIvkNodes) {
				strBuffer.append("-------->" + methodIvkNode + "\r\n");
			}
		}
		return strBuffer.toString();
	}


}
