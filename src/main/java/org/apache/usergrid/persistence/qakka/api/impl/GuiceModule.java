/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.usergrid.persistence.qakka.api.impl;

import com.google.inject.AbstractModule;
import com.netflix.config.ConfigurationManager;
import org.apache.usergrid.persistence.qakka.QakkaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Dave Johnson (snoopdave@apache.org) on 11/28/16.
 */
public class GuiceModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger( GuiceModule.class );

    static {
        try {
            // load properties from one properties file using Netflix Archaius so that GuicyFig will see them
            ConfigurationManager.loadCascadedPropertiesFromResources( "qakka" );
            logger.info("qakka.properties loaded");
        } catch (Throwable t) {
            logger.error("Unable to load qakka.properties");
        }
    }
    
    @Override
    protected void configure() {
        install( new QakkaModule() );
    }
}
