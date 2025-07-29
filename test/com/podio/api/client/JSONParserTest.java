package com.podio.api.client;

import com.podio.api.client.JSONParser;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
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

	if (resultMap.get("\"Image\"") instanceof HashMap imageJSON) {

	    if (imageJSON.get("\"Thumbnail\"") instanceof HashMap thumbnailJSON) {

		if(thumbnailJSON.get("\"Url\"") instanceof String urlValue) {

		    assertEquals("\"http://www.example.com/image/481989943\"", urlValue);
		    assertEquals(125.0, thumbnailJSON.get("\"Height\""));
		    assertEquals(100.0, thumbnailJSON.get("\"Width\""));
		}
	    }

	    assertTrue(imageJSON.get("\"IDs\"") instanceof List);
	    
	    if (imageJSON.get("\"IDs\"") instanceof List iDsList) {

		assertEquals(4, iDsList.size());
		assertFalse(iDsList.isEmpty());
		assertEquals(116, iDsList.get(0));
		assertEquals(38793, iDsList.get(3));
	    }
	}
    }

    @Test
    void rFCSpecificationArrayParseTest() {

	final var result = jSONParser
	    .parseJSONArray("[{\"precision\": \"zip\",\"Latitude\": 37.7668, \"Longitude\": -122.3959, \"Address\": \"\", \"City\": \"SAN FRANCISCO\", \"State\": \"CA\", \"Zip\": \"94107\", \"Country\": \"US\"}, {\"precision\": \"zip\", \"Latitude\": 37.371991, \"Longitude\": -122.026020, \"Address\": \"\", \"City\": \"SUNNYVALE\", \"State\": \"CA\", \"Zip\": \"94085\", \"Country\": \"US\"}]")
	    .orElse(List.of());

	assertFalse(result.isEmpty());

	assertEquals(2, result.size());

	assertTrue(result.get(0) instanceof HashMap);

	assertEquals(37.7668, ((HashMap) result.get(0)).get("\"Latitude\""));
	assertEquals("\"SAN FRANCISCO\"", ((HashMap) result.get(0)).get("\"City\""));

	assertTrue(result.get(1) instanceof HashMap);

	assertEquals(37.371991, ((HashMap) result.get(1)).get("\"Latitude\""));
    }
}
