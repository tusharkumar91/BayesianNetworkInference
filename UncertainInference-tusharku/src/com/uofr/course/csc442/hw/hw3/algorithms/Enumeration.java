package com.uofr.course.csc442.hw.hw3.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.uofr.course.csc442.hw.hw3.model.BayesianNetwork;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetwork.Node;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetworkUtils;
import com.uofr.course.csc442.hw.hw3.model.InferenceAlgorithm;
import com.uofr.course.csc442.hw.hw3.model.InferenceAlgorithmType;

/**
 * Class to encapsulate the exact inference algorithm
 * utilizing the enumeration mechanism.
 * The code is modelled based on the pseudocode
 * in AIMA Fig 14.9 3rd ed. 
 * @author tusharkumar
 *
 */
public class Enumeration implements InferenceAlgorithm {
	
	/**
	 * Overridden method for asking the network for 
	 * the distribution of a query variable using the 
	 * provided evidences.
	 */
	@Override
	public Map<String, Double> ask(String query, Map<String, String> evidences, BayesianNetwork network){			
		Map<String, Double> distribution = new HashMap<String, Double>();
		List<String> domainOfX = network.getHeaderTable().get(query).getDomain();
		List<BayesianNetwork.Node> nodeList = BayesianNetworkUtils.getNodesInTopologicalOrder(network);
		if(evidences.containsKey(query)) {
			for(String possibleValue : domainOfX) {
				distribution.put(possibleValue, 0.0);
			}
			distribution.put(evidences.get(query), 1.0);
			return distribution;
		}
		for(String possibleValue : domainOfX) {
			Map<String, String> extendedEvidence = new HashMap<String, String>(evidences);
			extendedEvidence.put(query, possibleValue);
			distribution.put(possibleValue, enumerateAll(extendedEvidence, nodeList));
		}
		return BayesianNetworkUtils.normalize(distribution);
	}

	/**
	 * Method to recursively enumerate all possible
	 * values of each variable and computing the probability 
	 * using all values of all non-evidence variables and
	 * specified values of evidence variables
	 * @param extendedEvidence
	 * @param variables
	 * @return
	 */
	private static Double enumerateAll(Map<String, String> extendedEvidence, List<BayesianNetwork.Node> variables) {
		if(variables.isEmpty()) {
			return 1.0;
		}
		else {
			List<BayesianNetwork.Node> variablesCopy = new ArrayList<BayesianNetwork.Node>(variables);
			Node selectedVariable = variablesCopy.remove(0);
			if(extendedEvidence.containsKey(selectedVariable.getName())) {
				return computeProbability(selectedVariable, extendedEvidence) * 
						enumerateAll(extendedEvidence, variablesCopy);
			}
			else {
				double sum = 0.0;
				for(String possibleValue : selectedVariable.getDomain()) {
					Map<String, String> extendedEvidenceCopy = new HashMap<String, String>(extendedEvidence);
					extendedEvidenceCopy.put(selectedVariable.getName(), possibleValue);
					sum += computeProbability(selectedVariable, extendedEvidenceCopy) * 
							enumerateAll(extendedEvidenceCopy, variablesCopy);
				}
				return sum;
			}
		}
	}

	/**
	 * helper method to compute the probability using the CPT
	 * or Prior Values of a particular node
	 * @param selectedVariable - the variable to compute the probability of
	 * @param extendedEvidence - map of all evidence variable and values
	 * @return
	 */
	private static double computeProbability(Node selectedVariable, Map<String, String> extendedEvidence) {
		List<Integer> parentValues = new ArrayList<Integer>();
		for(Node parent : selectedVariable.getParents()) {
			if(parent.isRoot()) {
				continue;
			}
			String parentValue = extendedEvidence.get(parent.getName());
			parentValues.add(parent.getDomain().indexOf(parentValue));
		}
		int nodeValueIdx = selectedVariable.getDomain().indexOf(extendedEvidence.get(selectedVariable.getName()));
		if(parentValues.isEmpty()) {			
			return selectedVariable.getPriorTable().get(nodeValueIdx);
		}
		else{
			return selectedVariable.getConditionalTable().get(parentValues).get(nodeValueIdx);
		}
	}
	
	@Override
	public String toString() {
		return InferenceAlgorithmType.ENUMERATION.getAlgorithmDescription();
	}
}
