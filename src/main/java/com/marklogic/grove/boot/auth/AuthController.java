package com.marklogic.grove.boot.auth;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.FailedRequestException;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.client.ext.modulesloader.ssl.SimpleX509TrustManager;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.grove.boot.AbstractController;
import com.marklogic.grove.boot.MarkLogicConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This is intended for development only, as it simply records a user as being
 * "logged in" by virtue of being able to instantiate a DatabaseClient, thereby
 * assuming that the login credentials correspond to a MarkLogic user.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController extends AbstractController {

	protected final static String SESSION_USERNAME_KEY = "grove-spring-boot-username";
	protected final static String SESSION_DATABASE_CLIENT_KEY = "grove-spring-boot-client";

	@Autowired
	private MarkLogicConfig markLogicConfig;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public SessionStatus login(@RequestBody LoginRequest request, HttpSession session, HttpServletResponse response) {
		logger.info("Logging in user: " + request.getUsername());
		// TODO Handle error appropriately

		DatabaseClient client;

		
		// if connecting to MarkLogic using SSL
		// DHS only uses SSL, mlUseSSL in application.properties has to be set to 'true'
		if (markLogicConfig.getSimpleSsl()) {
			SecurityContext dbSecurityContext = new DatabaseClientFactory.BasicAuthContext(request.getUsername(),
					request.getPassword());

			dbSecurityContext.withSSLContext(
					SimpleX509TrustManager.newSSLContext(),
					new SimpleX509TrustManager());

			dbSecurityContext
					.withSSLHostnameVerifier(com.marklogic.client.DatabaseClientFactory.SSLHostnameVerifier.ANY);

			client = DatabaseClientFactory.newClient(markLogicConfig.getHost(),
					markLogicConfig.getRestPort(), dbSecurityContext);

		} else {
			client = DatabaseClientFactory.newClient(markLogicConfig.getHost(), markLogicConfig.getRestPort(),
					new DatabaseClientFactory.DigestAuthContext(request.getUsername(), request.getPassword()));
		}

		// Equivalent to a "HEAD" check
		final String userDocumentUri = "/api/users/" + request.getUsername() + ".json";
		try {
			client.newJSONDocumentManager().read(userDocumentUri, new InputStreamHandle());
		} catch (FailedRequestException ex) {
			if (ex.getMessage().contains("Unauthorized")) {
				response.setStatus(401);
				return null;
			}
			throw ex;
		} catch (ResourceNotFoundException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully authenticated request, though could not find user document at: "
						+ userDocumentUri);
			}
		}

		session.setAttribute(SESSION_USERNAME_KEY, request.getUsername());
		session.setAttribute(SESSION_DATABASE_CLIENT_KEY, client);
		return new SessionStatus(true);
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public void logout(HttpSession session, HttpServletResponse response) {
		logger.info("Logging out: " + getAuthenticatedUsername(session));
		DatabaseClient client = (DatabaseClient) session.getAttribute(SESSION_DATABASE_CLIENT_KEY);
		if (client != null) {
			client.release();
		}
		session.invalidate();
		response.setStatus(HttpStatus.NO_CONTENT.value());
	}

	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public SessionStatus status(HttpSession session) {
		String username = getAuthenticatedUsername(session);
		return new SessionStatus(username, username != null);
	}

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public UserProfile profile(HttpSession session) {
		UserProfile p = new UserProfile();
		p.setUsername(getAuthenticatedUsername(session));
		return p;
	}

	private String getAuthenticatedUsername(HttpSession session) {
		return (String) session.getAttribute(SESSION_USERNAME_KEY);
	}
}
