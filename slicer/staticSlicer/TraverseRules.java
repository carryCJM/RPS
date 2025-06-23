/**
 * author: Jason
 * 2016��6��22��
 * TODO
 */
package oo.com.iseu.slicer.staticSlicer;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * @author Jason
 *
 */
public interface TraverseRules {
	boolean checkIfApplicable(Node node, Direction dir, Set<Node> setPending, GraphDatabaseService db);
}
