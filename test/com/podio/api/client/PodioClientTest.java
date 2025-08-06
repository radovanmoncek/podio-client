package com.podio.api.client;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.logging.*;

public class PodioClientTest {
    private static PodioClient podioClient;

    @BeforeAll
    static void setup(){

	final var consoleHandler = new ConsoleHandler();

	consoleHandler.setLevel(Level.ALL);

	final var log = Logger.getLogger(PodioClient.class.getName());

	log.setLevel(Level.ALL);
	log.addHandler(consoleHandler);
	
        podioClient = PodioClient.returnInstance();
    }

    @Test
    void singletonTest(){

	assertEquals(podioClient, PodioClient.returnInstance());
    }

    @Test
    void incorrectLoginTest() {

	final var creds = new HashMap<String, String>();

	creds.put("email", "testMail");
	creds.put("password", "testPass");
	
	assertThrows(Exception.class, () -> podioClient.login(creds));
    }

    @AfterAll
    static void closableTest(){

	podioClient.close();
    }
}
