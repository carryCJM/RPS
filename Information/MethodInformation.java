package oo.com.iseu.Information;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;

public class MethodInformation implements Serializable{
	private BitSet memObjs;        //�Ƿ�Ե��ö�������޸�
	private int paraChange;        //��ÿһ���������޸������ÿһ��bitΪ�������Ӧindex�������޸������0��ʾ���޸ģ�1��ʾ�޸ģ���ʼȫΪ0
	private int checkTable;          //��¼�����б�������޸������0��ʾ���ý������޸ģ�1��ʾû���޸ģ���ʼȫΪ1
	public MethodInformation() {
		super();
		memObjs = new BitSet();
		this.paraChange = 0;
		this.checkTable = -1;
	}
	//����ı��������
	public boolean isObjChange() {
		return !memObjs.isEmpty();
	}
	
	public void setObjChange(int index) {
		if(index>=0){
			memObjs.set(index);
		}
	}
	
	public boolean isNeedAdd(ArrayList<Integer> indices) {
		for (Integer integer : indices) {
			//����û�м�¼��
			if (integer>=0&&!memObjs.get(integer)) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Integer> getMemIndex() {
		ArrayList<Integer> indices = new ArrayList<>();
		String []strs = memObjs.toString().split("[,}{ ]");
		for (String str : strs) {
			str = str.trim();
			if (!str.equals("")) {
				indices.add(Integer.valueOf(str));
			}	
		}
		return indices;
	}
	
	//�����ı��������
	/**
	 * index �Ų����Ƿ��޸�
	 * @param index
	 * @return
	 */
	public boolean isParameterChange(int index) {
		if (index<0||index>31) {
			return false;
		}
		return ((paraChange&(1<<index))&(checkTable)&(1<<index))>0;
	}

	/**
	 * ���ò����޸������index�Ų����޸�
	 * @param index
	 */
	public void parameterChange(int index) {
		if (index<0||index>31) {
			return ;
		}
		paraChange = (paraChange|(1<<index));
	}
	
	/**
	 * ��������Ƿ��޸�
	 * @return
	 */
	public boolean isAnyParaChange() {
		return (paraChange&checkTable)>0;
	}

	/**
	 * ��ȡ������¼ֵ
	 * @return
	 */
	public int getParaChange() {
		return paraChange;
	}
	
	//���������������
	 /**
	  * �������õ�����index�Ų�����������
	  * @param index
	  */
	public void checkTableAdjust(int index) {
		if (index<0||index>31) {
			return ;
		}
		checkTable = checkTable&(~(1<<index));
	}
	
	/**
	 * ����checkTable��index���Ƿ�Ϊ1��trueΪ����û������
	 * @param index
	 * @return
	 */
	public boolean isCheckTableOk(int index) {
		if (index<0||index>31) {
			return false;
		}
		return (checkTable&(1<<index))>0;
	}
	

	public int getCheckTable() {
		return checkTable;
	}

	public void setCheckTable(int checkTable) {
		this.checkTable = checkTable;
	}

	
	
	
	@Override
	public String toString() {
		return "isObjChange:"+memObjs+"\n"+"para:"+paraChange+"\n"+"checkTable:"+checkTable+"\n";
	}
}
