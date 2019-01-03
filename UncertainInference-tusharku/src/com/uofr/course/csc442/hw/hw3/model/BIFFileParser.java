package com.uofr.course.csc442.hw.hw3.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * 
 * Class to parse a given bif file and create a 
 * Bayesian Network from it using all the values 
 * extracted from the file.
 * @author tusharkumar
 *
 */
public final class BIFFileParser {
	
	/**
	 * Main Method to create the Bayesian Network
	 * from the bif file provided at the path
	 * @param filePath
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static BayesianNetwork getNetworkFromFile(File file) throws ParserConfigurationException, SAXException, IOException {
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.parse(file);
	    doc.getDocumentElement().normalize();	    	    
	    Map<String, List<String>> parentMap = new HashMap<String, List<String>>();	    
		Map<String, List<String>> variableDomainMap = getVariableDomainMap(doc);
	    
		NodeList nList = doc.getElementsByTagName(BayesianNetworkConstants.DEFINITION);
	    Map<String, Map<Integer, Double>> probValMap = new HashMap<String, Map<Integer, Double>>();
	    Map<String, Map<List<Integer>, Map<Integer, Double>>> conditionalProbValMap = new HashMap<String, Map<List<Integer>, Map<Integer, Double>>>();
	    
	    for (int temp = 0; temp < nList.getLength(); temp++) {
	        Node nNode = nList.item(temp);
	        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	        	Element eElement = (Element) nNode;
	        	String varName = eElement.getElementsByTagName(BayesianNetworkConstants.FOR_TAG)
	                      .item(0).getTextContent();
	        	NodeList givenList = eElement.getElementsByTagName(BayesianNetworkConstants.GIVEN_TAG);
	        	if(givenList == null || givenList.getLength() == 0) {
	        		buildPriorProbabilityMap(probValMap, eElement, varName);
	        	}
	        	else {
	        		Map<List<Integer>, Map<Integer, Double>> conditionalProbValMapForIdx = new HashMap<List<Integer>, Map<Integer, Double>>();
	        		List<String> parents = new ArrayList<String>();
		        	for(int childCount = 0; childCount < givenList.getLength(); childCount++) {
		        		parents.add(givenList.item(childCount).getTextContent());
		        	}
		        	parentMap.put(varName, parents);
		        	Node table = eElement.getElementsByTagName(BayesianNetworkConstants.TABLE_TAG).item(0);
		        	NodeList childNodes = table.getChildNodes();

		        	List<List<Integer>> condProbMapKeys = getCPTKeys(givenList);
		        	
		        	int index = 0;
		        	if(childNodes.getLength() == 1) {
		        		buildCPTFromSingleLineTable(variableDomainMap, 
		        				varName, conditionalProbValMapForIdx, childNodes,
								condProbMapKeys, index);
		        	}
		        	else {
		        		buildCPTForMultiLineTable(conditionalProbValMapForIdx, 
		        				childNodes, condProbMapKeys, index);
		        	}
		        	conditionalProbValMap.put(varName, conditionalProbValMapForIdx);
	        	}
	        }
	    }	    
	    return BayesianNetworkUtils.generateBayesianNetwork(probValMap, conditionalProbValMap, variableDomainMap, parentMap);
	}

	/**
	 * Method to extract the conditional probability table
	 * values from files which have multi line format 
	 * for these tables. Like aima-alarm.xml
	 * @param conditionalProbValMapForIdx
	 * @param ch
	 * @param condProbMapKeys
	 * @param index
	 */
	private static void buildCPTForMultiLineTable(Map<List<Integer>, Map<Integer, Double>> conditionalProbValMapForIdx,
			NodeList childNodes, List<List<Integer>> condProbMapKeys, int index) {
		for(int childCount = 0; childCount < childNodes.getLength(); childCount++) {
			Node a = childNodes.item(childCount);
			String content = a.getTextContent().trim();
			if(content != null && content.length() > 0 && !a.getNodeName().contains(BayesianNetworkConstants.COMMENT_TEXT)) {
				String[] probVal = content.trim().split(" ");		        			
				Map<Integer, Double> probValMapForIdx = new HashMap<Integer, Double>();
				for(int idx = 0, count=0; idx < probVal.length; idx++) {	
					if(probVal[idx].trim().length() > 0) {
						probValMapForIdx.put(count, Double.parseDouble(probVal[idx]));
						count++;
					}
				}
				conditionalProbValMapForIdx.put(condProbMapKeys.get(index), probValMapForIdx);		        			
				index++;
			}
		}
	}

