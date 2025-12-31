package podio;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.*;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 * Parses JSON objects as specified by <a href="https://www.rfc-editor.org/rfc/pdfrfc/rfc8259.txt.pdf">The JavaScript Object Notation (JSON) Data Interchange Format</a>.
 * Implementation is done by linear search and "modally" skipping "offending" passages. This means linear worst case complexity, formally written as O(n) todo (recursion, quadratic "to an extent").
 Additional resources and credit:
 <a href="https://vajithc.medium.com/parsing-json-without-libraries-build-your-own-json-reader-in-java-1db8e6165039">source</a>.
*/
public final class JSONParser {
    private final Logger logger = Logger.getLogger(JSONParser.class.getName());
    private final Character BEGIN_ARRAY = '[';
    private final Character BEGIN_OBJECT = '{';
    private final Character END_ARRAY = ']';
    private final Character END_OBJECT = '}';
    private final Character NAME_SEPARATOR = ':';
    private final Character VALUE_SEPARATOR = ',';

    public List<Object> parseJSON(String jSONString) {
	try {
	    final HashMap<String, Object> parsedJSON = new HashMap<>();

	    if (jSONString.charAt(0) != BEGIN_OBJECT || jSONString.charAt(jSONString.length() - 1) != END_OBJECT)
		return List.of();

	    if (jSONString.equals("{}"))
		return List.of(parsedJSON);

	    jSONString = jSONString.substring(1, jSONString.length() - 1);

	    if (jSONString.charAt(jSONString.length() - 1) != VALUE_SEPARATOR.charValue())
		jSONString = jSONString.concat(VALUE_SEPARATOR.toString());

	    var inQuotes = false;
	    var lastValueSeparator = -1;

	    for (var i = 0; i < jSONString.length(); i++) {

		if(jSONString.charAt(i) == '"') {
		    if (i > 0 && jSONString.charAt(i - 1) == '\\')
			continue;

		    inQuotes = !inQuotes;
		}

		if (inQuotes)
		    continue;

		if (jSONString.charAt(i) == NAME_SEPARATOR) {

		    try {
			final var key = jSONString.substring(lastValueSeparator + 1, i);
			final var value = jSONString.substring(i + 1, lastValueSeparator = indexOfIgnoreQuotes(VALUE_SEPARATOR, jSONString.substring(i + 1, jSONString.length())) + i + 1);

			i = lastValueSeparator;

			parsedJSON.put(key.trim(), value.trim());
		    }
		    catch(final Exception e) {

			logger.throwing(getClass().getName(), "parseJSON", e);
			logger.throwing(getClass().getName(), "parseJSON", new Exception(jSONString.substring(i, Math.min(jSONString.length(), 10 + i))));
		    }
		}
	    }

	    parsedJSON.forEach((key, value) -> {

		    if (value instanceof String stringValue) {

			final var firstCh = stringValue.charAt(0);

			if(firstCh == BEGIN_ARRAY)
			    parsedJSON.replace(key, parseJSONArray(stringValue));

			if (firstCh == BEGIN_OBJECT)
			    parsedJSON.replace(key, parseJSON(stringValue).getFirst());

			try {

			    parsedJSON.replace(key, Long.parseLong(stringValue));

			    return;
			}
			catch (final Exception ignored) {}

			try {

			    parsedJSON.replace(key, Double.parseDouble(stringValue));
			}
			catch (final Exception ignored) {}
		    }
		});

	    return List.of(parsedJSON);
	}
	catch (final Exception e) {

	    logger.throwing(getClass().getName(), "parseJSON", e);

	    return List.of();
	}
    }

    /**
     * Algorithm taken partially from: https://vajithc.medium.com/parsing-json-without-libraries-build-your-own-json-reader-in-java-1db8e6165039
     */
    public List<Object> parseJSONArray(String jSONString) {
	final var parsedJSON = new LinkedList<Object>();

	if (jSONString.charAt(0) != BEGIN_ARRAY || jSONString.charAt(jSONString.length() - 1) != END_ARRAY)
	    return List.of();

	if (jSONString.equals("[]"))
	    return List.of();

	jSONString = jSONString.substring(1, jSONString.length() - 1);

	if (jSONString.charAt(jSONString.length() - 1) != VALUE_SEPARATOR)
	    jSONString = jSONString.concat(VALUE_SEPARATOR.toString());

	var lastValueSep = -1;
	var skip = false;
	var objectConsumerCount = 0;
	var arrayConsumerCount = 0;

	for(var i = 0; i < jSONString.length(); i++) {

	    final var curCh = jSONString.charAt(i);

	    if (curCh == '"') {
		if (i > 0 && jSONString.charAt(i - 1) == '\\')
		    continue;

		skip = !skip;
	    }

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
	}

	return parsedJSON
	    .stream()
	    .map(value -> {

		    try {

			return Long.parseLong((String) value);
		    }
		    catch (Exception ignored) {}

		    try {

			return Double.parseDouble((String) value);
		    }
		    catch (Exception ignored) {}

		    if (((String) value).charAt(0) == BEGIN_OBJECT) {

			return parseJSON((String) value).getFirst();
		    }

		    return value;

		})
	    .toList();
    }

    /**
     * Algorithm taken partially from: <a href="https://vajithc.medium.com/parsing-json-without-libraries-build-your-own-json-reader-in-java-1db8e6165039">source</a>
     */
    public int indexOfIgnoreQuotes(int ch, String s) {

	var skip = false;
	var index = -1;
	var oO = 0;
	var oA = 0;

	for (var i = 0; i < s.length(); i++) {

	    var curCh = s.charAt(i);

	    if (curCh == '"') {
		if(i > 0 && s.charAt(i - 1) == '\\')
		    continue;

		skip = !skip;
	    }

	    if (skip)
		continue;

	    if (curCh == BEGIN_OBJECT)
		oO++;

	    if (curCh == END_OBJECT)
		oO--;

	    if (curCh == BEGIN_ARRAY)
		oA++;

	    if (curCh == END_ARRAY)
		oA--;

	    if (oO != 0 || oA != 0)
		continue;

	    if (curCh == ch){

		index = i;

		break;
	    }
	}

	return index;
    }
}
