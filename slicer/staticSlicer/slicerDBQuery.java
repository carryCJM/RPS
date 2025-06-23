/**
 * author: Jason
 * 2015��11��26��
 * TODO
 */
package oo.com.iseu.slicer.staticSlicer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import oo.com.iseu.UI.Basic.SliceDirection;
import oo.com.iseu.helper.SDGHelper;
import oo.com.iseu.slicer.helper.SlicerHelper;
import oo.storage.DGEdge;
import oo.storage.NEO4JAccess;

/**
 * @author Jason
 *
 */
public class slicerDBQuery {

	public static int progressValue;
	private static String DB_PATH = NEO4JAccess.DB_PATH;

	public static void main(String[] args) {
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);// ���ݿⴴ�����ȡ

		Direction direction = Direction.INCOMING;

		String rootFilePath = "F:\\������Ƭ\\TestResult\\��Դ����~\\From ZLY\\JSvmLib";

//		sliceByStmt(db, "D:\\ѧϰ\\LabTask\\OOTestCase\\src\\superConstructorInvocationTest\\SuperConstructorInvocationTest.java", 17, rootFilePath, direction);

		String[] strVar = {};
		Set<Node> setResult = getNodesFromSC(strVar, db, "F:\\������Ƭ\\TestResult\\��Դ����~\\From ZLY\\JSvmLib\\src\\lib\\SimplifiedSMO\\SimplifiedSmo.java", 256, rootFilePath,
				direction);

