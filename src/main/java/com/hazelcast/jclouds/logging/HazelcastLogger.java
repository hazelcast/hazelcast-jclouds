/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.hazelcast.jclouds.logging;

import java.util.logging.Level;

import org.jclouds.logging.Logger;

import com.hazelcast.logging.ILogger;

/**
 * Bridges the jclouds logging framework to Hazelcast logging.
 */
public class HazelcastLogger implements Logger {

    /**
     * Creates the jclouds logger that bridges messages to Hazelcast.
     */
    public static class Factory implements LoggerFactory {
        public Logger getLogger(String category) {
            return new HazelcastLogger(category,
                    com.hazelcast.logging.Logger.getLogger(category));
        }
    }

    private final ILogger logger;
    private final String category;

    public HazelcastLogger(String category, ILogger logger) {
        this.logger = logger;
        this.category = category;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public void trace(String message, Object... args) {
        logger.finest(String.format(message, args));
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isFinestEnabled();
    }

    @Override
    public void debug(String message, Object... args) {
        logger.fine(String.format(message, args));
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isFineEnabled();
    }

    @Override
    public void info(String message, Object... args) {
        logger.info(String.format(message, args));
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public void warn(String message, Object... args) {
        logger.warning(String.format(message, args));
    }

    @Override
    public void warn(Throwable throwable, String message, Object... args) {
        logger.warning(String.format(message, args), throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public void error(String message, Object... args) {
        logger.severe(String.format(message, args));
    }

    @Override
    public void error(Throwable throwable, String message, Object... args) {
        logger.severe(String.format(message, args), throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

}
