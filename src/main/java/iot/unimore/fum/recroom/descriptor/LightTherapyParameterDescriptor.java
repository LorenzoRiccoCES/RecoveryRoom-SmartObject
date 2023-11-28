package iot.unimore.fum.recroom.descriptor;

import iot.unimore.fum.recroom.model.LightTherapyModel;

import java.util.UUID;

public class LightTherapyParameterDescriptor extends SmartObjectResource<LightTherapyModel>{

    private LightTherapyModel lightTherapyModel;

    private static final String RESOURCE_TYPE = "iot.parameter.configuration";

    public LightTherapyParameterDescriptor(LightTherapyModel lightTherapyModel) {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        this.lightTherapyModel = lightTherapyModel;
    }

    @Override
    public LightTherapyModel loadUpdatedValue() {
        return this.lightTherapyModel;
    }

    public LightTherapyModel getLightTherapyModel() {
        return lightTherapyModel;
    }

    public void setLightTherapyModel(LightTherapyModel lightTherapyModel) {
        this.lightTherapyModel = lightTherapyModel;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LightTherapyParameterDescriptor{");
        sb.append("LightTherapyParameterDescriptor=").append(lightTherapyModel);
        sb.append('}');
        return sb.toString();

    }


}