		SlicerHelper.StoreSlice(setResult, rootFilePath, db);
//		long varID = 66;
//		sliceByVar(db, varID, rootFilePath);
	}
	
	public static ArrayList<String> staticSlice(GraphDatabaseService db, Direction direction, String faultFile, int faultLine, ArrayList<String> vars ) {
		ArrayList<String> result=new ArrayList<String>();
		
		String rootFilePath = "D:\\workspace\\OOJavaSlicerBug\\Repository\\Slices\\workspace\\YingYongCeShi";
		String[] strVar = { "arr[p.i].b" };
		if(vars!=null && vars.size() != 0) {
			int index = vars.size();
			strVar = new String[index];
			for(int i = 0; i<index; i++) {
				strVar[i] = vars.get(i);
			}
		} 
		Set<Node> setResult = getNodesFromSC(strVar, db, faultFile, faultLine, rootFilePath, direction);
		Iterator<Node>  it = setResult.iterator();
		Transaction transaction = db.beginTx();
		while(it.hasNext()) {
			Node tempNode  = it.next();
			if(tempNode.hasLabel(NEO4JAccess.StatementNode) ) {
				for(Relationship relationship : tempNode.getRelationships(Direction.INCOMING, DGEdge.controlDepd)) {
					if(relationship.getStartNode().hasLabel(NEO4JAccess.InstanceMethodNode)) {
						it.remove();
					}
				}
			} else {
				it.remove();
			}
		}
		for(Node node : setResult) {
			if (node.hasProperty("FilePath")&&node.hasProperty("Startline")) {
				String filePath = (String)node.getProperty("FilePath");
				Integer startLine = Integer.valueOf((String) node.getProperty("Startline"));
				if (startLine<=0) {
					continue;
				}
				result.add(filePath + "," + startLine);
			}
		}
		transaction.close();
		return result;
	}

	/**
	 * @param strVar
	 * @param i 
	 * @param string 
	 * @param db 
	 * @param direction 
	 * @param rootFilePath 
	 */
	public static Set<Node> getNodesFromSC(String[] strVars, GraphDatabaseService db, String file, int line, String rootFilePath, Direction direction) {
		// TODO Auto-generated method stub
		Set<Node> nodeStart = new HashSet<>();
		if (strVars!=null) {
			for (String var : strVars) {
				nodeStart.addAll(getVarNodes(var, file, line, db));
			}
		}

		if (nodeStart.size() == 0) {
			//��Ȼ�޷��ҵ���Ӧ��Ա����λ�ã�������������
			System.out.println("Cannot find related variable nodes. Change strategy to slice by statement...");
			return sliceByStmt(db, file, line, rootFilePath, direction);

		} else {
			//���ҵ��������ҵ���NodeΪ׼�������Ƭ
			Long ids[] = new Long[nodeStart.size()];
			Set<Long> setNodes = new HashSet<>();

			int i = 0;
			for (Node node : nodeStart) {
				ids[i++] = node.getId();
				setNodes.add(node.getId());
			}

			System.out.print("Found slice criterion: ");
			for (Long id : ids)
				System.out.print(id + ",");
			System.out.println();

			return multi(setNodes, db, direction);

//			SlicerHelper.StoreSlice(setSliceResult, rootFilePath, db);

		}

	}

	public static Set<Node> getVarNodes(String strVar, String file, int line, GraphDatabaseService db) {
		System.err.println(strVar);
		String strVarName = strVar, strFieldName = null, strGetNode = null;
		if (strVar.contains(".")) {
			strFieldName = strVar.substring(strVar.lastIndexOf(".") + 1, strVar.length());
			strVarName = strVar.substring(0, strVar.lastIndexOf("."));
		}

		if (strFieldName != null)
			strGetNode = "MATCH node WHERE node.FilePath = \"" + file.replace("\\", "\\\\") + "\" AND node.Startline = \"" + line + "\" AND node.VarName = \"" + strVarName
					+ "\" AND node.Name = \"" + strFieldName + "\" RETURN node";
		else if (!strVarName.equals(""))
			strGetNode = "MATCH node WHERE node.FilePath = \"" + file.replace("\\", "\\\\") + "\" AND node.Startline = \"" + line + "\" AND node.VarName = \"" + strVarName
					+ "\" RETURN node";

		System.out.println(strGetNode);

		Set<Node> nodeStart = new HashSet<>();

		if (strGetNode != null) {
			try (Transaction tx = db.beginTx(); Result result = db.execute(strGetNode)) {
				while (result.hasNext()) {
					Map<String, Object> row = result.next();
					// setSliceResult.add(row.get("n"));
					nodeStart.add((Node) row.get("node"));
				}
			}
			
		}
		if (nodeStart.size() == 0&&!strVar.equals("")) {
			//try 2nd time
			System.out.println("Try second time...");

			strGetNode = "MATCH node WHERE node.FilePath = \"" + file.replace("\\", "\\\\") + "\" AND node.Startline = \"" + line + "\" AND node.VarName = \"" + strVar
					+ "\" RETURN node";

			System.out.println(strGetNode);

			try (Transaction tx = db.beginTx(); Result result = db.execute(strGetNode)) {
				while (result.hasNext()) {
					Map<String, Object> row = result.next();
					// setSliceResult.add(row.get("n"));
					nodeStart.add((Node) row.get("node"));
				}
			}
		}
		
		return nodeStart;
	}


	/**
	 * @param db
	 * @param file
	 * @param line
	 * @param outputFile
	 * @param direction
	 */
	public static Set<Node> sliceByStmt(GraphDatabaseService db, String file, int line, String outputFile, Direction direction) {
		// TODO Auto-generated method stub

		//��ø������е�Node
		Set<Long> setNodes = SlicerHelper.getNodesFromStmt(db, file, line);

		Set<Node> setSliceResult = multi(setNodes, db, direction);
		System.out.println(setSliceResult);
		//�洢���

		return setSliceResult;

//		SlicerHelper.StoreSlice(setSliceResult, outputFile, db);

//		progressValue = 100; /**************************progress update***********************/

	}

	/**
	 * @param db
	 * @param direction 
	 * @param string
	 * @param i
	 * @param rootFilePath
	 */
	private static Set<Node> sliceByStmt(GraphDatabaseService db, String file, int line, String outputFile) {
		return sliceByStmt(db, file, line, outputFile, Direction.INCOMING);
	}

	/**
	 * Not support DirectionBoth yet
	 * @param db
	 * @param varID
	 * @param rootFilePath
	 * @param direction
	 */
	private static void sliceByVar(GraphDatabaseService db, long varID, String rootFilePath, Direction direction) {
		// TODO Auto-generated method stub
		Set<Node> setResultForVar = new HashSet<>();
		setResultForVar = singe(varID, db, direction, setResultForVar);

		System.out.println("Slice result for varID: " + varID);
		System.out.println(setResultForVar);
		System.out.println("End.");

		SlicerHelper.StoreSlice(setResultForVar, rootFilePath, db);

	}

	/**
	 * @param db
	 * @param varID
	 * @param rootFilePath
	 */
	private static void sliceByVar(GraphDatabaseService db, long varID, String rootFilePath) {
		// TODO Auto-generated method stub
		sliceByVar(db, varID, rootFilePath, Direction.INCOMING);
	}


	@SuppressWarnings("deprecation")
	public static Set<Node> multi(Set<Long> cls1_nodes, GraphDatabaseService db, Direction direction) {
		Set<Node> setSliceResult = new HashSet<>();

		for (Long id : cls1_nodes) {
			Set<Node> result_id = singe(id, db, direction, setSliceResult);
			for (Node n : result_id)
				setSliceResult.add(n);
		}
		//System.out.println(setSliceResult);
		//System.out.println("EndOfAll.");
		return setSliceResult;
	}

	@SuppressWarnings("deprecation")
	public static Set<Node> multi(Set<Long> cls1_nodes, GraphDatabaseService db) {
		return multi(cls1_nodes, db, Direction.INCOMING);
	}

	public static Set<Node> singe(Long id, GraphDatabaseService db, Direction direction, Set<Node> setResult) {
		Set<Node> setSliceResultForP1 = new HashSet<>();
//		Set<Node> setSliceResult = new HashSet<>();

		// String strQueryStep1 = String.format("match (n)-[r]->(b{ID:\"{d}\"})
		// where type(r)<>\"outputParameter\" return n,b", 57);
		String strGetNode = "match (n {ID:\"" + id.toString() + "\"}) return n";
		Node nodeStart = null;

		TraverseRulesManager traverseRulesManager = TraverseRulesManager.getInstance();
		TraverseRuleUtil.initLists();
		traverseRulesManager.addRules(TraverseRuleUtil.getAllRules(direction));

		try (Transaction tx = db.beginTx(); Result result = db.execute(strGetNode)) {

			//System.out.println("Start.");
			while (result.hasNext()) {
				Map<String, Object> row = result.next();
				// setSliceResult.add(row.get("n"));
				nodeStart = (Node) row.get("n");
			}

			Set<Node> setPendingP1 = new HashSet<>();
			Set<Node> setPendingP2 = new HashSet<>();

			if (direction == Direction.INCOMING) {
				// ����Phase1����Parameter_out�߱���
				for (Node node : db.traversalDescription().depthFirst().relationships(DGEdge.classMember, direction).relationships(DGEdge.controlDepd, direction)
						.relationships(DGEdge.instantiation, direction).relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction)
						.relationships(DGEdge.typeAnalysis, direction).relationships(DGEdge.dataDepd, direction).relationships(DGEdge.inputParameter, direction)
						.relationships(DGEdge.methodInvocation, direction).relationships(DGEdge.methodImplment, direction).relationships(DGEdge.interfaceImplement, direction)
						.relationships(DGEdge.inherit, direction).relationships(DGEdge.abstractMember, direction).relationships(DGEdge.pkgMember, direction)
						.relationships(DGEdge.pkgDepd, direction).relationships(DGEdge.summaryEdge, direction).relationships(DGEdge.dataMember, direction)
						.relationships(DGEdge.EnumConstant, direction).relationships(DGEdge.initDataDependenceEdge, direction).relationships(DGEdge.toObject, direction)
						.traverse(nodeStart).nodes()) {
					if (!setResult.contains(node)) {
						if (SDGHelper.isAdditionalTraverse)
							traverseRulesManager.checkIfApplicable(node, direction, setPendingP1, db);
						setSliceResultForP1.add(node);
					}
				}
			} else if (direction == Direction.OUTGOING) {
				// ǰ��Phase1����Parameter_in & mtdIvk �߱���
				for (Node node : db.traversalDescription().depthFirst().relationships(DGEdge.classMember, direction).relationships(DGEdge.controlDepd, direction)
						.relationships(DGEdge.instantiation, direction).relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction)
						.relationships(DGEdge.typeAnalysis, direction).relationships(DGEdge.dataDepd, direction).relationships(DGEdge.outputParameter, direction)
//						.relationships(DGEdge.methodInvocation, direction)
						.relationships(DGEdge.methodImplment, direction).relationships(DGEdge.interfaceImplement, direction)
						.relationships(DGEdge.inherit, direction).relationships(DGEdge.abstractMember, direction).relationships(DGEdge.pkgMember, direction)
						.relationships(DGEdge.pkgDepd, direction).relationships(DGEdge.summaryEdge, direction).relationships(DGEdge.dataMember, direction)
						.relationships(DGEdge.EnumConstant, direction).relationships(DGEdge.initDataDependenceEdge, direction).relationships(DGEdge.toObject, direction)
						.traverse(nodeStart).nodes()) {
					if (!setResult.contains(node)) {
						if (SDGHelper.isAdditionalTraverse)
							traverseRulesManager.checkIfApplicable(node, direction, setPendingP1, db);
						setSliceResultForP1.add(node);
					}
				}
			}
			//System.out.println(setSliceResultForP1);
			//System.out.println("EndOfP1.");
