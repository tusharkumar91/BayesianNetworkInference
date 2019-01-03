package com.uofr.course.csc442.hw.hw3.model;

import com.uofr.course.csc442.hw.hw3.algorithms.Enumeration;
import com.uofr.course.csc442.hw.hw3.algorithms.GibbsSampling;
import com.uofr.course.csc442.hw.hw3.algorithms.LikelihoodWeighting;
import com.uofr.course.csc442.hw.hw3.algorithms.RejectionSampling;

/**
 * Factory to manufacture the inference algorithm 
 * object and provide to the client.
 * @author tusharkumar
 *
 */
public final class InferenceAlgorithmFactory {

	/**
	 * Main factory method tht does the creation of 
	 * algorithm work
	 * @param algorithmType
	 * @param numberOfIterations
	 * @return
	 */
	public static final InferenceAlgorithm getInferenceAlgorithm(InferenceAlgorithmType algorithmType, int numberOfSamples , int burnInSamples) {
		InferenceAlgorithm algorithm = null;
		switch(algorithmType) {
		case ENUMERATION:
			algorithm = new Enumeration();
			break;
		case LIKELIHOOD:
			algorithm = new LikelihoodWeighting(numberOfSamples);
			break;
		case REJECTION:
			algorithm = new RejectionSampling(numberOfSamples);
			break;
		case GIBBS:
			algorithm = new GibbsSampling(numberOfSamples, burnInSamples);
			break;
		}		
		return algorithm;
	}
}
