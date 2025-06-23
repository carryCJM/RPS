/**
 * author: Jason
 * 2016��1��20��
 * TODO
 */
package oo.com.iseu.slicer.helper;

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

import oo.com.iseu.slicer.store.SliceStorage;
import oo.storage.DGEdge;
import oo.storage.NEO4JAccess;

/**
 * @author Jason
 *
 */
public class SlicerHelper {

	/**
	 * ���ô洢ģ��洢��Ƭ���
	 * @param setSliceResult
	 * @param db 
	 * @param outputFile 
	 */
	public static void StoreSlice(Set<Node> setSliceResult, String outputFile, GraphDatabaseService db) {
		// TODO Auto-generated method stub
		SliceStorage slice = new SliceStorage(outputFile);// String
		// rootFilePath
		try (Transaction tx = db.beginTx()) {

			for (Node node : setSliceResult) {

				if (node.hasLabel(NEO4JAccess.MemberVariableNode)) {
					if (!node.hasProperty(NEO4JAccess.ElementPath)) {
						Iterator<Relationship> rels = node.getRelationships(DGEdge.dataMember, Direction.INCOMING).iterator();
						while (rels.hasNext()) {
							Node nodeEnd = rels.next().getStartNode();
							if (nodeEnd.hasProperty(NEO4JAccess.ElementPath))
								node.setProperty(NEO4JAccess.ElementPath, nodeEnd.getProperty(NEO4JAccess.ElementPath));
						}
					}
				}
				if (node.hasLabel(NEO4JAccess.EnumConstantNode)) {
//					System.out.println("L108 Flag:"+(node.toString()));
					if (!node.hasProperty(NEO4JAccess.ElementPath)) {
						Iterator<Relationship> rels = node.getRelationships(DGEdge.EnumConstant, Direction.INCOMING).iterator();
						while (rels.hasNext()) {
							Node nodeEnd = rels.next().getStartNode();
							if (nodeEnd.hasProperty(NEO4JAccess.ElementPath)) {
								node.setProperty(NEO4JAccess.ElementPath, nodeEnd.getProperty(NEO4JAccess.ElementPath));
//								break;
							}
						}
					}
				}
			}
			slice.sliceHandle(setSliceResult);
			tx.close();
		}
	}
	
	public static HashMap<String, TreeSet<Integer>> StoreSlice(Set<Node> setSliceResult, GraphDatabaseService db) {
		// TODO Auto-generated method stub
		HashMap<String, TreeSet<Integer>> result = null;
		SliceStorage slice = new SliceStorage("");// String
		// rootFilePath
		try (Transaction tx = db.beginTx()) {

			for (Node node : setSliceResult) {

				if (node.hasLabel(NEO4JAccess.MemberVariableNode)) {
					if (!node.hasProperty(NEO4JAccess.ElementPath)) {
						Iterator<Relationship> rels = node.getRelationships(DGEdge.dataMember, Direction.INCOMING).iterator();
						while (rels.hasNext()) {
							Node nodeEnd = rels.next().getStartNode();
							if (nodeEnd.hasProperty(NEO4JAccess.ElementPath))
								node.setProperty(NEO4JAccess.ElementPath, nodeEnd.getProperty(NEO4JAccess.ElementPath));
						}
					}
				}
				if (node.hasLabel(NEO4JAccess.EnumConstantNode)) {
//					System.out.println("L108 Flag:"+(node.toString()));
					if (!node.hasProperty(NEO4JAccess.ElementPath)) {
						Iterator<Relationship> rels = node.getRelationships(DGEdge.EnumConstant, Direction.INCOMING).iterator();
						while (rels.hasNext()) {
							Node nodeEnd = rels.next().getStartNode();
							if (nodeEnd.hasProperty(NEO4JAccess.ElementPath)) {
								node.setProperty(NEO4JAccess.ElementPath, nodeEnd.getProperty(NEO4JAccess.ElementPath));
//								break;
							}
						}
					}
				}
			}
			result = slice.getSlice(setSliceResult);
			tx.close();
		}
		return result;
	}

	/**
	 * �����ļ�·�����к�ȷ������׼���ڽڵ�
	 * @param db
	 * @param file
	 * @param line
	 */
	public static Set<Long> getNodesFromStmt(GraphDatabaseService db, String file, int line) {
		// TODO Auto-generated method stub
		String strGetNode = "MATCH node WHERE node.FilePath = \"" + file.replace("\\", "\\\\") + "\" AND node.Startline = \"" + line + "\"  RETURN node";

		Set<Node> nodeStart = new HashSet<>();

		try (Transaction tx = db.beginTx(); Result result = db.execute(strGetNode)) {
			//���Ը��ݴ��벹ȫ����ҵ����з���Ҫ��Ľڵ�
			while (result.hasNext()) {
				Map<String, Object> row = result.next();
				// setSliceResult.add(row.get("n"));
				nodeStart.add((Node) row.get("node"));
			}
			//���ֻ�ҵ�һ���ڵ㣬����ͼ�����ҵ����з���Ҫ��Ľڵ�
			if (nodeStart.size() == 1) {
				for (Node node : db.traversalDescription().depthFirst().relationships(DGEdge.objMember, Direction.OUTGOING).relationships(DGEdge.parameter, Direction.OUTGOING)
						.relationships(DGEdge.typeAnalysis, Direction.OUTGOING).relationships(DGEdge.toObject, Direction.OUTGOING).traverse(nodeStart).nodes()) {
					nodeStart.add(node);
				}
			}
		}
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

		return setNodes;
	}
}
