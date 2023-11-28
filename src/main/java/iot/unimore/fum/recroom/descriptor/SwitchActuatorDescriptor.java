package iot.unimore.fum.recroom.descriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public class SwitchActuatorDescriptor extends SmartObjectResource<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(SwitchActuatorDescriptor.class);

    private static final String LOG_DISPLAY_NAME = "SwitchActuator";

    private static final String RESOURCE_TYPE = "iot.actuator.switch";

    private Boolean isActive;

    public SwitchActuatorDescriptor() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        this.isActive = false;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
        notifyUpdate(isActive);
    }

    @Override
    public Boolean loadUpdatedValue() {
        return this.isActive;
    }

    public static void main(String[] args) {

        SwitchActuatorDescriptor switchResource = new SwitchActuatorDescriptor();

        logger.info("New {} Resource Created with Id: {} ! {} New Value: {}",
                switchResource.getType(),
                switchResource.getId(),
                LOG_DISPLAY_NAME,
                switchResource.loadUpdatedValue());

        switchResource.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }


        });

    }
}

