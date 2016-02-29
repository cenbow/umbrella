/*
 * Copyright 2002-2014 the original author or authors.
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
package com.harmony.umbrella.log.message;

import java.io.Serializable;

import com.harmony.umbrella.log.LogInfo;
import com.harmony.umbrella.log.Message;

/**
 * @author wuxii@foxmail.com
 */
public class LogInfoMessage implements Message, Serializable {

    private static final long serialVersionUID = 2679492894838444102L;

    private final LogInfo logInfo;

    public LogInfoMessage(LogInfo logInfo) {
        this.logInfo = logInfo;
    }

    @Override
    public String getFormat() {
        return logInfo.toString();
    }

    @Override
    public String getFormattedMessage() {
        return logInfo.toString();
    }

    @Override
    public Object[] getParameters() {
        return null;
    }

    @Override
    public Throwable getThrowable() {
        return logInfo.getException();
    }

    @Override
    public String toString() {
        return "LogInfoMessage [logInfo=" + logInfo + "]";
    }

}
