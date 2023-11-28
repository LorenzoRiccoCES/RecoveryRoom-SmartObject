package iot.unimore.fum.recroom.descriptor;

import iot.unimore.fum.recroom.model.BootWrapModel;
import iot.unimore.fum.recroom.model.LightTherapyModel;

import java.util.UUID;

public class BootWrapParameterDescriptor extends SmartObjectResource<BootWrapModel>{
    
    private BootWrapModel bootWrapModel;

    private static final String RESOURCE_TYPE = "iot.parameter.configuration";

    public BootWrapParameterDescriptor(BootWrapModel bootWrapModel) {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        this.bootWrapModel = bootWrapModel;
    }

    @Override
    public BootWrapModel loadUpdatedValue() {
        return this.bootWrapModel;
    }

    public BootWrapModel getBootWrapModel() {
        return bootWrapModel;
    }

    public void setBootWrapModel(BootWrapModel bootWrapModel) {
        this.bootWrapModel = bootWrapModel;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BootWrapParameterDescriptor{");
        sb.append("BootWrapParameterDescriptor=").append(bootWrapModel);
        sb.append('}');
        return sb.toString();

    }


}
