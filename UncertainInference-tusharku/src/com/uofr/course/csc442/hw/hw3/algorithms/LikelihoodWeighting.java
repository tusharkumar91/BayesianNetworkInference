package com.uofr.course.csc442.hw.hw3.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.uofr.course.csc442.hw.hw3.model.AbstractApproximateInferenceAlgorithm;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetwork;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetworkUtils;
import com.uofr.course.csc442.hw.hw3.model.InferenceAlgorithmType;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetwork.Node;

/**
 * Class to encapsulate the approximate inference algorithm
 * utilizing the Likelihood Weighting mechanism.
 * The code is modeled based on the pseudocode
 * in AIMA Fig 14.15 3rd ed. 
 * @author tusharkumar
 *
 */
public class LikelihoodWeighting extends AbstractApproximateInferenceAlgorithm{
	
	public LikelihoodWeighting(int numberOfSamples) {
		super(numberOfSamples);
	}

	//Variable to keep track of weight of current sample
	private double currentSampleweight = 1.0; 
	
	/**
	 * Overridden method for asking the network for 
	 * the distribution of a query variable using the 
	 * provided evidences.
	 */
	@Override
	public Map<String, Double> ask(String query, Map<String, String> evidences, BayesianNetwork network){
		Map<String, Double> distribution = new HashMap<String, Double>();
		for(int sampleCount=1; sampleCount <= numberOfSamples; sampleCount++) {
			Map<String, String> sample = getSampleFromBayesianNetwork(network, evidences);
			distribution.put(sample.get(query), distribution.getOrDefault(sample.get(query), 0.0) + currentSampleweight);			
		}	
		
		for(String value : network.getHeaderTable().get(query).getDomain()) {
			if(!distribution.containsKey(value)) {
				distribution.put(value, 0.0);
			}
		}
		return BayesianNetworkUtils.normalize(distribution);
	}

	/**
	 * Helper method to get a sample from the given bayesian network
	 * @param network
	 * @param evidences
	 * @return
	 */
	private Map<String, String> getSampleFromBayesianNetwork(BayesianNetwork network,
			Map<String, String> evidences) {
		Map<String, String> sample = new HashMap<String, String>();
		List<BayesianNetwork.Node> nodeList = BayesianNetworkUtils.getNodesInTopologicalOrder(network);
		currentSampleweight = 1.0;
		for(Node node : nodeList) {			
			if(evidences.containsKey(node.getName())) {
				sample.put(node.getName(), evidences.get(node.getName()));
				currentSampleweight = currentSampleweight * computeProbability(node, sample);				
			}
			else {
			if(node.getPriorTable().isEmpty()) {
				Map<List<Integer>, Map<Integer, Double>> cpt = node.getConditionalTable();
				List<Integer> parentIndex = new ArrayList<Integer>();
				for(Node parent : node.getParents()) {
					String parentVal = sample.get(parent.getName());
					parentIndex.add(parent.getDomain().indexOf(parentVal));
				}
				Map<Integer, Double> cptForParentValue = cpt.get(parentIndex);
				BayesianNetworkUtils.generateSampleFromDistribution(sample, node, cptForParentValue);
			}
			else {
				Map<Integer, Double> prior = node.getPriorTable();
				BayesianNetworkUtils.generateSampleFromDistribution(sample, node, prior);
			}
			}
		}
		return sample;
	}
	
	/**
	 * Helper method to compute the probability of a 
	 * node given the current sample from the network
	 * @param selectedVariable
	 * @param currentSample
	 * @return
	 */
	private static double computeProbability(Node selectedVariable, Map<String, String> currentSample) {
		List<Integer> parentValues = new ArrayList<Integer>();
		for(Node parent : selectedVariable.getParents()) {
			if(parent.isRoot()) {
				continue;
			}
			String parentValue = currentSample.get(parent.getName());
			parentValues.add(parent.getDomain().indexOf(parentValue));
		}
		int nodeValueIdx = selectedVariable.getDomain().indexOf(currentSample.get(selectedVariable.getName()));
		if(parentValues.isEmpty()) {			
			return selectedVariable.getPriorTable().get(nodeValueIdx);
		}
		else{
			return selectedVariable.getConditionalTable().get(parentValues).get(nodeValueIdx);
		}
	}
	
	@Override
	public String toString() {
		return InferenceAlgorithmType.LIKELIHOOD.getAlgorithmDescription();
	}
}
