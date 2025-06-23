package oo.com.iseu.slicer.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.neo4j.graphdb.Node;

import oo.com.iseu.helper.ASTHelper;

public class SliceStorage {
	
	public  String SliceStoreRootFilePath;           							//���̴洢��Ŀ¼
	public  HashMap<String, TreeSet<Integer>> slices; 							//��Ƭ��

	
	public SliceStorage(String rootFilePath) {
		SliceStoreRootFilePath = rootFilePath;                           			 //��Ƭ�洢��·��                                           					//���ݿ�                             
		slices = new HashMap<>();
	}
	
	
	public void sliceHandle(Set<Node> nodes) {
		for (Node node : nodes) {
//			System.out.println(node+"\t"+node.hasProperty("FilePath")+"\t"+node.hasProperty("Startline"));
			if (node.hasProperty("FilePath")&&node.hasProperty("Startline")) {
				String filePath = (String)node.getProperty("FilePath");
				Integer startLine = Integer.valueOf((String) node.getProperty("Startline"));
//				System.out.println(node.getId()+":\t"+startLine);
				if (startLine<=0) {
					continue;
				}
				if (slices.containsKey(filePath)) {
					slices.get(filePath).add(startLine);
				}
				else{
					TreeSet<Integer> fileLineSet = new TreeSet<>();
					fileLineSet.add(startLine);
					slices.put(filePath, fileLineSet);
				}
			}
		}
		slicesStorage();
	}
	
	public HashMap<String, TreeSet<Integer>> getSlice(Set<Node> nodes) {
		for (Node node : nodes) {
//			System.out.println(node+"\t"+node.hasProperty("FilePath")+"\t"+node.hasProperty("Startline"));
			if (node.hasProperty("FilePath")&&node.hasProperty("Startline")) {
				String filePath = (String)node.getProperty("FilePath");
				Integer startLine = Integer.valueOf((String) node.getProperty("Startline"));
//				System.out.println(node.getId()+":\t"+startLine);
				if (startLine<=0) {
					continue;
				}
				if (slices.containsKey(filePath)) {
					slices.get(filePath).add(startLine);
				}
				else{
					TreeSet<Integer> fileLineSet = new TreeSet<>();
					fileLineSet.add(startLine);
					slices.put(filePath, fileLineSet);
				}
			}
		}
		return slices;
	}
	
	
	/**
	 * ��Ƭ�洢
	 */
	public void slicesStorage() {
		System.out.println("StoreStart...");
		Set<Entry<String, TreeSet<Integer>>> slicesSet = slices.entrySet();
		long slicesNumber = 0;
		for (Entry<String, TreeSet<Integer>> entry : slicesSet) {
			String filePath = entry.getKey();
			
			File file = new File(SliceStoreRootFilePath);
			if(!file.isDirectory()){
				ASTHelper.RecordLog(this.getClass().getName(), "Ŀ¼�����ڣ�");

				//				System.err.println("Ŀ¼�����ڣ�");
				return ;
			}
			int startIndex = filePath.indexOf(':')+1;
			//��洢���ļ�·��
			String newFilePath = SliceStoreRootFilePath+filePath.substring(startIndex);
			int desFileIndex = newFilePath.lastIndexOf('\\');
			//��洢��Ŀ¼·��
			String newDirPath = newFilePath.substring(0, desFileIndex);
			//Ŀ¼����
			File dir = new File(newDirPath);
			makeDir(dir);
			//������ļ�
			System.out.println("Storing File: " + newFilePath);
			sliceOutputToFile(entry,filePath,newFilePath);
			slicesNumber += entry.getValue().size();
		}
		System.out.println("Store Finished.");
		System.out.println("Total:"+slicesNumber);
	}
	
	/**
	 * ��Դ�ļ��ж�ȡ�е�����䣬�������洢��Ŀ���ļ���
	 * @param entry
	 * @param srcFilePath   ��    Դ�ļ�
	 * @param desFilePath   ��    Ŀ���ļ�
	 */
	public void sliceOutputToFile(Entry<String, TreeSet<Integer>> entry,String srcFilePath,String desFilePath) {
		//�ļ�����
		File file = new File(desFilePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			PrintWriter printWriter = new PrintWriter(file);
			BufferedReader srcFileReader = new BufferedReader(
										   new InputStreamReader(
										   new FileInputStream(
										   new File(srcFilePath))));
			TreeSet<Integer> fileSlices = entry.getValue();
			int lastLineNumber = entry.getValue().last();
			
		/*	System.out.println("�кţ�");
			for(int startLine : entry.getValue()) {
				System.out.println(startLine);
			}
			System.out.println("���룺");*/
			for(int i=1;i<=lastLineNumber;++i){
				if (fileSlices.contains(i)) {
					try {
						String line = srcFileReader.readLine();
						printWriter.println(i+":"+line);
						printWriter.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					try {
						String line = srcFileReader.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			printWriter.close();
			srcFileReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * �ݹ鴴��Ŀ¼
	 * @param file �� ��׶�Ŀ¼
	 */
	public void makeDir(File file){
		if(file.getParentFile().exists()){
		   file.mkdir();
		}
		else{
		   makeDir(file.getParentFile());
		   file.mkdir();
	    }
	}
}
