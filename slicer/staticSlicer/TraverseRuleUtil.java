/**
 * author: Jason
 * 2016��6��22��
 * TODO
 */
package oo.com.iseu.slicer.staticSlicer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import oo.storage.DGEdge;
import oo.storage.NEO4JAccess;

/**
 * Rule creator
 * @author Jason
 *
 */
public class TraverseRuleUtil {

	static List<TraverseRules> listBack = new ArrayList<>();
	static List<TraverseRules> listFore = new ArrayList<>();
	/**
	 * Rule: RULE_STATNODE_CONTROLDEPT_STATNODE
	 * From: StatementNode
	 * Edge: Control Dependence Edge
	 * To: Statement Node
	 * 
	 */
	public static final TraverseRules B_RULE_STATNODE_CONTROLDEPT_STATNODE = new TraverseRules() {

		@Override
		public boolean checkIfApplicable(Node node, Direction dir, Set<Node> setPending, GraphDatabaseService db) {
			if (node.hasLabel(NEO4JAccess.StatementNode)) {
				Iterator<Relationship> rels = node.getRelationships(DGEdge.controlDepd, dir).iterator();
				while (rels.hasNext()) {
					Node nodeEnd = rels.next().getStartNode();

					if (nodeEnd.hasLabel(NEO4JAccess.StatementNode)) {
						switch (dir) {
						case INCOMING:
							addExtraNodes(nodeEnd, Direction.OUTGOING, setPending, db);
							break;
						case OUTGOING:
							addExtraNodes(nodeEnd, Direction.INCOMING, setPending, db);
							break;
						default:
							break;
						}
					}
				}
				return true;
			} else
				return false;
		}
	};

	public static final TraverseRules B_RULE_STATNODE_INITDATADEPT_STATNODE = new TraverseRules() {

		public boolean checkIfApplicable(Node node, Direction dir, Set<Node> setPending, GraphDatabaseService db) {

			if (node.hasLabel(NEO4JAccess.StatementNode)) {
				Iterator<Relationship> rels = node.getRelationships(DGEdge.initDataDependenceEdge, dir).iterator();
				while (rels.hasNext()) {
					Node nodeEnd = rels.next().getStartNode();

					if (nodeEnd.hasLabel(NEO4JAccess.StatementNode)) {
						switch (dir) {
						case INCOMING:
							addExtraNodes(nodeEnd, Direction.OUTGOING, setPending, db);
							break;
						case OUTGOING:
							addExtraNodes(nodeEnd, Direction.INCOMING, setPending, db);
							break;
						default:
							break;
						}
					}
				}
				return true;
			} else
				return false;
		}

	};

	public static final TraverseRules B_RULE_STATNODE_DATADEPT_STATNODE = new TraverseRules() {

		public boolean checkIfApplicable(Node node, Direction dir, Set<Node> setPending, GraphDatabaseService db) {

			if (node.hasLabel(NEO4JAccess.StatementNode)) {
				Iterator<Relationship> rels = node.getRelationships(DGEdge.dataDepd, dir).iterator();
				while (rels.hasNext()) {
					Node nodeEnd = rels.next().getStartNode();

					if (nodeEnd.hasLabel(NEO4JAccess.StatementNode)) {
						switch (dir) {
						case INCOMING:
							addExtraNodes(nodeEnd, Direction.OUTGOING, setPending, db);
							break;
						case OUTGOING:
							addExtraNodes(nodeEnd, Direction.INCOMING, setPending, db);
							break;
						default:
							break;
						}
					}
				}
				return true;
			} else
				return false;
		}

	};

	public static final TraverseRules F_RULE_FIELDNODE_DATADEPT_FIELDNODE = new TraverseRules() {

		public boolean checkIfApplicable(Node node, Direction dir, Set<Node> setPending, GraphDatabaseService db) {

			if (node.hasLabel(NEO4JAccess.MemberVariableNode)) {
				Iterator<Relationship> rels = node.getRelationships(DGEdge.dataDepd, dir).iterator();
				while (rels.hasNext()) {
					Node nodeEnd = rels.next().getEndNode();

					if (nodeEnd.hasLabel(NEO4JAccess.MemberVariableNode)) {
						switch (dir) {
						case INCOMING:
							addExtraNodes(nodeEnd, Direction.OUTGOING, setPending, db);
							break;
						case OUTGOING:
							addExtraNodes(nodeEnd, Direction.INCOMING, setPending, db);
							break;
						default:
							break;
						}
					}
				}
				return true;
			} else
				return false;
		}
	};

	public static final TraverseRules F_RULE_ACTUALNODE_DATADEPT_ACTUALNODE = new TraverseRules() {

		public boolean checkIfApplicable(Node node, Direction dir, Set<Node> setPending, GraphDatabaseService db) {

			if (node.hasLabel(NEO4JAccess.ActualParaOutNode)) {
				Iterator<Relationship> rels = node.getRelationships(DGEdge.dataDepd, dir).iterator();
				while (rels.hasNext()) {
					Node nodeEnd = rels.next().getEndNode();

					if (nodeEnd.hasLabel(NEO4JAccess.ActualParaInNode)) {
						switch (dir) {
						case INCOMING:
							addExtraNodes(nodeEnd, Direction.OUTGOING, setPending, db);
							break;
						case OUTGOING:
							addExtraNodes(nodeEnd, Direction.INCOMING, setPending, db);
							break;
						default:
							break;
						}
					}
				}
				return true;
			} else
				return false;
		}
	};

	/**
	 * Include the structor under/above nodeEnd
	 * @param nodeEnd  start node
	 * @param direction Direction
	 * @param setPending set
	 * @param db db
	 */
	private static void addExtraNodes(Node nodeEnd, Direction direction, Set<Node> setPending, GraphDatabaseService db) {
		for (Node node : db.traversalDescription().depthFirst().relationships(DGEdge.toObject, direction).relationships(DGEdge.typeAnalysis, direction)
				.relationships(DGEdge.objMember, direction).relationships(DGEdge.parameter, direction).traverse(nodeEnd).nodes()) {
			if (direction.equals(Direction.OUTGOING) && (node.hasLabel(NEO4JAccess.ActualParaInNode) || node.hasLabel(NEO4JAccess.MemberVariableNode))
					|| node.hasLabel(NEO4JAccess.PolyObjectExtensionNode) || (node.hasLabel(NEO4JAccess.ActualParaOutNode))) {
				setPending.add(node);
			} else if (direction.equals(Direction.INCOMING) && node.hasLabel(NEO4JAccess.StatementNode)) {
				setPending.add(node);
			}

		}
	}

	static void initLists() {
		if (listBack.isEmpty()) {
			listBack.add(B_RULE_STATNODE_CONTROLDEPT_STATNODE);
			listBack.add(B_RULE_STATNODE_DATADEPT_STATNODE);
			listBack.add(B_RULE_STATNODE_INITDATADEPT_STATNODE);
		}

		if (listFore.isEmpty()) {
			listFore.add(F_RULE_FIELDNODE_DATADEPT_FIELDNODE);
			listFore.add(F_RULE_ACTUALNODE_DATADEPT_ACTUALNODE);
		}
	}

	public static List<TraverseRules> getAllRules(Direction dir) {
		if (dir == Direction.INCOMING)
			return listBack;
		if (dir == Direction.OUTGOING)
			return listFore;

		return new ArrayList<>();
	}
}
