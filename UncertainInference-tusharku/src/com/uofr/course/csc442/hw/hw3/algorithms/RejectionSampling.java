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
 * utilizing the Rejection Sampling mechanism.
 * The code is modeled based on the pseudocode
 * in AIMA Fig 14.14 3rd ed. 
 * @author tusharkumar
 *
 */
public class RejectionSampling extends AbstractApproximateInferenceAlgorithm{
	
	public RejectionSampling(int numberOfSamples) {
		super(numberOfSamples);
	}

	/**
	 * Overridden method for asking the network for 
	 * the distribution of a query variable using the 
	 * provided evidences.
	 */
	@Override
	public Map<String, Double> ask(String query, Map<String, String> evidences, 
			BayesianNetwork network){
		Map<String, Double> distribution = new HashMap<String, Double>();
		for(int sampleCount=1; sampleCount <= numberOfSamples; sampleCount++) {
			Map<String, String> sample = getSampleFromBayesianNetwork(network);
			if(isConsistent(evidences, sample)) {
				distribution.put(sample.get(query), 
						distribution.getOrDefault(sample.get(query), 0.0) + 1.0);
			}
		}	
		for(String value : network.getHeaderTable().get(query).getDomain()) {
			if(!distribution.containsKey(value)) {
				distribution.put(value, 0.0);
			}
		}
		return BayesianNetworkUtils.normalize(distribution);
	}

	/**
	 * Method to check if a given sample is
	 * consistent with the given evidences
	 * @param evidences
	 * @param sample
	 * @return
	 */
	private static boolean isConsistent(Map<String, String> evidences, Map<String, String> sample) {
		for(Map.Entry<String, String> entry : evidences.entrySet()) {
			if(!sample.get(entry.getKey()).equalsIgnoreCase(entry.getValue())) {
				return false;
			}
		}
		return true;
	}

	private static Map<String, String> getSampleFromBayesianNetwork(BayesianNetwork network) {
		Map<String, String> sample = new HashMap<String, String>();
		List<BayesianNetwork.Node> nodeList = BayesianNetworkUtils.getNodesInTopologicalOrder(network);
		for(Node node : nodeList) {
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
		return sample;
	}
	
	@Override
	public String toString() {
		return InferenceAlgorithmType.REJECTION.getAlgorithmDescription();
	}
}
