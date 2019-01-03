package com.uofr.course.csc442.hw.hw3.model;

import java.util.Map;

/**
 * Interface to establish the contract and generic method
 * for all inference algorithms be it exact or approximate. 
 * @author tusharkumar
 *
 */
public interface InferenceAlgorithm {
	
	/**
	 * Method to ask the network to return the answer to an input 
	 * query given a set of evidences for the query
	 * @param query - the variableName to query the distribution for
	 * @param evidences - mapping of all evidence variableNames to their values
	 * @param network - BayesianNetwork 
	 * @return
	 */
	public Map<String, Double> ask(String query, Map<String, String> evidences, BayesianNetwork network);
}
