/*
 * Copyright 2015 Luke Daley
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
package grails.plugin.remotecontrol

import grails.test.mixin.integration.Integration
import spock.lang.Specification


@Integration
class RemoteControlSpec extends Specification {
    def remoteControl // can't initialize here because the plugin setup did not yet run.

    def setup () {
        remoteControl = new RemoteControl()
    }

    // work around "MissingMethodException" when calling `remoteControl` inline
    def remote (Closure code) {
        remoteControl code
    }

    def "the result of the command run on the server is sent back and is returned" () {
        expect:
        remote { 1 + 1 } == 2
    }
}
