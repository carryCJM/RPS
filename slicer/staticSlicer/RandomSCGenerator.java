/**
 * author: Jason
 * 2016��10��25��
 * TODO
 */
package oo.com.iseu.slicer.staticSlicer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import oo.com.iseu.slicer.helper.SlicerHelper;
import oo.storage.NEO4JAccess;

/**
 * @author Jason
 *
 */
public class RandomSCGenerator {
	String strRootPath;
	List<RandomSliceCriterion> sc = new ArrayList<>();
	Map<String, Integer> info = new HashMap<>();

	/**
	 * generator constructor
	 */
	public RandomSCGenerator(String strFilePath) {
		// TODO Auto-generated constructor stub
		strRootPath = strFilePath;
		listNext(new File(strFilePath), 0);
		sc.clear();
	}

	public void generate(int times) {
		Random rand = new Random(47);
		List<String> fileList = new ArrayList<>(info.keySet());
		int fileNums = info.size();

		for (int i = 0; i < times; i++) {
			int chosenFile = rand.nextInt(fileNums);
			String key = fileList.get(chosenFile);
			//��ֹ����0
			int chosenLine = rand.nextInt(info.get(key) - 1) + 1;
			RandomSliceCriterion curSC = new RandomSliceCriterion(chosenLine, key);
			sc.add(curSC);
		}
	}

	public void startProcess() {
		String DB_PATH = NEO4JAccess.DB_PATH;

		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);// ���ݿⴴ�����ȡ

		Direction direction = Direction.INCOMING;

		for (int i = 0; i < sc.size(); i++) {
			String outputPath = strRootPath;
			String[] strVar = {};
			Set<Node> setResult = slicerDBQuery.getNodesFromSC(strVar, db, sc.get(i).strFilePath, sc.get(i).line, strRootPath, direction);
			SlicerHelper.StoreSlice(setResult, outputPath, db);
		}
	}

	/**
	 * @param curLine
	 * @return
	 */
	private RandomSliceCriterion calcuate(int curLine) {
		// TODO Auto-generated method stub
		return null;
	}

	private class RandomSliceCriterion {
		int line;
		String strFilePath;

		/**
		 * @param line
		 * @param strFilePath
		 */
		public RandomSliceCriterion(int line, String strFilePath) {
			super();
			this.line = line;
			this.strFilePath = strFilePath;
		}

	}

	/**
	 * ��ȡ�ļ�����java�ļ�
	 * @param dir
	 */
	private long listNext(File dir, long iCount) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			//�ж��Ƿ����ļ��У�������ļ��м������¼���
			if (files[i].isDirectory()) {
				iCount = listNext(files[i], iCount);
			} else {
				try {
					if (files[i].getName().endsWith(".java")) {
						//						System.out.println(files[i].getAbsolutePath());
						long curCount = iCount;
						iCount = javaLine(files[i], iCount);
						int line = (int) (iCount - curCount);
						info.put(files[i].getAbsolutePath(), line);
						//						System.out.println(files[i].getAbsolutePath() + " " + iCount);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return iCount;
	}

	/**
	  * ��ȡjava�ļ��������������������հ�������ע������
	  * @param f
	  * @throws IOException 
	  * @throws FileNotFoundException 
	  */
	private long javaLine(File f, long iCount) throws FileNotFoundException, IOException {
		InputStream input = new FileInputStream(f);
		BufferedReader b = new BufferedReader(new InputStreamReader(input));
		String value = b.readLine();
		if (value != null)
			while (value != null) {
				iCount++;
				value = b.readLine();
			}
		b.close();
		input.close();
		return iCount;
	}

	/**
	  * ��java�ļ����ַ�������ʽ��ȡ
	  * @param f
	  * @return
	  * @throws FileNotFoundException
	  * @throws IOException
	  */
	private String fromFile(File f) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(f);
		byte[] b = new byte[(int) f.length()];
		fis.read(b);
		fis.close();
		return new String(b);
	}
}
