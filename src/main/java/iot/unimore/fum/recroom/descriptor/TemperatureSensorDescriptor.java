package iot.unimore.fum.recroom.descriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class TemperatureSensorDescriptor extends SmartObjectResource<Double>{

    private static Logger logger = LoggerFactory.getLogger(TemperatureSensorDescriptor.class);

    private static final double MIN_TEMPERATURE_VALUE = 70.0;

    private static final double MAX_TEMPERATURE_VALUE = 100.0;

    private static final double MIN_TEMPERATURE_VARIATION = 0.5;

    private static final double MAX_TEMPERATURE_VARIATION = 1.5;

    private static final String LOG_DISPLAY_NAME = "TemperatureSensor";

    public static final long UPDATE_PERIOD = 5000;

    private static final long TASK_DELAY_TIME = 5000;

    private static final String RESOURCE_TYPE = "iot.sensor.temperature";

    private Double updatedValue;

    private Random random;

    private Timer updateTimer = null;

    public TemperatureSensorDescriptor() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init();
    }

    private void init(){

        try{

            this.random = new Random(System.currentTimeMillis()); //seme di randomicità, inizializzazione dei sistemi random
            this.updatedValue = MIN_TEMPERATURE_VALUE + this.random.nextDouble()*(MAX_TEMPERATURE_VALUE - MIN_TEMPERATURE_VALUE); //valore double equamente distribuito tra 0.0 e 1.0

            startPeriodicEventValueUpdateTask();

        }catch (Exception e){
            logger.error("Error initializing the IoT Resource ! Msg: {}", e.getLocalizedMessage());
        }

    }

    //simulazione della generazione dei dati tramite timer
    private void startPeriodicEventValueUpdateTask(){

        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer(); //schedula task periodici in funzione di...
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    //generazione di un numero diverso di temperatura in funzione dei massimi e minimi di vairazione
                    double variation = (MIN_TEMPERATURE_VARIATION + MAX_TEMPERATURE_VARIATION*random.nextDouble()) * (random.nextDouble() > 0.5 ? 1.0 : -1.0); //variazione positiva o negativa in modalità random
                    updatedValue = updatedValue + variation;
                    notifyUpdate(updatedValue); //notifica del cambio di valore

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD); //ritardo di partenza, periodo di aggiornamento

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }

    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedValue;
    } //restituisce il valore aggiornato

    public static void main(String[] args) {

        TemperatureSensorDescriptor temperatureSensorDescriptor = new TemperatureSensorDescriptor();
        logger.info("New {} Resource Created with Id: {} ! {} New Value: {}",
                temperatureSensorDescriptor.getType(),
                temperatureSensorDescriptor.getId(),
                LOG_DISPLAY_NAME,
                temperatureSensorDescriptor.loadUpdatedValue());

        //non volendo andare in polling si ricevono in modo asincrono
        //metodo che mi permette di aggiungere un listener
        temperatureSensorDescriptor.addDataListener(new ResourceDataListener<Double>() {
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