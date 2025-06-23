package oo.com.iseu.Invoke;

import java.util.HashMap;
import java.util.TreeSet;

import oo.com.iseu.UI.Basic.SliceDirection;
import oo.com.iseu.slicer.staticSlicer.slicerDBQuery;


public class Invoke {
	
	
	private static String String(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String SlicesRepository = System.getProperty("user.dir")+"\\Repository\\Slices";
	public static String GraphsRepository = System.getProperty("user.dir")+"\\Repository\\Graphs";
	public static String CFGRepository = System.getProperty("user.dir")+"\\Repository\\CFGs";
	public static String TraceRepository = System.getProperty("user.dir")+"\\Repository\\Traces";
	
	public  HashMap<String, TreeSet<Integer>> OOStaticSlicing(String sdgPath,String filePath,int lineNumber,String vars[],SliceDirection sliceDirection) {
		return slicerDBQuery.ooStaticSliceByStmt(sdgPath, filePath,lineNumber,vars, sliceDirection);
	}
	
	/**
	 * �������̬��Ƭ
	 * @param sdgPath ��sdg����·��
	 * @param tracePath �����й켣�ļ�
	 * @param filePath ���ļ�
	 * @param lineNumber ���к�
	 * @param sliceDirection ����Ƭ����
	 * @return ��Ƭ��
	 */
	
}
