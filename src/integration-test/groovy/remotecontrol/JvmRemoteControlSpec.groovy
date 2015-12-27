/*
 * Copyright 2015 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package remotecontrol

import grails.plugin.remotecontrol.Application
import grails.plugin.remotecontrol.Remote
import grails.test.mixin.integration.Integration
import io.remotecontrol.client.RemoteException
import spock.lang.Requires
import spock.lang.Specification


/**
 * This test class contains the tests that will only succeed if the application and the test do
 * NOT run in the same jvm.
 *
 *  when running 'grails test-app' the application and tests run by default in the same jvm and
 *  the test classes are accessible. In that case the tests will not fail. So we only run them
 *  when 'baseUrl' is set to test against an already running application.
 */

@Requires({ System.getProperty("baseUrl") })
@Integration(applicationClass=Application)
class JvmRemoteControlSpec extends Specification implements Remote {

    def setup () {
        initRemote ()
    }

    def "any class referenced has to be available in the remote app, classes defined in test are not" () {
        def a = new RemoteControlLocal()

        when:
        remote { a }

        then:
        RemoteException e = thrown ()
        assertCause (e, ClassNotFoundException)
    }

    def "a command can not instantiate a class that is not in the remote app" () {
        when:
        remote {
            new RemoteControlLocal ()
        }

        then:
        RemoteException e = thrown ()
        assertCause (e, NoClassDefFoundError)
    }

    def "accessing a property that is not in the delegate causes a MissingPropertyException" () {
        when:
        remote { iDontExist == true }

        then:
        RemoteException e = thrown ()
        assertCause (e, MissingPropertyException)
    }
}