	/**
	 * Method to extract the conditional probability table
	 * values from files which have multi line format 
	 * for these tables. Like dog-program.xml
	 * @param variableDomainMap
	 * @param varName
	 * @param conditionalProbValMapForIdx
	 * @param ch
	 * @param condProbMapKeys
	 * @param index
	 */
	private static void buildCPTFromSingleLineTable(Map<String, List<String>> variableDomainMap, String varName,
			Map<List<Integer>, Map<Integer, Double>> conditionalProbValMapForIdx, NodeList childNodes,
			List<List<Integer>> condProbMapKeys, int index) {
		int varDomainSize = variableDomainMap.get(varName).size();
		Node a = childNodes.item(0);
		String content = a.getTextContent().trim();
		if(content != null && content.length() > 0 && !a.getNodeName().contains(BayesianNetworkConstants.COMMENT_TEXT)) {
			content = content.trim().replaceAll(" +", " ");
			String[] probVal = content.trim().split(" ");		        			
			Map<Integer, Double> probValMapForIdx = new HashMap<Integer, Double>();
			for(int idx = 0, count=0; idx < probVal.length; idx++) {	
				if(probVal[idx].trim().length() > 0) {
					probValMapForIdx.put(count, Double.parseDouble(probVal[idx]));
					count++;
				}
				if(((count) % varDomainSize) == 0) {
					conditionalProbValMapForIdx.put(condProbMapKeys.get(index), probValMapForIdx);		        			
					probValMapForIdx = new HashMap<Integer, Double>();
					index++;
					count=0;
				}
			}			
		}
	}

	/**
	 * Method to build the necessary conditional probability
	 * integer keys by creating a bit representation of the 
	 * number of parents present so 2 parents means 2^2 = 4 distributions
	 * required.
	 * We will be converting all numbers from 0 to 4-1 into its bit form 
	 * [0,0 ; 0,1 ; 1,0 ; 1,1]
	 * @param givenList
	 * @return
	 */
	private static List<List<Integer>> getCPTKeys(NodeList givenList) {
		List<List<Integer>> condProbMapKeys = new ArrayList<List<Integer>>();
		for(int number = 0;number<Math.pow(2, givenList.getLength());number++){
			String bitRepresentation = Integer.toBinaryString(number);
			if(bitRepresentation.length() < givenList.getLength()) {
				StringBuilder str = new StringBuilder();
				for(int i=bitRepresentation.length(); i < givenList.getLength(); i++) {
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
	 * Method to build the prior probability mapping
	 * from the table given in the bif files.
	 * @param probValMap
	 * @param eElement
	 * @param varName
	 */
	private static void buildPriorProbabilityMap(Map<String, Map<Integer, Double>> probValMap, Element eElement,
			String varName) {
		String table = eElement.getElementsByTagName(BayesianNetworkConstants.TABLE_TAG).item(0).getTextContent();
		String[] probVal = table.split(" ");
		Map<Integer, Double> probValMapForIdx = new HashMap<Integer, Double>();
		for(int idx = 0 ; idx < probVal.length; idx++) {
			probValMapForIdx.put(idx, Double.parseDouble(probVal[idx]));
		}
		probValMap.put(varName, probValMapForIdx);
	}

	/**
	 * Method to build the domains of all variable utilizing 
	 * the information present in the variable tag of the the 
	 * XML bif file.
	 * @param doc
	 * @return
	 */
	private static Map<String, List<String>> getVariableDomainMap(Document doc) {
		NodeList nList = doc.getElementsByTagName(BayesianNetworkConstants.VARIABLE_TAG);
	    Map<String, List<String>> variableDomainMap = new HashMap<String, List<String>>();
	    for (int temp = 0; temp < nList.getLength(); temp++) {
	        Node nNode = nList.item(temp);
	        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	        	Element eElement = (Element) nNode;
	        	String varName = eElement.getElementsByTagName(BayesianNetworkConstants.NAME_TAG)
	                      .item(0).getTextContent();
	        	List<String> outcomes = new ArrayList<String>();
	        	NodeList outcomeList = eElement.getElementsByTagName(BayesianNetworkConstants.OUTCOME_TAG);
	        	for(int childCount = 0; childCount < outcomeList.getLength(); childCount++) {
	        		outcomes.add(outcomeList.item(childCount).getTextContent());
	        	}
	        	variableDomainMap.put(varName, outcomes);	        	
	        }
	    }
		return variableDomainMap;
	}	
}
