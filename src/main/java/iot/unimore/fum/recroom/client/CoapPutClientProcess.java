package iot.unimore.fum.recroom.client;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CoapPutClientProcess {

    private final static Logger logger = LoggerFactory.getLogger(CoapPutClientProcess.class);

    private static final String COAP_ENDPOINT = "coap://127.0.0.1:5687/parameter-bootwrap";

    public static void main(String[] args) {

        CoapClient coapClient = new CoapClient(COAP_ENDPOINT);

        Request request = new Request(Code.PUT);

        String myPayload = "bootwrap_alternative_mode";
        logger.info("PUT Request Random Payload: {}", myPayload);
        request.setPayload(myPayload);

        request.setConfirmable(true);

        logger.info("Request Pretty Print: \n{}", Utils.prettyPrint(request));

        CoapResponse coapResp = null;

        try {

            coapResp = coapClient.advanced(request);

            logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResp));

            String text = coapResp.getResponseText();
            logger.info("Payload: {}", text);
            logger.info("Message ID: " + coapResp.advanced().getMID());
            logger.info("Token: " + coapResp.advanced().getTokenString());

        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
    }
}
