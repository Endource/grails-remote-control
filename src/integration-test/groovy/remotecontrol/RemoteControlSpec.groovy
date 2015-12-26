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
package remotecontrol

import grails.core.GrailsApplication
import grails.plugin.remotecontrol.Application
import grails.plugin.remotecontrol.Person
import grails.plugin.remotecontrol.RemoteControl
import grails.test.mixin.integration.Integration
import io.remotecontrol.UnserializableCommandException
import io.remotecontrol.client.RemoteException
import io.remotecontrol.client.UnserializableReturnException
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Specification


/**
 * This test case shows how to use the remote control and some of it's limitations with regard to
 * serialization and scope.
 *
 * The remote control object has an exec(Closure) method, and an alias for that as call(Closure).
 * The call(Closure) variant allows the use of the Groovy language feature where you can essentially
 * treat an object like a method, which is how “remote { … }” works below (i.e. it's really
 * “remote.call { … }). This doesn't always work though as you will see (due to Groovy), so
 * sometimes you need to use .exec().
 *
 * Where we are passing a closure to the remote control object, that closure gets executed INSIDE
 * the application we are functionally testing, which may be in a different JVM on a completely
 * different machine. This works by sending the closure over HTTP to the application (which must
 * have the remote-control plugin installed).
 *
 * An example use for this would be creating/deleting domain data inside your remote application
 * for testing purposes.
 */

@Integration(applicationClass=Application)
class RemoteControlSpec extends Specification {
    def remoteControl // can't initialize here because the plugin setup did not yet run.

    def setup () {
        remoteControl = new RemoteControl()
    }

    // work around "MissingMethodException" when calling `remoteControl` inline
    def remote (Closure code) {
        remoteControl code
    }

    def remote (Closure... codes) {
        remoteControl codes
    }

    def "the result of the command run on the server is sent back and is returned" () {
        expect:
        remote { 1 + 1 } == 2
    }

    def "the command has access to the app context to access any bean defined there" () {
        expect:
        remote {
            ctx.grailsApplication instanceof GrailsApplication
        }
    }

    def "a command can create and manipulate domain data in a hibernate session that is flushed at the end" () {
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

    def "we can access lexical scope (within limits)" () {
        def a = 1

        expect:
        2 == remote { a + 1 }
    }

    def "anything in lexical scope we access must be serializable" () {
        when:
        def a = System.out
        remote {
            a
        }

        then:
        thrown (UnserializableCommandException)
    }

    def anIvar = 2
    def "owner ivars can't be accessed because they aren't really lexical scope, so get treated as bean names from the app context" () {
        when:
        remote {
            anIvar * 2
        }

        then:
        RemoteException e = thrown ()
        e.cause instanceof MissingPropertyException
    }

    def "we can pass curried commands" () {
        def command = { it + 2 }

        expect:
        4 == remote (command.curry (2))
    }

    def "we can curry a command as many times as we need to" () {
        def command = { a, b -> a + b }
        def curry1 = command.curry (1)
        def curry2 = curry1.curry (1)

        expect:
        2 == remote (curry2)
    }

    def "currying args must be serializable" () {
        when:
        remote ({ it }.curry (System.out))

        then:
        thrown (UnserializableCommandException)
    }

    // when running 'grails test-app' the application and tests run in the same jvm and the test
    // classes are accessible. In that case the test will not fail. So we only run it when 'baseUrl'
    // is set to test against an already running application.
    @Requires({ System.getProperty("baseUrl") })
    def "any class referenced has to be available in the remote app, classes defined in test are not" () {
        def a = new RemoteControlLocal()

        when:
        remote { a }

        then:
        RemoteException e = thrown ()
        assertCause (e, ClassNotFoundException)
    }

    void assertCause (Throwable t, Class expected) {
        Throwable cause = t.cause
        while (true) {
            if (!cause) {
                assert null == expected
            }
            else if (cause.class == expected) {
                return
            }
            else {
                cause = cause.cause
            }
        }
    }

    @Ignore("application and tests are run in the same jvm, the test class is accessible.")
    def "a command can not instantiate a class that is not in the remote app" () {
        when:
        remote {
            new RemoteControlLocal ()
        }

        then:
        thrown (NoClassDefFoundError)
    }

    def "multiple commands can be chained, passing each result to the next command as it's single argument" () {
        expect:
        3 == remote ({ 1 }, { it + 1 }, { it + 1 })
    }

    def "the delegate of commands is like a map and can store properties" () {
        expect:
        3 == remote ({ num = 1 }, { num = num + 1 }, { num + 1 })
    }

    @Ignore("fails with RemoteException instead of MissingPropertyException.")
    def "accessing a property that is not in the delegate causes a MissingPropertyException" () {
        when:
        remote { iDontExist == true }

        then:
        thrown (MissingPropertyException)
    }

    def "a command can set properties of a remote bean" () {
        when:
        remote { ctx.theService.value = 1 }

        then:
        1 == remote { ctx.theService.value }


        when:
        remote { ctx.theService.setValue (2) }

        then:
        2 == remote { ctx.theService.value }


        when:
        remote { ctx.theService.setProperty('value', 3) }

        then:
        3 == remote { ctx.theService.value }
    }

    def "a command can call methods dynamically" () {
        def methodName = "setValue"

        when:
        remote { ctx.theService."$methodName"(1) }

        then:
        1 == remote { ctx.theService.value }

        when:
        remote { ctx.theService.invokeMethod(methodName, 2) }

        then:
        2 == remote { ctx.theService.value }
    }

}

class RemoteControlLocal implements Serializable {}
