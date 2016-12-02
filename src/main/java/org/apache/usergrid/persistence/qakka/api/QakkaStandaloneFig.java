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

package org.apache.usergrid.persistence.qakka.api;

import org.safehaus.guicyfig.Default;
import org.safehaus.guicyfig.FigSingleton;
import org.safehaus.guicyfig.GuicyFig;
import org.safehaus.guicyfig.Key;

import java.io.Serializable;

/**
 * Created by Dave Johnson (snoopdave@apache.org) on 12/1/16.
 */
@FigSingleton
public interface QakkaStandaloneFig extends GuicyFig, Serializable {
    
    String QUEUE_AUTH_ENABLED = "queue.auth.enabled";
    
    String QUEUE_AUTH_USERNAME = "queue.auth.username";
    
    String QUEUE_AUTH_PASSWORD = "queue.auth.password";

    @Key(QUEUE_AUTH_ENABLED)
    @Default("false")
    String getAuthEnabled();

    @Key(QUEUE_AUTH_USERNAME)
    @Default("admin")
    String getAdminUsername();

    @Key(QUEUE_AUTH_PASSWORD)
    @Default("admin")
    String getAdminPassword();
}