/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.mongodb;

import com.mongodb.util.TestCase;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoCredentialTest extends TestCase {

    @Test
    public void testCredentials() {
        MongoCredential credentials;

        credentials = new MongoCredential("user", "pwd".toCharArray());
        assertEquals("user", credentials.getUserName());
        assertArrayEquals("pwd".toCharArray(), credentials.getPassword());
        assertEquals(MongoAuthenticationMechanism.MONGO_CR, credentials.getMechanism());
        assertEquals("admin", credentials.getSource());

        credentials = new MongoCredential("user", "pwd".toCharArray(), "test");
        assertEquals("user", credentials.getUserName());
        assertArrayEquals("pwd".toCharArray(), credentials.getPassword());
        assertEquals(MongoAuthenticationMechanism.MONGO_CR, credentials.getMechanism());
        assertEquals("test", credentials.getSource());

        credentials = new MongoCredential("user", "pwd".toCharArray(), MongoAuthenticationMechanism.MONGO_CR);
        assertEquals("user", credentials.getUserName());
        assertArrayEquals("pwd".toCharArray(), credentials.getPassword());
        assertEquals(MongoAuthenticationMechanism.MONGO_CR, credentials.getMechanism());
        assertEquals("admin", credentials.getSource());

        credentials = new MongoCredential("user", MongoAuthenticationMechanism.GSSAPI);
        assertEquals("user", credentials.getUserName());
        assertNull(credentials.getPassword());
        assertEquals(MongoAuthenticationMechanism.GSSAPI, credentials.getMechanism());
        assertEquals("$external", credentials.getSource());

        credentials = new MongoCredential("user", "pwd".toCharArray(), MongoAuthenticationMechanism.MONGO_CR, "test");
        assertEquals("user", credentials.getUserName());
        assertArrayEquals("pwd".toCharArray(), credentials.getPassword());
        assertEquals(MongoAuthenticationMechanism.MONGO_CR, credentials.getMechanism());
        assertEquals("test", credentials.getSource());

        try {
            new MongoCredential("user", null, MongoAuthenticationMechanism.MONGO_CR, "test");
            fail("MONGO-CR must have a password");
        } catch (IllegalArgumentException e) {
            // all good
        }

        try {
            new MongoCredential("user", "a".toCharArray(), MongoAuthenticationMechanism.GSSAPI);
            fail("GSSAPI must not have a password");
        } catch (IllegalArgumentException e) {
            // all good
        }
    }

    @Test
    public void testCredentialsStore() {
        char[] password = "pwd".toCharArray();
        MongoCredentialsStore store;

        store = new MongoCredentialsStore();
        assertTrue(store.getDatabases().isEmpty());
        assertNull(store.get("test"));

        store = new MongoCredentialsStore((MongoCredential) null);
        assertTrue(store.getDatabases().isEmpty());
        assertNull(store.get("test"));

        MongoCredential credentials = new MongoCredential("user", password);
        store = new MongoCredentialsStore(credentials);
        Set<String> expected;
        expected = new HashSet<String>();
        expected.add("admin");
        assertEquals(expected, store.getDatabases());
        assertEquals(credentials, store.get("admin"));
        assertNull(store.get("test"));

        List<MongoCredential> credentialsList;

        final MongoCredential credentials1 = new MongoCredential("user", password, "db1");
        final MongoCredential credentials2 = new MongoCredential("user", "pwd".toCharArray(), "db2");
        credentialsList = Arrays.asList(credentials1, credentials2);
        store = new MongoCredentialsStore(credentialsList);
        expected = new HashSet<String>();
        expected.add("db1");
        expected.add("db2");
        assertEquals(expected, store.getDatabases());
        assertEquals(credentials1, store.get("db1"));
        assertEquals(credentials2, store.get("db2"));
        assertNull(store.get("db3"));
        assertEquals(credentialsList, store.asList());

        credentialsList = Arrays.asList(credentials1, new MongoCredential("user2", password, "db1"));
        try {
            new MongoCredentialsStore(credentialsList);
            fail("should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
