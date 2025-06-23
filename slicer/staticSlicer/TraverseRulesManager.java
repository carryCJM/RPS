/**
 * author: Jason
 * 2016��6��22��
 * TODO
 */
package oo.com.iseu.slicer.staticSlicer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * @author Jason
 *
 */
public class TraverseRulesManager {

	private List<TraverseRules> listRules = new ArrayList<>();

	private static TraverseRulesManager manager = new TraverseRulesManager();

	/**
	 * 
	 */
	private TraverseRulesManager() {

	}

	/**
	 * Return the singleton instance of TraverseRulesManager
	 * @return TraverseRulesManager
	 */
	public static TraverseRulesManager getInstance() {
		return manager;
	}

	/**
	 * Add one rule to List
	 * @param rule rule to be added.
	 */
	public void addOneRule(TraverseRules rule) {
		listRules.add(rule);
	}

	public void addRules(List<TraverseRules> list) {
		listRules.addAll(list);
	}

	/**
	 * Remove one rule in list
	 * @param rule rule to be removed
	 * @return Is successfuly removed 
	 */
	public boolean removeOneRule(TraverseRules rule){
		return listRules.remove(rule);
	}

	/**
	 * @return Rule List
	 */
	public List<TraverseRules> getRules() {
		return listRules;
	}


	/**
	 * Manager method. Invoke rules in list one by one.
	 * @param node pending node
	 * @param dir Direction
	 * @param setPending  New nodes to get to next round traverse.
	 * @param db db
	 * @return
	 */
	public boolean checkIfApplicable(Node node, Direction dir, Set<Node> setPending, GraphDatabaseService db) {
		for (TraverseRules traverseRules : listRules) {
			traverseRules.checkIfApplicable(node, dir, setPending, db);
//			if (isApplicable)
//				return isApplicable;
		}
		return true;
	}
}
