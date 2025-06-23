package oo.com.iseu.CSlicer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.neo4j.cypher.ExtendedExecutionResult;
import org.neo4j.cypher.internal.compiler.v1_9.parser.ParserPattern.No;
import org.neo4j.cypher.internal.compiler.v2_2.PathImpl;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import oo.com.iseu.UI.Basic.SliceDirection;
import oo.storage01.DGEdge;
import oo.storage01.NEO4JAccess;

public class ConcurrentStaticSlicer extends ConcurrentSlicer{
	public static int progressValue;
	public boolean isBackward = true;
	public boolean stateOnlyFlag = false;
	public boolean finishFlag = true;
	public HashMap<Long,HashMap<String, Long>> currentNodesThreadWitness;//��ǰ�ڵ�
	public LinkedList<Long> traverseNodeListOne;
	public LinkedList<Long> traverseNodeListTwo;
	private HashSet<Long> threadSearchNodes;
	private Long runtime;
	
	
	
	public ConcurrentStaticSlicer(String dataBasePath, String rootFilePath) {
		super(dataBasePath, rootFilePath);
		progressValue = 0;
		currentNodesThreadWitness = new HashMap<>();
		traverseNodeListOne = new LinkedList<>();
		traverseNodeListTwo = new LinkedList<>();
		threadSearchNodes = new HashSet<>();
	}

