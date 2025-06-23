package oo.com.iseu.node;

import java.util.ArrayList;

/**
 *
 * LastModified:
 *
 * @Author Xixi Zhao
 * @Date 2015��11��3��
 * 
 *       ö������
 * 
 */
public class EnumNode extends ClassNode {
	public ArrayList<EnumConstantNode> listEnumConstant = new ArrayList<EnumConstantNode>();

	public class EnumConstantNode {
		public String strModifiers;
		public String strEnumCstName;
		public int startLine = -1;
		public String strFilePath = null;
		public ArrayList<Object> listArguments = new ArrayList<>();

		@Override
		public String toString() {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("--->[EnumConstantName] " + strEnumCstName + "\r\n");
			stringBuffer.append("----->[EnumConstantModifier] " + strModifiers + "\r\n");
			stringBuffer.append("----->[Arguements] " + "\r\n");
			if (listArguments.isEmpty()) {
				stringBuffer.append("----->" + null + "\r\n");
			} else {
				for (Object object : listArguments) {
					stringBuffer.append("------->" + object + "\r\n");
				}
			}
			return stringBuffer.toString();
		}
	}

	@Override
	public String toString() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("==========================���´�ӡ��EnumNode��toString������===========================" + "\r\n");
		strBuffer.append("[Enum] " + strClassName + "\r\n");

		strBuffer.append("-->[Implements Interface] \r\n");
		if (listInterface.isEmpty()) {
			strBuffer.append("----> 0 interface implemented\r\n");
		} else {
			for (InterfaceNode interfaceNode : listInterface) {
				strBuffer.append("----> " + interfaceNode.getInterfaceName() + "\r\n");
			}
		}

		strBuffer.append("-->[Enum Constatnt] \r\n");
		if (listEnumConstant.isEmpty()) {
			strBuffer.append("----> 0 method declared in this class\r\n");
		} else {
			for (EnumNode.EnumConstantNode enumConstantNode : listEnumConstant) {
				strBuffer.append(enumConstantNode.toString());
			}
		}

		strBuffer.append("-->[Declaring Method] \r\n");
		if (listMethod.isEmpty()) {
			strBuffer.append("----> 0 method declared in this class\r\n");
		} else {
			for (MethodNode methodNode : listMethod) {
				strBuffer.append(methodNode);
			}
		}

		strBuffer.append("-->[Declaring Field] \r\n");
		if (listField.isEmpty()) {
			strBuffer.append("----> 0 field declared in this class\r\n");
		} else {
			for (FieldNode fieldNode : listField) {
				strBuffer.append(fieldNode);
			}
		}
		return strBuffer.toString();
	}
}