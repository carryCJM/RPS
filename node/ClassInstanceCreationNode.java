/**
 * author: Jason
 * 2015��11��25��
 * TODO
 */
package oo.com.iseu.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author Jason
 *
 */
public class ClassInstanceCreationNode {

	//������ڵ���ͷ���
	String strInClass;
	String strInMethod;

	//���ڵ�
	ASTNode nodeStatement;
	
	//ʵ������������
	String strInstanceName;
	String strInstanceType;
	
	//ʵ��������
	String strInitType;
	
	//ʵ�������õĹ��췽��
	String strIvkClass;
	String strIvkMtd;
	List<String> strFormalParas = new ArrayList<String>();
	
	//ʵ�� 
	List<String> strActualParas = new ArrayList<String>();
	
	String strStartPos = null;

	/**
	 * @return the strStartPos
	 */
	public String getStrStartPos() {
		return strStartPos;
	}

	/**
	 * @param strInClass
	 * @param strInMethod
	 * @param nodeStatement
	 * @param strInstanceName
	 * @param strInstaceType
	 * @param strInitType
	 * @param strIvkClass
	 * @param strIvkMtd
	 * @param strFormalParas
	 * @param strActualParas
	 */
	public ClassInstanceCreationNode(String strInClass, String strInMethod, ASTNode nodeStatement,
			String strInstanceName, String strInstanceType, String strInitType, String strIvkClass, String strIvkMtd,
			List<String> strFormalParas, List<String> strActualParas, String strStartPos) {
		super();
		this.strInClass = strInClass;
		this.strInMethod = strInMethod;
		this.nodeStatement = nodeStatement;
		this.strInstanceName = strInstanceName;
		this.strInstanceType = strInstanceType;
		this.strInitType = strInitType;
		this.strIvkClass = strIvkClass;
		this.strIvkMtd = strIvkMtd;
		this.strFormalParas = strFormalParas;
		this.strActualParas = strActualParas;
		this.strStartPos = strStartPos;
	}

	/**
	 * @return the strInClass
	 */
	public String getStrInClass() {
		return strInClass;
	}

	/**
	 * @return the strInMethod
	 */
	public String getStrInMethod() {
		return strInMethod;
	}

	/**
	 * @return the nodeStatement
	 */
	public ASTNode getNodeStatement() {
		return nodeStatement;
	}

	/**
	 * @return the strInstanceName
	 */
	public String getStrInstanceName() {
		return strInstanceName;
	}

	/**
	 * @return the strInstanceType
	 */
	public String getStrInstanceType() {
		return strInstanceType;
	}

	/**
	 * @return the strInitType
	 */
	public String getStrInitType() {
		return strInitType;
	}

	/**
	 * @return the strIvkClass
	 */
	public String getStrIvkClass() {
		return strIvkClass;
	}

	/**
	 * @return the strIvkMtd
	 */
	public String getStrIvkMtd() {
		return strIvkMtd;
	}

	/**
	 * @return the strFormalParas
	 */
	public List<String> getStrFormalParas() {
		return strFormalParas;
	}

	/**
	 * @return the strActualParas
	 */
	public List<String> getStrActualParas() {
		return strActualParas;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ClassInstanceCreationNode [strInClass=" + strInClass + ", strInMethod=" + strInMethod
				+ ", nodeStatement=" + nodeStatement + ", strInstanceName=" + strInstanceName + ", strInstanceType="
				+ strInstanceType + ", strInitType=" + strInitType + ", strIvkClass=" + strIvkClass + ", strIvkMtd="
				+ strIvkMtd + ", strFormalParas=" + strFormalParas + ", strActualParas=" + strActualParas
				+ ", strStartPos=" + strStartPos + "]";
	}

}
