package oo.com.iseu.CSlicer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.cypher.ExtendedExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;

import oo.com.iseu.UI.Basic.SliceDirection;
import oo.storage01.NEO4JAccess;

public class ConcurrentDynamicSlicer extends ConcurrentSlicer{
	public static int progressValue;
	/*version 2016-4-1-5*/
	public boolean isBackward = true;
	public boolean isNewTrace = true;
	public boolean stateOnlyFlag = false;
	public boolean finishFlag = true;
	public String timeMark;
	public LinkedList<Long> traverseNodeListOne;
	public LinkedList<Long> traverseNodeListTwo;
	public String waitThreadRelateEdges;
	public HashMap<String, String> trace=new HashMap<String, String>();

	public ConcurrentDynamicSlicer(String dataBasePath, String rootFilePath) {
		super(dataBasePath, rootFilePath);
		traverseNodeListOne = new LinkedList<>();
		traverseNodeListTwo = new LinkedList<>();
		waitThreadRelateEdges="notify|FutureGet|countDown|interrupt";
		progressValue = 0;
	}
	
	public void cleanStatus() {
		traverseNodeListOne.clear();
		traverseNodeListTwo.clear();
		visitedNodes.clear();
		currentNodes.clear();
		trace.clear();
	}
	
	public void sliceBegin(SliceCriterion criterion) {
		switch (criterion.sliceDirection) {
		case FORWARD:
			isBackward = false;
			sliceHandle(criterion);
			break;
		case BACKWARD:
			isBackward = true;
			sliceHandle(criterion);
			break;
		case ALL:
			isBackward = false;
			sliceHandle(criterion);
			cleanStatus();   
			isNewTrace=false;
			isBackward = true;
			sliceHandle(criterion);
			isNewTrace=true;
			break;
		default:
			break;
		}
	}

