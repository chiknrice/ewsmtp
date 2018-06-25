package org.chiknrice.mail

import org.subethamail.smtp.AuthenticationHandler
import org.subethamail.smtp.AuthenticationHandlerFactory
import org.subethamail.smtp.RejectException
import org.subethamail.smtp.auth.LoginFailedException
import org.subethamail.smtp.auth.PlainAuthenticationHandlerFactory
import org.subethamail.smtp.auth.UsernamePasswordValidator

import javax.mail.AuthenticationFailedException
import javax.mail.Session
import javax.mail.Transport

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
class EwsAuthHandlerFactory implements AuthenticationHandlerFactory {

    final String ewsUrl

    EwsAuthHandlerFactory(String ewsUrl) {
        this.ewsUrl = ewsUrl
    }

    @Override
    List<String> getAuthenticationMechanisms() {
        return ["PLAIN"]
    }

    @Override
    AuthenticationHandler create() {
        return new EwsAuthHandler()
    }

    /**
     * Authentication is done by building the EwsTransport and connecting.  After successful authentication, the same
     * service would be used later to send  messages.
     */
    class EwsAuthHandler implements AuthenticationHandler {
        Session ewsSession
        Transport ewsTransport

        private final AuthenticationHandler delegate

        {
            PlainAuthenticationHandlerFactory delegateFactory = new PlainAuthenticationHandlerFactory(new UsernamePasswordValidator() {

                @Override
                void login(String username, String password) throws LoginFailedException {
                    // initalize a session
                    def properties = new Properties()
                    // don't save sent messages...
                    properties.setProperty("org.sourceforge.net.javamail4ews.transport.EwsTransport.SendAndSaveCopy", "false")
                    Session session = Session.getInstance(properties)

                    // get the EWS transport implementation
                    Transport lTransport = session.getTransport("ewstransport")

                    // connect to the exchange server - the transport is configured to authentication on connection due
                    // to org.sourceforge.net.javamail4ews.util.Util.VerifyConnectionOnConnect=true
                    // this is needed to feedback to the smtp client immediately (during AUTH command) so that the client
                    // can properly react (some clients prompt the user for username/password if auth fails)
                    try {
                        lTransport.connect(ewsUrl, username, password)
                    } catch (AuthenticationFailedException e) {
                        // wrong url returns:
                        // - javax.mail.AuthenticationFailedException: The request failed. null
                        // wrong credentials returns:
                        // - javax.mail.AuthenticationFailedException: The request failed. The request failed. The remote server returned an error: (401)Unauthorized
                        // TODO: need to behave differently depending on the exception message
                        e.printStackTrace()
                        throw new LoginFailedException()
                    }
                    ewsSession = session
                    ewsTransport = lTransport
                }

            })
            delegate = delegateFactory.create()
        }

        @Override
        String auth(String clientInput) throws RejectException {
            return delegate.auth(clientInput)
        }

        @Override
        Object getIdentity() {
            return delegate.getIdentity()
        }
    }
}
