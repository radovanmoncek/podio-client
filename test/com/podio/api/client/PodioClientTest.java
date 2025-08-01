package com.podio.api.client;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PodioClientTest {
    private static PodioClient podioClient;

    @BeforeAll
    static void setup(){

        podioClient = PodioClient.returnInstance();
    }

    @Test
    void singletonTest(){

	assertEquals(podioClient, PodioClient.returnInstance());
    }

    @AfterAll
    static void closableTest(){

	podioClient.close();
    }
}
