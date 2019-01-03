package com.uofr.course.csc442.hw.hw3.simulation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.uofr.course.csc442.hw.hw3.algorithms.Enumeration;
import com.uofr.course.csc442.hw.hw3.algorithms.GibbsSampling;
import com.uofr.course.csc442.hw.hw3.algorithms.LikelihoodWeighting;
import com.uofr.course.csc442.hw.hw3.algorithms.RejectionSampling;
import com.uofr.course.csc442.hw.hw3.model.BIFFileParser;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetwork;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetwork.Node;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetworkUtils;
import com.uofr.course.csc442.hw.hw3.model.InferenceAlgorithm;
import com.uofr.course.csc442.hw.hw3.model.InferenceAlgorithmFactory;
import com.uofr.course.csc442.hw.hw3.model.InferenceAlgorithmType;

/**
 * Class to simulate the creation of networks
 * with larger nodes and then test the execution
 * time of the different inference algorithms 
 * with it.
 * @author tusharkumar
 */
public class InferenceSimulator {
	/**
	 * Helper method to generate a random query
	 * @param bn
	 * @return
	 */
	private String generateRandomQuery(BayesianNetwork bn) {
		List<String> nodes = new ArrayList<String>(bn.getHeaderTable().keySet());
		int randomEvidenceIdx = ThreadLocalRandom.current().nextInt(0, nodes.size());
		return nodes.get(randomEvidenceIdx);
	}

	/**
	 * Helper method to generate a random set of evidences
	 * @param domainOfVars
	 * @param bn
	 * @return
	 */
	private Map<String, String> generateRandomEvidences(List<String> domainOfVars, BayesianNetwork bn) {
		Map<String, String> evidence = new HashMap<String, String>();
		int evidenceLen = ThreadLocalRandom.current().nextInt(1, 4);
		List<String> nodes = new ArrayList<String>(bn.getHeaderTable().keySet());
		for(int i=0; i<evidenceLen;) {
			int randomEvidenceIdx = ThreadLocalRandom.current().nextInt(0, nodes.size());
			if(evidence.containsKey(nodes.get(randomEvidenceIdx))) {
				continue;
			}
			else {
				int randomEvidenceValueIdx = ThreadLocalRandom.current().nextInt(0, domainOfVars.size());
				evidence.put(nodes.get(randomEvidenceIdx), domainOfVars.get(randomEvidenceValueIdx));
				i++;
			}
		}
		return evidence;
	}

	/**
	 * Method to generate a set of child nodes given the set of nodes
	 * from which parents must be selected
	 * @param nodeList
	 * @param nextNode
	 * @param nodeSize
	 * @param domainOfVars
	 * @param headerTable
	 * @param bn
	 */
	private void generateChildNodes(List<Node> nodeList, int nextNode, int nodeSize, List<String> domainOfVars, Map<String, Node> headerTable, BayesianNetwork bn) {
		if(nodeSize <= 0) {
			return;
		}
		double val = Math.random() * 10;
		int childSize = 1 + (int)Math.ceil(val);
		List<Node> newNodeList = new ArrayList<Node>();
		for(int i=0; i< childSize; i++) {	
			int min = 1;
			int max = nodeList.size();
			List<Integer> parentIndexes = new ArrayList<Integer>();
			
			int parentSize = ThreadLocalRandom.current().nextInt(min, max);
			int count = parentSize;
			while(count > 0) {
				int parentIndex = ThreadLocalRandom.current().nextInt(0, max);
				if(!parentIndexes.contains(parentIndex)) {
					count--;
					parentIndexes.add(parentIndex);
				}
			}
			Map<List<Integer>, Map<Integer, Double>> conditionalTable = new HashMap<List<Integer>, Map<Integer, Double>>();
			List<List<Integer>> cptKeys = getCPTKeys(parentIndexes);
			for(List<Integer> cptKey : cptKeys) {
				Map<Integer, Double> condMap = new HashMap<Integer, Double>();
				double p = Math.random();
				condMap.put(0, p);
				condMap.put(1, 1-p);
				conditionalTable.put(cptKey, condMap);
			}
			
			Node childNode = bn.new Node(Integer.toString(nextNode), domainOfVars);
			childNode.setConditionalTable(conditionalTable);
			for(int parentIdx : parentIndexes) {
				Node parentNode = nodeList.get(parentIdx);
				parentNode.addChild(childNode);
			}
			headerTable.put(Integer.toString(nextNode), childNode);
			newNodeList.add(childNode);
			nextNode++;
			nodeSize--;					
		}
		generateChildNodes(newNodeList, nextNode, nodeSize, domainOfVars, headerTable, bn);
	}
	
