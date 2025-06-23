package Main;

import java.io.File;

/*
 * 2016.1.21  增加Soap存储节点显示
 * */

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.xml.bind.ParseConversionEvent;
import javax.xml.crypto.KeySelector.Purpose;

import org.codehaus.janino.util.StringPattern;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;

import com.iseu.Main_Rest.Main_REST;
import com.iseu.handler.Neo4j_RESTHandler;

import Main_Instru.CopyProbe;
import Main_Instru.Main_Instru;
import Main_Server.Main_Server;
import Main_Slicer.Slice;
import Main_Slicer.sliceHandle;
import cn.edu.seu.Client.ClientCPStorage;
import cn.edu.seu.Main_Soap.Main_Soap;
import cn.edu.seu.Main_Socket.Main_Socket;
import cn.edu.seu.Server.ServerCPStorage;
import cn.edu.seu.SoapBase.Store;
import oo.com.iseu.Invoke.Invoke;
import oo.com.iseu.UI.Basic.SliceDirection;
import scala.annotation.elidable;
import scala.reflect.internal.Trees.CaseDef;
import scala.reflect.runtime.SymbolLoaders.PackageScope;
import storage02.NEO4JAccess;
import storage02.Elements.ElementPath;
import storage02.Elements.ElementStartline;

public class Main {

	public static int progress = 0;
	public static int temp = 0;
//	public static String project="System.getProperty(\"user.dir\")";
	public static String project="D:\\work2\\SDN_V1.0.1";

	public static String projectPath = ""; // 程序路径 或 服务器端路径
	public static String ClientPath = ""; // 只针对Soap的客户端路径
	public static String DBPath = ""; // 数据库路径
	public static String filePath = ""; // 准则路径
	public static String line = ""; // 准则行号
	public static String trace = "";// Soap 执行路径记录地址     D:/traceFile.txt

	public static void main(String[] args) throws Exception {
		DG("D:\\DIS3",true,"D:\\EclipseJeePRJ\\OOJavaSlicerV1.2.2.1\\Repository\\Graphs\\DIS3.db");
		String []a={""};
//		staticSlice("F:\\DIS3\\client\\Distributed_TestJaxWSClient1\\src\\cn\\edu\\seu\\testsoap\\client\\JAXWSClient.java", "11", "F:\\DIS33.db", 0,  "F:\\a",a);
//		staticSlice("F:\\DIS3\\Distributed_TestJaxWSServer1\\src\\cn\\edu\\seu\\testsoap\\HelloWorldImpl.java", "20", "F:\\DIS3.db", 1,  "F:\\a",a);
//		dySlice("D:\\DIS3\\Distributed_TestJaxWSServer1\\src\\cn\\edu\\seu\\testsoap\\HelloWorldImpl.java", "12", "D:\\EclipseJeePRJ\\OOJavaSlicerV1.2.2.1\\Repository\\Graphs\\DIS3.db","D:\\EclipseJeePRJ\\OOJavaSlicerV1.2.2.1\\Repository\\Traces\\traceFile.txt", 1,  "D:\\A",a);
	}

	
	public static void DG(String folder, boolean multiProject,String DBPath) throws Exception // 构建依赖图
	{
		run();
		Store.init();
		NEO4JAccess.DB_PATH=DBPath;
		
		progress = 0;
		if(!multiProject)
		{
			projectPath=folder;
			ClientPath=folder;
		}
		else {
			File project= new File(folder);
			String []paths =project.list();
			for(String path:paths)
			{
				if(path.contains("client"))
					ClientPath=folder+"\\client";
				if(!path.contains("client"))
					projectPath=folder+"\\"+path;
			}
			
		}
		Main_Socket.start(folder, DBPath);
		progress = 40;
		Main_REST.start(folder, DBPath);
		if (ClientPath == null||ClientPath.equals(""))
			ClientPath = projectPath;
		progress = 70;
		Main_Soap.start(projectPath, ClientPath, DBPath);
		
		if (NEO4JAccess.graphDb!=null) {
			NEO4JAccess.graphDb.shutdown();
			NEO4JAccess.graphDb=null;
		}
		if(multiProject)
		{
			Main_Instru.distributedMove(folder);
//			System.out.println(projectPath.substring(0,projectPath.lastIndexOf("\\")));
		}
		else
		{
			Main_Instru.copyOOProbe(folder+"_Instrument");
		}
			
		progress = 100;
	}

