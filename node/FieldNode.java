package oo.com.iseu.node;

/**
 * @author Jason
 * ��������: ��Ա��������
		��������
		�������η�
		������ʼֵ
		FilePath
		startLine
 */
public class FieldNode {
	public String strFieldName;
	public String strFieldType;
	public String strFieldModifiers;
	public String strFieldValue;
	public String strFieldValueType;
	public String strInClass;
	public String strFilePath = null;
	public int startLine = -1;
	

	/**
	 * @param strFieldName
	 * @param strFieldType
	 * @param strFieldModifiers
	 * @param strFieldValue
	 */
	public FieldNode(String strFieldName, String strFieldType, String strFieldModifiers, String strFieldValue, String strFieldValueType) {
		this.strFieldName = strFieldName;
		this.strFieldType = strFieldType;
		this.strFieldModifiers = strFieldModifiers;
		this.strFieldValue = strFieldValue;
		this.strFieldValueType = strFieldValueType;
	}
	
	
	public void printFieldInfo(){
		System.out.println("------FieldInfomationPrint------");
		System.out.println("[Name] " + strFieldName);
		System.out.println("[Type] " + strFieldType);
		System.out.println("[Modifier] " + strFieldModifiers);
		System.out.println("[Initializer] " + strFieldValue);
		System.out.println("------EndofFieldInfoPrint------");
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("---->[Field] " + strFieldName + "\r\n");
		stringBuffer.append("---->[FieldInClass] " + strInClass + "\r\n");
		stringBuffer.append("------>[Type] " + strFieldType + "\r\n");
		stringBuffer.append("------>[Modifier] " + strFieldModifiers + "\r\n");
		stringBuffer.append("------>[Initializer] " + strFieldValue + "\r\n");
		stringBuffer.append("------>[InitializerType] " + strFieldValueType + "\r\n");
		return stringBuffer.toString();
	}

}
