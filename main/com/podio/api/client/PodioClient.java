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
    private static final Logger logger = Logger.getLogger(PodioClient.class.getName());
    public static final String PODIO_API_BASE_URI = "https://api.podio.com/";
    public static final String OAUTH_URI = PODIO_API_BASE_URI + "oauth/token/v2";
    public static final String APP_ENDPOINT = PODIO_API_BASE_URI + "app/";
    public static final String ITEM_ENDPOINT = PODIO_API_BASE_URI + "item/";
    public static final String ORG_ENDPOINT = PODIO_API_BASE_URI + "org/";
    public static final String SPACE_ENDPOINT = PODIO_API_BASE_URI + "space/";
    public static final String USER_ENDPOINT = PODIO_API_BASE_URI + "user/";
    public static final String REFERENCE_ENDPOINT = PODIO_API_BASE_URI + "reference/";
    private static PodioClient instance;
    private final JSONParser jSONParser;
    private final HttpClient httpClient;
    private final Timer tokenRefreshTimer;
    private long callCount = 0;
    private Map<String, Object> authenticationResponseBody;
    private TimerTask tokenRefreshTask;

    private PodioClient() {

	jSONParser = new JSONParser();
     	httpClient = HttpClient.newHttpClient();
     	tokenRefreshTimer = new Timer();
     	tokenRefreshTask = new TimerTask() {

     		@Override
     		public void run() {
     		}
	    };
    }

    public static PodioClient returnInstance() {

     	return Objects.requireNonNullElse(instance, instance = new PodioClient());
    }

    public void login(final String clientID, final String clientSecret, final String email, final String password) throws Exception {

	try {

	    final var loginRequestBody = new StringBuilder();

	    loginRequestBody
		.append("{")
		.append("grant_type")
		.append(":")
		.append("password")
		.append(",")
		.append("username")
		.append(":")
		.append(email)
		.append("password")
		.append(":")
		.append(password)
		.append("client_id")
		.append(":")
		.append(clientID)
		.append(",")
		.append("redirect_uri")
		.append(":")
		.append("")
		.append(",")
		.append("client_secret")
		.append(":")
		.append(clientSecret)
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

	    authenticationResponseBody = jSONParser
		.parseJSON(loginPOSTResponse.body())
		.orElse(new HashMap<>());

            logger.info(authenticationResponseBody.toString());

            tokenRefreshTimer.schedule(tokenRefreshTask = new TimerTask() {

		    public void run() {

			try {
				
			    final var refreshTokenRequest = new StringBuilder()
				.append("{")
				.append("grant_type")
				.append(":")
				.append("refresh_token")
				.append(",")
				.append("refresh_token")
				.append(":")
				.append(authenticationResponseBody.get("\"refresh_token\""))
				.append(",")
				.append("client_id")
				.append(":")
				.append(clientID)
				.append(",")
				.append("client_secret")
				.append(":")
				.append(clientSecret)
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
			} catch (URISyntaxException | InterruptedException | IOException e) {

			    logger.throwing(getClass().getName(), "login", e);
			}
		    }
		},
		(int) (authenticationResponseBody.get("\"expires_in\"") * 1000)
		);
        } catch (URISyntaxException | IOException | InterruptedException e) {

	    logger.throwing(getClass().getName(), "login", e);
        }
    }

    public Optional<Map<String, Object>> sendGET(final String endpoint) {

        try {

            logger.log(Level.INFO, "{0} GET", endpoint);

            final var httpResponse = httpClient
		.send(
		      HttpRequest
		      .newBuilder()
		      .uri(new URI(endpoint))
		      .header("Authorization", "OAuth2 " + authenticationResponseBody.get("\"access_token\""))
		      .GET()
		      .build(),
		      HttpResponse
		      .BodyHandlers
		      .ofString()
		      );

            logger.log(Level.FINEST, "Current call count: {0}", ++callCount);

            if (httpResponse.statusCode() >= 400) {

                logger.log(Level.SEVERE, "Response code above 400 {0}", httpResponse);

                if (httpResponse.statusCode() == 420)
                    logger.severe("Rate limit reached");

                return Optional.empty();
            }

            TimeUnit.MILLISECONDS.sleep(500);

            return jSONParser.parseJSON(httpResponse.body());
        } catch (IOException | InterruptedException | URISyntaxException e) {

	    logger.throwing(getClass().getName(), "GET", e);

            return Optional.empty();
        }
    }

    public Optional<Map<String, Object>> sendPOST(final String endpoint, final String body) {

	try {

	    logger.log(Level.INFO, "{0} POST", endpoint);

            final var httpResponse = httpClient.send(HttpRequest
						     .newBuilder()
						     .uri(URI.create(endpoint))
						     .header("Authorization", "OAuth2 " + authenticationResponseBody.get("\"access_token\""))
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

            if (httpResponse.statusCode() >= 400) {
                logger.log(Level.FINEST, "Response code above 400 {0}", httpResponse);

                if (httpResponse.statusCode() == 420)
                    logger.severe("Rate limit reached");

                return Optional.empty();
            }

            TimeUnit.MILLISECONDS.sleep(500);

            return jSONParser.parseJSON(httpResponse.body());
        } catch (IOException | InterruptedException e) {

            logger.throwing(getClass().getName(), "POST", e);

            return Optional.empty();
        }
    }
    
    @Override
    public void close() {

        httpClient.close();
        tokenRefreshTask.cancel();
        tokenRefreshTimer.cancel();
    }
}
