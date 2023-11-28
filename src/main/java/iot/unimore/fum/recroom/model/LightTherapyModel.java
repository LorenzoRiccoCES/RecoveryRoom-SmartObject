package iot.unimore.fum.recroom.model;

public class LightTherapyModel {

    public static String DEFAULT_OPERATIONAL_MODE = "light_default_mode";

    public static String ALTERNATIVE_OPERATIONAL_MODE = "light_alternative_mode";

    private double lenghtOfLight = 660;

    private double timeOfLight = 10000.0;

    private String operationalMode = DEFAULT_OPERATIONAL_MODE;

    public LightTherapyModel() {
    }

    public LightTherapyModel(double levelOfPressure, long timeOfPressure, String operationalMode) {
        this.lenghtOfLight = levelOfPressure;
        this.timeOfLight = timeOfPressure;
        this.operationalMode = operationalMode;
    }

    public String getOperationalMode() {
        return operationalMode;
    }

    public void setOperationalMode(String operationalMode) {this.operationalMode = operationalMode;}

    public double getLenghtOfLight() {
        return lenghtOfLight;
    }

    public void setLenghtOfLight(double lenghtOfLight) {
        this.lenghtOfLight = lenghtOfLight;
    }

    public double getTimeOfLight() {
        return timeOfLight;
    }

    public void setTimeOfLight(double timeOfLight) {
        this.timeOfLight = timeOfLight;
    }

    @Override
    public String toString() {
        return "BootWrapModel{" +
                "levelOfPressure=" + lenghtOfLight +
                ", timeOfPressure=" + timeOfLight +
                ", operationalMode='" + operationalMode + '\'' +
                '}';
    }
}

