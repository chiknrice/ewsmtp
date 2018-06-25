package org.chiknrice.mail

import org.subethamail.smtp.server.SMTPServer

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
class SmtpToEwsProxy {

    static void main(String[] args) {
        def cli = new CliBuilder(usage: 'ewsmtp -[hup]')
        // Create the list of options.
        cli.with {
            h longOpt: 'help', 'Show usage information'
            u longOpt: 'ews-url', args: 1, argName: 'url', 'EWS URL'
            p longOpt: 'port-number', args: 1, argName: 'port', 'Port'
        }
        def options = cli.parse(args)
        if (!options) {
            return
        }
        // Show usage text when -h or --help option is used.
        if (options.h) {
            cli.usage()
            return
        }

        // default port
        int port = 25
        if(options.p && options.p.isInteger()) {
            port = options.p.toInteger()
        }

        if (options.u) {
            SmtpMessageHandlerFactory messageHandlerFactory = new SmtpMessageHandlerFactory()
            SMTPServer smtpServer = new SMTPServer(messageHandlerFactory)
            smtpServer.setAuthenticationHandlerFactory(new EwsAuthHandlerFactory(options.u))
            smtpServer.setPort(port)
            smtpServer.start()
        } else {
            cli.usage()
        }

    }

}
