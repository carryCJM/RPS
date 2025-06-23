package oo.com.iseu.CSlicer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExtendedExecutionResult;
import org.neo4j.cypher.internal.compiler.v2_2.PathImpl;
import org.neo4j.cypher.internal.compiler.v2_2.helpers.StringRenderingSupport;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.shell.impl.SystemOutput;

import oo.com.iseu.slicer.helper.SlicerHelper;
import oo.storage01.DGEdge;
import oo.storage01.NEO4JAccess;
import scala.deprecated;
import scala.annotation.meta.setter;

public class ConcurrentSlicer {
	public  GraphDatabaseService database = null;
	public  ExecutionEngine engine;
	public  Transaction transaction;
	public  String DBpath ;                           			//���ݿ�·��
	public  String SliceStoreRootFilePath;           			//���̴洢��Ŀ¼
	public  String sliceFile;
	public  HashMap<String, TreeSet<Integer>> slices; 			//��Ƭ��
	public  HashSet<Long> visitedNodes;               			//���ʹ��Ľڵ�
	public  LinkedList<Long> currentNodes;
	//����ʱ�ߵķ���
	public  String threadRelateEdges;
	public  String withoutParameterOutEdges;
	public  String parameterOutEdge;
	public  String withoutParaInAndMethodInvokeEdges;
	//public  String forwardStep1Edges;
	public  String forwardStep2Edges;
	
	public boolean isBackward = true;
	public HashSet<Long> methodNodeID;
	public long initLocationMethodID;
	public  ArrayList<String> forwardEdges;
	
	{
		//���̵߳ı߼���
		threadRelateEdges = "notify|s_communicate|FutureGet|countDown|interrupt|authorityAcquire|authorityRelease|threadStart";
		//�ǿ��̱߳�ȥ���������
		withoutParameterOutEdges = "classMember|controlDepd|instantiation|objMember|parameter|typeAnalysis|initDataDependenceEdge|dataDepd|inputParameter|methodInvocation|methodImplment|"
				+ "methodOverwrite|interfaceImplement|inherit|abstractMember|pkgMember|pkgDepd|theadSecurity|competenceAcquire|competenceRelease|competenceDepd|"
				+ "interClassMessage|anonymousClassDeclare|CFGedge|summaryEdge|dataMember|toObject|threadRisk";
		//�ǿ��̱߳�ȥ���������뼰��������&�̵߳���
		withoutParaInAndMethodInvokeEdges = "classMember|controlDepd|instantiation|objMember|parameter|typeAnalysis|initDataDependenceEdge|dataDepd|outputParameter|methodImplment|methodOverwrite|"
				+ "interfaceImplement|inherit|abstractMember|pkgMember|pkgDepd|theadSecurity|competenceAcquire|competenceRelease|competenceDepd|interClassMessage|"
				+ "anonymousClassDeclare|CFGedge|summaryEdge|dataMember|toObject|threadRisk";
		//���������
		parameterOutEdge = "outputParameter";
		forwardStep2Edges = "classMember|controlDepd|instantiation|objMember|parameter|typeAnalysis|initDataDependenceEdge|dataDepd|methodImplment|methodOverwrite|"
				+ "interfaceImplement|inherit|abstractMember|pkgMember|pkgDepd|theadSecurity|competenceAcquire|competenceRelease|competenceDepd|interClassMessage|"
				+ "anonymousClassDeclare|CFGedge|summaryEdge|dataMember|toObject|threadRisk";
	}
	/**
	 * ��ȡ���ݿ�
	 * @param dataBasePath ���ݿ��·��
	 * @return 
	 */
	public ConcurrentSlicer(String dataBasePath,String rootFilePath) {
		SliceStoreRootFilePath = rootFilePath;                            //��Ƭ�洢��·��
		DBpath = dataBasePath;                                            //·������
		database = new GraphDatabaseFactory().newEmbeddedDatabase(DBpath);//���ݿⴴ�����ȡ
		registerShutdownHook(database);                                
		engine = new ExecutionEngine(database,StringLogger.logger(new File("log"))); //��ѯ���洴��
		transaction = database.beginTx();
		slices = new HashMap<>();
		visitedNodes = new HashSet<>();
		currentNodes = new LinkedList<>();
		methodNodeID = new HashSet<>();
	}
	