	public static void instru(String projectPath) throws Exception // Socket插装 插装后的工程与原工程保存在同一文件夹下
	{
		run();
		Main_Instru.distributedInstrument(projectPath);
		progress = 100;
	}

	
	
	public static void staticSlice(String filePath,String line ,String DBPath, int direction,String sliceStorePath,String[] strVars) //static slicing
	{
		NEO4JAccess.init();
		sliceHandle.init();
		run();
		progress=20;
		NEO4JAccess.DB_PATH=DBPath;
		Slice.filePath=filePath;
		Slice.line=line;
		Slice.DBPath=DBPath;
		Slice.direction=direction;
		Slice.sliceStorePath=sliceStorePath;
		Invoke invoke = new Invoke();
		progress=30;
		HashMap<String, TreeSet<Integer>>  ooslice = new HashMap<>();
		switch (direction)
		{
		case 0:
			ooslice= invoke.OOStaticSlicing(DBPath, filePath, Integer.parseInt(line), strVars, SliceDirection.FORWARD);
			break;
		case 1:
			ooslice= invoke.OOStaticSlicing(DBPath, filePath, Integer.parseInt(line), strVars, SliceDirection.BACKWARD);
			break;
		case 2:
			ooslice=invoke.OOStaticSlicing(DBPath, filePath, Integer.parseInt(line), strVars, SliceDirection.ALL);
			break;
		}
		progress=50;
		HashMap<String, ArrayList<Integer>> dyslice = sliceHandle.treeToArray(ooslice);
		NEO4JAccess.totalHashSlice.add(dyslice);
		progress=70;
		Slice.getAllSlice(dyslice);
		
		if (NEO4JAccess.graphDb != null) {
			NEO4JAccess.graphDb.shutdown();
			NEO4JAccess.graphDb = null;
		}
		
		progress=90;
		Slice.visualization(NEO4JAccess.tmpHash,sliceStorePath);
		progress=100;
	}
	
	public static HashMap<String, TreeSet<Integer>> staticSlice(String DBPath,String filePath,int line ,String[] strVars,int direction,String sliceStorePath) //static slicing
	{
		NEO4JAccess.init();
		sliceHandle.init();
		run();
		NEO4JAccess.DB_PATH=DBPath;
		Set<Node> nodeStart = new HashSet<>();
		if (strVars!=null) {
			for (String var : strVars) {
				nodeStart.addAll(sliceHandle.getVarNodes(var, filePath, line,NEO4JAccess.getDB()));
			}
		}
		if (nodeStart.size() == 0) {
			Slice.var=0;
			//System.out.println("Cannot find related variable nodes. Change strategy to slice by statement...");
			Slice.staticSlice(filePath, ""+line, DBPath, direction,sliceStorePath);
			progress = 100;
			HashMap<String, TreeSet<Integer>> result = new HashMap<>();
			Set<Entry<String, ArrayList<Integer>>> temp = NEO4JAccess.tmpHash.entrySet();
			for(Entry<String, ArrayList<Integer>> entry : temp){
				TreeSet<Integer> set = new TreeSet<>();
				set.addAll(entry.getValue());
				result.put(entry.getKey(), set);
			}
			return result;
		}
		else{
			Slice.var=1;
			for(Node n : nodeStart)
			{
				Slice.staticSlice((String)n.getProperty(NEO4JAccess.ElementPath),(String)n.getProperty(NEO4JAccess.ElementStartline), DBPath,direction,sliceStorePath);
				sliceHandle.storeHashSlice.add(NEO4JAccess.tmpHash);
				NEO4JAccess.tmpHash=null;
			}
			sliceHandle.mergeSlice();
			HashMap<String, TreeSet<Integer>> result = new HashMap<>();
			Set<Entry<String, ArrayList<Integer>>> temp = sliceHandle.TotalHashSlice.entrySet();
			for(Entry<String, ArrayList<Integer>> entry : temp){
				TreeSet<Integer> set = new TreeSet<>();
				set.addAll(entry.getValue());
				result.put(entry.getKey(), set);
			}
			return result;
		}
	}
	
