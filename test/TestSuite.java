import java.util.HashMap;
import java.util.logging.*;
import java.util.*;

import podio.*;

public class TestSuite {
    private static PodioClient podioClient;
    private static JSONParser jSONParser;
    private static final Logger logger = Logger.getLogger(TestSuite.class.getName());
    
    public static void main(final String[] args){
	setLoggerToLevel(logger, Level.ALL);
	logger.info("running tests");

	final var log = Logger.getLogger(PodioClient.class.getName());

	log.setLevel(Level.ALL);
	log
	    .getParent()
	    .getHandlers()[0]
	    .setLevel(Level.ALL);

	jSONParser = new JSONParser();
	podioClient = new PodioClient();

	final var tests = TestSuite.class.getDeclaredMethods();

	logger.info("finding tests");

	for (var i = 0; i < tests.length; ++i) {
	    final var test = tests[i];

	    if (test.getName().contains("Test")){
		logger.info("running test " + test.getName());
		
		try {
		    test.invoke(null);
		}
		catch (final AssertionError exception) {
		    logger.severe("Test " + test.getName() + " failed with error " + exception.getMessage());
		    logger.throwing(TestSuite.class.getName(), "main", exception);
		    System.exit(1);

		    return;
		}
		catch (final Exception exception) {
		    logger.throwing(TestSuite.class.getName(), "main", exception);
		    System.exit(1);

		    return;
		}
	    }
	}

	podioClient.close();
	System.exit(0);
    }

    static void incorrectLoginTest() {
	final var creds = new HashMap<String, String>();

	creds.put("email", "testMail");
	creds.put("password", "testPass");

	Exception expectedException = null;

	try {
	    podioClient.login(creds);
	}
	catch (final Exception exception) {
	    expectedException = exception;
	}

	assert expectedException instanceof Exception;
    }

    static void emptyJSONTest() {
	final var result = jSONParser.parseJSON("{}");

	assert !result.isEmpty();
    }

    static void invalidJSONTest() {
	final var result1 = jSONParser.parseJSON("{");
	final var result2 = jSONParser.parseJSON("}");

	assert result1.isEmpty();
	assert result2.isEmpty();
    }

    static void singleValueJSONObjectTest() {
	final var result = jSONParser.parseJSON("{\"test\": \"value\"}");

	assert (!result.isEmpty());

	final var resultMap = (Map<String, Object>) result.getFirst();

	assert !resultMap.isEmpty();
	assert resultMap.containsKey("\"test\"");
	assert "\"value\"".equals(resultMap.get("\"test\""));
    }

    static void valueSeparatorJSONObjectTest() {
	final var result = jSONParser.parseJSON(
		"{\"test1\": \"value1\", \"test2\": \"value2\",}"
		);

	assert (!result.isEmpty());

	final var resultMap = (Map<String, Object>) result.getFirst();

	assert !resultMap.isEmpty();
	assert (resultMap.containsKey("\"test1\""));
	assert (resultMap.containsKey("\"test2\""));
	assert "\"value1\"".equals(resultMap.get("\"test1\""));
	assert "\"value2\"".equals(resultMap.get("\"test2\""));
    }

    static void emptyJSONArrayTest() {
	final var resultList = jSONParser.parseJSONArray("[]");

	//assertFalse(result.isEmpty());

	//final var resultList = (Map<String, Object>) result.getFirst();

	assert (resultList.isEmpty());
    }

    static void invalidjSONArrayTest() {
	final var result = jSONParser.parseJSONArray("[");

	assert (result.isEmpty());
    }

    static void nonEmptyJSONArrayTest() {
	final var resultList = jSONParser.parseJSONArray("[\"test\"]");

	assert !resultList.isEmpty();

	//final var resultList = (Map<String, Object>) result.getFirst();

	assert !resultList.isEmpty();
	assert "\"test\"".equals(resultList.get(0));
    }


    static void rFCSpecificationParseTest() {
	final var result = jSONParser.parseJSON("{\"Image\": {\"Width\": 800, \"Height\": 600, \"Title\": \"View from 15th Floor\", \"Thumbnail\": {\"Url\": \"http://www.example.com/image/481989943\", \"Height\": 125, \"Width\": 100}, \"Animated\" : false, \"IDs\": [116, 943, 234, 38793]}}");

	assert !result.isEmpty();

	final var resultMap = (Map<String, Object>) result.getFirst();

	assert !resultMap.isEmpty();
	assert resultMap.get("\"Image\"") instanceof HashMap<?, ?>;

	if (resultMap.get("\"Image\"") instanceof HashMap<?, ?> imageJSON) {
	    assert (imageJSON.get("\"Thumbnail\"") instanceof HashMap<?, ?> thumbnailJSON);

	    if (imageJSON.get("\"Thumbnail\"") instanceof HashMap<?, ?> thumbnailJSON) {
		assert thumbnailJSON.get("\"Url\"") instanceof String urlValue;

		if(thumbnailJSON.get("\"Url\"") instanceof String urlValue) {
		    assert "\"http://www.example.com/image/481989943\"".equals(urlValue);
		    assert 125L == (Long) thumbnailJSON.get("\"Height\"");
		    assert 100L == (Long) thumbnailJSON.get("\"Width\"");
		}
	    }

	    assert (imageJSON.get("\"IDs\"") instanceof List<?>);

	    if (imageJSON.get("\"IDs\"") instanceof List<?> iDsList) {
		assert 4 == iDsList.size();
		assert !iDsList.isEmpty();
		assert 116L == (Long)(iDsList.get(0));
		assert 38793L == (Long)(iDsList.get(3));
	    }
	}
    }

