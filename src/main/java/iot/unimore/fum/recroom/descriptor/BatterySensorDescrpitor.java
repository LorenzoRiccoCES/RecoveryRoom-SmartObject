package iot.unimore.fum.recroom.descriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BatterySensorDescrpitor extends SmartObjectResource<Double>{

    private static final Logger logger = LoggerFactory.getLogger(BatterySensorDescrpitor.class);

    private static final double MIN_BATTERY_LEVEL = 20.0;

    private static final double MAX_BATTERY_LEVEL = 99.0;

    private static final double MIN_BATTERY_LEVEL_CONSUMPTION = 1.0;

    private static final double MAX_BATTERY_LEVEL_CONSUMPTION = 5.0;

    private static final long UPDATE_PERIOD = 5000;

    private static final long TASK_DELAY_TIME = 5000;

    public static final String RESOURCE_TYPE = "iot.sensor.battery";

    private static final String LOG_DISPLAY_NAME = "EnergySensor";

    private double updatedBatteryLevel;

    private Random random = null;

    private Timer updateTimer = null;

    private boolean isActive = true;

    public BatterySensorDescrpitor() {
        super(UUID.randomUUID().toString(), BatterySensorDescrpitor.RESOURCE_TYPE);
        init();
    }

    public BatterySensorDescrpitor(String id, String type) {
        super(id, type);
        init();
    }


    private void init(){

        try{

            this.random = new Random(System.currentTimeMillis());
            this.updatedBatteryLevel = MIN_BATTERY_LEVEL + this.random.nextDouble()*(MAX_BATTERY_LEVEL - MIN_BATTERY_LEVEL);

            startPeriodicEventValueUpdateTask();

        }catch (Exception e){
            logger.error("Error init Battery Resource Object ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void startPeriodicEventValueUpdateTask(){

        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.random = new Random(System.currentTimeMillis());
            this.updateTimer = new Timer();

            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(isActive){ //sensore consuma se è attivo
                        updatedBatteryLevel = updatedBatteryLevel - (MIN_BATTERY_LEVEL_CONSUMPTION + MAX_BATTERY_LEVEL_CONSUMPTION * random.nextDouble());
                        if (updatedBatteryLevel<=20.0)
                            updatedBatteryLevel = MAX_BATTERY_LEVEL;
                    }
                    else
                        updatedBatteryLevel = 99.0;

                    notifyUpdate(updatedBatteryLevel);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedBatteryLevel;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public static void main(String[] args) {

        BatterySensorDescrpitor batterySensorDescrpitor = new BatterySensorDescrpitor();
        logger.info("New {} Resource Created with Id: {} ! {} Battery Level: {}",
                batterySensorDescrpitor.getType(),
                batterySensorDescrpitor.getId(),
                LOG_DISPLAY_NAME,
                batterySensorDescrpitor.loadUpdatedValue());

        //Add Resource Listener
        batterySensorDescrpitor.addDataListener(new ResourceDataListener<Double>() {
            @Override
            public void onDataChanged(SmartObjectResource<Double> resource, Double updatedValue) {
                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Battery Level Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }
}
