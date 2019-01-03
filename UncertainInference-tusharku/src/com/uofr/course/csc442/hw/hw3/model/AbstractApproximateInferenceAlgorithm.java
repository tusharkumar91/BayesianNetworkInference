package com.uofr.course.csc442.hw.hw3.model;


/**
 * Abstract class to encapsulate all common
 * functionalities of Approximate Inference Algorithms
 * like setting the 
 * @author tusharkumar
 *
 */
public abstract class AbstractApproximateInferenceAlgorithm implements InferenceAlgorithm{

	//Number of samples till which the inference would be run
	protected int numberOfSamples;

	public void setNumberOfSamples(int numberOfSamples) {
		this.numberOfSamples = numberOfSamples;
	}		
	
	public AbstractApproximateInferenceAlgorithm(int numberOfSamples) {
		this.numberOfSamples = numberOfSamples;
	}
}