	/**
	 * ������̬��Ƭ�㷨ʵ��
	 * @param criterion  ����Ƭ׼��
	 */
	public void sliceHandle(SliceCriterion criterion) {
		try {
			if(!slicePrepare(criterion,isNewTrace)){
				System.out.println("Prepare Exception");
				return;
			}
			//second time
			if (criterion.sliceDirection!=SliceDirection.ALL||(criterion.sliceDirection==SliceDirection.ALL&&isBackward)) {
				progressValue = 55; /********###########*********/
			}
			else {
				progressValue = 5; /********###########*********/
			}
			System.out.println("��ʼ��Ƭ...");
			System.out.println(currentNodes);
			System.out.println(visitedNodes);	
			while(!currentNodes.isEmpty()){
				Long nodeID = currentNodes.getFirst();
				currentNodes.removeFirst();
				traverseNodeListOne.add(nodeID);
				//1.StepOne
				while(!traverseNodeListOne.isEmpty()){
					Long nodeIDX = traverseNodeListOne.getFirst();
					Node nodeX=getNode(nodeIDX);
					traverseNodeListOne.removeFirst();
					//(1)ͨ��������ͬ��������CountDown,FutureGet,�ж�
					ArrayList<Node> nodes0 = new ArrayList<>();
					//���������ж�
					if (isBackward) {
						nodes0 = getNodesToSrc(nodeIDX, threadRelateEdges,true);
					}
					else{
						nodes0 = getNodesFromSrc(nodeIDX, threadRelateEdges);
					}
					for (Node nodeY : nodes0) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)) {		
							if(isNotStatementNode(nodeY,nodeX) || isMethodEntryNode(nodeY,nodeX) || (isMark(nodeY) && (isParaOut(nodeX) || isNeededToWait(nodeY, nodeX,waitThreadRelateEdges) || isNeededToInclude(nodeY, nodeX)))){
								if(isMark(nodeY)){	
									ArrayList<Node> relateNodes = getRelateNode(nodeY);					
									for (Node node : relateNodes) {
										nodeRecordHandle(nodeIDX, node, currentNodes);
									}								
								}
								else{
									nodeRecordHandle(nodeIDX, nodeY, currentNodes);
								}
							}
						}
					}
					//(2)����������������
					ArrayList<Node> nodes1 = new ArrayList<>();
					if (isBackward) {
						nodes1 = getNodesToSrc(nodeIDX, withoutParameterOutEdges,false);
					}
					else{
						nodes1 = getNodesFromSrc(nodeIDX, withoutParameterOutEdges);
					}
					for (Node nodeY : nodes1) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)) {
							if(isNotStatementNode(nodeY,nodeX) || isMethodEntryNode(nodeY,nodeX) || (isMark(nodeY) && (isParaOut(nodeX) || isNeededToWait(nodeY, nodeX,waitThreadRelateEdges) || isNeededToInclude(nodeY, nodeX)))){
								if(isMark(nodeY)){
									ArrayList<Node> relateNodes = getRelateNode(nodeY);					
									for (Node node : relateNodes) {
										nodeRecordHandle(nodeIDX, node, traverseNodeListOne);
									}		
								}
								else{							
									nodeRecordHandle(nodeIDX, nodeY, traverseNodeListOne);
								}
							}
						}
					}	
					//(3)����������
					ArrayList<Node> nodes2 = new ArrayList<>();
					if (isBackward) {
						nodes2 = getNodesToSrc(nodeIDX, parameterOutEdge,false);
					}
					else{
						nodes2 = getNodesFromSrc(nodeIDX, parameterOutEdge);
					}
					for (Node nodeY : nodes2) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)) {
							if(isNotStatementNode(nodeY,nodeX) || isMethodEntryNode(nodeY,nodeX) || (isMark(nodeY) && (isParaOut(nodeX) || isNeededToWait(nodeY, nodeX,waitThreadRelateEdges) || isNeededToInclude(nodeY, nodeX)))){
								if(isMark(nodeY)){
									ArrayList<Node> relateNodes = getRelateNode(nodeY);					
									for (Node node : relateNodes) {
										nodeRecordHandle(nodeIDX, node, traverseNodeListTwo);
									}					
								}
								else{
									nodeRecordHandle(nodeIDX, nodeY, traverseNodeListTwo);
								}
							}
						}
					}
				}
				//˫��
				if (criterion.sliceDirection==SliceDirection.ALL&&!isBackward) {
					//first time
					if (progressValue<30) {
						progressValue++; /********###########*********/	
					}
				}
				else{
					if (progressValue<60) {
						progressValue++; /********###########*********/	
					}
				}
				//2.StepTwo
				while(!traverseNodeListTwo.isEmpty()){
					Long nodeIDX = traverseNodeListTwo.getFirst();
					Node nodeX=getNode(nodeIDX);
					traverseNodeListTwo.removeFirst();
					//(1)ͨ��������ͬ��������CountDown,FutureGet,�ж�
					ArrayList<Node> nodes0 = new ArrayList<>();
					//���������ж�
					if (isBackward) {
						nodes0 = getNodesToSrc(nodeIDX, threadRelateEdges,true);
					}
					else{
						nodes0 = getNodesFromSrc(nodeIDX, threadRelateEdges);
					}
					for (Node nodeY : nodes0) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)) {
							if(isNotStatementNode(nodeY,nodeX) || isMethodEntryNode(nodeY,nodeX) || (isMark(nodeY) && (isParaOut(nodeX) || isNeededToWait(nodeY, nodeX,waitThreadRelateEdges) || isNeededToInclude(nodeY, nodeX)))){
								if(isMark(nodeY)){
									ArrayList<Node> relateNodes = getRelateNode(nodeY);	
									for (Node node : relateNodes) {
										nodeRecordHandle(nodeIDX, node, currentNodes);
									}					
								}
								else{
									nodeRecordHandle(nodeIDX, nodeY, currentNodes);
								}
							}
						}
					}
					//(2)��������������ߺͺ������ñ�
					ArrayList<Node> nodes1 = new ArrayList<>();
					//���������ж�
					if (isBackward) {
						nodes1 = getNodesToSrc(nodeIDX, withoutParaInAndMethodInvokeEdges,false);
					}
					else{
						nodes1 = getNodesFromSrc(nodeIDX, withoutParaInAndMethodInvokeEdges);
					}	
					for (Node nodeY : nodes1) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)) {
							if(isNotStatementNode(nodeY,nodeX) || isMethodEntryNode(nodeY,nodeX) || (isMark(nodeY) && (isParaOut(nodeX) || isNeededToWait(nodeY, nodeX,waitThreadRelateEdges) || isNeededToInclude(nodeY, nodeX)))){
								if(isMark(nodeY)){
									ArrayList<Node> relateNodes = getRelateNode(nodeY);					
									for (Node node : relateNodes) {
										nodeRecordHandle(nodeIDX, node, traverseNodeListTwo);
									}					
								}
								else{
									nodeRecordHandle(nodeIDX, nodeY, traverseNodeListTwo);
								}
							}
						}
					}
				}
				//˫��
				if (criterion.sliceDirection==SliceDirection.ALL&&!isBackward) {
					//first time
					if (progressValue<30) {
						progressValue++; /********###########*********/	
					}
				}
				else{
					if (progressValue<60) {
						progressValue++; /********###########*********/	
					}
				}
			}
			//˫��
			if (criterion.sliceDirection==SliceDirection.ALL&&!isBackward) {
				//first time
				progressValue=45; /********###########*********/	
			}
			else{
				progressValue=85; /********###########*********/	
			}
			//��ȫ��    ��   ȫ��&������Ƭ
			if (criterion.sliceDirection!=SliceDirection.ALL||(criterion.sliceDirection==SliceDirection.ALL&&isBackward)) {
				slicesStorage(stateOnlyFlag);
			}
			//˫���һ��
			if (criterion.sliceDirection==SliceDirection.ALL&&!isBackward) {
				//first time
				progressValue=50; /********###########*********/	
			}
			else{
				progressValue=100; /********###########*********/	
			}
			System.out.println(slices);
		} finally {
			if (criterion.sliceDirection!=SliceDirection.ALL||(criterion.sliceDirection==SliceDirection.ALL&&isBackward)) {
				transactionFinish(finishFlag);
			}
			System.out.println("��Ƭ���.");		
		}
	}

	/**
	 * ��Ƭ�ڵ㼯��ʼ��
	 * @param criterion  �� ��Ƭ׼��
	 * @return 
	 */
	public boolean slicePrepare(SliceCriterion criterion,boolean isNewTrace) {
		Set<Node> nodes = findStartNodes(criterion);
		if (nodes==null) {
			return false;
		}
		if(isNewTrace){
			System.out.println("��ʼ���...");
			try(
				FileInputStream fileInput = new FileInputStream(criterion.traceFilePath);			
				BufferedReader bufr = new BufferedReader(new InputStreamReader(fileInput))){
				System.out.println(criterion.traceFilePath);
				String value=null;
				String []par=null;
				String traceId=null;
				String key = null;
				String val = null;
				String []keyPar=null;
				String []valPar=null;
				while ((value=bufr.readLine())!=null){
//					System.err.println(value);
					par=value.split(",");	
					traceId=trace.get(par[1]+","+par[2]);				
					if(traceId==null)
						trace.put(par[1]+","+par[2],par[0]+","+par[0]);
					else{
						trace.put(par[1]+","+par[2],traceId+","+par[0]);
					}
					/*if(!propertyReset(par[1],Integer.parseInt(par[2]),Long.parseLong(par[0]))){
						System.out.println("Trace Exception");
						return false;
					}*/
				}
				if(trace.size()>0){										
					Iterator<Entry<String, String>> iter = trace.entrySet().iterator();
					while (iter.hasNext()) {
						HashMap.Entry<String, String> entry = (HashMap.Entry<String, String>) iter.next();
						key = entry.getKey();
						val = entry.getValue();
						keyPar=key.split(",");	
						valPar=val.split(",");	
						if(!propertyReset(keyPar[0],Integer.parseInt(keyPar[1]),Long.parseLong(valPar[0]),Long.parseLong(valPar[valPar.length-1]))){
							System.out.println(keyPar[0]);
							System.out.println(keyPar[1]);
							System.out.println(valPar[0]);
							System.out.println(valPar[1]);
							System.out.println("Trace Exception");
							return false;
						}
					}
				}
				System.out.println("������.");
			}
			catch(Exception e){
				System.out.println("Trace Exception");
				return false;
			}
		}
		for (Node node : nodes) {
			Long nodeID = node.getId();
			visitedNodes.add(nodeID);
			currentNodes.add(nodeID);
			sliceAdd(node);
		}		
		return true;
	}

	/**
	 * �����ڵ����������ڵ��������	
	 * @param node  ��  ���ڵ�
	 * @return
	 */
	public ArrayList<Node> getRelateNode(Node node) {
		ArrayList<Node> resultNodes = new ArrayList<>();
		if (node==null) {
			return resultNodes;
		}
//		Transaction transaction = database.beginTx();
		try {
			if(node.hasProperty("FilePath") && node.hasProperty("Startline")){
				String filePath = (String) node.getProperty("FilePath");
				String startLine = (String) node.getProperty("Startline");
				String query = "MATCH n WHERE n.FilePath = \""+
						filePath.replace("\\", "\\\\")+"\" AND n.Startline = \""+
						startLine+"\"  RETURN n";

				//System.out.println(query);
				ExtendedExecutionResult result = engine.execute(query);
				if (result.isEmpty()) {
					System.out.println("Relate Nodes not find!");
					return resultNodes;
				}
				//��ȡnode��
				ResourceIterator<Node> nodes = result.javaColumnAs("n");
				while(nodes.hasNext()){
					Node n = nodes.next();	
					resultNodes.add(n);
				}
			}
			else{
				resultNodes.add(node);
			}
		}
		catch(Exception e){

		}
//		finally {
//			transaction.close();
//		}
		return resultNodes;

	}


	/**
	 * �ж�nodeY�Ƿ�Ϊ������ڽڵ�
	 * @param nodeY   ��    Ŀ
	 * @param nodeX   ��    Դ
	 * @return
	 */
	public boolean isMethodEntryNode(Node nodeY,Node nodeX) {
//		Transaction transaction = database.beginTx();
		try {
			if (nodeY.hasLabel(NEO4JAccess.StatementNode)) {
				/*String query = "START n=node("+node.getId()+") "//�ڲ���û��parameter
						+ "MATCH n-[:parameter]->b "
						+ "RETURN b";*/
				String query = "START n=node("+nodeY.getId()+") "
						+ "MATCH b-[:controlDepd]->n "
						+ "RETURN b";
				//��ȡ��ѯ���
				//System.out.println(query);
				ExtendedExecutionResult result = engine.execute(query);
				ResourceIterator<Node> nodes = result.javaColumnAs("b");
				while(nodes.hasNext()){
					Node n = nodes.next();	
					if(n.hasLabel(NEO4JAccess.InstanceMethodNode)){//����ڵ��Ǵ���
						nodeY.setProperty(NEO4JAccess.ElementVirtualMark, timeMark);
						nodeY.setProperty(NEO4JAccess.ElementStartID, nodeX.getProperty(NEO4JAccess.ElementStartID));
						nodeY.setProperty(NEO4JAccess.ElementEndID, nodeX.getProperty(NEO4JAccess.ElementEndID));
//						transaction.success();
						return true;
					}
				}					
			}		
		} 
		catch(Exception e){

		}
//		finally {
//			transaction.close();
//		}
		return false;
	}
	
	/**
	 * �ж�nodeY�Ƿ�Ϊ������ڽڵ�
	 * @param nodeY   ��    Ŀ
	 * @return
	 */
	public boolean isMethodEntryNode(Node nodeY) {
//		Transaction transaction = database.beginTx();
		try {
			if (nodeY.hasLabel(NEO4JAccess.StatementNode)) {
				/*String query = "START n=node("+node.getId()+") "//�ڲ���û��parameter
						+ "MATCH n-[:parameter]->b "
						+ "RETURN b";*/
				String query = "START n=node("+nodeY.getId()+") "
						+ "MATCH b-[:controlDepd]->n "
						+ "RETURN b";
				//��ȡ��ѯ���
				//System.out.println(query);
				ExtendedExecutionResult result = engine.execute(query);
				ResourceIterator<Node> nodes = result.javaColumnAs("b");
				while(nodes.hasNext()){
					Node n = nodes.next();	
					if(n.hasLabel(NEO4JAccess.InstanceMethodNode)){
						return true;
					}
				}
			}		
		} 
		catch(Exception e){

		}
//		finally {
//			transaction.close();
//		}
		return false;
	}


	/**
	 * �ж�nodeY�ڵ��Ƿ�Ϊ��ִ�нڵ�
	 * @param nodeY   ��    Ŀ
	 * @param nodeX   ��    Դ
	 * @return  
	 */
	public boolean isNotStatementNode(Node nodeY,Node nodeX) {	
//		Transaction transaction = database.beginTx();
		try{
			if(!nodeY.hasLabel(NEO4JAccess.StatementNode)){//�� StatementNode�ڵ��Ǵ���
				if(!nodeY.hasProperty(NEO4JAccess.ElementMark) || !nodeY.getProperty(NEO4JAccess.ElementMark).equals(timeMark)){
					nodeY.setProperty(NEO4JAccess.ElementStartID, nodeX.getProperty(NEO4JAccess.ElementStartID));
					nodeY.setProperty(NEO4JAccess.ElementEndID, nodeX.getProperty(NEO4JAccess.ElementEndID));
//					transaction.success();
					return true;
				}		
			}	
		}
		catch(Exception e){

		}
//		finally{
//			transaction.close();
//		}
		return false;
	}

	/**
	 * �ж�node�ڵ��Ƿ񱻹켣���
	 * @param node   ��    ���жϽڵ�
	 * @return  
	 */
	public boolean isMark(Node node) {	
//		Transaction transaction = database.beginTx();
		try{
			if(node.hasProperty(NEO4JAccess.ElementMark) && node.getProperty(NEO4JAccess.ElementMark).equals(timeMark)){
				return true;
			}	
		}
		catch(Exception e){

		}
//		finally{		
//			transaction.close();
//		}
		return false;
	}

	/**
	 * ��ִ���Ⱥ�˳���ж�Y---->X(����)��X---->Y(ǰ��)�ڵ���Y�Ƿ�Ӧ�ñ���������
	 * @param nodeY   ��    Ŀ
	 * @param nodeX   ��    Դ
	 * @return  
	 */
	public boolean isNeededToInclude(Node nodeY,Node nodeX) {		
//		Transaction transaction = database.beginTx();
		try{
			Long nodeIDX;
			Long nodeIDY;
			if (isBackward) {
				nodeIDX = (Long)nodeX.getProperty(NEO4JAccess.ElementEndID);
				nodeIDY = (Long)nodeY.getProperty(NEO4JAccess.ElementStartID);
				return nodeIDY < nodeIDX;
			}
			else{
				nodeIDX = (Long)nodeX.getProperty(NEO4JAccess.ElementStartID);
				nodeIDY = (Long)nodeY.getProperty(NEO4JAccess.ElementEndID);
				return nodeIDY > nodeIDX;
			}
		}
		catch(Exception e){
			return true;//��StatementNode������ڵ�
		}
//		finally{
//			transaction.close();
//		}
	}

	/**
	 * �ж�Y---->X(����)��X---->Y(ǰ��)�ڵ���Ƿ���ָ�����͵ı�
	 * @param nodeY   ��    Ŀ
	 * @param nodeX   ��    Դ
	 * @return  
	 */
	public boolean isNeededToWait(Node nodeY,Node nodeX,String edges){
//		Transaction transaction = database.beginTx();
		try{
			String query;
			if (isBackward) {
				query = "START a=node("+nodeY.getId()+"), b=node("+nodeX.getId()+") MATCH a-[r:"+edges+"]->b return a";
			}
			else{
				query = "START a=node("+nodeY.getId()+"), b=node("+nodeX.getId()+") MATCH b-[r:"+edges+"]->a return a";
			}
			//System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (!result.isEmpty()) {
				return true;
			}
		}
		catch(Exception e){

		}
