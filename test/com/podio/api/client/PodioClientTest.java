package com.podio.api.client;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PodioClientTest {
    private static PodioClient podioClient;

    @BeforeAll
    static void setup(){

        podioClient = PodioClient.returnNewInstance();
    }

    @Test
    void loginTest() throws Exception {
        podioClient.login(
                System.getenv(""),
                System.getenv(""),
                System.getenv(""),
                System.getenv("")
        );

        final var authenticationResponseBodyField = podioClient.getClass().getDeclaredField("authenticationResponseBody");
        authenticationResponseBodyField.setAccessible(true);

        assertNotNull(authenticationResponseBodyField.get(podioClient));
    }
}
