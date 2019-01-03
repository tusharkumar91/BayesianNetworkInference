package com.uofr.course.csc442.hw.hw3.model;

/**
 * Enum created to represent the options
 * for running the program
 * @author tusharkumar
 *
 */
public enum OptionType {

	BIF_NAME("file"),
	INFERENCE_ALGO("algo"),
	EVIDENCE("evidence"),
	QUERY("query"),
	SAMPLES("samples"),
	MC_BURN_IN("burn-in"),
	FILE_PATH("filepath");
	
	private String optionName;
	
	private OptionType(String optionName) {
		this.optionName = optionName;
	}

	public String getOptionName() {
		return optionName;
	}
	
	public String toString() {
		return optionName;
	}	
	
	public static OptionType getOptionTypeFromName(String optionName) {
		for(OptionType optionType: OptionType.values()) {
			if(optionType.optionName.equalsIgnoreCase(optionName)) {
				return optionType;
			}
		}
		return null;
	}
	
	public static String getValidOptionNames() {
		StringBuilder str = new StringBuilder();
		for(OptionType optionType: OptionType.values()) {
			str.append(optionType.optionName);
			str.append(",");
			str.append(BayesianNetworkConstants.SPACE);
		}
		return str.toString();
	}
}
