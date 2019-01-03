package com.uofr.course.csc442.hw.hw3.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to encapsulate the mechanics
 * behind a Bayesian Network. Its modeled 
 * as a graph with Vertices being the variables.
 * Edges indicate the establishment of dependency
 * between two variables/vertices/nodes
 * @author tusharkumar
 *
 */
public class BayesianNetwork {
	private Node root;
	private String networkName;
	private Map<String, Node> headerTable;
	
	public BayesianNetwork() {
		this.root = new Node();	
		this.root.isRoot = true;
	}		
	
	public String getNetworkName() {
		return networkName;
	}

	public Node getRootNode() {
		return this.root;
	}
	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	public Map<String, Node> getHeaderTable() {
		return headerTable;
	}

	public void setHeaderTable(Map<String, Node> headerTable) {
		this.headerTable = headerTable;
	}

	/**
	 * Inner class for representing the node of the graph.
	 * Its not something that needs to exist outside 
	 * this class because its functionalities are very much
	 * tied to BayesianNetwork class and hence
	 * keeping it as a inner class versus moving out
	 * and let it have its own identity.
	 * @author tusharkumar
	 *
	 */
	public class Node {
		private String name;
		private List<String> domain;
		private List<Node> parents;
		private List<Node> children;
		private Map<Integer, Double> priorTable;
		private Map<List<Integer>, Map<Integer, Double>> conditionalTable;
		private boolean isRoot = false;
				
		public void addChild(Node child) {
			this.children.add(child);
			child.parents.add(this);
		}
		
		public Node(String name, List<String> domain) {
			this.children = new ArrayList<Node>();
			this.parents = new ArrayList<Node>();
			this.setPriorTable(new HashMap<Integer, Double>());
			this.setConditionalTable(new HashMap<List<Integer>, Map<Integer, Double>>());
			this.name = name;
			this.domain = domain;
		}
		
		public Node() {			
			this(null, null);
		}
		
		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append("Name : " + this.name + "\n");
			str.append("Parents : [ ");
			for(Node node : this.parents) {
				str.append(node.name + " ");
			}
			str.append("]");
			return str.toString(); 
		}
		
		public List<Node> getParents() {
			return parents;
		}
		public void setParents(List<Node> parents) {
			this.parents = parents;
		}
		public List<Node> getChildren() {
			return children;
		}
		public void setChildren(List<Node> children) {
			this.children = children;
		}
		public Map<Map<String, String>, Map<String, Double>> getProbabilityTable() {
			return probabilityTable;
		}
		public void setProbabilityTable(Map<Map<String, String>, Map<String, Double>> probabilityTable) {
			this.probabilityTable = probabilityTable;
		}
		private Map<Map<String, String>, Map<String, Double>> probabilityTable;
		
		public List<String> getDomain() {
			return domain;
		}
		public void setDomain(List<String> domain) {
			this.domain = domain;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public boolean isRoot() {
			return isRoot;
		}

		public void setRoot(boolean isRoot) {
			this.isRoot = isRoot;
		}

		public Map<List<Integer>, Map<Integer, Double>> getConditionalTable() {
			return conditionalTable;
		}

		public void setConditionalTable(Map<List<Integer>, Map<Integer, Double>> conditionalTable) {
			this.conditionalTable = conditionalTable;
		}

		public Map<Integer, Double> getPriorTable() {
			return priorTable;
		}

		public void setPriorTable(Map<Integer, Double> priorTable) {
			this.priorTable = priorTable;
		}				
	}
}
