/*
 * *author: Duan Yulong
 * 2015��10��15��
 */
package oo.com.iseu.node;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author Duan Yulong
 *
 * LastModifyTime:2015-10-22
 * ��ӳ�Ա����statements��strInClass, strInMethod
 * �޸Ĺ�����
 * ��ӷ���getStrInMethod, getStrInClass, getStatements
 */

public class InstantiationNode {
	public String strIdentifierName;
	public String strIdentifierType;
	public String strInstantiationType1;
	public String strInstantiationType2;
	public boolean isClassInstanceCreation = false;
	private ASTNode statements;
	private String strInClass;
	private String strInMethod;
	private boolean isMethodInvocation = false;
	
	
	public boolean isMethodInvocation() {
		return isMethodInvocation;
	}
	public void setMethodInvocation(boolean isMethodInvocation) {
		this.isMethodInvocation = isMethodInvocation;
	}
	public InstantiationNode () {
		
	}
	/*
	 * ������
	 */
	public InstantiationNode (String strIdentifierName, String strIdentifierType, String strInstantiationType1, String strInstantiationType2, boolean isClsInsCreation) {
		this.strIdentifierName = strIdentifierName;
		this.strIdentifierType = strIdentifierType;
		this.strInstantiationType1 = strInstantiationType1;
		this.strInstantiationType2 = strInstantiationType2;
		this.isClassInstanceCreation = isClsInsCreation;
	}
	
	public InstantiationNode (String strIdentifierName, String strIdentifierType, String strInstantiationType1, String strInstantiationType2,  String strInClass,String strInMethod, ASTNode statements, boolean isClsInsCreation ) {
		this.strIdentifierName = strIdentifierName;
		this.strIdentifierType = strIdentifierType;
		this.strInstantiationType1 = strInstantiationType1;
		this.strInstantiationType2 = strInstantiationType2;
		this.strInClass = strInClass;
		this.strInMethod = strInMethod;
		this.statements = statements;
		this.isClassInstanceCreation = isClsInsCreation;
	}
	/*
	 * ����strIdentifierName
	 */
	public String getStrIdentifierName() {
		return strIdentifierName;
	}
	
	/*
	 * ����strIdentifierType
	 */
	public String getStrIdentifierType() {
		return strIdentifierType;
	}
	
	/*
	 * ����strInstantiationType1
	 */
	public String getStrInstantiationType1() {
		return strInstantiationType1;
	}
	
	/*
	 * ����strInstantiationType2
	 */
	public String getStrInstantiationType2() {
		return strInstantiationType2;
	}
	
	/*
	 * ����statements
	 */
	public ASTNode getStatements() {
		return statements;
	}
	
	/*
	 * ����strInClass
	 */
	public String getStrInClass() {
		return strInClass;
	}
	
	/*
	 *���� strInMethod
	 */
	public String getStrInMethod() {
		return strInMethod;
	}
	/*
	 * ��ӡ��Ϣ
	 */
	public void printInstantiationNodeInfo() {
		System.out.println("==============InstantiationNodeInfoPrint==============");
		System.out.println("[strIdentifierName]: " + strIdentifierName);
		System.out.println("[strIdentifierType]: " + strIdentifierType);
		System.out.println("[strInstantiationType1]: " + strInstantiationType1);
		System.out.println("[strInstantiationType2]: " + strInstantiationType2);
		System.out.println("[strInClass]:" + strInClass);
		System.out.println("[strInMethod]:" + strInMethod);
		System.out.println("[statements]:" + statements);
		
	}
}
