package com.marklogic.grove.boot.auth;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

public class AuthControllerTest {

	private AuthController controller = new AuthController();
	private MockHttpSession session = new MockHttpSession();

	@Test
	public void status() {
		SessionStatus status = controller.status(session);
		assertFalse(status.isAuthenticated());
		assertNull(status.getUsername());

		session.setAttribute(controller.SESSION_USERNAME_KEY, "testuser");
		status = controller.status(session);
		assertTrue(status.isAuthenticated());
		assertEquals("testuser", status.getUsername());
	}

	@Test
	public void logout() {
		assertFalse(session.isInvalid());
		controller.logout(session);
		assertTrue(session.isInvalid());
	}
}