	public void transactionFinish(boolean finishFlag) {
		if(!finishFlag)
			return;
		//transaction.success();
		if (transaction!=null) {
			transaction.close();
		}
		if (database!=null) {
			database.shutdown();
		}
		System.err.println("Current slicing db ShutDown!!!!");
	}
	
	private void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (graphDb!=null) {
					graphDb.shutdown();
				}	
			}
		});
	}
	
	/**
	 * ͨ���ڵ�ID��ȡ�ڵ�
	 * @param nodeID  �� �ڵ�ID
	 * @return
	 */
	public Node getNode(Long nodeID) {
		if (nodeID<0) {
			return null;
		}
		//��������
//		Transaction transaction = database.beginTx();
//		try{
			//��ѯ���
			String query = "START n=NODE("+nodeID+") return n";
			//��ȡ��ѯ���
//			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the node");
				return null;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("n");
			if(nodes.hasNext()){
				Node node = nodes.next();
//				transaction.success();
				return node;
			}
//		}	
//		finally {
//			transaction.close();
//		}
		//System.out.println("NULL");
		return null;
	}

	/**
	 * �ж��Ƿ�Ϊ������ڽڵ�
	 * @param node : ��ѯ�ڵ�
	 * @return
	 */
	public boolean isMethodEntryNode(Node node) {
//		Transaction transaction = database.beginTx();
//		try {
			if (node.hasLabel(NEO4JAccess.StatementNode)) {
				String query = "START n=node("+node.getId()+") "
								+ "MATCH n-[:parameter]->b "
								+ "RETURN b";
				//��ȡ��ѯ���
//				System.out.println(query);
				ExtendedExecutionResult result = engine.execute(query);
				if (!result.isEmpty()) {
					return true;
				}
//				transaction.success();
			}
//		} finally {
//			transaction.close();
//		}
		return false;
	}
	
	/**
	 * ��ȡfrom--->to������·��
	 * @param from
	 * @param to
	 * @return
	 */
	public ArrayList<PathImpl> getPaths(Long from,Long to) {
		ArrayList<PathImpl> resultPaths = new ArrayList<>();
		//��������
//		Transaction transaction = database.beginTx();
//		try{
			//��ѯ���
			String query =  " START a=node("+from+"),b=node("+to+")"+ 
							" MATCH p=a-[:classMember|controlDepd|toObject|typeAnalysis|methodInvocation|FutureGet|parameter*]->b"+ 
							" RETURN p";
			//System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Path not find!");
				return null;
			}
			//�洢�Ž��P:path1,P:path2...
			ResourceIterator<Map<String, Object>> nodeResult = result.javaIterator();
			while (nodeResult.hasNext()) {
				Map<String, Object> map = (Map<String, Object>) nodeResult.next();
				System.out.println(map);
				Collection<Object> nodes = map.values();
				for (Object object : nodes) {
					if (object instanceof PathImpl) {
						PathImpl path = (PathImpl)object;
						resultPaths.add(path);
					}
				}
			}
//			transaction.success();
//		}	
//		finally {
//			transaction.close();
//		}
		return resultPaths;
	}
	


	/**
	 * ��ȡ�ڵ㼯��
	 * @param filePath ����·��
	 * @param lineNumber �ڵ��к�
	 * @return �����ڵ�����ݿ�ID�ţ�����Ϊlong
	 */
	public ArrayList<Node> getNodes(String filePath,int lineNumber) {
		//��������
//		Transaction transaction = database.beginTx();
//		try{
			//��ѯ���
			String query = "MATCH node WHERE node.FilePath = \""+
					filePath.replace("\\", "\\\\")+"\" AND node.Startline = \""+
					lineNumber+"\"  RETURN node";
			//��ȡ��ѯ���
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
//				System.out.println("Can not find the statnode");
				return null;
			}
			//System.out.println(result.dumpToString());ֱ�ӽ��������ˣ�result�����
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("node");
			ArrayList<Node> resultNodes = new ArrayList<>();
			while(nodes.hasNext()){
				Node node = nodes.next();
				resultNodes.add(node);
			}
//			transaction.success();
			return resultNodes;