//		finally {			
//			transaction.close();
//		}
		return false;
	}
	
	/**
	 * �ж�X�Ƿ���FormalParaOutNode
	 * @param nodeX   ��    Դ
	 * @return  
	 */
	public boolean isParaOut(Node nodeX){
		try{//returnʱ����return���󷽷�������traceId˳���෴
			if(nodeX.hasLabel(NEO4JAccess.FormalParaOutNode)){
				return true;
			}
		}
		catch(Exception e){

		}
		return false;
	}

	/**
	 * �ڵ��¼����
	 * 1.���ʹ����ô���
	 * 2.������Ƭ(·�����к�)
	 * @param src  :�ڵ��ǰ��
	 * @param node ���������Ľڵ�
	 */
	public void nodeRecordHandle(Long src,Node node,LinkedList<Long> nodeList){
//		System.out.println(src+"-->"+node.getId());
		Long nodeID = node.getId();
		if (visitedNodes.contains(nodeID)) {
			return;
		}
		else{
			visitedNodes.add(nodeID);			  	
			nodeList.add(nodeID);
		    sliceAdd(node);
		}
	}

	/**
	 * ���ڵ�����������뵽��Ƭ����
	 * @param node
	 */
	public void sliceAdd(Node node) {
		if (node==null) {
			return ;
		}
		//��������
//		Transaction transaction = database.beginTx();
		
		try{
			if ((stateOnlyFlag && node.hasLabel(NEO4JAccess.StatementNode) && !isMethodEntryNode(node)) || (!stateOnlyFlag && (node.hasLabel(NEO4JAccess.StatementNode)||node.hasLabel(NEO4JAccess.ClassNode)||
					node.hasLabel(NEO4JAccess.MemberVariableNode)||node.hasLabel(NEO4JAccess.InstanceMethodNode)||
					node.hasLabel(NEO4JAccess.InterfaceNode)||node.hasLabel(NEO4JAccess.AbstractMethodNode) || 
					node.hasLabel(NEO4JAccess.ActualParaInNode)||node.hasLabel(NEO4JAccess.ActualParaOutNode)))){
				if (node.hasProperty("FilePath") && node.hasProperty("Startline") && ((node.hasProperty(NEO4JAccess.ElementMark) && node.getProperty(NEO4JAccess.ElementMark).equals(timeMark)) || (node.hasProperty(NEO4JAccess.ElementVirtualMark) && node.getProperty(NEO4JAccess.ElementVirtualMark).equals(timeMark)) || node.hasLabel(NEO4JAccess.ClassNode))) {
					//					node.addLabel(NEO4JAccess.SlicedNode);				
					String filePath = (String)node.getProperty("FilePath");
					Integer startLine = Integer.valueOf((String) node.getProperty("Startline"));
					if (startLine<=0) {
						return;
					}
					if (slices.containsKey(filePath)) {
						slices.get(filePath).add(startLine);
					}
					else{
						TreeSet<Integer> fileLineSet = new TreeSet<>();
						fileLineSet.add(startLine);
						slices.put(filePath, fileLineSet);
					}
//					System.out.println("<"+node.getId()+","+startLine+">");
				}
			}
		}
		catch(Exception e){

		}
//		finally{
//			transaction.close();
//		}
	}

	/**
	 * ��ǹ켣
	 * @param filePath ����·��
	 * @param lineNumber �ڵ��к�
	 * @param traceId �켣���
	 * @return �����ڵ�����ݿ�ID�ţ�����Ϊlong
	 */
	public boolean propertyReset(String filePath,int lineNumber, long traceStartId, long traceEndId) {
		//��������
//		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "MATCH node WHERE node.FilePath = \""+
					filePath.replace("\\", "\\\\")+"\" AND node.Startline = \""+
					lineNumber+"\"  RETURN node";
			//��ȡ��ѯ���
			//System.err.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			//System.out.println(result.size());
			if (result.isEmpty()) {
				System.out.println("Can not find the statenode");
				return false;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("node");
			while(nodes.hasNext()){
				Node node = nodes.next();
				/*û��transaction.success()�������ݿ��б��δ��ʾ�����ǲ�ѯ��ʱ���������*/
				node.setProperty(NEO4JAccess.ElementMark, timeMark);
				node.setProperty(NEO4JAccess.ElementStartID, traceStartId);
				node.setProperty(NEO4JAccess.ElementEndID, traceEndId);	
//				System.out.println(node.getId());
				/*if(node.hasProperty(NEO4JAccess.ElementMark)){
					node.setProperty(NEO4JAccess.ElementEndID, traceId);
				}
				else{
					node.setProperty(NEO4JAccess.ElementMark, 1);
					node.setProperty(NEO4JAccess.ElementStartID, traceId);
					node.setProperty(NEO4JAccess.ElementEndID, traceId);	
				}		*/			
			}
//			transaction.success();	
			return true;
		}	
		catch(Exception e){

		}
//		finally {			
//			transaction.close();
//		}
		return false;
	}

	public static void main(String[] args) {
		
		String dataBasePath = "G:\\OOJavaSlicer_huaweiBug\\Repository\\Graphs\\TestCase.db";
		ConcurrentDynamicSlicer concurrentSlicer = new ConcurrentDynamicSlicer(dataBasePath,"D:\\");
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		concurrentSlicer.timeMark=dateFormat.format(date);
//		concurrentSlicer.stateOnlyFlag= true;
		//Criterion
//		String filePath = "E:\\temp\\TestCase\\src\\com\\TestCase06\\Task.java";
		String filePath = "G:\\TestCase\\src\\com\\TestCase07\\CountDown2.java";
		int lineNumber = 37;
		ArrayList<String> vars = new ArrayList<>();
		vars.add("counter");
		String traceFile = "G:\\OOJavaSlicer_huaweiBug\\Repository\\Traces\\traceFile.txt";
//		String traceFile = "E:\\temp\\TestCase_Instrument\\Repository\\Traces\\traceFile.txt";
		SliceCriterion criterion = new SliceCriterion(filePath, lineNumber, vars, traceFile,SliceDirection.BACKWARD);
		//SliceHandle
//		concurrentSlicer.sliceHandle(criterion);
		concurrentSlicer.sliceBegin(criterion);
	}

}
