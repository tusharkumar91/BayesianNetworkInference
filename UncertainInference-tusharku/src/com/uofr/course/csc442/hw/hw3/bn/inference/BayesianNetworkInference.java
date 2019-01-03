package com.uofr.course.csc442.hw.hw3.bn.inference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.uofr.course.csc442.hw.hw3.model.BIFFileParser;
import com.uofr.course.csc442.hw.hw3.model.BayesianNetwork;
import com.uofr.course.csc442.hw.hw3.model.InferenceAlgorithm;
import com.uofr.course.csc442.hw.hw3.model.InferenceAlgorithmFactory;
import com.uofr.course.csc442.hw.hw3.model.InferenceAlgorithmType;
import com.uofr.course.csc442.hw.hw3.model.OptionType;

/**
 * Main class to execute the inference procedure for all
 * kinds of example bif files using any of the inference 
 * algorithm
 * @author tusharkumar
 *
 */
public class BayesianNetworkInference {

	/**
	 * Method that uses the parameter provided as input
	 * to run the inference by first building the network
	 * from the given input bif file and then running
	 * provided algorithm on it.
	 * @param algorithmType
	 * @param query
	 * @param evidences
	 * @param filePath
	 * @param argfilePath 
	 * @param numberOfIterations
	 * @param burnInIterations
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void ask(InferenceAlgorithmType algorithmType, String query, 
			Map<String, String> evidences, String filePath,
			String argfilePath, int numberOfSamples, int burnInSamples) throws ParserConfigurationException, SAXException, IOException {
		File file;
		if(filePath != null) {
			file = new File(filePath);
		}
		else {
			file = new File(argfilePath);
		}
		BayesianNetwork network = BIFFileParser.getNetworkFromFile(file);
		if(!(network.getHeaderTable().containsKey(query))){
			throw new IllegalArgumentException("Query variable - '" + query + "' not one of the nodes of network "
					+ "which has the following nodes : " + network.getHeaderTable().keySet());
		}
		if(InferenceAlgorithmType.ALL.equals(algorithmType)) {
			for(InferenceAlgorithmType currentAlgorithmType : InferenceAlgorithmType.values()) {
				if(InferenceAlgorithmType.ALL.equals(currentAlgorithmType)){
					continue;
				}
				long startTime = System.currentTimeMillis();
				InferenceAlgorithm algorithm = InferenceAlgorithmFactory.getInferenceAlgorithm(currentAlgorithmType, numberOfSamples, burnInSamples);
				Map<String, Double> distribution = algorithm.ask(query, evidences, network);
				long timeTaken = System.currentTimeMillis() - startTime;
				printOutputForInference(query, algorithm, distribution, timeTaken, evidences);				
			}
		}
		else {
			long startTime = System.currentTimeMillis();
			InferenceAlgorithm algorithm = InferenceAlgorithmFactory.getInferenceAlgorithm(algorithmType, numberOfSamples, burnInSamples);
			Map<String, Double> distribution = algorithm.ask(query, evidences, network);
			long timeTaken = System.currentTimeMillis() - startTime;
			printOutputForInference(query, algorithm, distribution, timeTaken, evidences);			
		}
	}
	
	private void printOutputForInference(String query, InferenceAlgorithm algorithm,
			Map<String, Double> distribution, long timeTaken, Map<String, String> evidences) {
		StringBuilder str = new StringBuilder();
		str.append("==================================================");
		str.append("\n");
		str.append("Result of Running Inference Algorithm : ");
		str.append(algorithm.toString());
		str.append("\n\n");
		str.append("Evidence Given : ");
		str.append(evidences);
		str.append("\n\n");
		for(Map.Entry<String, Double> entry : distribution.entrySet()) {
			str.append("Probability for " + query + " being " + entry.getKey() + " = " + entry.getValue());
			str.append("\n");
			str.append("\n");
		}
		str.append("Time Taken for running the algorithm (in ms) = " + timeTaken);
		str.append("\n\n");
		System.out.println(str.toString());				
	}

	/**
	 * Main method to run the inference algorithms. 
	 * Example run arguments - "-algo gibbs -iter 100000 -burn-in 1000 -query A -file aima-alarm -evidence J true 
	 * -algo : The board Type you want to play on {enumeration, likelihood, rejection, gibbs, all}
	 * -iter : How many iterations should be chosen for approximate inference algorithm {Integer}
	 * --burn-in : What should be the burn in iterations period for GIBBS {Integer}
	 * -query : How many iterations of game you want to play {String}
	 * -file : While file should be used to build the graph {aima-alarm, aima-wet-grass, dog-problem}
	 * -evidence : What evidence you want to provide - (-evidence evidenceVariableName1 value1 evidenceVariableName2 value2)
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		String queryVariable = null;
		String FILE_DIRECTORY_PATH = "src/com/uofr/course/csc442/hw/hw3/examples/";
		String argfilePath = null;
		String filePath = null;
		Map<String, String> evidences = new HashMap<String, String>();
		InferenceAlgorithmType algorithmType = InferenceAlgorithmType.ENUMERATION;
		int numberOfSamples = 100000;
		int burnInSamples = 0;
		
		
		if(args.length > 0) {
			for(int i=0; i<args.length;) {
				if(args[i].charAt(0) != '-') {
					throw new IllegalArgumentException("argument name not prefixed with -");
				}
				if(args[i].length() <= 1) {
					throw new IllegalArgumentException("argument name must be of more than 1 character");
				}
				OptionType argumentName = OptionType.getOptionTypeFromName(args[i].substring(1));
				if(argumentName == null) {
					throw new IllegalArgumentException("argument name must be amongst the following : " + OptionType.getValidOptionNames() + 
					" but was " + args[i].substring(1));
				}
				
				if(i+1 >= args.length) {
					throw new IllegalArgumentException("No value given for option " + argumentName.getOptionName());
				}
				String argumentValue = args[i+1];
				switch(argumentName) {
				case INFERENCE_ALGO:
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
					if("all".equalsIgnoreCase(argumentValue.toLowerCase())) {
						algorithmType = InferenceAlgorithmType.ALL;
					}
					i += 2;
					break;
				case QUERY:
					queryVariable = argumentValue;
					i += 2;
					break;
				case EVIDENCE:
					i++;
					while(i < args.length && args[i].charAt(0) != '-') {
						if(i+1 >= args.length) {
							throw new IllegalArgumentException("No value given for evidence variable " + args[i]);
						}
						evidences.put(args[i], args[i+1]);
						i += 2;
					}
					break;
				case BIF_NAME:
					if("aima-alarm".equals(argumentValue)) {
						filePath = FILE_DIRECTORY_PATH + argumentValue + ".xml";
					}
					else if("aima-wet-grass".equals(argumentValue)) {
						filePath = FILE_DIRECTORY_PATH + argumentValue + ".xml";
					}
					else if("dog-problem".equals(argumentValue)) {
						filePath = FILE_DIRECTORY_PATH + argumentValue + ".xml";
					}
					i += 2;
					break;
				case SAMPLES:
					try{
						numberOfSamples = Integer.parseInt(argumentValue);
					}
					catch(NumberFormatException e) {
						throw new IllegalArgumentException("Unable to get the integral value for sample size");
					}
					i += 2;
					break;
				case MC_BURN_IN:
					try{
						burnInSamples = Integer.parseInt(argumentValue);
					}
					catch(NumberFormatException e) {
						throw new IllegalArgumentException("Unable to get the integral value for burn in iterations");
					}
					i += 2;
					break;	
				case FILE_PATH:
					argfilePath = argumentValue;
					i += 2;
				}								
			}
		}
		
		if(queryVariable == null) {
			throw new IllegalStateException("Query variable was not provided as an argument");
		}
				
		if(filePath == null && argfilePath == null) {
			throw new IllegalStateException("A file name was provided that was not amongst('aima-alarm/aima-wet-grass/dog-problem "
					+ "and there was no filepath provided for this");
		}
		
		
		BayesianNetworkInference bayesianNetworkInference = new BayesianNetworkInference();
		try {
			bayesianNetworkInference.ask(algorithmType, queryVariable, evidences, filePath, argfilePath, numberOfSamples, burnInSamples);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println("An error occurred file parsing the XML file");
			e.printStackTrace();
		}
	}	
}