//			if (progressValue < 66) {
//				progressValue++; /**************************progress update***********************/
//			}
			setResult.addAll(setSliceResultForP1);

			if (direction == Direction.INCOMING) {
				// ����Phase2����Parameter_in�ߺ�MethodInvocation��
				for (Node nodeP1 : setSliceResultForP1) {
					for (Node nodePend : db.traversalDescription().depthFirst().relationships(DGEdge.classMember, direction).relationships(DGEdge.controlDepd, direction)
							.relationships(DGEdge.instantiation, direction).relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction)
							.relationships(DGEdge.typeAnalysis, direction).relationships(DGEdge.dataDepd, direction).relationships(DGEdge.outputParameter, direction)
							.relationships(DGEdge.methodImplment, direction).relationships(DGEdge.interfaceImplement, direction).relationships(DGEdge.inherit, direction)
							.relationships(DGEdge.abstractMember, direction).relationships(DGEdge.pkgMember, direction).relationships(DGEdge.pkgDepd, direction)
							.relationships(DGEdge.summaryEdge, direction).relationships(DGEdge.dataMember, direction).relationships(DGEdge.toObject, direction)
							.relationships(DGEdge.EnumConstant, direction).relationships(DGEdge.initDataDependenceEdge, direction).traverse(nodeP1).nodes()) {
						if (!setResult.contains(nodePend)) {
							if (SDGHelper.isAdditionalTraverse)
								traverseRulesManager.checkIfApplicable(nodePend, direction, setPendingP2, db);
							setResult.add(nodePend);
						}
					}
				}
			} else if (direction == Direction.OUTGOING) {
				// ǰ��Phase2����Parameter_out�ߺ�MethodInvocation��
				for (Node nodeP1 : setSliceResultForP1) {
					for (Node nodePend : db.traversalDescription().depthFirst().relationships(DGEdge.classMember, direction).relationships(DGEdge.controlDepd, direction)
							.relationships(DGEdge.instantiation, direction).relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction)
							.relationships(DGEdge.typeAnalysis, direction).relationships(DGEdge.dataDepd, direction).relationships(DGEdge.inputParameter, direction)
							.relationships(DGEdge.methodImplment, direction).relationships(DGEdge.interfaceImplement, direction).relationships(DGEdge.inherit, direction)
							.relationships(DGEdge.methodInvocation, direction)
							.relationships(DGEdge.abstractMember, direction).relationships(DGEdge.pkgMember, direction).relationships(DGEdge.pkgDepd, direction)
							.relationships(DGEdge.summaryEdge, direction).relationships(DGEdge.dataMember, direction).relationships(DGEdge.toObject, direction)
							.relationships(DGEdge.EnumConstant, direction).relationships(DGEdge.initDataDependenceEdge, direction).traverse(nodeP1).nodes()) {
						if (!setResult.contains(nodePend)) {
							if (SDGHelper.isAdditionalTraverse)
								traverseRulesManager.checkIfApplicable(nodePend, direction, setPendingP2, db);
							setResult.add(nodePend);
						}
					}
				}
			}

			setPendingP1.removeAll(setResult);
			setPendingP2.removeAll(setResult);

