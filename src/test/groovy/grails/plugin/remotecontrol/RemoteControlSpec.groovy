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

import grails.core.GrailsApplication
import grails.test.mixin.integration.Integration
import io.remotecontrol.client.RemoteException
import io.remotecontrol.client.UnserializableReturnException
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

    def "the remote command has access to the app context to access any bean defined there" () {
        expect:
        remote {
            ctx.grailsApplication instanceof GrailsApplication
        }
    }

    def "a remote command can create and manipulate domain data in a hibernate session that is flushed at the end" () {
        when:
        def id = remote {
            def person = new Person (name: "Me")
            person.save ()
            person.id
        }

        then:
        remote { Person.countByName("Me") } == 1

        when:
        remote {
            Person.get (id).delete ()
        }

        then:
        remote { Person.countByName("Me") } == 0
    }

    def "commands can contain other closures" () {
        expect:
        [2, 3, 4] == remote {
            [1, 2, 3].collect { it + 1 }
        }
    }

    def "if the command throws, we throw a RemoteException client side with the actual exception as the cause" () {
        when:
        remote {
            throw new Exception ('bang!')
        }

        then:
        RemoteException e = thrown ()
        e.cause.class == Exception
        e.cause.message == 'bang!'
    }

    def "if the command returns something that is unserializable, we throw an UnserializableReturnException" () {
        when:
        remote {
            System.out
        }

        then:
        thrown (UnserializableReturnException)
    }

    def "if the command returns an exception but does not throw it, we just return the exception" () {
        expect:
        remote { new Exception () } instanceof Exception
    }

}
