package oo.com.iseu.UI.Basic;



import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;
/**
 *���ļ�ϵͳ��ָ��Ŀ¼�е���Ŀ¼���ļ�ת��Ϊ���ṹ
 */
public class DirectoryToTree{
    private DefaultMutableTreeNode root=null;
    private File objectFile=null;
    public static boolean isJavaProject;
    /**
     *ʹ��ָ����Ŀ¼��ʼ�����캯������Ŀ¼ָ��ת��Ϊ���ṹ��Ŀ¼
     */
    public DirectoryToTree(File dir){
    	isJavaProject = false;
        if(dir.isDirectory()){
                root=new DefaultMutableTreeNode(dir.getName());
                objectFile=dir;
        }
        else{
            System.out.println("The parameter you given is not a directory,the program will be terminated!");
            System.exit(0);
        }
    }
    /**
     *ʹ���ַ�����ʾĿ¼·������ָ����Ŀ¼·������ʼ�����캯������Ŀ¼ָ��ת��Ϊ���ṹ��Ŀ¼
     */
    public DirectoryToTree(String directorypath){
    	isJavaProject = false;
        File dir=new File(directorypath);
        if(dir.isDirectory()){
                root=new DefaultMutableTreeNode(dir.getName());
                objectFile=dir;
        }
        else{
            System.out.println("The parameter you given is not a directory,the program will be terminated!");
            System.exit(0);
        }
    }
    /**
     *�þ�̬����ͨ�����������������ָ����Ŀ¼�е���Ŀ¼���ļ�ת��Ϊ���ṹ�����ڵ��ɴ����������
     */
    public static void createDirectoryTree(File dir,DefaultMutableTreeNode baseroot){
        if(dir.isDirectory()){;}
        else{
            System.out.println("The parameter you given is not a directory,the program will be terminated!");
            System.exit(0);
        }
        int i=0;
        File [] f=new File[dir.listFiles().length];
        f=dir.listFiles();
        while(i<f.length){
            if(f[i].isDirectory()&&f[i].listFiles().length>0){
//            	System.out.println(f[i].getName());
                DefaultMutableTreeNode childroot=null;
                childroot=new DefaultMutableTreeNode(f[i].getName());
               
                baseroot.add(childroot);
                createDirectoryTree(f[i],childroot);
            }
            else if (f[i].isFile()) {
            	baseroot.add(new DefaultMutableTreeNode(f[i].getName()));
                if (f[i].getName().endsWith(".java")) {
                	
					isJavaProject = true;
				}
            }
            i=i+1;
        }
    }
    /**
     *�õ�ͨ�����캯��ȷ����Ŀ¼������Ŀ¼�����ļ���Ӧ�����ṹ�ĸ��ڵ�
     */
    public DefaultMutableTreeNode createDefaultTree(){
        createDirectoryTree(objectFile,root);
        
        return root;
    }
}
