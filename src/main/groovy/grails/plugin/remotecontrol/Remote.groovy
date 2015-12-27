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
package grails.plugin.remotecontrol


trait Remote {
    private def remoteControl // can't initialize here because the plugin setup did not yet run.

    void initRemote () {
        remoteControl = new RemoteControl()
    }

    /**
     * Work around "MissingMethodException"s when calling `remoteControl` inline. At some places
     * groovy doesn't understand that it should use the call method of the RemoteControl class.
     *
     * @param codes Closures that should run "remote"
     * @return result of the last closure
     */
    def remote (Closure... codes) {
        remoteControl.exec (codes)
    }

    /**
     * Helper method that checks if the cause of the given throwable is a expected exception. If
     * the direct cause doesn't match it will check the nested causes until the expected exception
     * is found or there is no other cause. If there is no cause it will fail.
     *
     * @param t the throwable to check
     * @param expected the type of cause we expect
     */
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
}
