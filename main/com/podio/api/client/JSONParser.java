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

	jSONString = jSONString.substring(1, jSONString.lastIndexOf(END_OBJECT));

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
	
         for (var i = 0; i <= jSONString.length() - 1; i++) {

	     if(jSONString.charAt(i) == '"')
	 	inQuotes = !inQuotes;
	     
	     if (inQuotes)
	 	continue;

	     if (jSONString.charAt(i) == BEGIN_OBJECT.charValue()) {

		 parsedJSON.put(jSONString.substring(0, i).trim(), jSONString.substring(i, jSONString.lastIndexOf(END_OBJECT.charValue()) + 1).trim());

		//i += jSONString.lastIndexOf(END_OBJECT) + 1;
		i = 0;
		jSONString = jSONString.substring(jSONString.lastIndexOf(END_OBJECT.charValue()) + 1, jSONString.length());

		continue;
	    }
	    
	    if (jSONString.charAt(i) == NAME_SEPARATOR.charValue()) {

		parsedJSON.put(jSONString.substring(0, i).trim(), jSONString.substring(i + 1, jSONString.indexOf(VALUE_SEPARATOR)).trim());

		jSONString = jSONString.substring(Math.min(jSONString.indexOf(VALUE_SEPARATOR) + 1, jSONString.length() - 1), jSONString.length());

		/*i -= jSONString.indexOf(VALUE_SEPARATOR) + 1;*/
		i = 0;
	    }
	}

	 logger.info(parsedJSON.toString());
	parsedJSON.forEach((key, value) -> {

		final var stringValue = (String) value;
		if(stringValue.startsWith(BEGIN_ARRAY.toString()))
		     parsedJSON.replace(key, parseJSONArray(stringValue));
		if (stringValue.startsWith(BEGIN_OBJECT.toString()))
		     parsedJSON.replace(key, parseJSON(stringValue));
	});
	
	return Optional.of(parsedJSON);
    }

    Optional<List<Object>> parseJSONArray(String jSONString) {

	final LinkedList<Object> parsedJSON = new LinkedList<>();

	if (jSONString.charAt(0) != BEGIN_ARRAY || jSONString.charAt(jSONString.length() - 1) != END_ARRAY)
	    return Optional.empty();

	if (jSONString.equals("[]"))
	    return Optional.of(parsedJSON);
	
	jSONString = jSONString.replace(" ", "");
	jSONString = jSONString.substring(1, jSONString.length() - 1);

	var tempValue = "";
	
	for(var i = 0; i <= jSONString.length() - 1; i++) {
	    
	    switch (jSONString.charAt(i)) {

		/*case VALUE_SEPARATOR -> {

		parsedJSON.add(tempValue);
		tempValue = "";
		}*/
	    
	    default -> {
		    
		tempValue = tempValue.concat(jSONString.substring(i, i + 1));

		if (i == jSONString.length() - 1)
		    parsedJSON.add(tempValue);
	    }
	    }
	}
	
	return Optional.of(parsedJSON);
    }
}
