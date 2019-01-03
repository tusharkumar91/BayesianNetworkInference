package com.uofr.course.csc442.hw.hw3.model;

/**
 * ENUM to represent the different types
 * of algorithms we have in store
 * for running inference on BN
 * @author tusharkumar
 *
 */
public enum InferenceAlgorithmType {
	ENUMERATION("Enumeration", "Enumeration Exact Inference"),
	LIKELIHOOD("Likelihood", "Likelihood Weighting Approximate Inference"),
	REJECTION("Rejection",  "Rejection Sampling Approximate Inference"),
	GIBBS("Gibbs", "Gibbs Sampling Approximate Inference"),
	ALL("All", "All");
	
	private String algorithmName;
	private String algorithmDescription;
	
	private InferenceAlgorithmType(String algorithmName, String algorithmDescription) {
		this.algorithmName = algorithmName;
		this.algorithmDescription = algorithmDescription;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}
	
	public String getAlgorithmDescription() {
		return algorithmDescription;
	}
	
	public String toString() {
		return algorithmName;
	}	
	
	public static InferenceAlgorithmType getAlgorithmTypeFromName(String algorithmName) {
		for(InferenceAlgorithmType algorithmType: InferenceAlgorithmType.values()) {
			if(algorithmType.algorithmName.equalsIgnoreCase(algorithmName)) {
				return algorithmType;
			}
		}
		return null;
	}
}
