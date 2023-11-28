package iot.unimore.fum.recroom;

import iot.unimore.fum.recroom.descriptor.*;
import iot.unimore.fum.recroom.resource.BatterySensorResource;
import iot.unimore.fum.recroom.resource.HumiditySensorResource;
import iot.unimore.fum.recroom.resource.SwitchActuatorResource;
import iot.unimore.fum.recroom.resource.TemperatureSensorResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class SaunaSmartObjectProcess extends CoapServer {

    private final static Logger logger = LoggerFactory.getLogger(SaunaSmartObjectProcess.class);

    public SaunaSmartObjectProcess(int port) {

        super(port);

        //invento deviceId
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());

        //Temperature
        TemperatureSensorDescriptor saunaTemperatureSensorDescriptor = new TemperatureSensorDescriptor();
        TemperatureSensorResource saunaTemperatureSensorResource = new TemperatureSensorResource(deviceId, "temperature-sauna", saunaTemperatureSensorDescriptor);
        this.add(saunaTemperatureSensorResource);

        //Humidity
        HumiditySensorDescriptor saunaHumiditySensorDescriptor = new HumiditySensorDescriptor();
        HumiditySensorResource saunaHumiditySensorResource = new HumiditySensorResource(deviceId, "humidity-sauna", saunaHumiditySensorDescriptor);
        this.add(saunaHumiditySensorResource);

        //Battery
        BatterySensorDescrpitor saunaBatterySensorDescriptor = new BatterySensorDescrpitor();
        BatterySensorResource saunaBatterySensorResource = new BatterySensorResource(deviceId, "battery-sauna", saunaBatterySensorDescriptor);
        this.add(saunaBatterySensorResource);

        //Switch
        SwitchActuatorDescriptor saunaSwitchActuatorDescriptor = new SwitchActuatorDescriptor();
        SwitchActuatorResource saunaSwitchActuatorResource = new SwitchActuatorResource(deviceId, "switch-sauna", saunaSwitchActuatorDescriptor);
        this.add(saunaSwitchActuatorResource);

        //Check Stop, il listener è lo switch
        saunaSwitchActuatorDescriptor.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                logger.info("Updated Switch Value: {} ", updatedValue);
                saunaBatterySensorDescriptor.setActive(updatedValue);
            }
        });

    }

    public static void main(String[] args) {

        SaunaSmartObjectProcess saunaSmartObjectProcess = new SaunaSmartObjectProcess(5685);
        saunaSmartObjectProcess.start();

        logger.info("Coap Server Started ! Available resources: ");

        saunaSmartObjectProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });

    }
}