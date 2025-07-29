package com.podio.api.client;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.*;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 * Parses JSON objects as specified by <a href="https://www.rfc-editor.org/rfc/pdfrfc/rfc8259.txt.pdf">The JavaScript Object Notation (JSON) Data Interchange Format</a>

 Additional resources and credit:
 <a href="https://vajithc.medium.com/parsing-json-without-libraries-build-your-own-json-reader-in-java-1db8e6165039">source</a>
 */
public final class JSONParser {
    private static final Logger logger = Logger.getLogger(JSONParser.class.getName());
    private final static Character BEGIN_ARRAY = '[';
    private final static Character BEGIN_OBJECT = '{';
    private final static Character END_ARRAY = ']';
    private final static Character END_OBJECT = '}';
    private final static Character NAME_SEPARATOR = ':';
    private final static Character VALUE_SEPARATOR = ',';

    Optional<Map<String, Object>> parseJSON(String jSONString) {

	final HashMap<String, Object> parsedJSON = new HashMap<>();

	if (jSONString.charAt(0) != BEGIN_OBJECT || jSONString.charAt(jSONString.length() - 1) != END_OBJECT)
	    return Optional.empty();

	if (jSONString.equals("{}"))
	    return Optional.of(parsedJSON);

	jSONString = jSONString.substring(1, jSONString.length() - 1/*.lastIndexOf(END_OBJECT)*/);

	if (jSONString.charAt(jSONString.length() - 1) != VALUE_SEPARATOR.charValue())
	     jSONString = jSONString.concat(VALUE_SEPARATOR.toString());

	// // final var substituted = new ArrayList<String>();
	// // var substitutedJSONList = List.of(jSONString.splitWithDelimiters("(\".*\":)|(\".*\",)|(\".*\")", 0));
	
	// // logger.info(substitutedJSONList.toString());

	// // var substitutedJSON = substitutedJSONList
	// //     .stream()
	// //     .map(entry -> {

	// // 	    if(entry.startsWith("\"")) {

	// // 		substituted.add(entry);
			
	// // 		return "%" + (substituted.size() - 1);
	// // 	    }

	// // 	    return entry;
	// //     })
	// // 	.reduce("{}", (partial, current) -> partial.concat(current));

	var inQuotes = false;
	var lastValueSeparator = -1;

	/*final var arrayIndices = new Stack<Integer>();
	 final var objectIndices = new Stack<Integer>();
	 final var tempKey = new StringBuilder(), tempValue = new StringBuilder();*/
	
	for (var i = 0; i < jSONString.length(); i++) {

	     if(jSONString.charAt(i) == '"')
	 	inQuotes = !inQuotes;

	     if (inQuotes)
		 continue;
	     
	     /*if(jSONString.charAt(i) == BEGIN_ARRAY.charValue())
		 arrayIndices.push(i);

	     if (jSONString.charAt(i) == END_ARRAY.charValue()) {

		 tempValue.delete(0, tempValue.length() - 1);
		 
		 tempValue.append(jSONString.substring(arrayIndices.pop(), 0));
		 }*/

	     //if (jSONString.charAt(i) == BEGIN_OBJECT.charValue()) {

	     //parsedJSON.put(jSONString.substring(0, i).trim(), jSONString.substring(i, /*jSONString.lastIndexOf*/lastIndexOfIgnoreQuotes((END_OBJECT.charValue()) + 1, jSONString)).trim());

		 /*i += jSONString.lastIndexOf(END_OBJECT) + 1;*/
	     //	i = 0;

	     //	jSONString = jSONString.substring(/*jSONString.lastIndexOf*/lastIndexOfIgnoreQuotes((END_OBJECT.charValue()) + 1, jSONString), jSONString.length());

	     //	continue;
	     //}

	     //if (jSONString.charAt(i) == BEGIN_ARRAY.charValue()) {

	     //parsedJSON.put(jSONString.substring(0, i).trim(), jSONString.substring(i, /*jSONString.*/lastIndexOfIgnoreQuotes(END_ARRAY.charValue(), jSONString) + 1).trim());

		 /*i += jSONString.lastIndexOf(END_OBJECT) + 1;*/
		 //	i = 0;

		 //jSONString = jSONString.substring(/*jSONString.*/lastIndexOfIgnoreQuotes(END_ARRAY.charValue(), jSONString) + 1, jSONString.length());

		 //continue;
		 //}
	     
	    if (jSONString.charAt(i) == NAME_SEPARATOR) {

		final var key = jSONString.substring(lastValueSeparator + 1, i);//.trim();
		final var value = jSONString.substring(i + 1, lastValueSeparator = indexOfIgnoreQuotes(VALUE_SEPARATOR, jSONString.substring(i + 1, jSONString.length())) + i + 1);//.trim();
		
		i = lastValueSeparator;
		
		parsedJSON.put(key.trim(), value.trim());
		
		//parsedJSON.put(jSONString.substring(0, i).trim(), jSONString.substring(i + 1, /*jSONString.*/indexOfIgnoreQuotes(VALUE_SEPARATOR, jSONString)).trim());

		//jSONString = jSONString.substring(Math.min(/*jSONString.*/indexOfIgnoreQuotes(VALUE_SEPARATOR, jSONString) + 1, jSONString.length() - 1), jSONString.length());

		/*i -= jSONString.indexOf(VALUE_SEPARATOR) + 1;*/
		//i = 0;
	    }
	}

	parsedJSON.forEach((key, value) -> {

		 if (value instanceof String stringValue) {

		     final var firstCh = stringValue.charAt(0);
		     
		     if(firstCh == BEGIN_ARRAY)
			 parsedJSON.replace(key, parseJSONArray(stringValue).orElse(new ArrayList<>()));
		     
		     if (firstCh == BEGIN_OBJECT)
			 parsedJSON.replace(key, parseJSON(stringValue).orElse(new HashMap<>()));

		     try {

			 //parsedJSON.replace(key, Integer.parseInt(stringValue));
			 parsedJSON.replace(key, Double.parseDouble(stringValue));
		     }
		     catch (final Exception ignored) {}
		 }
	     });
	
	     return Optional.of(parsedJSON);
    }

