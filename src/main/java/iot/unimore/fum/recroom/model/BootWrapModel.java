package iot.unimore.fum.recroom.model;

public class BootWrapModel {

    public static String DEFAULT_OPERATIONAL_MODE = "bootwrap_default_mode";

    public static String ALTERNATIVE_OPERATIONAL_MODE = "bootwrap_alternative_mode";


    private double timeOfPressure = 10000.0;

    private String operationalMode = DEFAULT_OPERATIONAL_MODE;

    public BootWrapModel() {
    }

    public BootWrapModel(double levelOfPressure, long timeOfPressure, String operationalMode) {
        this.timeOfPressure = timeOfPressure;
        this.operationalMode = operationalMode;
    }

    public double getTimeOfPressure() {
        return timeOfPressure;
    }

    public void setTimeOfPressure(long timeOfPressure) {
        this.timeOfPressure = timeOfPressure;
    }

    public String getOperationalMode() {
        return operationalMode;
    }

    public void setOperationalMode(String operationalMode) {this.operationalMode = operationalMode; }

    @Override
    public String toString() {
        return "BootWrapModel{" +
                ", timeOfPressure=" + timeOfPressure +
                ", operationalMode='" + operationalMode + '\'' +
                '}';
    }
}
