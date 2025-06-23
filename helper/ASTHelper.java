package oo.com.iseu.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;

import oo.com.iseu.node.ClassNode;
import oo.com.iseu.node.EnumNode;
import oo.com.iseu.node.GenericityNode;
import oo.com.iseu.node.InterfaceNode;
import oo.com.iseu.node.MethodNode;
import oo.com.iseu.node.PackageNode;
import oo.com.iseu.node.ParameterNode;

/**
 * 
 * @author Jason Helper: �ṩһϵ�о�̬����
 *
 */

public class ASTHelper {

	public static ArrayList<PackageNode> alPackage = new ArrayList<PackageNode>();
	public static ArrayList<ClassNode> alClass = new ArrayList<ClassNode>();
	public static ArrayList<InterfaceNode> alInterface = new ArrayList<InterfaceNode>();
	public static ArrayList<EnumNode> alEnum = new ArrayList<EnumNode>();
	public static ArrayList<GenericityNode> alGenericityNode = new ArrayList<>();

	// �����ļ���·���Լ������޸�ʱ��
	public static Map<String, String> alFileLastModifiledTime = new HashMap<String, String>();

	public static int processValue;
	public static String cfgPath;


	/**
	 * Ϊ�ȶ���ִ��������м�¼
	 * 
	 * @return boolean
	 */
	public static boolean releaseStaticDataForASTHelper() {
		try {
			alPackage.clear();
			alClass.clear();
			alInterface.clear();
			alEnum.clear();
			alGenericityNode.clear();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param node
	 * @return node����ʼ�к� ͨ��ASTNode�������뵥Ԫ�����ظĽڵ��ʼ�к� LastModifyTime: 2015-9-25
	 */
	public static int helperGetLineNumber(ASTNode node) {
		CompilationUnit unit = (CompilationUnit) node.getRoot();
		return unit.getLineNumber(node.getStartPosition());
	}


	/**
	 * @param mtdParas1
	 * @param mtdParas2
	 * @return
	 */
	private static boolean isParaEqual(List<ParameterNode> mtdParas1, List<ParameterNode> mtdParas2) {
		// TODO Auto-generated method stub
		try {
			if (mtdParas1 == null && mtdParas2 == null) {
				return true;
			} else if (mtdParas1 != null && mtdParas2 != null) {
				if (mtdParas1.size() != mtdParas2.size())
					return false;
				else {
					for (int i = 0; i < mtdParas1.size(); i++) {
						if (mtdParas1.get(i).parameterType.equals(mtdParas2.get(i).parameterType))
							continue;
						else
							return false;
					}
					return true;
				}
			}
			return false;
		} catch (NullPointerException e) {
			return false;
		}
	}

	/**
	 * ��ӡalPackage�д洢�����а� LastModifyTime��2015-9-25
	 */
	public static void printPkg() {
		System.out.println("----[Package]----");
		for (PackageNode PackageNode : ASTHelper.alPackage) {
			System.out.println(PackageNode.getStrPackageName());
		}
		System.out.println("----[Package Print End]----");
		System.out.println();
	}

	/**
	 * @param strInterfaceName
	 *            ���������ҵ���interface��alInterface�е�λ��
	 * @return λ��index���Ҳ���-1 LastModifyTime��2015-10-4
	 */
	public static int findInterfaceByName(String strInterfaceName) {
		for (int i = 0; i < ASTHelper.alInterface.size(); i++) {
			if (ASTHelper.alInterface.get(i).getInterfaceName().equals(strInterfaceName)) {
				return i;
			}
		}
		return -1;
	}

	public static int findClassByName(String strClassName) {
		for (int i = 0; i < ASTHelper.alClass.size(); i++) {
			if (ASTHelper.alClass.get(i).getClassName().equals(strClassName)) {
				return i;
			}
		}
		return -1;
	}

	public static int findEnumByName(String strEnumName) {
		for (int i = 0; i < ASTHelper.alEnum.size(); i++) {
			if (ASTHelper.alEnum.get(i).getClassName().equals(strEnumName)) {
				return i;
			}
		}
		return -1;
	}

	public static int findGenericByName(String strGenericName) {
		for (int i = 0; i < ASTHelper.alGenericityNode.size(); i++) {
			if (ASTHelper.alGenericityNode.get(i).getGenericityName().equals(strGenericName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @param String
	 *            pkgName ���ݰ����ҵ�pkg��alPackage�е�λ��
	 * @return λ��index���Ҳ���-1 LastModifyTime��2015-10-3
	 */
	public static int findPackageByName(String pkgName) {
		for (int i = 0; i < ASTHelper.alPackage.size(); i++) {
			if (ASTHelper.alPackage.get(i).getStrPackageName().equals(pkgName)) {
				return i;
			}
		}
		ASTHelper.RecordLog(ASTHelper.class.getName(), "Find Package " + pkgName + " Failed!");
		return -1;
	}

	// //���ҷ�����PrDG
	// public static PrDG findMtdPrdg(String className, String methodName,
	// List<ParameterNode> mtdParas) {
	// for (MtdPrdg m : ASTHelper.alMtdPrdg) {
	// if (className.equals(m.className) && methodName.equals(m.methodName)) {
	// if (isParaEqual(m.mtdParas, mtdParas))
	// return m.prDG;
	// }
	// }
	// return null;
	// }
	//
	//

	// �ж�simpleName�Ƿ�Ϊ����
	public static boolean isArrayNode(SimpleName node) {
//		boolean flag = false;
//		ASTNode parentNode = node.getParent();
//		if (parentNode instanceof ArrayAccess) {
//			if (((ArrayAccess) parentNode).getArray().equals(node)) {
//				flag = true;
//			}
//
//		}
		ITypeBinding iTypeBinding = node.resolveTypeBinding();
		if (iTypeBinding != null) {
			// ɨ�����г�Ա�����ڷ����е�ʹ�õ�simplename�ڵ�
			if(iTypeBinding.getName() != null && iTypeBinding.getName().contains("[]")) {
				return true;
			}
			if(iTypeBinding.getQualifiedName() != null && iTypeBinding.getQualifiedName().contains("[]")) {
				return true;
			}
			if(iTypeBinding.toString() != null && iTypeBinding.toString().contains("[]")) {
				return true;
			}
		}
		return false;

	}

	// �ж�simpleName�Ƿ�Ϊ�±�Ϊ����������
	public static boolean isNumberIndexArrayNode(ASTNode node) {
		boolean flag = false;
		ASTNode parentNode = node.getParent();
		if (parentNode instanceof ArrayAccess) {
			if (((ArrayAccess) parentNode).getArray().equals(node)) {
				ASTNode indexNode = ((ArrayAccess) parentNode).getIndex();
				if (indexNode instanceof NumberLiteral) {
					return true;
				}
			}
		}
		return flag;

	}

	// �ҷ�������������ڵ�Method(Xixi Zhao)
	public static boolean findMethodNode(String strInMethod, List<String> listInMethodParas, MethodNode methodNode) {
		boolean find = false;
		if (strInMethod != null && strInMethod.equals(methodNode.strMethodName)) {
			if (listInMethodParas.isEmpty() && methodNode.listParameterTypes == null) {
				return true;
			} else if (listInMethodParas.isEmpty() == false && methodNode.listParameterTypes != null) {
				if (listInMethodParas.size() == methodNode.listParameterTypes.size()) {
					for (int i = 0; i < methodNode.listParameterTypes.size(); i++) {
						String ivkParaType = listInMethodParas.get(i);
						ParameterNode paraType = methodNode.listParameterTypes.get(i);
						if (paraType.getParameterType() != null && ivkParaType != null
								&& paraType.getParameterType().equals(ivkParaType)) {
							continue;
						} else {
							return false;
						}
					}
					find = true;
				}
			}
		}
		return find;
	}

	/**
	 * @param name
	 * @param string
	 * @throws IOException
	 * @throws SecurityException
	 */
	public static void RecordLog(String name, String strErrorMsg) {
		RecordLog(name, strErrorMsg, Level.ALL);
	}

	public static void RecordLog(String name, String strErrorMsg, Level level) {
		if (!SDGHelper.isDebug)
			return;

		try {
			Logger logger = Logger.getLogger(name);
			FileHandler fileHandler = new FileHandler(".\\LogRecord\\Log.log");
			fileHandler.setLevel(level);
//			logger.addHandler(fileHandler);

//			logger.info(strErrorMsg);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// System.err.println("SecurityExceptionLogError" + e.getMessage());
		} catch (IOException e) {
			// e.printStackTrace();
			// TODO Auto-generated catch block
			// System.err.println("IOExceptionLogError" + e.getMessage());
		}
	}
}