    /**
     * Algorithm taken partially from: https://vajithc.medium.com/parsing-json-without-libraries-build-your-own-json-reader-in-java-1db8e6165039
     */
    Optional<List<Object>> parseJSONArray(String jSONString) {

        final var parsedJSON = new LinkedList<Object>();

	if (jSONString.charAt(0) != BEGIN_ARRAY || jSONString.charAt(jSONString.length() - 1) != END_ARRAY)
	    return Optional.empty();

	if (jSONString.equals("[]"))
	    return Optional.of(parsedJSON);
	
	//jSONString = jSONString.replace(" ", "");
	jSONString = jSONString.substring(1, jSONString.length() - 1);

	if (jSONString.charAt(jSONString.length() - 1) != VALUE_SEPARATOR)
	    jSONString = jSONString.concat(VALUE_SEPARATOR.toString());
	
	//var tempValue = "";

	var lastValueSep = -1;
	var skip = false;
	var objectConsumerCount = 0;
	var arrayConsumerCount = 0;
	
	for(var i = 0; i < jSONString.length(); i++) {

	    final var curCh = jSONString.charAt(i);

	    if (curCh == '"')
		skip = !skip;

	    if (curCh == BEGIN_OBJECT)
		objectConsumerCount++;

	    if (curCh == END_OBJECT)
		objectConsumerCount--;

	    if (curCh == BEGIN_ARRAY)
		arrayConsumerCount++;

	    if (curCh == END_ARRAY)
		arrayConsumerCount--;
	    
	    if (skip || objectConsumerCount != 0 || arrayConsumerCount != 0)
		continue;
	    
	    if (curCh == VALUE_SEPARATOR) {

		parsedJSON.add(jSONString.substring(lastValueSep + 1, lastValueSep = indexOfIgnoreQuotes(VALUE_SEPARATOR, jSONString.substring(i, jSONString.length())) + i).trim());
		
		i = lastValueSep;
	    }
	    
	    //switch (jSONString.charAt(i)) {

		/*case VALUE_SEPARATOR -> {

		parsedJSON.add(tempValue);
		tempValue = "";
		}*/
	    
	    //default -> {
		    
	    //tempValue = tempValue.concat(jSONString.substring(i, i + 1));

	    //if (i == jSONString.length() - 1)
	    //parsedJSON.add(tempValue);
	    //}
	    //}
	}
	
	return Optional.of(parsedJSON
			   .stream()
			   .map(value -> {

				   try {

				       return Integer.parseInt((String) value);
				   }
				   catch (Exception ignored) {}

				   try {

				       return Double.parseDouble((String) value);
				   }
				   catch (Exception ignored) {}

				   if (((String) value).charAt(0) == BEGIN_OBJECT) {

				       return parseJSON((String) value).orElse(new HashMap());
				   }
		    
				   return value;

			       })
			   .toList()
			   );
	
	//return Optional.of(parsedJSON);
    }
    
    /**
     * Algorithm taken partially from: https://vajithc.medium.com/parsing-json-without-libraries-build-your-own-json-reader-in-java-1db8e6165039
     */
    public int lastIndexOfIgnoreQuotes(int ch, String s) {

	var skip = false;
	var index = -1;

	for (var i = s.length() - 1; i >= 0; i--) {

	    var curCh = s.charAt(i);

	    if (curCh == '"')
		skip = !skip;

	    if (skip)
		continue;
	    
	    if (curCh == ch){

		index = i;

		break;
	    }
	}

	return index;
    }

    /**
     * Algorithm taken partially from: https://vajithc.medium.com/parsing-json-without-libraries-build-your-own-json-reader-in-java-1db8e6165039
     */
    public int indexOfIgnoreQuotes(int ch, String s) {
	
	var skip = false;
	var index = -1;

	for (var i = 0; i < s.length(); i++) {

	    var curCh = s.charAt(i);

	    if (curCh == '"')
		skip = !skip;
	    
	    if (skip)
		continue;

	    if (curCh == BEGIN_OBJECT) {

		i = lastIndexOfIgnoreQuotes(END_OBJECT, s);
	    }

	    if (curCh == BEGIN_ARRAY) {

		i = /*Math.max(*/lastIndexOfIgnoreQuotes(END_ARRAY, s)/*, i)*/;
	    }
	    
	    if (curCh == ch){

		index = i;

		break;
	    }
	}

	return index;
    }
}
