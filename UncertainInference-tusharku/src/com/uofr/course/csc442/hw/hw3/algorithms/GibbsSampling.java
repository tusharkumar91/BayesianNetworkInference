package com.uofr.course.csc442.hw.hw3.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.uofr.course.csc442.hw.hw3.model.AbstractApproximateInferenceAlgorithm;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetwork;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetworkUtils;
import com.uofr.course.csc442.hw.hw3.model.InferenceAlgorithmType;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetwork.Node;

/**
 * Class to encapsulate the approximate inference algorithm
 * utilizing the Gibbs Sampling mechanism.
 * The code is modeled based on the pseudocode
 * in AIMA Fig 14.16 3rd ed. 
 * @author tusharkumar
 *
 */
public class GibbsSampling extends AbstractApproximateInferenceAlgorithm{
	
	public GibbsSampling(int numberOfSamples) {
		super(numberOfSamples);
	}

	public GibbsSampling(int numberOfSamples, int burnInSamples) {
		super(numberOfSamples);
		this.burnInSamples = burnInSamples;		
	}

	private int burnInSamples = 0;
	
	/**
	 * Overridden method for asking the network for 
	 * the distribution of a query variable using the 
	 * provided evidences.
	 */
	@Override
	public Map<String, Double> ask(String query, Map<String, String> evidences, 
			BayesianNetwork network){
		List<BayesianNetwork.Node> nodeList = BayesianNetworkUtils.getNodesInTopologicalOrder(network);
		List<BayesianNetwork.Node> nonEvidenceNodes = new ArrayList<BayesianNetwork.Node>();
		for(Node node : nodeList) {
			if(!evidences.containsKey(node.getName())) {
				nonEvidenceNodes.add(node);
			}
		}
		
		Map<String, String> sample = new HashMap<String, String>();
		for(Node node : nonEvidenceNodes) {
			int randomDomainIndex = new Random().nextInt(node.getDomain().size());
			sample.put(node.getName(), node.getDomain().get(randomDomainIndex));
		}
			
		for(String key : evidences.keySet()) {
			sample.put(key, evidences.get(key));
		}
		
		Map<String, Double> distribution = new HashMap<String, Double>();
		int totalSamples = (int) numberOfSamples/nonEvidenceNodes.size();
		int sampleCount = 0;
		for(int iter = 1; iter <= totalSamples; iter++) {
			for(Node nonEvidenceNode : nonEvidenceNodes) {
				updateSample(nonEvidenceNode, sample, network);
				if(sampleCount >= burnInSamples) {
					distribution.put(sample.get(query), 
							distribution.getOrDefault(sample.get(query), 0.0) + 1.0);
				}
				sampleCount++;
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
	 * Method to update the value of given node in the sample
	 * by sampling of the Conditional Distribution of this node
	 * given its markov blanket.
	 * @param node
	 * @param sample
	 * @param network
	 */
	private static void updateSample(Node node, Map<String, String> sample, BayesianNetwork network) {
		Map<String, Double> probabilityValuesForNode = new HashMap<String, Double>();				
								
		if(node.getPriorTable().isEmpty()) {
			List<Integer> parentIndex = new ArrayList<Integer>();
			for(Node parent : node.getParents()) {
				String parentVal = sample.get(parent.getName());
				parentIndex.add(parent.getDomain().indexOf(parentVal));
			}
			Map<List<Integer>, Map<Integer, Double>> cpt = node.getConditionalTable();
			Map<Integer, Double> cptForParentValue = cpt.get(parentIndex);
			
			for(int domainIdx = 0; domainIdx < node.getDomain().size(); domainIdx++) {
				String domainValue = node.getDomain().get(domainIdx);
				sample.put(node.getName(), domainValue);
				double childProbability = computeProbability(node, sample);
				probabilityValuesForNode.put(domainValue, (cptForParentValue.get(domainIdx))*childProbability);
			}
		}		
		else {
			Map<Integer, Double> priorValues = node.getPriorTable();
			for(int domainIdx = 0; domainIdx < node.getDomain().size(); domainIdx++) {
				String domainValue = node.getDomain().get(domainIdx);
				sample.put(node.getName(), domainValue);
				double childProbability = computeProbability(node, sample);
				probabilityValuesForNode.put(domainValue, (priorValues.get(domainIdx))*childProbability);
			}
		}
		
		probabilityValuesForNode = BayesianNetworkUtils.normalize(probabilityValuesForNode);
		double p = Math.random();
		double cumulativeProbability = 0.0;
		Set<String> values = probabilityValuesForNode.keySet();
		for (String value : values) {
		    cumulativeProbability += probabilityValuesForNode.get(value);
		    if (p <= cumulativeProbability) {
		        sample.put(node.getName(), value);
		        break;
		    }
		}
	}

	/**
	 * Method to compute the Probability of a node
	 * conditioned on its parents having the value specified in the
	 * sample
	 * @param node
	 * @param sample
	 * @return
	 */
	private static double computeProbability(Node node, Map<String, String> sample) {
		double childProbability = 1.0;
		for(Node child : node.getChildren()) {
			List<Integer> childsParentIndex = new ArrayList<Integer>();
			Map<List<Integer>, Map<Integer, Double>> childCpt = child.getConditionalTable();
			for(Node parent : child.getParents()) {
				String parentVal = sample.get(parent.getName());
				childsParentIndex.add(parent.getDomain().indexOf(parentVal));
			}
			Map<Integer, Double> cptForChildValue = childCpt.get(childsParentIndex);
			int childSampleValueIndex = child.getDomain().indexOf(sample.get(child.getName()));
			childProbability *= cptForChildValue.get(childSampleValueIndex);			
		}
		return childProbability;
	}

	@Override
	public String toString() {
		return InferenceAlgorithmType.GIBBS.getAlgorithmDescription();
	}
	
	public int getBurnInSamples() {
		return burnInSamples;
	}

	public void setBurnInSamples(int burnInSamples) {
		this.burnInSamples = burnInSamples;
	}
}