//		}	
//		finally {
//			transaction.close();
//		}
	}

	/**
	 * ��������������ڵ�ʱ���������չ�ڵ㣨2016-4-26��
	 * @param src
	 * @param resultNodes
	 */
	public void getConditionNodes(Long src,ArrayList<Node> resultNodes) {
		Node node = getNode(src);
		//�������βδ����ڵ�
		if (node.hasLabel(NEO4JAccess.ConditionNode)&&node.hasProperty(NEO4JAccess.ElementStartline)&&node.hasProperty(NEO4JAccess.ElementPath)) {
			//System.out.println("ConditionNode:"+node.getId());
			//System.out.println(node.getProperty(NEO4JAccess.ElementStartline));
			//System.out.println(node.getProperty(NEO4JAccess.ElementPath));
			ArrayList<Node> nodes = getNodes(node.getProperty(NEO4JAccess.ElementPath).toString(),Integer.parseInt(node.getProperty(NEO4JAccess.ElementStartline).toString()) );
			for (Node n : nodes) {
//				if (n.hasLabel(NEO4JAccess.FormalParaOutNode)||n.hasLabel(NEO4JAccess.FormalParaInNode)) {
//					continue;
//				}
				resultNodes.add(n);
			}	
		}
	}
	 /**
	  * ��ָ�������͵�src�ڵ�Ľڵ㼯(�������)
	  * @param src   �� Դ�ڵ�
	  * @param edges �������ͼ�
	  * @param nodeList:�ڵ�ID��Ҫ����ļ���
	  */
	public ArrayList<Node> getNodesToSrc(Long src,String edges,boolean isThread){
		ArrayList<Node> resultNodes = new ArrayList<>();
		//��ȡnode��
		//��������
//		Transaction transaction = database.beginTx();
//		try{
			//��ѯ���
			String query = "START b=node("+src+") MATCH a-[r:"+edges+"]->b return a";
			//System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (!result.isEmpty()) {
//				System.out.println("Can not find the nodes!1");
				ResourceIterator<Node> nodes = result.javaColumnAs("a");
				while(nodes.hasNext()){
					Node node = nodes.next();
					resultNodes.add(node);
				}
			}
			if (!isThread) {
				getConditionNodes(src, resultNodes);
			}
//			transaction.success();

//			for (Node node : resultNodes) {
//				System.out.println(src+"->"+node+" ");
//			}
//			System.out.println();
			return resultNodes;
//		}
//		finally {
//			transaction.close();
//		}
	}
	
	/**
	 * ��ȡ�ڵ㼯��
	 * @param filePath ����·��
	 * @param lineNumber �ڵ��к�
	 * @return �����ڵ�����ݿ�ID�ţ�����Ϊlong
	 */
	public Node getStatementNode(String filePath,int lineNumber) {
		//��������
		//��ѯ���
		String query = "MATCH (node:StatementNode) WHERE node.FilePath = \""+
				filePath.replace("\\", "\\\\")+"\" AND node.Startline = \""+
				lineNumber+"\" RETURN node";
		//��ȡ��ѯ���
		//System.out.println(query);
		ExtendedExecutionResult result = engine.execute(query);
		if (result.isEmpty()) {
			System.out.println("Can not find the statnode");
			return null;
		}
		//��ȡnode��
		ResourceIterator<Node> nodes = result.javaColumnAs("node");
		Node node = nodes.next();

		return node;
	}
	public void forwardSliceControlEdgeAdd(ArrayList<Node> resultNodes,Long src) {
		Node node = getNode(src);
		//�������βδ����ڵ�
		if (!node.hasLabel(NEO4JAccess.FormalParaOutNode)&&node.hasProperty(NEO4JAccess.ElementStartline)&&node.hasProperty(NEO4JAccess.ElementPath)) {
//			System.err.println(node.getProperty(NEO4JAccess.ElementPath).toString());
//			System.err.println(Integer.parseInt(node.getProperty(NEO4JAccess.ElementStartline).toString()));
			Node sNode = getStatementNode( node.getProperty(NEO4JAccess.ElementPath).toString(),Integer.parseInt(node.getProperty(NEO4JAccess.ElementStartline).toString()));
			if (sNode!=null) {
				ArrayList<Node> nodes = getNodesFromSrcByEdgeType(sNode.getId(), DGEdge.controlDepd);
				for (Node n : nodes) {
					resultNodes.add(n);
				}
				ArrayList<Node> nodes2 = getNodesFromSrcByEdgeType(sNode.getId(), DGEdge.dataDepd);
				for (Node n : nodes2) {
					resultNodes.add(n);
				}
			}	
		}
	}
	/**
	 * (src)----->(....)
	 * @param src
	 * @param edges
	 * @return
	 */
	public ArrayList<Node> getNodesFromSrc(Long src,String edges) {
		ArrayList<Node> resultNodes = new ArrayList<>();
		//��ȡnode��
		//��������
//		Transaction transaction = database.beginTx();
//		try{
			//��ѯ���
			String query = "START a=node("+src+") MATCH a-[r:"+edges+"]->b return b";
			//System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (!result.isEmpty()) {
				ResourceIterator<Node> nodes = result.javaColumnAs("b");
				while(nodes.hasNext()){
					Node node = nodes.next();
					if (node.hasLabel(NEO4JAccess.FormalParaOutNode)&&node.hasProperty(NEO4JAccess.ElementMethodID)) {
						if (methodNodeID.contains(Long.parseLong(node.getProperty(NEO4JAccess.ElementMethodID).toString() ))) {
							continue;       //ͨ���������ý���ĺ���������parameterout����
						}
					}
					if (node.hasLabel(NEO4JAccess.InstanceMethodNode)&&initLocationMethodID!=node.getId()) {//��¼�������õ��ĺ����ڵ�(��Ϊ��ʼ���ں���)
						methodNodeID.add(node.getId());
					}
					resultNodes.add(node);
				}
//				transaction.success();
				//���������ӽڵ��������ڵ�Ŀ��������ɴ�ڵ�
				
//				for (Node node : resultNodes) {
//					System.err.print(src+"->"+node+" ");
//				}
//				System.err.println();
			}
			if (!isBackward) {
				//System.out.println("-----------------------------------------------");
				forwardSliceControlEdgeAdd(resultNodes, src);
			}
			
			return resultNodes;
