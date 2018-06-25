package org.chiknrice.mail

import org.subethamail.smtp.*

import javax.mail.Message
import javax.mail.internet.MimeMessage

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
class SmtpMessageHandlerFactory implements MessageHandlerFactory {

    MessageHandler create(MessageContext ctx) {
        return new SmtpMessageHandler(ctx)
    }

    class SmtpMessageHandler implements MessageHandler {

        MessageContext ctx
        EwsAuthHandlerFactory.EwsAuthHandler ewsServiceBridge

        SmtpMessageHandler(MessageContext ctx) {
            this.ctx = ctx
            // the authenticationHandler is actually holds the session and transport
            ewsServiceBridge = (EwsAuthHandlerFactory.EwsAuthHandler) ctx.authenticationHandler
        }

        void from(String from) throws RejectException {
            // this is just called by the SMTP client during MAIL FROM command
            // no need to handle the content the information is also in the header after the DATA command
        }

        void recipient(String recipient) throws RejectException {
            // this is just called by the SMTP client for each RCPT command (which is sent for each recepient address)
            // no need to handle the content the information is also in the header after the DATA command
        }

        void data(InputStream data) throws IOException {
            // parse
            def message = new MimeMessage(ewsServiceBridge.ewsSession, data)

            // and send
            ewsServiceBridge.ewsTransport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))
        }

        void done() {
            // no op (ideally the messages should be queued here then sent asynchronously, but that can be optimized
            // later if someone has the need for high performing proxy
        }

    }
}