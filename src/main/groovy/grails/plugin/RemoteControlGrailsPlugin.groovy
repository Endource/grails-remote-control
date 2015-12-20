/*
 * Copyright 2010, 2015 Luke Daley
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
package grails.plugin

import grails.plugin.remotecontrol.RemoteControl
import grails.plugin.remotecontrol.RemoteControlServlet
import grails.plugins.Plugin
import grails.util.BuildSettings
import grails.web.mapping.LinkGenerator
import org.springframework.boot.context.embedded.ServletRegistrationBean


class RemoteControlGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.9 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/**/*"
    ]

    def title = "Remote Control" // Headline display name of the plugin
    def author = "Luke Daley"
    def authorEmail = "ld@ldaley.com"
    def description = "Remotely control a Grails application (for functional testing)."

    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/remote-control"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "github", url: "https://github.com/alkemist/grails-remote-control/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/alkemist/grails-remote-control" ]

    Closure doWithSpring() { {->
        remoteControl (ServletRegistrationBean, new RemoteControlServlet (), "/${RemoteControl.RECEIVER_PATH}") {
            name = 'grails-remote-control'
            loadOnStartup = 1
        }
    } }

    void doWithDynamicMethods() {
    }

    void doWithApplicationContext () {
        def ctx = grailsApplication.mainContext
        LinkGenerator linkGenerator = ctx.getBean ("grailsLinkGenerator", LinkGenerator)

        System.setProperty (BuildSettings.FUNCTIONAL_BASE_URL_PROPERTY, linkGenerator.serverBaseURL)
    }

    void onChange(Map<String, Object> event) {
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
    }
}
