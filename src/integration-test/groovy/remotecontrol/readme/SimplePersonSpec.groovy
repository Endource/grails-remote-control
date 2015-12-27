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
import grails.test.mixin.integration.Integration
import grails.transaction.Transactional
import spock.lang.Specification


@Transactional
@Integration (applicationClass = Application)
class SimplePersonSpec extends Specification {

    def "store new Person" () {
        given:
        def person = new Person (name: 'Me')
        person.save (flush: true)

        // Somehow make some HTTP request and test that person is in the DB
        expect:
        true

        cleanup:
        person.delete (flush: true)
    }
}
