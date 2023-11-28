package iot.unimore.fum.recroom;

import iot.unimore.fum.recroom.descriptor.*;
import iot.unimore.fum.recroom.model.BootWrapModel;
import iot.unimore.fum.recroom.model.LightTherapyModel;
import iot.unimore.fum.recroom.resource.*;
import org.eclipse.californium.core.CoapServer;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

public class RecoveryRoomProcess extends CoapServer {

    private final static Logger logger = LoggerFactory.getLogger(RecoveryRoomProcess.class);

    private static final String RD_COAP_ENDPOINT_BASE_URL = "coap://127.0.0.1:5683/rd";

    //private static final String RD_COAP_ENDPOINT_BASE_URL = "coap://192.168.1.102:5683/rd";

    private static final String TARGET_LISTENING_IP_ADDRESS = "192.168.1.17";

    private static final int TARGET_COAP_PORT = 5783;

    public RecoveryRoomProcess() {

        super();

        // explicitly bind to each address to avoid the wildcard address reply problem
        // (default interface address instead of original destination)
        for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
            if (!addr.isLinkLocalAddress()) {
                CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
                builder.setInetSocketAddress(new InetSocketAddress(addr, TARGET_COAP_PORT));
                this.addEndpoint(builder.build());
            }
        }

        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());

        this.add(createBootWrapResource(deviceId));
        this.add(createLightTherapyResource(deviceId));
        this.add(createSaunaResource(deviceId));

    }

    private static void registerToCoapResourceDirectory(Resource rootResource, String endPointName, String sourceIpAddress, int sourcePort){

        try{

            //coap://192.168.1.102:5683/rd?ep=myEndpointName&base=coap://192.168.1.17:5783
            String finalRdUrl = String.format("%s?ep=%s&base=coap://%s:%d", RD_COAP_ENDPOINT_BASE_URL, endPointName, sourceIpAddress, sourcePort);

            logger.info("Registering to Resource Directory: {}", finalRdUrl);

            //Initialize coapClient
            CoapClient coapClient = new CoapClient(finalRdUrl);

            //Request Class is a generic CoAP message: in this case we want a GET.
            //"Message ID", "Token" and other header's fields can be set
            Request request = new Request(CoAP.Code.POST);

            //If the POST request has a payload it can be set with the following command
            request.setPayload(LinkFormat.serializeTree(rootResource));

            //Set Request as Confirmable
            request.setConfirmable(true);

            logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(request));

            //Synchronously send the POST request (blocking call)
            CoapResponse coapResponse = null;

            try {

                coapResponse = coapClient.advanced(request);

                //Pretty print for the received response
                logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResponse));

                //The "CoapResponse" message contains the response.
                String text = coapResponse.getResponseText();
                logger.info("Payload: {}", text);
                logger.info("Message ID: " + coapResponse.advanced().getMID());
                logger.info("Token: " + coapResponse.advanced().getTokenString());

            } catch (ConnectorException | IOException e) {
                e.printStackTrace();
                logger.info("problem");
            }

        }catch (Exception e){
            e.printStackTrace();
            logger.info("problem");
        }

    }

    private CoapResource createBootWrapResource(String deviceId) {

        CoapResource bootwrapResource = new CoapResource("bootwrap");

        //Pressure
        PressureSensorDescriptor bootwrapPressureSensorDescriptor = new PressureSensorDescriptor();
        PressureSensorResource bootwrapPressureSensorResource = new PressureSensorResource(deviceId, "pressure", bootwrapPressureSensorDescriptor);
        bootwrapResource.add(bootwrapPressureSensorResource);

        //Battery
        BatterySensorDescrpitor bootwrapBatterySensorDescriptor = new BatterySensorDescrpitor();
        BatterySensorResource bootwrapBatterySensorResource = new BatterySensorResource(deviceId, "battery", bootwrapBatterySensorDescriptor);
        bootwrapResource.add(bootwrapBatterySensorResource);

        //Switch
        SwitchActuatorDescriptor bootwrapSwitchActuatorDescriptor = new SwitchActuatorDescriptor();
        SwitchActuatorResource bootwrapSwitchActuatorResource = new SwitchActuatorResource(deviceId, "switch", bootwrapSwitchActuatorDescriptor);
        bootwrapResource.add(bootwrapSwitchActuatorResource);

        //BootWrapParameter
        BootWrapParameterDescriptor bootWrapParameterDescriptor = new BootWrapParameterDescriptor(new BootWrapModel());
        BootWrapParameterResource bootWrapParameterResource = new BootWrapParameterResource(deviceId, "parameter", bootWrapParameterDescriptor);
        bootwrapResource.add(bootWrapParameterResource);

        //Check Stop, il listener è lo switch
        bootwrapSwitchActuatorDescriptor.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                logger.info("Updated Switch Value: {} ", updatedValue);
                bootwrapBatterySensorDescriptor.setActive(updatedValue);
                bootwrapPressureSensorDescriptor.setActive(updatedValue);
            }
        });

        return bootwrapResource;
    }

    private CoapResource createLightTherapyResource(String deviceId) {

        CoapResource lightherapyResource = new CoapResource("lightherapy");

        //Battery
        BatterySensorDescrpitor lightTherapyBatterySensorDescriptor = new BatterySensorDescrpitor();
        BatterySensorResource lightTherapyBatterySensorResource = new BatterySensorResource(deviceId, "battery", lightTherapyBatterySensorDescriptor);
        lightherapyResource.add(lightTherapyBatterySensorResource);

        //Switch
        SwitchActuatorDescriptor lightTherapySwitchActuatorDescriptor = new SwitchActuatorDescriptor();
        SwitchActuatorResource lightTherapySwitchActuatorResource = new SwitchActuatorResource(deviceId, "switch", lightTherapySwitchActuatorDescriptor);
        lightherapyResource.add(lightTherapySwitchActuatorResource);

        //Parameter
        LightTherapyParameterDescriptor lightTherapyParameterDescriptor = new LightTherapyParameterDescriptor(new LightTherapyModel());
        LightTherapyParameterResource lightTherapyParameterResource = new LightTherapyParameterResource(deviceId, "parameter", lightTherapyParameterDescriptor);
        lightherapyResource.add(lightTherapyParameterResource);

        //Check Stop, il listener è lo switch
        lightTherapySwitchActuatorDescriptor.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                logger.info("Updated Switch Value: {} ", updatedValue);
                lightTherapyBatterySensorDescriptor.setActive(updatedValue);
            }
        });

        return lightherapyResource;

    }

    private CoapResource createSaunaResource(String deviceId){

        CoapResource saunaResource = new CoapResource("sauna");

        //Temperature
        TemperatureSensorDescriptor saunaTemperatureSensorDescriptor = new TemperatureSensorDescriptor();
        TemperatureSensorResource saunaTemperatureSensorResource = new TemperatureSensorResource(deviceId, "temperature", saunaTemperatureSensorDescriptor);
        saunaResource.add(saunaTemperatureSensorResource);

        //Humidity
        HumiditySensorDescriptor saunaHumiditySensorDescriptor = new HumiditySensorDescriptor();
        HumiditySensorResource saunaHumiditySensorResource = new HumiditySensorResource(deviceId, "humidity", saunaHumiditySensorDescriptor);
        saunaResource.add(saunaHumiditySensorResource);

        //Battery
        BatterySensorDescrpitor saunaBatterySensorDescriptor = new BatterySensorDescrpitor();
        BatterySensorResource saunaBatterySensorResource = new BatterySensorResource(deviceId, "battery", saunaBatterySensorDescriptor);
        saunaResource.add(saunaBatterySensorResource);

        //Switch
        SwitchActuatorDescriptor saunaSwitchActuatorDescriptor = new SwitchActuatorDescriptor();
        SwitchActuatorResource saunaSwitchActuatorResource = new SwitchActuatorResource(deviceId, "switch", saunaSwitchActuatorDescriptor);
        saunaResource.add(saunaSwitchActuatorResource);

        //Check Stop, il listener è lo switch
        saunaSwitchActuatorDescriptor.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                logger.info("Updated Switch Value: {} ", updatedValue);
                saunaBatterySensorDescriptor.setActive(updatedValue);
            }
        });

        return saunaResource;
    }

    public static void main(String[] args) {

        RecoveryRoomProcess recoveryRoomSmartObjectProcess = new RecoveryRoomProcess();
        logger.info("Starting Coap Server...");
        recoveryRoomSmartObjectProcess.start();
        logger.info("Coap Server Started ! Available resources: ");

        recoveryRoomSmartObjectProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
        });

        registerToCoapResourceDirectory(recoveryRoomSmartObjectProcess.getRoot(),
                "testCoapEndpoint",
                TARGET_LISTENING_IP_ADDRESS,
                TARGET_COAP_PORT
        );

    }
}