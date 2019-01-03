package com.uofr.course.csc442.hw.hw3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.uofr.course.csc442.hw.hw3.model.BayesianNetwork.Node;

/**
 * Utility class for aggregating all utility methods required 
 * for inference and Bayesian Network traversal by the programs.
 * Kept it as a final class to avoid its duplication or 
 * creation of its sub-classes. All methods which are 
 * supposed to be used by clients are static whereas those that
 * are strictly acting as a helper of the utlity methods and
 * are not required by clients are kept as private.
 * @author tusharkumar
 *
 */
public final class BayesianNetworkUtils {

	/**
	 * Method to generate a Bayesian Network from the given information
	 * @param priorValMap - Prior Probability Mappings
	 * @param conditionalProbValMap - CPT for variables
	 * @param variableDomainMap
	 * @param parentMap
	 * @return
	 */
	public static final BayesianNetwork generateBayesianNetwork(
			Map<String, Map<Integer, Double>> priorProbValMap,
			Map<String, Map<List<Integer>, Map<Integer, Double>>> conditionalProbValMap,
			Map<String, List<String>> variableDomainMap,
			Map<String, List<String>> parentMap) {
		Map<String, BayesianNetwork.Node> headerTable = new HashMap<String, BayesianNetwork.Node>();
				
		BayesianNetwork bn = new BayesianNetwork();
		Node rootNode = bn.getRootNode();		
		
		//Create all nodes which only have root as parent
		for(String nodeName : priorProbValMap.keySet()) {			
			Node childNode = bn.new Node(nodeName, variableDomainMap.get(nodeName));
			childNode.setPriorTable(priorProbValMap.get(nodeName));
			rootNode.addChild(childNode);
			headerTable.put(nodeName, childNode);
		}
		
		Iterator<String> parentIter = parentMap.keySet().iterator();	
		/**
		 * Iterate over children and add any child whose all parents 
		 * are already added to network.
		 * Continue till all nodes are added.
		 */				
		while(parentIter.hasNext()) {
			Iterator<String> iter = parentMap.keySet().iterator();
			while(iter.hasNext()) {
				String nodeName = iter.next();
				List<String> parents = parentMap.get(nodeName);
				boolean areAllParentsAdded = true;
				for(String parent : parents) {
					if(!headerTable.containsKey(parent)) {
						areAllParentsAdded = false;
						break;
					}
				}
				if(areAllParentsAdded) {
					Node childNode = bn.new Node(nodeName, variableDomainMap.get(nodeName));
					childNode.setConditionalTable(conditionalProbValMap.get(nodeName));
					for(String parent : parents) {
						Node parentNode = headerTable.get(parent);
						parentNode.addChild(childNode);
					}
					headerTable.put(nodeName, childNode);
					iter.remove();
				}
			}
			parentIter = parentMap.keySet().iterator();
		}		
		bn.setHeaderTable(headerTable);
		return bn;		
	}

	/**
	 * Method to run the topological sort algorithm
	 * on nodes of the graph and return the sorted 
	 * order of nodes.
	 * @param network
	 * @return
	 */
	public static final List<BayesianNetwork.Node> getNodesInTopologicalOrder(BayesianNetwork network){
		List<BayesianNetwork.Node> nodeList = new ArrayList<BayesianNetwork.Node>();
		Map<String, Boolean> visitedNodeMap = new HashMap<String, Boolean>();
		for(BayesianNetwork.Node childNode : network.getRootNode().getChildren()) {
			dfs(childNode, nodeList, visitedNodeMap);
		}
		Collections.reverse(nodeList);
		return nodeList;
	}

	/**
	 * Helper method to recursively run DFS which will be used 
	 * as a backbone to run the topological sort algorithm,
	 * @param node
	 * @param nodeList
	 * @param visitedNodeMap
	 */
	private static final void dfs(BayesianNetwork.Node node, List<Node> nodeList, Map<String, Boolean> visitedNodeMap) {
		visitedNodeMap.put(node.getName(), true);
		for(BayesianNetwork.Node childNode : node.getChildren()) {
			if(!visitedNodeMap.containsKey(childNode.getName())){
				dfs(childNode, nodeList, visitedNodeMap);
			}
		}
		nodeList.add(node);
	}
	
	/**
	 * Utility method to normalize and ensure that the input mapping
	 * of domain value to numbers becomes a probability distribution.
	 * @param distribution - unnormalized mapping
	 * @return
	 */
	public static Map<String, Double> normalize(Map<String, Double> distribution) {
		Map<String, Double> normalizedDistribution = new HashMap<String, Double>();
		double normalizationConstant = 0.0;
		for(double probVal : distribution.values()) {
			normalizationConstant += probVal;
		}
		
		for(Map.Entry<String, Double> entry : distribution.entrySet()) {
			normalizedDistribution.put(entry.getKey(), entry.getValue()/normalizationConstant);
		}
		return normalizedDistribution;
	}
	
	/**
	 * Method to generate a random sample based on the given 
	 * distribution. Used by inference algorithms.
	 * @param sample
	 * @param node
	 * @param cptForParentValue
	 */
	public static void generateSampleFromDistribution(Map<String, String> sample, Node node,
			Map<Integer, Double> distribution) {
		double p = Math.random();
		double cumulativeProbability = 0.0;
		Set<Integer> values = distribution.keySet();
		for (Integer value : values) {
		    cumulativeProbability += distribution.get(value);
		    if (p <= cumulativeProbability) {
		        sample.put(node.getName(), node.getDomain().get(value));
		        break;
		    }
		}
	}
	
	/**
	 * Utility method to find the information lost
	 * when a given distribution is to be replaced by its 
	 * approximate distribution.
	 * @param dist1 - Observed distribution
	 * @param dist2 - Approximate distribution
	 * @return
	 */
	public static double KLDivergence(Map<String, Double> dist1, Map<String, Double> dist2) {
		double KLDivergence = 0.0;	
		for(String key : dist1.keySet()) {
			double value1 = dist1.get(key) == null ? 0.00001 : dist1.get(key) + 0.00001;
			double value2 = dist2.get(key) == null ? 0.00001 : dist2.get(key) + 0.00001;
			KLDivergence += value1 * Math.log( value1 / value2 );
		}
		//return information lost in bits
		return KLDivergence / Math.log(2);
	}

}
