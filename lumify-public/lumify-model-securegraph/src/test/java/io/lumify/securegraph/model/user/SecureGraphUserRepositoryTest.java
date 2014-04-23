package io.lumify.securegraph.model.user;

import io.lumify.core.model.ontology.Concept;
import io.lumify.core.model.ontology.OntologyRepository;
import io.lumify.core.model.user.AuthorizationRepository;
import io.lumify.core.model.user.InMemoryAuthorizationRepository;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.security.LumifyVisibility;
import org.securegraph.id.UUIDIdGenerator;
import org.securegraph.inmemory.InMemoryGraph;
import org.securegraph.inmemory.InMemoryGraphConfiguration;
import org.securegraph.search.DefaultSearchIndex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecureGraphUserRepositoryTest {
    @Mock
    private OntologyRepository ontologyRepository;

    @Mock
    private Concept userConcept;

    private AuthorizationRepository authorizationRepository;
    private SecureGraphUserRepository secureGraphUserRepository;

    @Before
    public void setup() {
        InMemoryGraphConfiguration config = new InMemoryGraphConfiguration(new HashMap());
        authorizationRepository = new InMemoryAuthorizationRepository();
        authorizationRepository.addAuthorizationToGraph(LumifyVisibility.SUPER_USER_VISIBILITY_STRING.toString());
        when(ontologyRepository.getOrCreateConcept((Concept) isNull(), eq(UserRepository.LUMIFY_USER_CONCEPT_ID), anyString())).thenReturn(userConcept);
        when(userConcept.getTitle()).thenReturn(UserRepository.LUMIFY_USER_CONCEPT_ID);

        secureGraphUserRepository = new SecureGraphUserRepository(authorizationRepository,
                new InMemoryGraph(config, new UUIDIdGenerator(config.getConfig()), new DefaultSearchIndex(config.getConfig())),
                ontologyRepository);
    }

    @Test
    public void testAddUser() {
        secureGraphUserRepository.addUser("12345", "testUser", "testPassword", new String[]{"auth1", "auth2"});

        SecureGraphUser secureGraphUser = (SecureGraphUser) secureGraphUserRepository.findByUsername("testUser");
        assertEquals("testUser", secureGraphUser.getDisplayName());
    }
}
