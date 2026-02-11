/* 
 * This class serves as a wrapper for the Podio API HTTPS client requests.
 */
PODIO_API_BASE_URI = "https://api.podio.com/";
OAUTH_URI = PODIO_API_BASE_URI + "oauth/token/v2";
APP_ENDPOINT = PODIO_API_BASE_URI + "app/";
ITEM_ENDPOINT = PODIO_API_BASE_URI + "item/";
ORG_ENDPOINT = PODIO_API_BASE_URI + "org/";
SPACE_ENDPOINT = PODIO_API_BASE_URI + "space/";
USER_ENDPOINT = PODIO_API_BASE_URI + "user/";
REFERENCE_ENDPOINT = PODIO_API_BASE_URI + "reference/";
JSONParser jSONParser;
HttpClient httpClient;
Timer tokenRefreshTimer;
callCount = 0;
Map<String, Object> authenticationResponseBody;
TimerTask tokenRefreshTask;
httpClient = HttpClient.newHttpClient();
tokenRefreshTimer = new Timer();

function login(credentials) {
    try {
	loginRequestBody = new StringBuilder()
	    .append("{")
	    .append("\"grant_type\"")
	    .append(":")
	    .append("\"password\"")
	    .append(",")
	    .append("\"username\"")
	    .append(":")
	    .append("\"")
	    .append(creds.get("email"))
	    .append("\"")
	    .append(",")
	    .append("\"password\"")
	    .append(":")
	    .append("\"")
	    .append(creds.get("password"))
	    .append("\"")
	    .append(",")
	    .append("\"client_id\"")
	    .append(":")
	    .append("\"")
	    .append(creds.get("clientID"))
	    .append("\"")
	    .append(",")
	    .append("\"redirect_uri\"")
	    .append(":")
	    .append("\"\"")
	    .append(",")
	    .append("\"client_secret\"")
	    .append(":")
	    .append("\"")
	    .append(creds.get("clientSecret"))
	    .append("\"")
	    .append("}");

	loginPOSTRequest = HttpRequest
	    .newBuilder()
	    .uri(new URI(OAUTH_URI))
	    .header("Content-Type", "application/json")
	    .POST(HttpRequest
		.BodyPublishers
		.ofString(loginRequestBody.toString())
	    )
	    .build();

	logger.info(loginPOSTRequest.toString());
	logger.info(loginRequestBody.toString());

	loginPOSTResponse = httpClient.send(loginPOSTRequest,
	    HttpResponse
	    .BodyHandlers
	    .ofString()
	);

	if (loginPOSTResponse.statusCode() >= 400) {
	    e = new Exception("Podio API authentication failed");

	    logger.throwing(loginPOSTResponse.toString(), "login", e);

	    throw e;
	}

	authenticationResponseBody = (Map<String, Object>) jSONParser
	    .parseJSON(loginPOSTResponse.body())
	    .getFirst();

	logger.info(authenticationResponseBody.toString());

	if (authenticationResponseBody.get("\"expires_in\"") instanceof Long expires_in) {
	    tokenRefreshTimer.schedule(tokenRefreshTask = new TimerTask() {
		public void run() {
		    try {
			refreshTokenRequest = new StringBuilder()
			    .append("{")
			    .append("\"grant_type\"")
			    .append(":")
			    .append("\"refresh_token\"")
			    .append(",")
			    .append("\"refresh_token\"")
			    .append(":")
			    .append("\"")
			    .append((String) authenticationResponseBody.get("\"refresh_token\""))
			    .append("\"")
			    .append(",")
			    .append("\"client_id\"")
			    .append(":")
			    .append("\"")
			    .append(creds.get("clientID"))
			    .append("\"")
			    .append(",")
			    .append("\"client_secret\"")
			    .append(":")
			    .append(creds.get("clientSecret"))
			    .append("}");

			final var loginPOSTRefreshTokenRequest = HttpRequest
			    .newBuilder()
			    .uri(new URI(OAUTH_URI))
			    .header("Content-Type", "application/json")
			    .POST(HttpRequest
				.BodyPublishers
				.ofString(refreshTokenRequest.toString()))
			    .build();

			httpClient.send(loginPOSTRefreshTokenRequest,
			    HttpResponse
			    .BodyHandlers
			    .ofString()
			);

			logger.info(loginRequestBody.toString());
		    } catch (URISyntaxException | InterruptedException | IOException e) {

			logger.throwing(getClass().getName(), "login", e);
		    }
		}
	    },
		expires_in * 1000L
	    );
	}

	return authenticationResponseBody;
    } catch (URISyntaxException | IOException | InterruptedException e) {
	logger.throwing(getClass().getName(), "login", e);

	return Map.of();
    }
}

function sendGET(endpoint) {
    try {
	logger.log(Level.INFO, "{0} GET", endpoint);

	httpResponse = httpClient
	    .send(
		HttpRequest
		.newBuilder()
		.uri(new URI(endpoint))
		.header("Authorization", "OAuth2 " + trimQuotes((String) authenticationResponseBody.get("\"access_token\"")))
		.GET()
		.build(),
		HttpResponse
		.BodyHandlers
		.ofString()
	    );

	logger.log(Level.FINEST, "Current call count: {0}", ++callCount);

	if (httpResponse.statusCode() >= 400) {

	    logger.log(Level.SEVERE, "Response code above 400 {0}", httpResponse);
	    logger.severe(httpResponse.body());

	    if (httpResponse.statusCode() == 420)
		logger.severe("Rate limit reached");

	    return List.of();
	}

	logger.info(httpResponse.body());

	TimeUnit.MILLISECONDS.sleep(500);

	final var res = jSONParser.parseJSON(httpResponse.body());

	if (!res.isEmpty())
	    return res;

	return jSONParser.parseJSONArray(httpResponse.body());
    } catch (IOException | InterruptedException | URISyntaxException e) {

	logger.throwing(getClass().getName(), "sendGET", e);

	return List.of();
    }
}

function sendPOST(endpoint, body) {
    try {
	logger.log(Level.INFO, "{0} POST", endpoint);

	final var httpResponse = httpClient.send(HttpRequest
	    .newBuilder()
	    .uri(URI.create(endpoint))
	    .header("Authorization", "OAuth2 " + trimQuotes((String) authenticationResponseBody.get("\"access_token\"")))
	    .header("Content-Type", "application/json")
	    .POST(HttpRequest
		.BodyPublishers
		.ofString(body)
	    )
	    .build(),
	    HttpResponse
	    .BodyHandlers
	    .ofString()
	);

	logger.log(Level.FINEST, "Current call count: {0}", ++callCount);
	logger.finest(httpResponse.body());

	if (httpResponse.statusCode() >= 400) {
	    logger.log(Level.FINEST, "Response code above 400 {0}", httpResponse);

	    if (httpResponse.statusCode() == 420)
		logger.severe("Rate limit reached");

	    return List.of();
	}

	TimeUnit.MILLISECONDS.sleep(500);

	final var res = jSONParser.parseJSON(httpResponse.body());

	if (!res.isEmpty())
	    return res;

	return jSONParser.parseJSONArray(httpResponse.body());
    } catch (IOException | InterruptedException e) {

	logger.throwing(getClass().getName(), "sendPOST", e);

	return List.of();
    }
}

@Override
public void close() {
    httpClient.close();

    if(tokenRefreshTask == null)
	return;

    tokenRefreshTask.cancel();
    tokenRefreshTimer.cancel();
}

function trimQuotes(s) {
    return s.substring(1, s.length() - 1);
}