//		}
//		finally {
//			transaction.close();
//		}
	}
	 /**
	  * ���ؾ����ض��ߵ���Դ�ڵ�Ľڵ㼯
	  * @param src       ��Դ�ڵ�
	  * @param edgeType  ��������
	  */
	public ArrayList<Node> getNodesToSrcByEdgeType(Long src,DGEdge edgeType){
		ArrayList<Node> resultNodes = new ArrayList<>();
		//��������
//		Transaction transaction = database.beginTx();
//		try{
			//��ѯ���
			String query = "START b=node("+src+") MATCH a-[r:"+edgeType+"]->b return a";
//			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
//				System.out.println("Can not find the nodes!3");
				return resultNodes;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("a");
			while(nodes.hasNext()){
				Node node = nodes.next();	
				resultNodes.add(node);
			}
//			transaction.success();
//		}	
//		finally {
//			transaction.close();
//		}
		return resultNodes;
	}

	 /**
	  * ����Դ�ڵ㾭���ض��ߵ���Ľڵ㼯
	  * @param src       ��Դ�ڵ�
	  * @param edgeType  ��������
	  */
	public ArrayList<Node> getNodesFromSrcByEdgeType(Long src,DGEdge edgeType){
		ArrayList<Node> resultNodes = new ArrayList<>();
		//��������
//		Transaction transaction = database.beginTx();
//		try{
			//��ѯ���
			String query = "START b=node("+src+") MATCH b-[r:"+edgeType+"]->a return a";
//			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
//				System.out.println("Can not find the nodes!3");
				return resultNodes;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("a");
			while(nodes.hasNext()){
				Node node = nodes.next();	
				resultNodes.add(node);
			}
//			transaction.success();
//		}	
//		finally {
//			transaction.close();
//		}
		return resultNodes;
	}
	/**
	 * ���϶�λ��Ƭ�洢
	 */
	public void locationSlicesStorage() {
		try{			
			if(sliceFile==null || sliceFile.equals(""))//��ʾ����Ҫ�洢
				return;
			FileOutputStream fileOutput = new FileOutputStream(sliceFile,true);
			BufferedWriter bufw = new BufferedWriter(new OutputStreamWriter(fileOutput));
			Set<Entry<String, TreeSet<Integer>>> slicesSet = slices.entrySet();
			int j=1;
			for (Entry<String, TreeSet<Integer>> entry : slicesSet) {
				String filePath = entry.getKey();			
				TreeSet<Integer> fileSlices = entry.getValue();
				for(int line:fileSlices){
					bufw.write((j++)+","+filePath+","+line+"\r\n");		
					bufw.flush();
				}
			}
			bufw.close();			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Storing Finished");
	}
	
	/**
	 * ��Ƭ�洢
	 */
	public void slicesStorage(boolean flag) {
		if(flag){
			locationSlicesStorage();
			return;
		}
		if (SliceStoreRootFilePath==null) {
			return;
		}
		File file = new File(SliceStoreRootFilePath);
		if (!file.isDirectory()) {
			//System.err.println("Ŀ¼�����ڣ�");
			return ;
		}
		Set<Entry<String, TreeSet<Integer>>> slicesSet = slices.entrySet();
		long slicesNumber = 0;
		for (Entry<String, TreeSet<Integer>> entry : slicesSet) {
			String filePath = entry.getKey();
			
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
			sliceOutputToFile(entry,filePath,newFilePath);
			slicesNumber += entry.getValue().size();
		}
		System.out.println("�洢���");
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
			srcFileReader.close();
			printWriter.close();
			
			//System.out.println(entry.getValue().last());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
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
	
	public void initLocationMethodIDRecord(String file,int line) {
		Node node = getStatementNode(file, line);
		if (node.hasProperty(NEO4JAccess.ElementMethodID)) {
			initLocationMethodID = Long.parseLong(node.getProperty(NEO4JAccess.ElementMethodID).toString());
			//System.out.println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ"+initLocationMethodID);
		}
	}
	
	public Set<Node> findVarNode(String strVar,String file,int line) {
		initLocationMethodIDRecord(file,line);
		String strVarName = strVar, strFieldName = null, strGetNode = null;
		if (strVar.contains(".")) {
			strFieldName = strVar.substring(strVar.lastIndexOf(".") + 1, strVar.length());
			strVarName = strVar.substring(0, strVar.lastIndexOf("."));
		}

		if (strFieldName != null)
			strGetNode = "MATCH node WHERE node.FilePath = \"" + file.replace("\\", "\\\\") + "\" AND node.Startline = \"" + line + "\" AND node.VarName = \"" + strVarName
					+ "\" AND node.Name = \"" + strFieldName + "\" RETURN node";
		else if(!strVarName.equals(""))
			strGetNode = "MATCH node WHERE node.FilePath = \"" + file.replace("\\", "\\\\") + "\" AND node.Startline = \"" + line + "\" AND node.VarName = \"" + strVarName
					+ "\" RETURN node";
		
		//System.out.println(strGetNode);

		Set<Node> nodeStart = new HashSet<>();
		if (strGetNode!=null) {
			try (Transaction tx = database.beginTx(); Result result = database.execute(strGetNode)) {
				while (result.hasNext()) {
					Map<String, Object> row = result.next();
					// setSliceResult.add(row.get("n"));
					nodeStart.add((Node) row.get("node"));
				}
			}	
		}
		
		if (nodeStart.size() == 0&&!strVar.equals("")) {
			//try 2nd time
			System.err.println("Try second time...");

			strGetNode = "MATCH node WHERE node.FilePath = \"" + file.replace("\\", "\\\\") + "\" AND node.Startline = \"" + line + "\" AND node.VarName = \"" + strVar
					+ "\" RETURN node";

			//System.out.println(strGetNode);

			try (Transaction tx = database.beginTx(); Result result = database.execute(strGetNode)) {
				while (result.hasNext()) {
					Map<String, Object> row = result.next();
					// setSliceResult.add(row.get("n"));
					nodeStart.add((Node) row.get("node"));
				}
			}
		}
		if (nodeStart.size() == 0) {
			//��Ȼ�޷��ҵ���Ӧ��Ա����λ�ã�����������ؽڵ�
			System.out.println("Cannot find related variable nodes. Change strategy to slice by statement...");
			ArrayList<Node> stmtNodes = getNodes(file, line);
			nodeStart.addAll(stmtNodes);
		} 
		System.err.println(nodeStart);

//		for (Node node : nodeStart) {
//			System.err.println(node);
//		}
		return nodeStart;
	}
	
	public Set<Node> findStartNodes(SliceCriterion criterion) {
		Set<Node> startNodes = new HashSet<>();
		System.err.println("Begin to find start nodes");
		System.err.println("vars:"+criterion.interestVars);
		for (String var : criterion.interestVars) {
			startNodes.addAll(findVarNode(var, criterion.filePath, criterion.lineNumber));
		}
		return startNodes;
	}
	
	public boolean isSameString(String s1,String s2) {
		if (s1!=null) {
			return s1.equals(s2);
		}
		else if (s2!=null) {
			return s2.equals(s1);
		}
		return true;
	}
	public boolean isSameStatement(Long idx,Long idy) {
		Node x = getNode(idx);
		Node y = getNode(idy);
		if (x.hasProperty(NEO4JAccess.ElementPath)&&y.hasProperty(NEO4JAccess.ElementPath)&&
			x.hasProperty(NEO4JAccess.ElementStartline)&&y.hasProperty(NEO4JAccess.ElementStartline)) {
			String xp = (String) x.getProperty(NEO4JAccess.ElementPath);
			String yp = (String) y.getProperty(NEO4JAccess.ElementPath);
			String xl = (String) x.getProperty(NEO4JAccess.ElementStartline);
			String yl = (String) y.getProperty(NEO4JAccess.ElementStartline);
//			System.out.println(xp);
//			System.out.println(yp);
//			System.out.println(xl);
//			System.out.println(yl);
//			System.out.println(isSameString(xp, yp)&&isSameString(xl, yl));
			return isSameString(xp, yp)&&isSameString(xl, yl);
		}
		return false;
	}
	
	public boolean isMethodNode(Node node) {
		if (node.hasProperty(NEO4JAccess.ElementMethodID)) {
			Long methodId = Long.valueOf((String) node.getProperty(NEO4JAccess.ElementMethodID)) ;
			Long id = node.getId();
			return id==(methodId+1);
		}
		return false;
	}
	/**
	 * �����ڵ����������ڵ��������(�ײ����)	
	 * @param node  ��  ���ڵ�
	 * @return
	 */
	public ArrayList<Node> getRelateNode(Node node,Long src) {
		ArrayList<Node> resultNodes = new ArrayList<>();
		if (node==null) {
			return resultNodes;
		}
		resultNodes.add(node);
		Node nodeSrc = getNode(src);
//		Transaction transaction = database.beginTx();
//		try {
		if (isBackward) {
			//·�������кŲ�ͬ�����еײ���չ
			if (node.hasLabel(NEO4JAccess.StatementNode)&&
				node.hasProperty("FilePath")&&node.hasProperty("Startline")&&
				nodeSrc.hasProperty("FilePath")&&nodeSrc.hasProperty("Startline")&&
				(!node.getProperty("FilePath").equals(nodeSrc.getProperty("FilePath"))||
				!node.getProperty("Startline").equals(nodeSrc.getProperty("Startline"))	
						)) {
//				String filePath = (String) node.getProperty("FilePath");
//				String startLine = (String) node.getProperty("Startline");
				Long id = node.getId();
				//String query = "MATCH n WHERE n.Startline=\""+startLine+"\" AND n.FilePath=\""+filePath.replace("\\", "\\\\")+"\" RETURN n";
				String query = "START a=node("+id+") MATCH a-[r:toObject|typeAnalysis|objMember|parameter*]->n RETURN n";
				if (isMethodNode(node)) {
					query = "START a=node("+id+") MATCH a-[r:toObject|typeAnalysis*]->n RETURN n";
				}
				//System.out.println(query);
				ExtendedExecutionResult result = engine.execute(query);
				if (result.isEmpty()) {
//					System.out.println("Relate Nodes not find!");
					return resultNodes;
				}
				//��ȡnode��
				ResourceIterator<Node> nodes = result.javaColumnAs("n");
				while(nodes.hasNext()){
					Node n = nodes.next();	
					resultNodes.add(n);
					//System.out.println(n);
				}
			}
		}
//		else {
//			resultNodes = getNodesFromSrc(node.getId(), "toObject|typeAnalysis*");
//			resultNodes.add(node);
////			transaction.success();
//
//		}
		return resultNodes;	
//		} finally {
//			transaction.close();
//		}
	}
}
