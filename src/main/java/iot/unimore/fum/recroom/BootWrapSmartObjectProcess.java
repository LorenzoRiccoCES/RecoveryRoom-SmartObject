package iot.unimore.fum.recroom;

import iot.unimore.fum.recroom.descriptor.*;
import iot.unimore.fum.recroom.model.BootWrapModel;
import iot.unimore.fum.recroom.resource.*;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BootWrapSmartObjectProcess extends CoapServer {

    private final static Logger logger = LoggerFactory.getLogger(BootWrapSmartObjectProcess.class);

    public BootWrapSmartObjectProcess(int port) {

        super(port);

        //deviceId
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());

        //Pressure
        PressureSensorDescriptor bootwrapPressureSensorDescriptor = new PressureSensorDescriptor();
        PressureSensorResource bootwrapPressureSensorResource = new PressureSensorResource(deviceId, "pressure-bootwrap", bootwrapPressureSensorDescriptor);
        this.add(bootwrapPressureSensorResource);

        //Battery
        BatterySensorDescrpitor bootwrapBatterySensorDescriptor = new BatterySensorDescrpitor();
        BatterySensorResource bootwrapBatterySensorResource = new BatterySensorResource(deviceId, "battery-bootwrap", bootwrapBatterySensorDescriptor);
        this.add(bootwrapBatterySensorResource);

        //Switch
        SwitchActuatorDescriptor bootwrapSwitchActuatorDescriptor = new SwitchActuatorDescriptor();
        SwitchActuatorResource bootwrapSwitchActuatorResource = new SwitchActuatorResource(deviceId, "switch-bootwrap", bootwrapSwitchActuatorDescriptor);
        this.add(bootwrapSwitchActuatorResource);

        //BootWrapParameter
        BootWrapParameterDescriptor bootWrapParameterDescriptor = new BootWrapParameterDescriptor(new BootWrapModel());
        BootWrapParameterResource bootWrapParameterResource = new BootWrapParameterResource(deviceId, "parameter-bootwrap", bootWrapParameterDescriptor);
        this.add(bootWrapParameterResource);

        //Check Stop, il listener è lo switch
        bootwrapSwitchActuatorDescriptor.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                logger.info("Updated Switch Value: {} ", updatedValue);
                bootwrapBatterySensorDescriptor.setActive(updatedValue);
                bootwrapPressureSensorDescriptor.setActive(updatedValue);
            }
        });

    }

    public static void main(String[] args) {

        BootWrapSmartObjectProcess bootWrapSmartObjectProcess = new BootWrapSmartObjectProcess(5687);
        bootWrapSmartObjectProcess.start();

        logger.info("Coap Server Started ! Available resources: ");

        bootWrapSmartObjectProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });

    }
}
