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

package org.jenkinsci.plugins.relution_publisher.net;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.model.ServerVersion;
import org.jenkinsci.plugins.relution_publisher.model.constants.Headers;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SessionManager extends RequestManager implements AuthenticatedNetwork {

    /**
     * The serial version number of this class.
     * <p>
     * This version number is used to determine whether a serialized representation of this class
     * is compatible with the current implementation of the class.
     * <p>
     * <b>Note</b> Maintainers must change this value <b>if and only if</b> the new version of this
     * class is not compatible with old versions.
     * @see
     * <a href="http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html">
     * Versioning of Serializable Objects</a>.
     */
    private static final long    serialVersionUID = 1L;

    private final RequestFactory requestFactory;

    private Store                store;

    private String               sessionId;
    private ServerVersion        serverVersion;

    public SessionManager(final RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    private String parseSessionId(final String cookie) {
        if (StringUtils.isBlank(cookie)) {
            return null;
        }

        final Pattern pattern = Pattern.compile("^JSESSIONID=([^;]*);.*$");
        final Matcher matcher = pattern.matcher(cookie);

        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }

    private String parseSessionId(final ApiResponse response) {
        final HttpResponse httpResponse = response.getHttpResponse();

        final Header cookie = httpResponse.getFirstHeader(Headers.SET_COOKIE);

        if (cookie == null) {
            return null;
        }

        return this.parseSessionId(cookie.getValue());
    }

    private ServerVersion parseServerVersion(final ApiResponse response) {
        final HttpResponse httpResponse = response.getHttpResponse();

        final Header version = httpResponse.getFirstHeader(Headers.RELUTION_VERSION);

        if (version == null) {
            return new ServerVersion(null);
        }

        return new ServerVersion(version.getValue());
    }

    @Override
    public void logIn(final Store store) throws InterruptedException, ExecutionException, IOException {
        if (store == null) {
            throw new IllegalArgumentException("The specified argument cannot be null: store");
        }

        if (this.store != null) {
            throw new IllegalStateException("Already logged in");
        }

        final ApiRequest request = this.requestFactory.createLoginRequest(store);
        final ApiResponse response = this.execute(request);

        this.store = store;
        this.sessionId = this.parseSessionId(response);
        this.serverVersion = this.parseServerVersion(response);
    }

    @Override
    public boolean logOut() {
        if (this.store == null || this.sessionId == null) {
            return false;
        }

        try {
            final ApiRequest request = this.requestFactory.createLogoutRequest(this.store);
            this.execute(request);
            return true;
        } catch (final InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (final ExecutionException e) {
            e.printStackTrace();
            return false;
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            this.serverVersion = null;
            this.sessionId = null;
            this.store = null;
        }
    }

    @Override
    public ServerVersion getServerVersion() {
        return this.serverVersion;
    }

    @Override
    public void close() throws IOException {
        this.logOut();
        super.close();
    }
}
