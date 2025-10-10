package com.podio.api.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class serves as a wrapper for the Podio API HTTPS client requests.
 */
public final class PodioClient implements AutoCloseable {
    public final String PODIO_API_BASE_URI = "https://api.podio.com/";
    public final String OAUTH_URI = PODIO_API_BASE_URI + "oauth/token/v2";
    public final String APP_ENDPOINT = PODIO_API_BASE_URI + "app/";
    public final String ITEM_ENDPOINT = PODIO_API_BASE_URI + "item/";
    public final String ORG_ENDPOINT = PODIO_API_BASE_URI + "org/";
    public final String SPACE_ENDPOINT = PODIO_API_BASE_URI + "space/";
    public final String USER_ENDPOINT = PODIO_API_BASE_URI + "user/";
    public final String REFERENCE_ENDPOINT = PODIO_API_BASE_URI + "reference/";
    private final Logger logger;
    private final JSONParser jSONParser;
    private final HttpClient httpClient;
    private final Timer tokenRefreshTimer;
    private long callCount = 0;
    private Map<String, Object> authenticationResponseBody;
    private TimerTask tokenRefreshTask;

    public PodioClient() {

	logger = Logger.getLogger(getClass().getName());
	jSONParser = new JSONParser();
	httpClient = HttpClient.newHttpClient();
	tokenRefreshTimer = new Timer();
    }

    public Map<String, Object> login(final Map<String, String> creds) throws Exception {

	try {

	    final var loginRequestBody = new StringBuilder()
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

	    final var loginPOSTRequest = HttpRequest
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

	    final var loginPOSTResponse = httpClient.send(loginPOSTRequest,
							  HttpResponse
							  .BodyHandlers
							  .ofString()
							  );

	    if (loginPOSTResponse.statusCode() >= 400) {

		final var e = new Exception("Podio API authentication failed");

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

				final var refreshTokenRequest = new StringBuilder()
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

    public List<Object> sendGET(final String endpoint) {

	try {

	    logger.log(Level.INFO, "{0} GET", endpoint);

	    final var httpResponse = httpClient
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

    public List<Object> sendPOST(final String endpoint, final String body) {

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

    private String trimQuotes(final String s) {

	return s.substring(1, s.length() - 1);
    }
}
