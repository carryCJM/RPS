package oo.com.iseu.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @Author Xixi Zhao
 * @Date 2015��10��11��
 * 
 *       �����������String����Ϊlist<String>
 * 
 * @author Jason
 * @date 2015-10-17 ������䱣��ΪASTNode
 * 
 * @author Xixi Zhao
 * @date 2015-10-20 ���ӵ��÷����Ĳ�������
 * 
 * @Author Xixi Zhao
 * @Date 2015��11��30�� ɾ��listMtdIvkParas LastModified:
 *
 * @Author Duan Yulong
 * @Date 2015��12��4�� t��ӳ�Ա����private boolean isThisIvk; private boolean
 *       isSuperIvk; �޸Ĺ����� ��ӷ��� public boolean getIsThisIvk()�� public boolean
 *       getIsSuperIvk()
 *       
 * @author zhangxinyue
 * @Date 20160727 ��ӳ�Ա����isstatic����ӷ���setisstatic()��getisstatic() ����Ƿ��Ǿ�̬��������
 * 
 */
public class MethodIvkNode {
	private String strInClass;
	private String strInMethod;
	private ASTNode astNodeStatement;
	private ASTNode astMtdIvkNode;

	public ASTNode getAstMtdIvkNode() {
		return astMtdIvkNode;
	}

	public void setAstMtdIvkNode(ASTNode astMtdIvkNode) {
		this.astMtdIvkNode = astMtdIvkNode;
	}

	private String strIvkMtdName;
	private String strRetValue;
	private String strInstanceName;
	private String strInstanceType;
	private String strIvkClass;
	private int id;

	private List<String> listAcParas = new ArrayList<String>();
	private List<String> listFmParas = new ArrayList<String>();

	private boolean isThisIvk;
	private boolean isSuperIvk;

	private boolean isStaticIvk;

	/**
	 * @param strInClass
	 *            �������������
	 * @param strInMethod
	 *            ����������ڷ���
	 * @param astNodestatement
	 *            �������
	 * @param strInstanceName
	 *            ���÷�����ʵ����
	 * @param strInstanceType
	 *            ���÷�����ʵ������
	 * @param listAcParas
	 *            ���÷���ʵ��
	 * @param strIvkClass
	 *            �����ñ����÷�������
	 * @param strIvkMtdName
	 *            ���õķ���
	 * @param strRetValue
	 *            ���÷����ķ���ֵ
	 * @param listFmParas
	 *            ���÷����β�
	 */
	public MethodIvkNode(String strInClass, String strInMethod, ASTNode astNodestatement, String strInstanceName,
			String strInstanceType, List<String> listAcParas, String strIvkClass, String strIvkMtdName,
			String strRetValue, List<String> listFmParas, boolean isThisIvk, boolean isSuperIvk, int id) {
		this.strInClass = strInClass;
		this.strInMethod = strInMethod;
		this.astNodeStatement = astNodestatement;
		this.strInstanceName = strInstanceName;
		this.strInstanceType = strInstanceType;
		this.listAcParas = listAcParas;
		this.strIvkMtdName = strIvkMtdName;
		this.strIvkClass = strIvkClass;
		this.strRetValue = strRetValue;
		this.listFmParas = listFmParas;
		this.isThisIvk = isThisIvk;
		this.isSuperIvk = isSuperIvk;
		this.id = id;
	}

	public String getStrRetValue() {
		return strRetValue;
	}
	
	public int getId() {
		return id;
	}

	public boolean getIsThisIvk() {
		return isThisIvk;
	}

	public boolean getIsSuperIvk() {
		return isSuperIvk;
	}

	public String getInClass() {
		return strInClass;
	}

	public String getInMethod() {
		return strInMethod;
	}

	public ASTNode getStatement() {
		return astNodeStatement;
	}

	public String getInstanceName() {
		return strInstanceName;
	}

	public String getInstanceType() {
		return strInstanceType;
	}

	public String getIvkMtdName() {
		return strIvkMtdName;
	}

	public String getStrIvkClass() {
		return strIvkClass;
	}

	public List<String> getListAcParas() {
		return listAcParas;
	}

	public List<String> getListFmParas() {
		return listFmParas;
	}

	public void setIsStaticIvk(boolean isStaticIvk) {
		this.isStaticIvk = isStaticIvk;
	}

	public boolean getIsStaticIvk() {
		return isStaticIvk;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MethodIvkNode [strInClass=" + strInClass + ", strInMethod=" + strInMethod + ", astNodeStatement="
				+ astNodeStatement + ", strIvkMtdName=" + strIvkMtdName + ", strRetValue=" + strRetValue
				+ ", strInstanceName=" + strInstanceName + ", strInstanceType=" + strInstanceType + ", strIvkClass="
				+ strIvkClass + ", listAcParas=" + listAcParas + ", listFmParas=" + listFmParas + ", isThisIvk="
				+ isThisIvk + ", isSuperIvk=" + isSuperIvk + "]";
	}

	public void printMethodIvkInfo() {

		System.out.println("==============MethodInvokePrint==============");
		System.out.println("[In Class]: " + strInClass);
		System.out.println("[In Method]: " + strInMethod);
		// for (int i = 0; i < listStatements.size(); i++) {
		// if (i == 0) {
		// System.out.println("[Invoke Statement]: " + listStatements.get(i));
		// } else {
		// System.out.println("[Upper Statement]:" + listStatements.get(i));
		// }
		// }
		System.out.println("[Invoke Statement]: " + astNodeStatement.toString());
		System.out.println("[Instance Name]: " + strInstanceName);
		System.out.println("[Instance Type]:" + strInstanceType);

		System.out.println("[Actual Parameter]:");
		if (!listAcParas.isEmpty()) {
			for (String strAcPara : listAcParas) {
				String result[] = strAcPara.split("#");
				System.out.println("---> Name: " + result[0] + "      ---> Type: " + result[1] + "    --->isInstance:"
						+ result[2]);
			}
		} else {
			System.out.println("void");
		}
		System.out.println("[Invoke Declared class]:" + strIvkClass);
		System.out.println("[Invoke method]:" + strIvkMtdName);
		System.out.println("[Return value]:" + strRetValue);
		System.out.println("[Formal Parameter]:");
		if (!listFmParas.isEmpty()) {
			for (String strFmPara : listFmParas) {

				System.out.println("---> " + "Type: " + strFmPara);
			}
		} else {
			System.out.println("void");
		}

		System.out.println("==============EndOfMethodInvokePrint=================");
		System.out.println();
		System.out.println();
	}
}
