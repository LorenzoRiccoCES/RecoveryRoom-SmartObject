package iot.unimore.fum.recroom.descriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PressureSensorDescriptor extends SmartObjectResource<Double> {

    private static Logger logger = LoggerFactory.getLogger(PressureSensorDescriptor.class);

    private static final double MIN_PRESSURE_VALUE = 70.0;

    private static final double MAX_PRESSURE_VALUE = 100.0;

    private static final double MIN_PRESSURE_VARIATION = 0.5;

    private static final double MAX_PRESSURE_VARIATION = 1.5;

    private static final String LOG_DISPLAY_NAME = "PressureSensor";

    //Ms associated to data update
    public static final long UPDATE_PERIOD = 6000;

    private static final long TASK_DELAY_TIME = 6000;

    private static final String RESOURCE_TYPE = "iot.sensor.pressure";

    private Double updatedValue;

    private Random random;

    private Timer updateTimer = null;

    private boolean isActive = true;

    public PressureSensorDescriptor() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init();
    }

    private void init(){

        try{

            this.random = new Random(System.currentTimeMillis());
            this.updatedValue = MIN_PRESSURE_VALUE + this.random.nextDouble()*(MAX_PRESSURE_VALUE - MIN_PRESSURE_VALUE);

            startPeriodicEventValueUpdateTask();

        }catch (Exception e){
            logger.error("Error initializing the IoT Resource ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void startPeriodicEventValueUpdateTask(){

        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                if(isActive){
                    double variation = (MIN_PRESSURE_VARIATION + MAX_PRESSURE_VARIATION *random.nextDouble()) * (random.nextDouble() > 0.5 ? 1.0 : -1.0);
                    updatedValue = updatedValue + variation;
                    }
                    notifyUpdate(updatedValue);
                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }

    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedValue;
    }

    public void setUpdatedValue(double pressureValue){
        updatedValue = pressureValue;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public static void main(String[] args) {

        PressureSensorDescriptor pressureSensorDescriptor = new PressureSensorDescriptor();
        logger.info("New {} Resource Created with Id: {} ! {} New Value: {}",
                pressureSensorDescriptor.getType(),
                pressureSensorDescriptor.getId(),
                LOG_DISPLAY_NAME,
                pressureSensorDescriptor.loadUpdatedValue());

        pressureSensorDescriptor.addDataListener(new ResourceDataListener<Double>() {
            @Override
            public void onDataChanged(SmartObjectResource<Double> resource, Double updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }

}
