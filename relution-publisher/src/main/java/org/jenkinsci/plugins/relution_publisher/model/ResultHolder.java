/*
 * Copyright 2016 M-Way Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkinsci.plugins.relution_publisher.model;

import hudson.model.Result;


/**
 * Interface definition for a class that holds a {@link Result}.
 */
public interface ResultHolder {

    /**
     * @return The {@link Result}.
     */
    Result getResult();

    /**
     * Sets the result to the specified value.
     * @param result The {@link Result} to set.
     */
    void setResult(final Result result);
}
