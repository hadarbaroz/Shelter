package com.e.shelter;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntegrationTests {
    @Test
    public void FireBaseIntegrationTest() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        assertNotNull(firestore);
    }
}