	public void cleanStatus() {
		currentNodesThreadWitness.clear();
		traverseNodeListOne.clear();
		traverseNodeListTwo.clear();
		threadSearchNodes.clear();
		visitedNodes.clear();
		currentNodes.clear();
	}
	public void heheTest() {
		String query= "MATCH n RETURN n";
		ExtendedExecutionResult result = engine.execute(query);
		if (result.isEmpty()) {
			return ;
		}
		//��ȡnode��
		ResourceIterator<Node> nodes = result.javaColumnAs("n");
		while(nodes.hasNext()){
			Node n = nodes.next();	
			HashSet<Node> threads = new HashSet<>();
//			if (n.hasProperty(NEO4JAccess.ElementStartline)) {
//				System.out.println(n.getProperty(NEO4JAccess.ElementStartline));
//			}
			threadName(n, threads);
//			System.out.println("�ڵ����ͣ�"+n.getLabels());
//			System.out.println("�����̣߳�"+threads);
		}	
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
			//System.out.println("ALLLLLLL");
			isBackward = false;
			sliceHandle(criterion);
			cleanStatus();      
			isBackward = true;
			sliceHandle(criterion);
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
			slicePrepare(criterion);
			runtime = System.currentTimeMillis();
			//second time
			if (criterion.sliceDirection==SliceDirection.ALL&&isBackward) {
				progressValue = 55; /********###########*********/
			}
			else {
				progressValue = 5; /********###########*********/
			}
			//System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL:"+progressValue);
			System.out.println("INIT:"+currentNodes);
			System.out.println("INIT:"+visitedNodes);	
			System.out.println("Start-------------------------------------------------");
			while(!currentNodes.isEmpty()){
				System.err.println("Current:"+currentNodes);
				Long nodeID = currentNodes.getFirst();
				currentNodes.removeFirst();
				traverseNodeListOne.add(nodeID);
				
				//1.StepOne
				while(!traverseNodeListOne.isEmpty()){
					Long nodeIDX = traverseNodeListOne.getFirst();
//					System.out.println("----------------------------------------------");
//					System.out.println("Begin:"+nodeIDX);
					traverseNodeListOne.removeFirst();
					//(1)ͨ��������ͬ��������CountDown,FutureGet,�ж�,threadStart
					ArrayList<Node> nodes0 = new ArrayList<>();
					//���������ж�
					if (isBackward) {
						nodes0 = getNodesToSrc(nodeIDX, threadRelateEdges,true);
					}
					else{
						nodes0 = getNodesFromSrc(nodeIDX, threadRelateEdges);
					}
//					System.out.println("X:"+nodeIDX+"---"+currentNodesThreadWitness.get(nodeIDX));
					//System.out.println("The nodes Y is(Thread) : "+nodes0);
					for (Node nodeY : nodes0) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)||!isSameStatement(nodeIDX,nodeIDY)) {
							if (isNeededToInclude(nodeY, getNode(nodeIDX))) {
								ArrayList<Node> relateNodes = getRelateNode(nodeY,nodeIDX);
								for (Node node : relateNodes) {
									//System.out.println(node);
									nodeRecordHandle(nodeIDX, node, currentNodes,true);
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
//					System.out.println("X:"+nodeIDX);
//					System.out.println("The nodes Y is(WPOut) : "+nodes1);
					for (Node nodeY : nodes1) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)||!isSameStatement(nodeIDX,nodeIDY)/*&&isNeededToInclude(nodeY, getNode(nodeIDX))*/) {
							ArrayList<Node> relateNodes = getRelateNode(nodeY, nodeIDX);
							for (Node node : relateNodes) {
								nodeRecordHandle(nodeIDX, node, traverseNodeListOne,false);
							}
							//nodeRecordHandle(nodeIDX, nodeY, traverseNodeListOne,false);
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
//					System.out.println("X:"+nodeIDX);
//					System.out.println("The nodes Y is(POut) : "+nodes2);
					for (Node nodeY : nodes2) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)||!isSameStatement(nodeIDX,nodeIDY)/*&&isNeededToInclude(nodeY, getNode(nodeIDX))*/) {
							ArrayList<Node> relateNodes = getRelateNode(nodeY, nodeIDX);
							for (Node node : relateNodes) {
								nodeRecordHandle(nodeIDX, node, traverseNodeListTwo,false);
							}
							//nodeRecordHandle(nodeIDX, nodeY, traverseNodeListTwo,false);
						}
					}
					//IDX���̼߳�֤ȥ��
					currentNodesThreadWitness.remove(nodeIDX);
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
//					System.out.println("X:"+nodeIDX);
					//System.out.println("The nodes Y(Thread2) is : "+nodes0);
					for (Node nodeY : nodes0) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)||!isSameStatement(nodeIDX,nodeIDY)) {
							if (isNeededToInclude(nodeY, getNode(nodeIDX))) {
								ArrayList<Node> relateNodes = getRelateNode(nodeY,nodeIDX);
								for (Node node : relateNodes) {
									nodeRecordHandle(nodeIDX, node, currentNodes,true);
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
//					System.out.println("X:"+nodeIDX);
//					System.out.println("The nodes Y is(WPIn) : "+nodes1);
					for (Node nodeY : nodes1) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)||!isSameStatement(nodeIDX,nodeIDY)/*&&isNeededToInclude(nodeY, getNode(nodeIDX))*/) {
							ArrayList<Node> relateNodes = getRelateNode(nodeY, nodeIDX);
							for (Node node : relateNodes) {
								nodeRecordHandle(nodeIDX, node, traverseNodeListTwo,false);
							}
							//nodeRecordHandle(nodeIDX, nodeY, traverseNodeListTwo,false);
						}
					}
					//IDX���̼߳�֤ȥ��
					currentNodesThreadWitness.remove(nodeIDX);
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
			System.out.println("TIME:"+(System.currentTimeMillis()-runtime));
		}
	}
	
	public ArrayList<Node> getTEST() {
		ArrayList<Node> resultNodes = new ArrayList<>();
//		Transaction transaction = database.beginTx();
//		try {
			String query = "MATCH (n:SlicedNode) RETURN n ";
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
//				System.out.println("Relate Nodes not find!");
				return resultNodes;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("n");
			while(nodes.hasNext()){
				Node n = nodes.next();	
				resultNodes.add(n);
			}
			return resultNodes;
//		} finally {
//			transaction.close();
//		}
	}
	/**
	 * ��Ƭ�ڵ㼯��ʼ��
	 * @param criterion  �� ��Ƭ׼��
	 */
	public void slicePrepare(SliceCriterion criterion) {
		reverse_reduce();
		/************���ұ����ڵ�,��û���ҵ�������Ϊ��׼**************/
		Set<Node> nodes = findStartNodes(criterion);
		/**************************************************/
		if (nodes==null) {
			return;
		}
		System.out.println("��ʼ�ڵ㼯��"+nodes.size());
		for (Node node : nodes) {
			Long nodeID = node.getId();
			visitedNodes.add(nodeID);
//			nodeMarkSliced(node);
			currentNodes.add(nodeID);
			//�̼߳�֤��ʼ��
			HashMap<String, Long> hashMap = new HashMap<>();
			HashSet<Node> hashSet = new HashSet<>();
			threadName(node, hashSet);
			threadSearchNodes.clear();
//			System.out.println("Thread:"+hashSet);
//			Transaction transaction = database.beginTx();	
			if (!hashSet.isEmpty()) {
				for (Node n : hashSet) {
					try {
						hashMap.put((String) n.getProperty("Name"), nodeID);
					} finally {
//						transaction.success();
//						transaction.close();
					}
				}
			}	
			currentNodesThreadWitness.put(nodeID, hashMap);
			System.out.println("��ʼ���̼߳�֤��"+currentNodesThreadWitness.get(nodeID));
			sliceAdd(node);
		}
	}
	
