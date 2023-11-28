package iot.unimore.fum.recroom.client;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CoapObservingClientProcess {

    private final static Logger logger = LoggerFactory.getLogger(CoapObservingClientProcess.class);

    public static void main(String[] args) {

        String targetCoapResourceURL = "coap://127.0.0.1:5685/temperature";

        CoapClient client = new CoapClient(targetCoapResourceURL);

        logger.info("OBSERVING ... {}", targetCoapResourceURL);

        Request request = Request.newGet().setURI(targetCoapResourceURL).setObserve();
        request.setConfirmable(true);

        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                logger.info("Notification Response Pretty Print: \n{}", Utils.prettyPrint(response));
                logger.info("Value: " + content);
            }

            public void onError() {
                logger.error("OBSERVING FAILED");
            }
        });


        try {
            Thread.sleep(60*3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("CANCELLATION.....");
        relation.proactiveCancel();

    }

}