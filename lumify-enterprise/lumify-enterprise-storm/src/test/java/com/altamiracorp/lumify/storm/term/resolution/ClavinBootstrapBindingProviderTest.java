/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.term.resolution;

import static org.mockito.Mockito.*;

import com.altamiracorp.lumify.core.config.Configuration;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Configuration.class })
public class ClavinBootstrapBindingProviderTest {
    @Mock
    private Binder binder;
    @Mock
    private AnnotatedBindingBuilder<ClavinOntologyMapper> annotatedBindingBuilder;
    @Mock
    private ScopedBindingBuilder scopedBindingBuilder;

    private Configuration config;

    private ClavinBootstrapBindingProvider instance;

    @Before
    public void setup() {
        config = PowerMockito.mock(Configuration.class);
        instance = new ClavinBootstrapBindingProvider();
    }

    @Test
    public void testAddBindings() {
        when(binder.bind(ClavinOntologyMapper.class)).thenReturn(annotatedBindingBuilder);
        when(annotatedBindingBuilder.to(SimpleClavinOntologyMapper.class)).thenReturn(scopedBindingBuilder);

        instance.addBindings(binder, config);

        verify(binder).bind(ClavinOntologyMapper.class);
        verify(annotatedBindingBuilder).to(SimpleClavinOntologyMapper.class);
        verify(scopedBindingBuilder).in(Scopes.SINGLETON);
    }
}
