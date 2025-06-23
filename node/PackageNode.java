/**
 * author: Jason
 * 2015��9��25��
 * TODO
 */
package oo.com.iseu.node;

import java.util.ArrayList;

/**
 * @author Jason ��������: ������ ��������List<ClassNode> �����Ľӿ�List<InterfaceNode>
 *         �����ķ���List<MethodNode> ��ð���������ϵ�İ�List<PackageNode> FilePath
 *
 */
public class PackageNode {
	String strPackageName;
	public ArrayList<ClassNode> listClassMember = new ArrayList<ClassNode>();
	public ArrayList<InterfaceNode> listInterfaceMember = new ArrayList<InterfaceNode>();
	public ArrayList<PackageNode> listPackageConnect = new ArrayList<PackageNode>();
	public ArrayList<EnumNode> listEnumMember = new ArrayList<EnumNode>();
	public String strFilePath = null;

	/**
	 * @return the strPackageName
	 */
	public String getStrPackageName() {
		return strPackageName;
	}

	/**
	 * @param strPackageName
	 *            the strPackageName to set
	 */
	public void setStrPackageName(String strPackageName) {
		this.strPackageName = strPackageName;
	}

	// public void printPackageConnect() {
	// System.out.println("[Package]:" + strPackageName);
	// if (listPackageConnect.isEmpty())
	// System.out.println("Standalone Package.");
	// else {
	// System.out.println("Package Dependece Edge Detected:");
	// for (PackageNode PackageNode : listPackageConnect) {
	// System.out.println("-->" + PackageNode.getStrPackageName());
	// }
	// }
	// // System.out.println("----End of this Package----");
	// }

	@Override
	public String toString() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("[Package] " + strPackageName + "\r\n");

		if (listClassMember.isEmpty()) {
			strBuffer.append("-->[Class] null\r\n");
		} else {
			for (ClassNode classNode : listClassMember) {
				strBuffer.append("-->[Class] " + classNode.getClassName() + "\r\n");
			}
		}

		if (listInterfaceMember.isEmpty()) {
			strBuffer.append("-->[Interface] null\r\n");
		} else {
			for (InterfaceNode interfaceNode : listInterfaceMember) {
				strBuffer.append("-->[Interface] " + interfaceNode.getInterfaceName() + "\r\n");
			}
		}

		return strBuffer.toString();
	}

}
