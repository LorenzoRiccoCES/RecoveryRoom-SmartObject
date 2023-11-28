package iot.unimore.fum.recroom;

import iot.unimore.fum.recroom.descriptor.*;
import iot.unimore.fum.recroom.model.LightTherapyModel;
import iot.unimore.fum.recroom.resource.BatterySensorResource;
import iot.unimore.fum.recroom.resource.LightTherapyParameterResource;
import iot.unimore.fum.recroom.resource.SwitchActuatorResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LightTherapySmartObjectProcess extends CoapServer {

    private final static Logger logger = LoggerFactory.getLogger(LightTherapySmartObjectProcess.class);

    public LightTherapySmartObjectProcess(int port) {

        super(port);

        //invento deviceId
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());

        //Battery
        BatterySensorDescrpitor lightTherapyBatterySensorDescriptor = new BatterySensorDescrpitor();
        BatterySensorResource lightTherapyBatterySensorResource = new BatterySensorResource(deviceId, "battery-lightherapy", lightTherapyBatterySensorDescriptor);
        this.add(lightTherapyBatterySensorResource);

        //Switch
        SwitchActuatorDescriptor lightTherapySwitchActuatorDescriptor = new SwitchActuatorDescriptor();
        SwitchActuatorResource lightTherapySwitchActuatorResource = new SwitchActuatorResource(deviceId, "switch-lightherapy", lightTherapySwitchActuatorDescriptor);
        this.add(lightTherapySwitchActuatorResource);

        //Parameter
        LightTherapyParameterDescriptor lightTherapyParameterDescriptor = new LightTherapyParameterDescriptor(new LightTherapyModel());
        LightTherapyParameterResource lightTherapyParameterResource = new LightTherapyParameterResource(deviceId, "parameter-lightherapy", lightTherapyParameterDescriptor);
        this.add(lightTherapyParameterResource);

        //Check Stop, il listener è lo switch
        lightTherapySwitchActuatorDescriptor.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                logger.info("Updated Switch Value: {} ", updatedValue);
                lightTherapyBatterySensorDescriptor.setActive(updatedValue);
            }
        });



    }

    public static void main(String[] args) {

        LightTherapySmartObjectProcess lightTherapySmartObjectProcess = new LightTherapySmartObjectProcess(5686);
        lightTherapySmartObjectProcess.start();

        logger.info("Coap Server Started ! Available resources: ");

        lightTherapySmartObjectProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });

    }
}