    /**
     * Inspired by Podio API
     */
    static void rFCSpecificationParseTestImproved() {
	final var result = jSONParser.parseJSON("{\"empty_array\":[], \"Image\": {\"Width\": 800, \"Height\": 600, \"null_value\":null, \"Title\": \"View from 15th Floor\", \"Thumbnail\": {\"Url\": \"http://www.example.com/image/481989943\", \"Height\": 125, \"null_value_in_object\":null, \"Width\": 100}, \"another_empty_array\":[], \"Animated\" : false, \"IDs\": [{\"test\":101, \"ugly\":\"{[]}\\\"ug\\\"l\\\"[]y_[]{}test\", \"another\":[1, 42, {\"array\":[]}]}, 116, {\"empty_array\":[], \"null_value\":null}, 943, 234, null, [null, \"person@example.com\"], 38793]}}");

	assert !result.isEmpty();

	final var resultMap = (Map<String, Object>) result.getFirst();

	assert !resultMap.isEmpty();

	logger.info(resultMap.toString());

	assert (resultMap.get("\"Image\"") instanceof HashMap<?, ?> imageJSON);

	if (resultMap.get("\"Image\"") instanceof HashMap<?, ?> imageJSON) {
	    assert (imageJSON.get("\"Thumbnail\"") instanceof HashMap<?, ?> thumbnailJSON);

	    if (imageJSON.get("\"Thumbnail\"") instanceof HashMap<?, ?> thumbnailJSON) {
		assert (thumbnailJSON.get("\"Url\"") instanceof String urlValue);

		if(thumbnailJSON.get("\"Url\"") instanceof String urlValue) {
		    assert "\"http://www.example.com/image/481989943\"".equals(urlValue);
		    assert 125L == (Long)thumbnailJSON.get("\"Height\"");
		    assert 100L == (Long)thumbnailJSON.get("\"Width\"");
		}
	    }

	    assert (imageJSON.get("\"IDs\"") instanceof List<?>);

	    if (imageJSON.get("\"IDs\"") instanceof List<?> iDsList) {
		assert 8 == iDsList.size();
		assert !iDsList.isEmpty();
		assert 116L == (Long)(iDsList.get(1));
		assert 38793L == (Long)(iDsList.get(7));
	    }
	}
    }

    static void rFCSpecificationArrayParseTest() {
	final var result = jSONParser
	    .parseJSONArray("[{\"precision\": \"zip\",\"Latitude\": 37.7668, \"Longitude\": -122.3959, \"Address\": \"\", \"City\": \"SAN FRANCISCO\", \"State\": \"CA\", \"Zip\": \"94107\", \"Country\": \"US\"}, {\"precision\": \"zip\", \"Latitude\": 37.371991, \"Longitude\": -122.026020, \"Address\": \"\", \"City\": \"SUNNYVALE\", \"State\": \"CA\", \"Zip\": \"94085\", \"Country\": \"US\"}]");

	logger.info(result.toString());

	assert !result.isEmpty();
	assert 2 == result.size();
	assert result.get(0) instanceof HashMap<?, ?>;

	final var resultMap1 = (HashMap<String, Object>) result.get(0);

	assert 37.7668 == (Double) resultMap1.get("\"Latitude\"");
	assert -122.3959 == (Double) resultMap1.get("\"Longitude\"");
	assert "\"SAN FRANCISCO\"".equals(resultMap1.get("\"City\""));
	assert result.get(1) instanceof HashMap<?, ?>;

	final var resultMap = (HashMap<String, Object>) result.get(1);

	assert 37.371991 == (Double) resultMap.get("\"Latitude\"");
	assert -122.026020 == (Double) resultMap.get("\"Longitude\"");
    }

    private static void setLoggerToLevel(final Logger logger, final Level level){
	logger.setLevel(level);

	final var handlers = logger.getParent().getHandlers();

	for (var i = 0; i < handlers.length; ++i){
	    handlers[i].setLevel(level);
	}
    }
}
