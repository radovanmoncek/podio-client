package com.podio.api.client;

import com.podio.api.client.JSONParser;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class JSONParserTest {

    private static JSONParser jSONParser;

    @BeforeAll
    static void setup() {

	jSONParser = new JSONParser();
    }

    @Test
    void emptyJSONTest() {

	Optional result = jSONParser.parseJSON("{}");

	assertFalse(result.isEmpty());
    }

    @Test
    void invalidJSONTest() {

	Optional result1 = jSONParser.parseJSON("{");
	Optional result2 = jSONParser.parseJSON("}");

	assertTrue(result1.isEmpty());
	assertTrue(result2.isEmpty());
    }

    @Test
    void singleValueJSONObjectTest() {
	
	final var result = jSONParser.parseJSON("{\"test\": \"value\"}");

	assertTrue(result.isPresent());

	final var resultMap = result.get();

	assertFalse(resultMap.isEmpty());
	assertTrue(resultMap.containsKey("\"test\""));
	assertEquals("\"value\"", resultMap.get("\"test\""));
    }

    @Test
    void valueSeparatorJSONObjectTest() {
	
	final var result = jSONParser.parseJSON(
					       "{\"test1\": \"value1\", \"test2\": \"value2\",}"
						);

	assertTrue(result.isPresent());

	final var resultMap = result.get();

	assertFalse(resultMap.isEmpty());
	assertTrue(resultMap.containsKey("\"test1\""));
	assertTrue(resultMap.containsKey("\"test2\""));
	assertEquals("\"value1\"", resultMap.get("\"test1\""));
	assertEquals("\"value2\"", resultMap.get("\"test2\""));
    }

    @Test
    void emptyJSONArrayTest() {

	final var result = jSONParser.parseJSONArray("[]");

	assertFalse(result.isEmpty());

	final var resultList = result.get();
	
	assertTrue(resultList.isEmpty());
    }

    @Test
    void invalidjSONArrayTest() {

	final var result = jSONParser.parseJSONArray("[");

	assertTrue(result.isEmpty());
    }

    @Test
    void nonEmptyJSONArrayTest() {

	final var result = jSONParser.parseJSONArray("[\"test\"]");

	assertFalse(result.isEmpty());

	final var resultList = result.get();
	
	assertFalse(resultList.isEmpty());

	assertEquals("\"test\"", resultList.get(0));
    }

    @Test
    void rFCSpecificationParseTest() {
	
	final var result = jSONParser.parseJSON("{\"Image\": {\"Width\": 800, \"Height\": 600, \"Title\": \"View from 15th Floor\", \"Thumbnail\": {\"Url\": \"http://www.example.com/image/481989943\", \"Height\": 125, \"Width\": 100}, \"Animated\" : false, \"IDs\": [116, 943, 234, 38793]}}");

	assertFalse(result.isEmpty());

	final var resultMap = result.get();

	assertFalse(resultMap.isEmpty());

	assertEquals("http://www.example.com/image/481989943", resultMap.get("Url"));
    }
}
