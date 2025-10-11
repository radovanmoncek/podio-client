import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class JSONParserTest {
    private static final Logger logger = Logger.getLogger(JSONParserTest.class.getName());
    private static JSONParser jSONParser;
    
    @BeforeAll
    static void setup() {

	jSONParser = new JSONParser();
    }

    @Test
    void emptyJSONTest() {

	final var result = jSONParser.parseJSON("{}");

	assertFalse(result.isEmpty());
    }

    @Test
    void invalidJSONTest() {

	final var result1 = jSONParser.parseJSON("{");
	final var result2 = jSONParser.parseJSON("}");

	assertTrue(result1.isEmpty());
	assertTrue(result2.isEmpty());
    }

    @Test
    void singleValueJSONObjectTest() {
	
	final var result = jSONParser.parseJSON("{\"test\": \"value\"}");

	assertTrue(!result.isEmpty());

	final var resultMap = (Map<String, Object>) result.getFirst();

	assertFalse(resultMap.isEmpty());
	assertTrue(resultMap.containsKey("\"test\""));
	assertEquals("\"value\"", resultMap.get("\"test\""));
    }

    @Test
    void valueSeparatorJSONObjectTest() {
	
	final var result = jSONParser.parseJSON(
						"{\"test1\": \"value1\", \"test2\": \"value2\",}"
						);

	assertTrue(!result.isEmpty());

	final var resultMap = (Map<String, Object>) result.getFirst();

	assertFalse(resultMap.isEmpty());
	assertTrue(resultMap.containsKey("\"test1\""));
	assertTrue(resultMap.containsKey("\"test2\""));
	assertEquals("\"value1\"", resultMap.get("\"test1\""));
	assertEquals("\"value2\"", resultMap.get("\"test2\""));
    }

    @Test
    void emptyJSONArrayTest() {

	final var resultList = jSONParser.parseJSONArray("[]");

	//assertFalse(result.isEmpty());

	//final var resultList = (Map<String, Object>) result.getFirst();
	
	assertTrue(resultList.isEmpty());
    }

    @Test
    void invalidjSONArrayTest() {

	final var result = jSONParser.parseJSONArray("[");

	assertTrue(result.isEmpty());
    }

    @Test
    void nonEmptyJSONArrayTest() {

	final var resultList = jSONParser.parseJSONArray("[\"test\"]");

	assertFalse(resultList.isEmpty());

	//final var resultList = (Map<String, Object>) result.getFirst();
	
	assertFalse(resultList.isEmpty());

	assertEquals("\"test\"", resultList.get(0));
    }

    @Test
    void rFCSpecificationParseTest() {
	
	final var result = jSONParser.parseJSON("{\"Image\": {\"Width\": 800, \"Height\": 600, \"Title\": \"View from 15th Floor\", \"Thumbnail\": {\"Url\": \"http://www.example.com/image/481989943\", \"Height\": 125, \"Width\": 100}, \"Animated\" : false, \"IDs\": [116, 943, 234, 38793]}}");

	assertFalse(result.isEmpty());

	final var resultMap = (Map<String, Object>) result.getFirst();

	assertFalse(resultMap.isEmpty());

	assertTrue(resultMap.get("\"Image\"") instanceof HashMap imageJSON);
	if (resultMap.get("\"Image\"") instanceof HashMap imageJSON) {

	    assertTrue(imageJSON.get("\"Thumbnail\"") instanceof HashMap thumbnailJSON);
	    if (imageJSON.get("\"Thumbnail\"") instanceof HashMap thumbnailJSON) {

		assertTrue(thumbnailJSON.get("\"Url\"") instanceof String urlValue);
		if(thumbnailJSON.get("\"Url\"") instanceof String urlValue) {

		    assertEquals("\"http://www.example.com/image/481989943\"", urlValue);
		    assertEquals(125L, thumbnailJSON.get("\"Height\""));
		    assertEquals(100L, thumbnailJSON.get("\"Width\""));
		}
	    }

	    assertTrue(imageJSON.get("\"IDs\"") instanceof List);
	    
	    if (imageJSON.get("\"IDs\"") instanceof List iDsList) {

		assertEquals(4, iDsList.size());
		assertFalse(iDsList.isEmpty());
		assertEquals(116L, iDsList.get(0));
		assertEquals(38793L, iDsList.get(3));
	    }
	}
    }

    /**
     * Inspired by Podio API
     */
    @Test
    void rFCSpecificationParseTestImproved() {
	
	final var result = jSONParser.parseJSON("{\"empty_array\":[], \"Image\": {\"Width\": 800, \"Height\": 600, \"null_value\":null, \"Title\": \"View from 15th Floor\", \"Thumbnail\": {\"Url\": \"http://www.example.com/image/481989943\", \"Height\": 125, \"null_value_in_object\":null, \"Width\": 100}, \"another_empty_array\":[], \"Animated\" : false, \"IDs\": [{\"test\":101, \"ugly\":\"{[]}\\\"ug\\\"l\\\"[]y_[]{}test\", \"another\":[1, 42, {\"array\":[]}]}, 116, {\"empty_array\":[], \"null_value\":null}, 943, 234, null, [null, \"person@example.com\"], 38793]}}");

	assertFalse(result.isEmpty());

	final var resultMap = (Map<String, Object>) result.getFirst();

	assertFalse(resultMap.isEmpty());

	logger.info(resultMap.toString());
	
	assertTrue(resultMap.get("\"Image\"") instanceof HashMap imageJSON);
	if (resultMap.get("\"Image\"") instanceof HashMap imageJSON) {

	    assertTrue(imageJSON.get("\"Thumbnail\"") instanceof HashMap thumbnailJSON);
	    if (imageJSON.get("\"Thumbnail\"") instanceof HashMap thumbnailJSON) {

		assertTrue(thumbnailJSON.get("\"Url\"") instanceof String urlValue);
		if(thumbnailJSON.get("\"Url\"") instanceof String urlValue) {

		    assertEquals("\"http://www.example.com/image/481989943\"", urlValue);
		    assertEquals(125L, thumbnailJSON.get("\"Height\""));
		    assertEquals(100L, thumbnailJSON.get("\"Width\""));
		}
	    }

	    assertTrue(imageJSON.get("\"IDs\"") instanceof List);
	    
	    if (imageJSON.get("\"IDs\"") instanceof List iDsList) {

		assertEquals(8, iDsList.size());
		assertFalse(iDsList.isEmpty());
		assertEquals(116L, iDsList.get(1));
		assertEquals(38793L, iDsList.get(7));
	    }
	}
    }
    
    @Test
    void rFCSpecificationArrayParseTest() {

	final var result = jSONParser
	    .parseJSONArray("[{\"precision\": \"zip\",\"Latitude\": 37.7668, \"Longitude\": -122.3959, \"Address\": \"\", \"City\": \"SAN FRANCISCO\", \"State\": \"CA\", \"Zip\": \"94107\", \"Country\": \"US\"}, {\"precision\": \"zip\", \"Latitude\": 37.371991, \"Longitude\": -122.026020, \"Address\": \"\", \"City\": \"SUNNYVALE\", \"State\": \"CA\", \"Zip\": \"94085\", \"Country\": \"US\"}]");

	logger.info(result.toString());

	assertFalse(result.isEmpty());

	assertEquals(2, result.size());

	assertTrue(result.get(0) instanceof HashMap);

	assertEquals(37.7668, ((HashMap) result.get(0)).get("\"Latitude\""));
	assertEquals(-122.3959, ((HashMap) result.get(0)).get("\"Longitude\""));
	assertEquals("\"SAN FRANCISCO\"", ((HashMap) result.get(0)).get("\"City\""));

	assertTrue(result.get(1) instanceof HashMap);

	assertEquals(37.371991, ((HashMap) result.get(1)).get("\"Latitude\""));
	assertEquals(-122.026020, ((HashMap) result.get(1)).get("\"Longitude\""));
    }
}
