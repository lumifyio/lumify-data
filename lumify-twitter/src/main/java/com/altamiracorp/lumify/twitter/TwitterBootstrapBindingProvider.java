/*
 * Copyright 2014 Altamira Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altamiracorp.lumify.twitter;

import com.altamiracorp.lumify.core.bootstrap.BootstrapBindingProvider;
import com.altamiracorp.lumify.core.config.Configuration;
import com.google.inject.Binder;
import com.google.inject.Scopes;

/**
 * This class provides Guice bindings for Twitter classes.
 */
public class TwitterBootstrapBindingProvider implements BootstrapBindingProvider {
    @Override
    public void addBindings(final Binder binder, final Configuration configuration) {
        binder.bind(LumifyTwitterProcessor.class).to(DefaultLumifyTwitterProcessor.class).in(Scopes.SINGLETON);
        binder.bind(UrlStreamCreator.class).to(URLUrlStreamCreator.class).in(Scopes.SINGLETON);
    }
}