	public static void dySlice(String filePath,String line ,String DBPath,String trace, int direction,String sliceStorePath,String[] strVars)//dy slicing
	{
		NEO4JAccess.init();
		sliceHandle.init();
		run();
		progress=0;
		NEO4JAccess.DB_PATH=DBPath;
		Slice.filePath=filePath;
		Slice.line=line;
		Slice.DBPath=DBPath;
		Slice.direction=direction;
		Slice.sliceStorePath=sliceStorePath;
		new NEO4JAccess(Slice.DBPath);
		Slice.storemark(new File(trace));
		Invoke invoke = new Invoke();
		progress=30;
		HashMap<String, TreeSet<Integer>>  ooslice = new HashMap<>();
		switch (direction)
		{
		case 0:
			ooslice= invoke.OOStaticSlicing(DBPath, filePath, Integer.parseInt(line), strVars, SliceDirection.FORWARD);
			break;
		case 1:
			ooslice= invoke.OOStaticSlicing(DBPath, filePath, Integer.parseInt(line), strVars, SliceDirection.BACKWARD);
			break;
		case 2:
			ooslice=invoke.OOStaticSlicing(DBPath, filePath, Integer.parseInt(line), strVars, SliceDirection.ALL);
			break;
		}
		progress=50;
		HashMap<String, ArrayList<Integer>> dyslice = sliceHandle.treeToArray(ooslice);
		NEO4JAccess.totalHashSlice.add(dyslice);
		progress=70;
		Slice.getAllSlice(dyslice);
		NEO4JAccess.dyNode();
		if (NEO4JAccess.graphDb != null) {
			NEO4JAccess.graphDb.shutdown();
			NEO4JAccess.graphDb = null;
		}
		
		progress=90;
		Slice.visualization(NEO4JAccess.tmpHash,sliceStorePath);
		progress=100;
	}
	public static HashMap<String, TreeSet<Integer>> dySlice(String DBPath,String filePath,int line ,String trace,String vars[], int direction,String sliceStorePath)//dy slicing
	{
		NEO4JAccess.init();
		sliceHandle.init();
		run();
		NEO4JAccess.DB_PATH=DBPath;
		
		Set<Node> nodeStart = new HashSet<>();
		if (vars!=null) {
			for (String var : vars) {
				nodeStart.addAll(sliceHandle.getVarNodes(var, filePath, line,NEO4JAccess.getDB()));
			}
		}

		if (nodeStart.size() == 0) {
			System.out.println("Cannot find related variable nodes. Change strategy to slice by statement...");
			Slice.dySlice(filePath, ""+line, DBPath, trace, direction,sliceStorePath);
			HashMap<String, TreeSet<Integer>> result = new HashMap<>();
			Set<Entry<String, ArrayList<Integer>>> temp = NEO4JAccess.tmpHash.entrySet();
			for(Entry<String, ArrayList<Integer>> entry : temp){
				TreeSet<Integer> set = new TreeSet<>();
				set.addAll(entry.getValue());
				result.put(entry.getKey(), set);
			}
			progress = 100;
			Main.progress=100;
			return result;
		}
		else{
			Slice.var=1;
			for(Node n : nodeStart)
			{
				if(progress<80)
					progress=progress+20;
				Slice.dySlice((String)n.getProperty(NEO4JAccess.ElementPath),(String)n.getProperty(NEO4JAccess.ElementStartline), DBPath,trace,direction,sliceStorePath);
				sliceHandle.storeHashSlice.add(NEO4JAccess.tmpHash);
				NEO4JAccess.tmpHash=null;
			}
			sliceHandle.mergeSlice();
			HashMap<String, TreeSet<Integer>> result = new HashMap<>();
			Set<Entry<String, ArrayList<Integer>>> temp = sliceHandle.TotalHashSlice.entrySet();
			for(Entry<String, ArrayList<Integer>> entry : temp){
				TreeSet<Integer> set = new TreeSet<>();
				set.addAll(entry.getValue());
				result.put(entry.getKey(), set);
			}
			progress = 100;
			Main.progress=100;
			return result;
		}
	}
	
	
	public static void  startServer(String DBPath) throws IOException  , InterruptedException   // Socket Server

	{
		run();
		Main_Server.start(DBPath);
	}
	
	public static void endTheTask() throws InterruptedException
	{
		Main_Server.endTheTask();
	}

	public static void run() {

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (progress != temp) {
					System.err.println("progress percentage:   " + progress + "%");
					temp = progress;
				}
				 if(progress==100){
					 System.out.println("Finished!");
					 timer.cancel();
				 }
					
			}
		}, 0, 1 * 100);
	}

}
