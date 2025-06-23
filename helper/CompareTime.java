package oo.com.iseu.helper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class CompareTime {
	
	
	public static ArrayList<ArrayList<String>> compareTime(String projectPath) {
		ArrayList<ArrayList<String>> alModifiledFilesName = new ArrayList<>();
//		String projectPath = "G:/EclipseWorkspace/OOTestCase_Final/src/Polymorphic";
		File directory = new File(projectPath);
		ArrayList<File> directoryFiles = new ArrayList<>();
		directoryFiles.add(directory);
		Calendar cal = Calendar.getInstance();
		Map<String, String> alLastModifiedTime = ASTHelper.alFileLastModifiledTime;
		ArrayList<String> alAbsolutePath = new ArrayList<>();
		for(String key : alLastModifiedTime.keySet()) {
			alAbsolutePath.add(key);
		}
		ArrayList<String> delFiles = new ArrayList<>();
		ArrayList<String> addFiles = new ArrayList<>();
		ArrayList<String> modifiedFiles = new ArrayList<>();
		while (!directoryFiles.isEmpty()) {
			File direct = directoryFiles.get(0);
			directoryFiles.remove(0);
			File[] files = direct.listFiles();
			if (files == null) {
				return null;
			}
			outer:
			for (File file : files) {
				if (file.isDirectory()) {
					directoryFiles.add(file);
				} else if (file.getPath().contains(".java")) {
					boolean isFind = false;
					long time = file.lastModified();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					cal.setTimeInMillis(time);
					String lastModifiedTime = formatter.format(cal.getTime());
					String absolutePath = file.getAbsolutePath();
					//����alLastModifiedTime
					for(String key : alLastModifiedTime.keySet()) {
						//�ҵ�ƥ���ļ�
						if(key.equals(absolutePath)) {
							isFind = true;
							if(alLastModifiedTime.get(key) != null && !alLastModifiedTime.get(key).isEmpty()) {
								String modifiedTime = alLastModifiedTime.get(key);
								//��alAbsolutePathɾ������
								for(int i = 0; i<alAbsolutePath.size(); i++) {
									if(alAbsolutePath.get(i).equals(key)) {
										alAbsolutePath.remove(i);
									}
								}
								//�ж��Ƿ��޸ģ�����޸ļ��뵽modifiedFiles����alAbsolutePath��ɾ��key
								if(!modifiedTime.equals(lastModifiedTime)) {
									modifiedFiles.add(file.getAbsolutePath());
								} 
							}
							continue outer;
						}
					}
					if(!isFind) {
						addFiles.add(file.getAbsolutePath());
					}
				} else if (file.getPath().endsWith(".class")) {

					boolean isFind = false;
					long time = file.lastModified();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					cal.setTimeInMillis(time);
					String lastModifiedTime = formatter.format(cal.getTime());
					String absolutePath = file.getAbsolutePath();
					//����alLastModifiedTime
					for(String key : alLastModifiedTime.keySet()) {
						//�ҵ�ƥ���ļ�
						if(key.equals(absolutePath)) {
							isFind = true;
							if(alLastModifiedTime.get(key) != null && !alLastModifiedTime.get(key).isEmpty()) {
								String modifiedTime = alLastModifiedTime.get(key);
								//��alAbsolutePathɾ������
								for(int i = 0; i<alAbsolutePath.size(); i++) {
									if(alAbsolutePath.get(i).equals(key)) {
										alAbsolutePath.remove(i);
									}
								}
								//�ж��Ƿ��޸ģ�����޸ļ��뵽modifiedFiles����alAbsolutePath��ɾ��key
								if(!modifiedTime.equals(lastModifiedTime)) {
									modifiedFiles.add(file.getAbsolutePath());
								}
							}
							continue outer;
						}
					}
					if(!isFind) {
						addFiles.add(file.getAbsolutePath());
					}
				}
			}
		}
		if(!alAbsolutePath.isEmpty()) {
			for(String delAbsolutePath : alAbsolutePath) {
				delFiles.add(delAbsolutePath);
			}
		}
		//�������
		for(String file : delFiles) {
			System.out.println("ɾ���ļ�: " + file);
		}
		for(String file : addFiles) {
			System.out.println("����ļ�: " + file);
		}
		for(String file : modifiedFiles) {
			System.out.println("�޸��ļ�: " + file);
		}
		System.out.println("�ļ���ӡ��޸ġ�ɾ��������" );
		alModifiledFilesName.add(modifiedFiles);
		alModifiledFilesName.add(addFiles);
		alModifiledFilesName.add(delFiles);
		return alModifiledFilesName;
	}	
}
