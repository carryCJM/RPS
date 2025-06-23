/**
 * author: Jason
 * 2015��10��6��
 * TODO
 */
package oo.com.iseu.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import oo.com.iseu.node.ClassNode;
import oo.com.iseu.node.InterfaceNode;
import oo.com.iseu.node.MethodNode;
import oo.com.iseu.node.PackageNode;
import oo.com.iseu.node.ParameterNode;

/**
 * @author Jason
 *
 */
public class SDGHelper {

	public static String strDBPath = "E:/graph.db";

	private static OffsetDateTime previousTime = null;
	private static OffsetDateTime previousTime_slice = null;

	public static boolean isDebug = true;
	public static boolean isDebug_slice = true;
	private static String strinfoPath = "E:/analyseData.dat";
	private static String strSlicePath = "E:/sliceData.dat";

	public static boolean isAdditionalTraverse = true;

	/**
	 * Ϊ�ȶ���ִ��������м�¼
	 * @return boolean
	 */
	
	public static void recordMemoryAndRuntime(String strRecordName) {
		if (!isDebug)
			return;

		long freeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		double freeMemoryMB = freeMemory / 1000.0 / 1024.0;

		System.out.println(strRecordName);
		System.out.println("Memory: " + freeMemoryMB + "MB");

		OffsetDateTime startTime = Instant.now().atOffset(ZoneOffset.ofHours(8));
		System.out.println("Time: " + startTime);
		
//		long minute = 0;
		double seconds = 0.0;
		String lastExeTime = null;
		if (previousTime != null) {
			Duration timeElapsed = Duration.between(previousTime, startTime);
			long minute = timeElapsed.toMillis();
			seconds = minute / 1000.0;
			lastExeTime = String.format("  %14fs\r\n", seconds);
		}
		previousTime = startTime;
		
//		OffsetDateTime instant = OffsetDateTime.parse(startTime.toString());
//		System.out.println("TimeParsed: " + instant);

		try {
			File file = new File(strinfoPath);
			boolean isFileExist = true;
			if (!file.exists()) {
				file.createNewFile();
				isFileExist = false;
			}
			FileOutputStream out = new FileOutputStream(file, true);
			if (isFileExist == false) {
				String strTitle = String.format("%-40s  %15s  %30s  %15s\r\n", "RecordEntry", "Used Memory", "Instant Time", "Execute Time(s)");
				String strTitleLine = String.format("%-40s  %15s  %30s  %15s\r\n", "-----------", "-----------", "------------", "---------------");
				out.write(strTitle.getBytes());
				out.write(strTitleLine.getBytes());
			}

			if (lastExeTime != null)
				out.write(lastExeTime.getBytes());

			String strRecord = null;
			if (strRecordName.equals("Finished")){
				strRecord = String.format("%-40s  %12f MB  %30s  %15s\r\n", strRecordName, freeMemoryMB, startTime.toString(), "---");
				previousTime = null;
			}
			else
				strRecord = String.format("%-40s  %12f MB  %30s", strRecordName, freeMemoryMB, startTime.toString());


			out.write(strRecord.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	/**
	 * @param packageNode
	 * @return
	 */

	/**
	 * @param InterfaceNode
	 * @return

	public static boolean isTwoMtdDecEqual(MethodNode methodNode1, MethodNode methodNode2) {
		// Step1:�ж��������������Ƿ����
		if (methodNode1.strMethodName.equals(methodNode2.strMethodName)) {

			// Step2:�жϲ����б��Ƿ�Ϊ��
			if (methodNode1.listParameterTypes == null) {
				if (methodNode2.listParameterTypes == null) {
					return true;
				}
			} else {
				// Step3:�ж������������������Ƿ����
				if (methodNode2.listParameterTypes == null) {
					return false;
				}
				int iMth1Size = methodNode1.listParameterTypes.size();
				int iMth2Size = methodNode2.listParameterTypes.size();
				if (iMth1Size == iMth2Size) {
					boolean isEqual = true;
					// Step4:�ж϶�Ӧ�����Ƿ����
					for (int i = 0; i < iMth1Size; i++) {
						if (methodNode1.listParameterTypes.get(i).parameterType == null
								|| methodNode2.listParameterTypes.get(i).parameterType == null) {
							return false;
						}
						if (!methodNode1.listParameterTypes.get(i).parameterType.equals(methodNode2.listParameterTypes
								.get(i).parameterType)) {
							isEqual = false;
							break;
						}
					}
					if (isEqual) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// �ҷ������õ�MethodEntryNode(Xixi Zhao)
	public static boolean findBindMethodEntry(String stIvkMtdName, List<String> listIvkMtdParas, MethodNode methodNode) {
		boolean find = false;
		if (stIvkMtdName.equals(methodNode.strMethodName)) {
			if (listIvkMtdParas.isEmpty() && methodNode.listParameterTypes == null) {
				return true;
			} else if (listIvkMtdParas.isEmpty() == false && methodNode.listParameterTypes != null) {
				if (listIvkMtdParas.size() == methodNode.listParameterTypes.size()) {
					for (int i = 0; i < methodNode.listParameterTypes.size(); i++) {
						String ivkParaType = listIvkMtdParas.get(i);
						ParameterNode paraType = methodNode.listParameterTypes.get(i);
						if(paraType.isGeneric()) {
							continue;
						}
						if (ivkParaType != null && paraType.getParameterType() != null
								&& ivkParaType.equals(paraType.getParameterType())) {
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
	 * @param strPrjPath
	 */
	public static void recordProjectInfo(String strPrjPath) {
		if (!isDebug)
			return;
		try {
			File file = new File(strinfoPath);
			boolean isFileExist = true;
			if (!file.exists()) {
				file.createNewFile();
				isFileExist = false;
			}
			FileOutputStream out = new FileOutputStream(file, true);
			if (isFileExist == false) {
				String strTitle = String.format("%-40s  %15s  %30s  %15s\r\n", "RecordEntry", "Used Memory", "Instant Time", "Execute Time(s)");
//				String strTitleLine = String.format("%-40s  %15s  %30s  %15s\r\n", "-----------", "-----------", "------------", "---------------");
				out.write(strTitle.getBytes());
//				out.write(strTitleLine.getBytes());
			}

			String strProjectTitle = "Project: " + strPrjPath + "\r\n";
			String strLine = "------------\r\n";

			out.write(strLine.getBytes());
			out.write(strProjectTitle.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param strPrjPath
	 */
	public static void recordSliceInfo(String strPrjPath) {
		if (!isDebug_slice)
			return;
		try {
			File file = new File(strSlicePath);
			boolean isFileExist = true;
			if (!file.exists()) {
				file.createNewFile();
				isFileExist = false;
			}
			FileOutputStream out = new FileOutputStream(file, true);
			if (isFileExist == false) {
				String strTitle = String.format("%-40s  %15s  %30s  %15s\r\n", "RecordEntry", "Used Memory", "Instant Time", "Execute Time(s)");
//					String strTitleLine = String.format("%-40s  %15s  %30s  %15s\r\n", "-----------", "-----------", "------------", "---------------");
				out.write(strTitle.getBytes());
//					out.write(strTitleLine.getBytes());
			}

			String strProjectTitle = "Project: " + strPrjPath + "\r\n";
			String strLine = "------------\r\n";

			out.write(strLine.getBytes());
			out.write(strProjectTitle.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void recordMemoryAndRuntimeForSlice(String strRecordName) {
		if (!isDebug_slice)
			return;

		long freeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		double freeMemoryMB = freeMemory / 1000.0 / 1024.0;

		System.out.println(strRecordName);
		System.out.println("Memory: " + freeMemoryMB + "MB");

		OffsetDateTime startTime = Instant.now().atOffset(ZoneOffset.ofHours(8));
		System.out.println("Time: " + startTime);

//		long minute = 0;
		double seconds = 0.0;
		String lastExeTime = null;
		if (previousTime_slice != null) {
			Duration timeElapsed = Duration.between(previousTime_slice, startTime);
			long minute = timeElapsed.toMillis();
			seconds = minute / 1000.0;
			lastExeTime = String.format("  %14fs\r\n", seconds);
		}
		previousTime_slice = startTime;

//		OffsetDateTime instant = OffsetDateTime.parse(startTime.toString());
//		System.out.println("TimeParsed: " + instant);

		try {
			File file = new File(strSlicePath);
			boolean isFileExist = true;
			if (!file.exists()) {
				file.createNewFile();
				isFileExist = false;
			}
			FileOutputStream out = new FileOutputStream(file, true);
			if (isFileExist == false) {
				String strTitle = String.format("%-40s  %15s  %30s  %15s\r\n", "RecordEntry", "Used Memory", "Instant Time", "Execute Time(s)");
				String strTitleLine = String.format("%-40s  %15s  %30s  %15s\r\n", "-----------", "-----------", "------------", "---------------");
				out.write(strTitle.getBytes());
				out.write(strTitleLine.getBytes());
			}

			if (lastExeTime != null)
				out.write(lastExeTime.getBytes());

			String strRecord = null;
			if (strRecordName.equals("Finished")) {
				strRecord = String.format("%-40s  %12f MB  %30s  %15s\r\n", strRecordName, freeMemoryMB, startTime.toString(), "---");
				previousTime_slice = null;
			} else
				strRecord = String.format("%-40s  %12f MB  %30s", strRecordName, freeMemoryMB, startTime.toString());

			out.write(strRecord.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