//	/**
//	 * �����ڵ����������ڵ��������(�ײ����)	
//	 * @param node  ��  ���ڵ�
//	 * @return
//	 */
//	public ArrayList<Node> getRelateNode(Node node,Long src) {
//		ArrayList<Node> resultNodes = new ArrayList<>();
//		if (node==null) {
//			return resultNodes;
//		}
//		resultNodes.add(node);
//		Node nodeSrc = getNode(src);
////		Transaction transaction = database.beginTx();
////		try {
//		if (isBackward) {
//			//·�������кŲ�ͬ�����еײ���չ
//			if (node.hasProperty("FilePath")&&node.hasProperty("Startline")&&
//				nodeSrc.hasProperty("FilePath")&&nodeSrc.hasProperty("Startline")&&
//				(!node.getProperty("FilePath").equals(nodeSrc.getProperty("FilePath"))||
//				!node.getProperty("Startline").equals(nodeSrc.getProperty("Startline"))	
//						)) {
////				String filePath = (String) node.getProperty("FilePath");
////				String startLine = (String) node.getProperty("Startline");
//				Long id = node.getId();
//				//String query = "MATCH n WHERE n.Startline=\""+startLine+"\" AND n.FilePath=\""+filePath.replace("\\", "\\\\")+"\" RETURN n";
//				String query = "START a=node("+id+") MATCH a-[r:edgeType1|edgeType2]->n RETURN n";
////				System.out.println(query);
//				ExtendedExecutionResult result = engine.execute(query);
//				if (result.isEmpty()) {
////					System.out.println("Relate Nodes not find!");
//					return resultNodes;
//				}
//				//��ȡnode��
//				ResourceIterator<Node> nodes = result.javaColumnAs("n");
//				while(nodes.hasNext()){
//					Node n = nodes.next();	
//					resultNodes.add(n);
//				}
//			}
//		}
////		else {
////			resultNodes = getNodesFromSrc(node.getId(), "toObject|typeAnalysis*");
////			resultNodes.add(node);
//////			transaction.success();
////
////		}
//		return resultNodes;	
////		} finally {
////			transaction.close();
////		}
//	}

	public ArrayList<Node> getRelateNodeInMethod(Node node) {
		ArrayList<Node> resultNodes = new ArrayList<>();
		if (node==null) {
			return resultNodes;
		}
		if (node.hasLabel(NEO4JAccess.ActualParaInNode)&&node.hasProperty("FilePath")&&node.hasProperty("Startline")) {
			String filePath = (String) node.getProperty("FilePath");
			String startLine = (String) node.getProperty("Startline");
			String query = "MATCH n WHERE n.Startline=\""+startLine+"\" AND n.FilePath=\""+filePath.replace("\\", "\\\\")+"\" RETURN n";
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
//				System.out.println("Relate Nodes not find!");
				return resultNodes;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("n");
			while(nodes.hasNext()){
				Node n = nodes.next();
				if (n.hasLabel(NEO4JAccess.ActualParaInNode)) {
					resultNodes.add(n);
				}		
			}
		}
		else{
			resultNodes.add(node);
		}
		return resultNodes;	
	}
	/**
	 * �ж�Y---->X�ڵ���Y�Ƿ�Ӧ�ñ���������(�̼߳�)
	 * @param nodeY   ��    Դ
	 * @param nodeX   ��    Ŀ
	 * @return  
	 */
	public boolean isNeededToInclude(Node nodeY,Node nodeX) {
		//System.out.println(nodeY+"�Ƿ񱻰�����");
		//��������
//		Transaction transaction = database.beginTx();
//		try{
			if (nodeY.hasLabel(NEO4JAccess.ClassNode)||nodeY.hasLabel(NEO4JAccess.ActualParaInNode)
				||nodeY.hasLabel(NEO4JAccess.ActualParaOutNode)||nodeY.hasLabel(NEO4JAccess.FormalParaInNode)||
				nodeY.hasLabel(NEO4JAccess.FormalParaOutNode)) {
//				System.out.println("YES");
				return true;
			}
			Long nodeIDX = nodeX.getId();
			Long nodeIDY = nodeY.getId();
			HashMap<String, Long> witness = currentNodesThreadWitness.get(nodeIDX);
			HashSet<Node> threads = new HashSet<>();
			threadName(nodeY, threads);
			threadSearchNodes.clear();
				System.out.println("ThreadNames:"+threads);
				System.out.println("WITNESS:"+witness);
//				
//				System.out.println("Size:"+threads.size());
			for (Node node : threads) {
				String threadName = (String) node.getProperty("Name");
				//T.has(��(s)&&Reachable(y,T[��(s)]))|| !T.has(��(s)) 
				if (isBackward) {
//					System.out.println(threadName);
					if (!witness.containsKey(threadName)||
						(witness.containsKey(threadName)&&reachable(nodeY,getNode(witness.get(threadName)), node))) {
//						System.out.println("YES");
						return true;
					}
				}
				else{
					if (!witness.containsKey(threadName)||
						(witness.containsKey(threadName)&&reachable(getNode(witness.get(threadName)),nodeY, node))) {
						System.out.println("YES");
						return true;
					}
				}
			}
			System.out.println("NO");
			return false;
//		}
//		finally{
//			transaction.close();
//		}
	}
	
	/**
	 * �ڵ��¼����
	 * 1.���ʹ����ô���
	 * 2.������Ƭ(·�����к�)
	 * 3.�����̼߳�֤
	 * @param src  :�ڵ��ǰ��
	 * @param node ���������Ľڵ�
	 */
	public void nodeRecordHandle(Long src,Node node,LinkedList<Long> nodeList,boolean isAcrossThread){
		Long nodeID = node.getId();
		if (visitedNodes.contains(nodeID)) {
			return;
		}
		else{
			visitedNodes.add(nodeID);
//			nodeMarkSliced(node);
//			System.out.println("Node:"+nodeID+" included!");
			nodeList.add(nodeID);
			//��������
//			Transaction transaction = database.beginTx();
//			try{
				sliceAdd(node);
				/*********************�̼߳�֤��¼*********************/
				
				currentNodesThreadWitness.put(nodeID,(HashMap<String, Long>)currentNodesThreadWitness.get(src).clone());	
				
				/********************
				 * 1.�ڵ������߳���
				 * 2.witness����
				 * ******************/
				if (node.hasLabel(NEO4JAccess.StatementNode)||node.hasLabel(NEO4JAccess.ClassNode)||
					node.hasLabel(NEO4JAccess.InstanceMethodNode)||node.hasLabel(NEO4JAccess.InterfaceNode)||node.hasLabel(NEO4JAccess.AbstractMethodNode)){
					HashMap<String, Long> threadWitness = currentNodesThreadWitness.get(nodeID);
					if (isAcrossThread) {
						HashSet<Node> threads = new HashSet<>();
						threadName(node, threads);
						threadSearchNodes.clear();
						for (Node threadNode : threads) {
							String threadName = (String) threadNode.getProperty("Name");
							threadWitness.put(threadName, nodeID);
						}
					}
					else{
						Set<Entry<String, Long>> vars = threadWitness.entrySet();
						for (Entry<String, Long> entry : vars) {
							entry.setValue(nodeID);
						}
					}

//					transaction.success();
					System.out.println("ID:"+nodeID+threadWitness);
				}
				
//			}
//			finally{
//				transaction.close();
//			}
		}
	}
	
	/**
	 * ��ȡfieldNode��·��
	 * @param node
	 * @return
	 */
	public String getFieldNodeFilePath(Node node) {
		ArrayList<Node> classNode = getNodesToSrcByEdgeType(node.getId(), DGEdge.dataMember);
		if (!classNode.isEmpty()) {
			return classNode.get(0).getProperty(NEO4JAccess.ElementPath).toString();
		}
		return null;
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
//		try{
			if ((stateOnlyFlag && node.hasLabel(NEO4JAccess.StatementNode) && !isMethodEntryNode(node)) || (!stateOnlyFlag && (node.hasLabel(NEO4JAccess.StatementNode)||node.hasLabel(NEO4JAccess.ClassNode)||
				node.hasLabel(NEO4JAccess.MemberVariableNode)||node.hasLabel(NEO4JAccess.InstanceMethodNode)||
				node.hasLabel(NEO4JAccess.InterfaceNode)||node.hasLabel(NEO4JAccess.AbstractMethodNode)||
				node.hasLabel(NEO4JAccess.ActualParaInNode)||node.hasLabel(NEO4JAccess.ActualParaOutNode)))){
				if (node.hasProperty("FilePath")&&node.hasProperty("Startline")||node.hasLabel(NEO4JAccess.MemberVariableNode)) {
					Integer startLine = Integer.valueOf((String) node.getProperty("Startline"));
					String filePath ;
					if (node.hasLabel(NEO4JAccess.MemberVariableNode)&&!node.hasProperty("FilePath")) {
						//System.out.println("MEMBERVAR!!!!!!!!!!!!");
						filePath = getFieldNodeFilePath(node);
					}
					else{
						filePath = (String)node.getProperty("FilePath");
					}
					node.addLabel(NEO4JAccess.SlicedNode);
//					System.out.println("IS Sliced? "+node.hasLabel(NEO4JAccess.SlicedNode));
//					System.out.println("File:"+filePath);
//					System.out.println("Line:"+startLine);
					
					if (startLine<=0||filePath==null) {
						System.out.println("Can't find!");
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
//		}
//		finally{
////			transaction.success();
//			transaction.close();
//		}
	}
	
	public void reverse_reduce() {
//		Transaction transaction = database.beginTx();
//		try {
			String query = "MATCH ()-[r:classMember_reverse|controlDepd_reverse|instantiation_reverse|objMember_reverse|parameter_reverse| typeAnalysis_reverse|"+ 
							"dataDepd_reverse|inputParameter_reverse|outputParameter_reverse|methodInvocation_reverse|methodImplment_reverse|methodOverwrite_reverse|interfaceImplement_reverse|"+
							"inherit_reverse|abstractMember_reverse|pkgMember_reverse|pkgDepd_reverse|threadStart_reverse|notify_reverse|s_communicate_reverse|FutureGet_reverse|"+
							"countDown_reverse|theadSecurity_reverse|competenceAcquire_reverse|competenceRelease_reverse|competenceDepd_reverse|m_communicate_reverse|mo_communicate_reverse|"+
							"connection_reverse|c_communicate_reverse|interClassMessage_reverse|anonymousClassDeclare_reverse|CFGedge_reverse|summaryEdge_reverse|dataMember_reverse|"+
							"toObject_reverse|interrupt_reverse|threadRisk_reverse|authorityAcquire_reverse|authorityRelease_reverse]->() DELETE r";
			ExtendedExecutionResult result = engine.execute(query);
			System.out.println(query);
//			transaction.success();
//		} finally {
//			transaction.close();
//		}
	}
	
	public void nodeMarkSliced(Node node) {
//		Transaction transaction = database.beginTx();
//		try {
			node.addLabel(NEO4JAccess.SliceNode);
			System.out.println(node.getId());
			System.out.println("HANDLE: "+node.hasLabel(NEO4JAccess.SliceNode));
//			transaction.success();
//		} finally {
//			transaction.close();
//		}
	}
	
//	/**
//	  * ���ش�Դ�ڵ㾭���ض��ߵ���Ľڵ㼯(��ʼ��ʱ�������)
//	  * @param src       ��Դ�ڵ�
//	  * @param edgeType  ��������
//	  */
//	public void getNodesFromSrcByEdgeType(Long src,DGEdge edgeType){
//		//��������
////		Transaction transaction = database.beginTx();
////		try{
//			//��ѯ���
//			String query = "START a=node("+src+") MATCH a-[r:"+edgeType+"]->b return b";
////			System.out.println(query);
//			ExtendedExecutionResult result = engine.execute(query);
//			if (result.isEmpty()) {
////				System.out.println("Can not find the nodes!");
//				return ;
//			}
//			//��ȡnode��
//			ResourceIterator<Node> nodes = result.javaColumnAs("b");
//			while(nodes.hasNext()){
//				Node node = nodes.next();	
////				nodeRecordHandle(src,node,currentNodes);
////				System.out.println(node.getId());
//			}
////			transaction.success();
////		}	
////		finally {
////			transaction.close();
////		}
//	}
	
	
	/**
	 * ��̬��Ƭ���ĺ���1����(node)
	 * ��¼һ��������ڵ��̣߳����ܱ�����߳�ִ��
	 * @param node     �����ڵ�
	 * @param threads  ���̼߳�(���ڴ洢�߳���)
	 */
	public void threadName(Node node,HashSet<Node> threads) {
//		System.out.println("NODE: "+node);
		if (node==null||threadSearchNodes.contains(node.getId())) {
			return ;
		}
		threadSearchNodes.add(node.getId());
		//��������
//		Transaction transaction = database.beginTx();
//		try{

			if (node.hasProperty("MethodID")) {
//				System.out.println("Method!");
//				System.out.println(node);
//				System.out.println("methodID:"+node.getProperty("MethodID"));
				long methodID = Long.valueOf((String) node.getProperty("MethodID"));
				if (methodID<0) {
					return ;
				}
				//��ȡ������ں����ڵ�
				Node methodNode = getNode(methodID);
				String methodName = (String) methodNode.getProperty("Name");
//				System.out.println(methodName);
				//��Ϊ�̺߳���
				if (methodName.equals("run")||methodName.equals("compute")||methodName.equals("call")) {
					Node classNode = getNode(Long.valueOf((String) methodNode.getProperty("ClassID")));
					threads.add(classNode);			
					return;
				}
				else if (methodName.equals("main")) {
					threads.add(methodNode);			
					return;
				}
				else {
					ArrayList<Node> nodes = getNodesToSrcByEdgeType(Long.valueOf((String) node.getProperty("MethodID")), DGEdge.methodInvocation);
					for (Node n : nodes) {
//						System.out.println(n);
						threadName(n, threads);
					}
				}
//				transaction.success();
			}
			else if (node.hasLabel(NEO4JAccess.ThreadEntryNode)) {
//				System.out.println("ThreadEntryNode");
				if (node.hasProperty("ClassID")) {
					Node classNode = getNode(Long.valueOf((String) node.getProperty("ClassID")));
					threads.add(classNode);			
					return;
				}
			}
			else if (node.hasLabel(NEO4JAccess.InstanceMethodNode)&&node.hasProperty("Name")) {
//				System.out.println("InstanceMethodNode");
				String methodName = (String) node.getProperty("Name");
//				System.out.println(methodName);
				//��Ϊ�̺߳���
				if (methodName.equals("run")||methodName.equals("compute")||methodName.equals("call")) {
					Node classNode = getNode(Long.valueOf((String) node.getProperty("ClassID")));
					threads.add(classNode);			
					return;
				}
				else if (methodName.equals("main")) {
					threads.add(node);			
					return;
				}
				else {
					ArrayList<Node> nodes = getNodesToSrcByEdgeType(node.getId(), DGEdge.methodInvocation);
					for (Node n : nodes) {
						threadName(n, threads);
					}
				}		
			}
			else if (node.hasLabel(NEO4JAccess.PolyObjectExtensionNode)) {
//				System.out.println("PolyObject");
				ArrayList<Node> reNode = getNodesToSrcByEdgeType(node.getId(), DGEdge.typeAnalysis);
				for (Node n : reNode) {
					threadName(n, threads);
				}
			}


//		}	
//		finally {
//			
//			transaction.close();
//		}	
	}
	
	public Node getObjectNode(Long src,DGEdge edgeType,boolean isToSrc) {
		//��������
//		Transaction transaction = database.beginTx();
//		try{
			//��ѯ���
			String query;
			if (isToSrc) {	
				query = "START b=node("+src+") MATCH a-[r:"+edgeType+"]->b return a";
			}
			else{
				query = "START b=node("+src+") MATCH b-[r:"+edgeType+"]->a return a";
			}
//			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
//				System.out.println("Can not find the nodes!");
				return null;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("a");
			if(nodes.hasNext()){
				Node node = nodes.next();
				return node;
			}			
//		}	
//		finally {
//			transaction.success();
//			transaction.close();
//		}
		return null;
	}
	/**
	 * ��̬��Ƭ���ĺ���2��Reachable(src,des)
	 * @return
	 */
	public boolean reachable(Node src,Node des,Node threadNode) {
		//1.ID�������ж�
		Long srcID = src.getId();
		Long desID = des.getId();
		Long thrID = threadNode.getId();
		if (srcID==-1||desID==-1||thrID==-1) {
			return false;
		}
		//2.�ֱ��ȡ�߳̽ڵ㵽���ڵ��·��
		ArrayList<PathImpl> srcPaths = getPaths(thrID, srcID);
		ArrayList<PathImpl> desPaths = getPaths(thrID, desID);
		//3.��·���ıȽ�
		if (desPaths==null||srcPaths==null) {
			return false;
		}
		for (PathImpl srcPath : srcPaths) {
			//(1)ThreadNode-------->srcNode:PATH
			Iterator<Node> srcNodes = srcPath.nodes().iterator();
			for (PathImpl desPath : desPaths) {
				//(2)ThreadNode-------->desNode:PATH
				Iterator<Node> desNodes = desPath.nodes().iterator();
				//��¼���ڵ��Ƿ����ѭ��
				boolean isParentLoop = false;
				Node srcNode=null;
				Node desNode=null;
				while (desNodes.hasNext()&&srcNodes.hasNext()) {
					srcNode = (Node) srcNodes.next();
					desNode = (Node) desNodes.next();
					//(3)·���ڵ�ID�Ƿ���ͬ
					if (srcNode.getId()!=desNode.getId()) {
//						System.out.println("SRC:"+srcNode.getId());
//						System.out.println("DES:"+desNode.getId());
						//(4)<���ڵ���ͬ��������>�Ƚ�NodeID��С
						if (srcNode.getId()<desNode.getId()||isParentLoop) {
//							System.out.println(src.getProperty("Startline")+" can reach to "+des.getProperty("Startline"));
							return true;
						}
						else{
//							System.out.println("can not reach!");
							break;
						}
					}
					else{
						//(5)���ڵ�ѭ����Ǵ���
						if (srcNode.hasLabel(NEO4JAccess.LoopNode)&&desNode.hasLabel(NEO4JAccess.LoopNode)) {
							isParentLoop = true;
						}
						else{
							isParentLoop = false;
						}
					}
				}
				if ((srcNode!=null&&desNode!=null&&srcNode.getId()==desNode.getId())
					&&desNodes.hasNext()&&!srcNodes.hasNext()) {
					return true;
				}
			}
		}
//		System.out.println(srcID+" can not reach to "+desID);
		return false;
	}

	
	public static void main(String[] args) {
		
		String dataBasePath = "G:\\OOJavaSlicer_huaweiBug\\Repository\\Graphs\\TestCase.db";
//		String dataBasePath = "H:\\hadoop-hdfs.db";
		ConcurrentStaticSlicer concurrentSlicer = new ConcurrentStaticSlicer(dataBasePath,"D:\\");
		//concurrentSlicer.isBackward = true;
		//concurrentSlicer.stateOnlyFlag= true;
		//Criterion
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase01\\Storage.java";
//		int lineNumber = 41;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("list");

		
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase02\\ForkJoinTest.java";
//		int lineNumber = 21;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("counter");
	
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase03\\SemaphoreTest.java";
//		int lineNumber = 27;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("semp");
		
		String filePath = "G:\\TestCase\\src\\com\\TestCase07\\CountDown2.java";
		int lineNumber = 37;
		ArrayList<String> vars = new ArrayList<>();
		vars.add("");
		
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase05\\ThreadInterrupt02.java";
//		int lineNumber = 31;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("semp");
		
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase06\\Task.java";
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase06\\TestFuture.java";
//		int lineNumber = 59;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("semp");
		
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase07\\CountDown2.java";
//		int lineNumber = 17;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("semp");
		
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase08\\TestJoin.java";
//		int lineNumber = 39;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("semp");
		
//		String filePath = "D:\\hadoop-2.7.1-src\\hadoop-hdfs-project\\hadoop-hdfs\\src\\main\\java\\org\\apache\\hadoop\\hdfs\\server\\common\\Util.java";
//		int lineNumber = 85;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("semp");
		
		SliceCriterion criterion = new SliceCriterion(filePath, lineNumber, vars,SliceDirection.BACKWARD);
		//SliceHandle
		concurrentSlicer.sliceHandle(criterion);
		System.out.println("finish");
//		concurrentSlicer.heheTest();
//		ArrayList<PathImpl> result = concurrentSlicer.getPaths((long)1, (long)97);
//		for (PathImpl pathImpl : result) {
//			Iterator<Node> nodes = pathImpl.nodes().iterator();
//			while (nodes.hasNext()) {
//				Node node = (Node) nodes.next();
//				System.out.println(node.getId());
//			}
//		}
	}
	
}