	/**
	 * Method to get the keys for the conditional 
	 * probability distribution.
	 * This will be jsut the bit representation of all 
	 * numbers from 0 to 2^(number of parents)
	 * @param givenList
	 * @return
	 */
	private static List<List<Integer>> getCPTKeys(List<Integer> givenList) {
		List<List<Integer>> condProbMapKeys = new ArrayList<List<Integer>>();
		for(int number = 0;number<Math.pow(2, givenList.size());number++){
			String bitRepresentation = Integer.toBinaryString(number);
			if(bitRepresentation.length() < givenList.size()) {
				StringBuilder str = new StringBuilder();
				for(int i=bitRepresentation.length(); i < givenList.size(); i++) {
					str.append('0');
				}
				bitRepresentation = str.toString() + bitRepresentation;
			}
			List<Integer> condProbMapKey = new ArrayList<>();
			for(int idx = 0; idx < bitRepresentation.length();idx++){
				//Is this bit set
				if(bitRepresentation.charAt(idx)=='1'){
					condProbMapKey.add(1);
				}
				else {
					condProbMapKey.add(0);
				}
			}
			condProbMapKeys.add(condProbMapKey);
		}
		return condProbMapKeys;
	}
	
	/**
	 * Method to generate graph of N nodes.
	 * It randomly selects K(a random number) as an integer 
	 * and then create K childNodes. It then 
	 * again randomly creates another K set of nodes 
	 * and then for each of them picks randomly parents 
	 * from the previously created K Nodes.
	 * Ofcourse this would cause the graph to be similar
	 * in looks to a tree where ever node is connected
	 * only to nodes a level above it.
	 * However fopr the sake of testing on number of nodes,
	 * I find this simplistic creation method to be just ok.
	 * @param algorithmType 
	 */
	public void runSimulationOnAutoGeneratedGraph(int maxNode, InferenceAlgorithmType algorithmType) {
		if(algorithmType != InferenceAlgorithmType.ALL) {
			runSimulationOnAutoGeneratedGraphForAlgo(maxNode, algorithmType);
			return;
		}
		
		List<Integer> enumerationTime = new ArrayList<Integer>();
		List<Integer> likelihoodTime = new ArrayList<Integer>();
		List<Integer> rejectionTime = new ArrayList<Integer>();
		List<Integer> gibbsTime = new ArrayList<Integer>();
		List<Integer> nodeCount = new ArrayList<Integer>();
		
		for(int nodeSize = 5; nodeSize <= maxNode; nodeSize++) {
			int currentNodeSize = nodeSize;
			int nextNode = 0;
			Map<String, BayesianNetwork.Node> headerTable = new HashMap<String, BayesianNetwork.Node>();
			BayesianNetwork bn = new BayesianNetwork();
			Node rootNode = bn.getRootNode();	
			List<String> domainOfVars = new ArrayList<String>();
			//We will fix the domain of all variables
			domainOfVars.add("true");
			domainOfVars.add("false");
			double val = Math.random() * 10;
			int childSize = 1 + (int)Math.ceil(val);
			List<Node> nodeList = new ArrayList<Node>();
			for(int i=0; i< childSize; i++) {					
				Node childNode = bn.new Node(Integer.toString(nextNode), domainOfVars);
				Map<Integer, Double> priorMap = new HashMap<Integer, Double>();
				double p = Math.random();
				priorMap.put(0, p);
				priorMap.put(1, 1-p);
				childNode.setPriorTable(priorMap);
				rootNode.addChild(childNode);
				nodeList.add(childNode);
				headerTable.put(Integer.toString(nextNode), childNode);
				nextNode++;		
				currentNodeSize--;
			}

			generateChildNodes(nodeList, nextNode, currentNodeSize, domainOfVars, headerTable, bn);
			bn.setHeaderTable(headerTable);
			Map<String, String> evidences = generateRandomEvidences(domainOfVars, bn);
			String query = generateRandomQuery(bn);		
			
			long startTime = System.currentTimeMillis();
			new Enumeration().ask(query, evidences, bn);
			enumerationTime.add((int) (System.currentTimeMillis() - startTime));	
			startTime = System.currentTimeMillis();
			new LikelihoodWeighting(10000).ask(query, evidences, bn);
			likelihoodTime.add((int) (System.currentTimeMillis() - startTime));	
			startTime = System.currentTimeMillis();
			new RejectionSampling(10000).ask(query, evidences, bn);
			rejectionTime.add((int) (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
			new GibbsSampling(10000).ask(query, evidences, bn);
			gibbsTime.add((int) (System.currentTimeMillis() - startTime));	
			nodeCount.add(nodeSize);
			System.out.println("Running simulation with node count of " + nodeSize);
		}

		System.out.println("\n");
		System.out.println("Results are for the following node counts");
		System.out.println(nodeCount);
		System.out.println("=============================");
		System.out.println("\n");
		System.out.println("Enumeration Timings in ms");
		System.out.println("--------------------------");
		System.out.println(enumerationTime);
		System.out.println("\n");
		System.out.println("Likelihood Weighting Timings in ms");
		System.out.println("--------------------------");
		System.out.println(likelihoodTime);
		System.out.println("\n");
		System.out.println("Rejection Sampling Timings in ms");
		System.out.println("--------------------------");
		System.out.println(rejectionTime);
		System.out.println("\n");
		System.out.println("Gibbs Sampling Timings in ms");
		System.out.println("--------------------------");
		System.out.println(gibbsTime);		
	}
	
	/**
	 * Helper method to execute simulation for a specific algorithm
	 * @param maxNode
	 * @param algorithmType
	 */
	private void runSimulationOnAutoGeneratedGraphForAlgo(int maxNode, InferenceAlgorithmType algorithmType) {
		List<Integer> algoTime = new ArrayList<Integer>();
		List<Integer> nodeCount = new ArrayList<Integer>();
	
		//for(int nodeSize = 5; nodeSize <= maxNode; nodeSize++) {
		int currentNodeSize = maxNode;
		int nextNode = 0;
		Map<String, BayesianNetwork.Node> headerTable = new HashMap<String, BayesianNetwork.Node>();
		BayesianNetwork bn = new BayesianNetwork();
		Node rootNode = bn.getRootNode();	
		List<String> domainOfVars = new ArrayList<String>();
		//We will fix the domain of all variables
		domainOfVars.add("true");
		domainOfVars.add("false");
		double val = Math.random() * 10;
		int childSize = 1 + (int)Math.ceil(val);
		List<Node> nodeList = new ArrayList<Node>();
		for(int i=0; i< childSize; i++) {					
			Node childNode = bn.new Node(Integer.toString(nextNode), domainOfVars);
			Map<Integer, Double> priorMap = new HashMap<Integer, Double>();
			double p = Math.random();
			priorMap.put(0, p);
			priorMap.put(1, 1-p);
			childNode.setPriorTable(priorMap);
			rootNode.addChild(childNode);
			nodeList.add(childNode);
			headerTable.put(Integer.toString(nextNode), childNode);
			nextNode++;		
			currentNodeSize--;
		}

		generateChildNodes(nodeList, nextNode, currentNodeSize, domainOfVars, headerTable, bn);
		bn.setHeaderTable(headerTable);
		Map<String, String> evidences = generateRandomEvidences(domainOfVars, bn);
		String query = generateRandomQuery(bn);		
		System.out.println("Running simulation with node count of " + maxNode);
		System.out.println("Generated a random query for node variable named " + query);
		System.out.println("Generated a random evidence for query : " + evidences);
		
		long startTime = System.currentTimeMillis();
		InferenceAlgorithm algo = InferenceAlgorithmFactory.getInferenceAlgorithm(algorithmType, 1000000, 10000);
		Map<String, Double> distribution = algo.ask(query, evidences, bn);
		algoTime.add((int) (System.currentTimeMillis() - startTime));	
		nodeCount.add(maxNode);
		
		//}

		System.out.println("\n");
		System.out.println("Results are for the following node counts");
		System.out.println(nodeCount);
		System.out.println("=============================");
		System.out.println("\n");
		System.out.println("Timings in ms for " + algorithmType.getAlgorithmDescription());
		System.out.println("--------------------------");
		System.out.println(algoTime);		
		System.out.println("\n");
		System.out.println("Distribution inferenced : " + distribution);
	}

	private void evaluateInferenceAlgorithm() throws ParserConfigurationException, SAXException, IOException {
		String query = "B";
		String FILE_DIRECTORY_PATH = "src/com/uofr/course/csc442/hw/hw3/examples/";
		String filePath = null;
		Map<String, String> evidence = new HashMap<String, String>();
		evidence.put("J", "true");
		evidence.put("M", "true");
		filePath = FILE_DIRECTORY_PATH + "aima-alarm.xml";
		BayesianNetwork network = BIFFileParser.getNetworkFromFile(new File(filePath));
		List<String> averageKLD = new ArrayList<String>();
		List<Integer> sample = new ArrayList<Integer>();
		List<Integer> time = new ArrayList<Integer>(); 		
		int stepSize = 1000;
		Map<String, Double> correctDist = new Enumeration().ask(query, evidence, network);
		
		double KLDSum = 0.0;
		long startTime = System.currentTimeMillis();
		/*for(int rep = 1; rep<=1; rep++) {
			KLDSum += BayesianNetworkUtils.KLDivergence(correctDist, 
					new Enumeration().ask(query, evidence, network));
		}
		averageKLD.add(Double.toString(KLDSum));
		time.add((int) (System.currentTimeMillis() - startTime));
		sample.add(1);*/
		stepSize = 1000;
		System.out.println(correctDist);
		for(int i=1000; i<=10000; i += stepSize) {
			KLDSum = 0.0;
			startTime = System.currentTimeMillis();
			Map<String, Double> approxDist = new LikelihoodWeighting(i).ask(query, evidence, network);
			for(int rep = 1; rep<=1; rep++) {
				KLDSum += BayesianNetworkUtils.KLDivergence(correctDist, 
						approxDist);
			}
			System.out.println(approxDist);
			System.out.println(KLDSum);
			//averageKLD.add(Double.toString(KLDSum));
			time.add((int) (System.currentTimeMillis() - startTime));
			sample.add(i);
		}
		
		System.out.println("KL Divergence score for comparing Enumeration and Gibbs Sampling with respect to number of samples");
		System.out.println("==================================================");
		System.out.println("\n");
		System.out.println("Sample Counts");
		System.out.println(sample);
		System.out.println("\n");
		System.out.println("KL Divergence scores");
		System.out.println(time);
	}
	
	/**
	 * Main method to run the simulation
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

		int maxNode = 20;
		InferenceAlgorithmType algorithmType = InferenceAlgorithmType.ALL;
		if(args.length > 0) {
			for(int i=0; i<args.length;) {
				if(args[i].charAt(0) != '-') {
					throw new IllegalArgumentException("argument name not prefixed with -");
				}
				if(args[i].length() <= 1) {
					throw new IllegalArgumentException("argument name must be of more than 1 character");
				}
				boolean isNodeArgument = "nodes".equalsIgnoreCase(args[i].substring(1));
				boolean isAlgoArgument = "algo".equalsIgnoreCase(args[i].substring(1));
				
				if(!isNodeArgument && !isAlgoArgument) {
					throw new IllegalArgumentException("argument name must be amongst the following : nodes or algo " + 
					" but was " + args[i].substring(1));
				}
				
				if(i+1 >= args.length) {
					throw new IllegalArgumentException("No value given for option " + (isNodeArgument ? "nodes" : "algo"));
				}
				String argumentValue = args[i+1];
				if(isAlgoArgument) {
					if("likelihood".equalsIgnoreCase(argumentValue.toLowerCase())) {
						algorithmType = InferenceAlgorithmType.LIKELIHOOD;
					}
					if("rejection".equalsIgnoreCase(argumentValue.toLowerCase())) {
						algorithmType = InferenceAlgorithmType.REJECTION;
					}
					if("gibbs".equalsIgnoreCase(argumentValue.toLowerCase())) {
						algorithmType = InferenceAlgorithmType.GIBBS;
					}
					if("enumeration".equalsIgnoreCase(argumentValue.toLowerCase())) {
						algorithmType = InferenceAlgorithmType.ENUMERATION;
					}
					i += 2;
				}
				if(isNodeArgument) {
					try{
						maxNode = Integer.parseInt(argumentValue);
					}
					catch(NumberFormatException e) {
						throw new IllegalArgumentException("Unable to get the integral value for maxNodes");
					}
					i += 2;
				}
			}
		}
		if(maxNode < 5) {
			System.out.println("Maximum nodes provided was less than 5 which would not really be useful and can cause issues with my procedure");
			System.out.println("Using 20 as value for maximum nodes instead");
			maxNode = 20;
		}
		if((algorithmType == InferenceAlgorithmType.ENUMERATION || algorithmType == InferenceAlgorithmType.ALL) && maxNode > 20) {
			System.out.println("Maximum nodes provided was more than 20 which might lead to huge runtimes for enumeration");
			System.out.println("Using 20 as value for maximum nodes instead(You could"
					+ " run specifying the algo as an approximate inference one for larger nodes");
			maxNode = 20;
		}
		InferenceSimulator simulator = new InferenceSimulator();
		simulator.runSimulationOnAutoGeneratedGraph(maxNode, algorithmType);
		//simulator.evaluateInferenceAlgorithm();
	}

}
