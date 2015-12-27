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
package remotecontrol.readme

import grails.plugin.remotecontrol.Application
import grails.plugin.remotecontrol.Person
import grails.plugin.remotecontrol.RemoteControl
import grails.test.mixin.integration.Integration
import grails.transaction.Transactional
import spock.lang.Specification


@Transactional
@Integration(applicationClass=Application)
class ChainedPersonSpec extends Specification {
    def remote

    def setup () {
        remote = new RemoteControl ()
    }


    def getPerson = {
        id -> Person.get(id)
    }

    // pass the result of the getPerson command to the given modifications command
    def modifyPerson(id, Closure modifications) {
        remote (getPerson.curry(id), modifications)
    }

    def "modify Person" () {
        given:
        def id = remote {
            def person = new Person (name: "Me")
            person.save ()
            person.id
        }

        when:
        // Change the name
        modifyPerson (id) {
            it.setName ("New Name")
            it.save (flush: true)
            null // return must be serializable
        }

        // Somehow make some HTTP request and test that the person's name has changed
        then:
        true

        cleanup:
        modifyPerson (id) {
            it.delete ()
        }
    }
}
