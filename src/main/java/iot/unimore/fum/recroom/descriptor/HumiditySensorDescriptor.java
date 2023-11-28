package iot.unimore.fum.recroom.descriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class HumiditySensorDescriptor extends SmartObjectResource<Double>{
    
    private static Logger logger = LoggerFactory.getLogger(HumiditySensorDescriptor.class);

    private static final double MIN_HUMIDITY_VALUE = 10.0;

    private static final double MAX_HUMIDITY_VALUE = 25.0;

    private static final double MIN_HUMIDITY_VARIATION = 0.1;

    private static final double MAX_HUMIDITY_VARIATION = 1.0;

    private static final String LOG_DISPLAY_NAME = "HumiditySensor";

    public static final long UPDATE_PERIOD = 5000;

    private static final long TASK_DELAY_TIME = 5000;

    private static final String RESOURCE_TYPE = "iot.sensor.humidity";

    private Double updatedValue;

    private Random random;

    private Timer updateTimer = null;

    public HumiditySensorDescriptor() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init();
    }

    private void init(){

        try{

            this.random = new Random(System.currentTimeMillis());
            this.updatedValue = MIN_HUMIDITY_VALUE + this.random.nextDouble()*(MAX_HUMIDITY_VALUE - MIN_HUMIDITY_VALUE);

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

                    double variation = (MIN_HUMIDITY_VARIATION + MAX_HUMIDITY_VARIATION *random.nextDouble()) * (random.nextDouble() > 0.5 ? 1.0 : -1.0);
                    updatedValue = updatedValue + variation;
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

    public static void main(String[] args) {

        HumiditySensorDescriptor humiditySensorDescriptor = new HumiditySensorDescriptor();
        logger.info("New {} Resource Created with Id: {} ! {} New Value: {}",
                humiditySensorDescriptor.getType(),
                humiditySensorDescriptor.getId(),
                LOG_DISPLAY_NAME,
                humiditySensorDescriptor.loadUpdatedValue());

        humiditySensorDescriptor.addDataListener(new ResourceDataListener<Double>() {
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