//			System.out.println("BBBB--P1" + setPendingP1);
//			System.out.println("BBBB--P2" + setPendingP2);

			while (setPendingP1.size() + setPendingP2.size() > 0) {
//				System.out.println(setPending);
				Set<Node> tempSetP1 = new HashSet<>();
				Set<Node> tempSetP2 = new HashSet<>();
//				Set<Node> allSet = new HashSet<>(setPendingP1);
//				allSet.addAll(setPendingP2);
				System.out.println("+++++++++++This round++++++++++++");
				System.out.println("P1:" + setPendingP1);
				System.out.println("P2:" + setPendingP2);

				//Node which was found in P1, do the complete traverse.
				for (Node nodeInSet1 : setPendingP1) {
					System.out.println("--------Dealing P1:" + nodeInSet1);
					setSliceResultForP1.clear();
					if (direction == Direction.INCOMING) {
						//����p1
						for (Node node : db.traversalDescription().depthFirst().relationships(DGEdge.classMember, direction).relationships(DGEdge.controlDepd, direction)
								.relationships(DGEdge.instantiation, direction).relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction)
								.relationships(DGEdge.typeAnalysis, direction).relationships(DGEdge.dataDepd, direction).relationships(DGEdge.inputParameter, direction)
								.relationships(DGEdge.methodInvocation, direction).relationships(DGEdge.methodImplment, direction)
								.relationships(DGEdge.interfaceImplement, direction).relationships(DGEdge.inherit, direction).relationships(DGEdge.abstractMember, direction)
								.relationships(DGEdge.pkgMember, direction).relationships(DGEdge.pkgDepd, direction).relationships(DGEdge.summaryEdge, direction)
								.relationships(DGEdge.dataMember, direction).relationships(DGEdge.EnumConstant, direction).relationships(DGEdge.initDataDependenceEdge, direction)
								.relationships(DGEdge.toObject, direction).traverse(nodeInSet1).nodes()) {

							if (!setResult.contains(node)) {
								if (SDGHelper.isAdditionalTraverse)
									traverseRulesManager.checkIfApplicable(node, direction, tempSetP1, db);
								setSliceResultForP1.add(node);
							}
						}
					} else if (direction == Direction.OUTGOING) {
						// ǰ��Phase1����Parameter_in�߱���
						for (Node node : db.traversalDescription().depthFirst().relationships(DGEdge.classMember, direction).relationships(DGEdge.controlDepd, direction)
								.relationships(DGEdge.instantiation, direction).relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction)
								.relationships(DGEdge.typeAnalysis, direction).relationships(DGEdge.dataDepd, direction).relationships(DGEdge.outputParameter, direction)
//								.relationships(DGEdge.methodInvocation, direction)
								.relationships(DGEdge.methodImplment, direction)
								.relationships(DGEdge.interfaceImplement, direction).relationships(DGEdge.inherit, direction).relationships(DGEdge.abstractMember, direction)
								.relationships(DGEdge.pkgMember, direction).relationships(DGEdge.pkgDepd, direction).relationships(DGEdge.summaryEdge, direction)
								.relationships(DGEdge.dataMember, direction).relationships(DGEdge.EnumConstant, direction).relationships(DGEdge.initDataDependenceEdge, direction)
								.relationships(DGEdge.toObject, direction).traverse(nodeInSet1).nodes()) {
							if (!setResult.contains(node)) {
								if (SDGHelper.isAdditionalTraverse)
									traverseRulesManager.checkIfApplicable(node, direction, tempSetP1, db);
								setSliceResultForP1.add(node);
							}
						}
					}
					setResult.addAll(setSliceResultForP1);

					if (direction == Direction.INCOMING) {
						// Phase2����Parameter_in�ߺ�MethodInvocation��
						for (Node nodeP1 : setSliceResultForP1) {
							for (Node nodePend : db.traversalDescription().depthFirst().relationships(DGEdge.classMember, direction).relationships(DGEdge.controlDepd, direction)
									.relationships(DGEdge.instantiation, direction).relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction)
									.relationships(DGEdge.typeAnalysis, direction).relationships(DGEdge.dataDepd, direction).relationships(DGEdge.outputParameter, direction)
									.relationships(DGEdge.methodImplment, direction).relationships(DGEdge.interfaceImplement, direction).relationships(DGEdge.inherit, direction)
									.relationships(DGEdge.abstractMember, direction).relationships(DGEdge.pkgMember, direction).relationships(DGEdge.pkgDepd, direction)
									.relationships(DGEdge.summaryEdge, direction).relationships(DGEdge.dataMember, direction).relationships(DGEdge.toObject, direction)
									.relationships(DGEdge.EnumConstant, direction).relationships(DGEdge.initDataDependenceEdge, direction).traverse(nodeP1).nodes()) {
								if (!setResult.contains(nodePend)) {
									if (SDGHelper.isAdditionalTraverse)
										traverseRulesManager.checkIfApplicable(nodePend, direction, tempSetP2, db);
									setResult.add(nodePend);
								}
							}
						}
					} else if (direction == Direction.OUTGOING) {
						// ǰ��Phase2����Parameter_out�ߺ�MethodInvocation��
						for (Node nodeP1 : setSliceResultForP1) {
							for (Node nodePend : db.traversalDescription().depthFirst().relationships(DGEdge.classMember, direction).relationships(DGEdge.controlDepd, direction)
									.relationships(DGEdge.instantiation, direction).relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction)
									.relationships(DGEdge.typeAnalysis, direction).relationships(DGEdge.dataDepd, direction).relationships(DGEdge.inputParameter, direction)
									.relationships(DGEdge.methodInvocation, direction)
									.relationships(DGEdge.methodImplment, direction).relationships(DGEdge.interfaceImplement, direction).relationships(DGEdge.inherit, direction)
									.relationships(DGEdge.abstractMember, direction).relationships(DGEdge.pkgMember, direction).relationships(DGEdge.pkgDepd, direction)
									.relationships(DGEdge.summaryEdge, direction).relationships(DGEdge.dataMember, direction).relationships(DGEdge.toObject, direction)
									.relationships(DGEdge.EnumConstant, direction).relationships(DGEdge.initDataDependenceEdge, direction).traverse(nodeP1).nodes()) {
								if (!setResult.contains(nodePend)) {
									if (SDGHelper.isAdditionalTraverse)
										traverseRulesManager.checkIfApplicable(nodePend, direction, tempSetP2, db);
									setResult.add(nodePend);
								}
							}
						}
					}

				}

				//Node which found in P2,only do the 2nd traverse.
				for (Node nodeInSet2 : setPendingP2) {

					System.out.println("--------Dealing P2:" + nodeInSet2);

					if (direction == Direction.INCOMING) {
						//����P2
						for (Node nodePend : db.traversalDescription().depthFirst().relationships(DGEdge.dataDepd, direction).relationships(DGEdge.controlDepd, direction)
								.relationships(DGEdge.instantiation, direction).relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction)
								.relationships(DGEdge.typeAnalysis, direction).relationships(DGEdge.classMember, direction).relationships(DGEdge.outputParameter, direction)
								.relationships(DGEdge.methodImplment, direction).relationships(DGEdge.interfaceImplement, direction).relationships(DGEdge.inherit, direction)
								.relationships(DGEdge.abstractMember, direction).relationships(DGEdge.pkgMember, direction).relationships(DGEdge.pkgDepd, direction)
								.relationships(DGEdge.summaryEdge, direction).relationships(DGEdge.dataMember, direction).relationships(DGEdge.toObject, direction)
								.relationships(DGEdge.EnumConstant, direction).relationships(DGEdge.initDataDependenceEdge, direction).traverse(nodeInSet2).nodes()) {

							if (!setResult.contains(nodePend)) {
//								System.out.println("P2: " + nodePend);
								if (SDGHelper.isAdditionalTraverse)
									traverseRulesManager.checkIfApplicable(nodePend, direction, tempSetP2, db);
								setResult.add(nodePend);
							}
						}
					} else if (direction == Direction.OUTGOING) {
						//ǰ��p2
						for (Node nodePend : db.traversalDescription().depthFirst().relationships(DGEdge.dataDepd, direction).relationships(DGEdge.controlDepd, direction)
								.relationships(DGEdge.instantiation, direction).relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction)
								.relationships(DGEdge.typeAnalysis, direction).relationships(DGEdge.classMember, direction).relationships(DGEdge.inputParameter, direction)
								.relationships(DGEdge.methodImplment, direction).relationships(DGEdge.interfaceImplement, direction).relationships(DGEdge.inherit, direction)
								.relationships(DGEdge.methodInvocation, direction)
								.relationships(DGEdge.abstractMember, direction).relationships(DGEdge.pkgMember, direction).relationships(DGEdge.pkgDepd, direction)
								.relationships(DGEdge.summaryEdge, direction).relationships(DGEdge.dataMember, direction).relationships(DGEdge.toObject, direction)
								.relationships(DGEdge.EnumConstant, direction).relationships(DGEdge.initDataDependenceEdge, direction).traverse(nodeInSet2).nodes()) {

							if (!setResult.contains(nodePend)) {
//								System.out.println("P2: " + nodePend);
								if (SDGHelper.isAdditionalTraverse)
									traverseRulesManager.checkIfApplicable(nodePend, direction, tempSetP2, db);
								setResult.add(nodePend);
							}
						}
					}

				}

				setPendingP1 = tempSetP1;
				setPendingP2 = tempSetP2;
				tempSetP1.clear();
				tempSetP2.clear();

				setPendingP1.removeAll(setResult);
				setPendingP2.removeAll(setResult);

			}

			//System.out.println(setSliceResult);
			//System.out.println("EndOfP2");
			if (progressValue < 66) {
				progressValue++; /**************************progress update***********************/
			}
			return setResult;
		}
	}


	/**
	 * @param split
	 * @param dataBasePath
	 * @param text
	 * @param parseInt
	 * @param slicesRepository
	 * @param sliceDirection
	 */
	public static void ooStaticSliceByStmt(String[] strVars, String dbPath, String filePath, int lineNumber, String sliceStoreDir, SliceDirection sliceDirection) {
		// TODO Auto-generated method stub
		progressValue = 0;
		SDGHelper.recordSliceInfo(filePath);

		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);// ���ݿⴴ�����ȡ
		Set<Node> setSliceResult = new HashSet<>();
		SDGHelper.recordMemoryAndRuntimeForSlice("SliceStart");
		switch (sliceDirection) {
		case FORWARD:
			setSliceResult = getNodesFromSC(strVars, db, filePath, lineNumber, sliceStoreDir, Direction.OUTGOING);
			break;
		case BACKWARD:
			setSliceResult = getNodesFromSC(strVars, db, filePath, lineNumber, sliceStoreDir, Direction.INCOMING);
			break;
		case ALL:
			setSliceResult = getNodesFromSC(strVars, db, filePath, lineNumber, sliceStoreDir, Direction.INCOMING);
			setSliceResult.addAll(getNodesFromSC(strVars, db, filePath, lineNumber, sliceStoreDir, Direction.OUTGOING));
			break;
		}
		SDGHelper.recordMemoryAndRuntimeForSlice("StoreSliceStart");

		SlicerHelper.StoreSlice(setSliceResult, sliceStoreDir, db);

		progressValue = 100;
		SDGHelper.recordMemoryAndRuntimeForSlice("Finished");
		db.shutdown();
	}
	
	/**
	 * @param split
	 * @param dataBasePath
	 * @param text
	 * @param parseInt
	 * @param slicesRepository
	 * @param sliceDirection
	 */
	public static HashMap<String, TreeSet<Integer>> ooStaticSliceByStmt(String dbPath, String filePath, int lineNumber,String[] strVars, SliceDirection sliceDirection) {
		// TODO Auto-generated method stub
		progressValue = 0;
		SDGHelper.recordSliceInfo(filePath);
		if (NEO4JAccess.graphDb != null) {
			NEO4JAccess.graphDb.shutdown();
			NEO4JAccess.graphDb = null;
		}
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);// ���ݿⴴ�����ȡ
		Set<Node> setSliceResult = new HashSet<>();
		SDGHelper.recordMemoryAndRuntimeForSlice("SliceStart");
		switch (sliceDirection) {
		case FORWARD:
			setSliceResult = getNodesFromSC(strVars,db, filePath, lineNumber,null, Direction.OUTGOING);
			break;
		case BACKWARD:
			setSliceResult = getNodesFromSC(strVars,db, filePath, lineNumber,null, Direction.INCOMING);
			break;
		case ALL:
			setSliceResult =  getNodesFromSC(strVars,db, filePath, lineNumber,null, Direction.INCOMING);
			setSliceResult.addAll( getNodesFromSC(strVars,db, filePath, lineNumber,null, Direction.OUTGOING));
			break;
		}

		HashMap<String, TreeSet<Integer>> result = SlicerHelper.StoreSlice(setSliceResult, db);

		db.shutdown();
		return result;
	}
	

}
