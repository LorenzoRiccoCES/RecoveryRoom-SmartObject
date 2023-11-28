package iot.unimore.fum.recroom.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import iot.unimore.fum.recroom.descriptor.ResourceDataListener;
import iot.unimore.fum.recroom.descriptor.SmartObjectResource;
import iot.unimore.fum.recroom.descriptor.TemperatureSensorDescriptor;
import iot.unimore.fum.recroom.utils.CoreInterfaces;
import iot.unimore.fum.recroom.utils.SenMLPack;
import iot.unimore.fum.recroom.utils.SenMLRecord;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class TemperatureSensorResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(TemperatureSensorResource.class);

    private static final String OBJECT_TITLE = "TemperatureSensor";

    private static final Number SENSOR_VERSION = 0.1;

    private String UNIT = "Cel";

    private TemperatureSensorDescriptor temperatureSensorDescriptor;

    private ObjectMapper objectMapper; //per lavorare in SenML + Json

    private Double updatedTemperatureValue = 0.0;

    private String deviceId;

    public TemperatureSensorResource(String deviceId, String name, TemperatureSensorDescriptor temperatureSensorDescriptor) {

        super(name);

        if (temperatureSensorDescriptor != null && deviceId != null) {

            this.deviceId = deviceId;

            this.temperatureSensorDescriptor = temperatureSensorDescriptor;


            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); //non include i valori nulli

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", temperatureSensorDescriptor.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_S.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));
        } else
            logger.error("Error -> NULL Raw Reference !");

        //la risorsa coap è in ascolto della risorsa originale, notifica quando arriva valore nuovo e me lo salvo
        this.temperatureSensorDescriptor.addDataListener(new ResourceDataListener<Double>() {
            @Override
            public void onDataChanged(SmartObjectResource<Double> resource, Double updatedValue) {
                updatedTemperatureValue = updatedValue;
                changed();
            }
        });

    }

    private Optional<String> getJsonSenmlResponse() {

        try {

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s:%s", this.deviceId, this.getName()));
            senMLRecord.setBver(SENSOR_VERSION);
            senMLRecord.setU(UNIT);
            senMLRecord.setV(updatedTemperatureValue);
            senMLRecord.setT(System.currentTimeMillis());

            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        //option che dice al client quando scadrà il valore della risorsa, quando non è più un dato fresco
        exchange.setMaxAge(TemperatureSensorDescriptor.UPDATE_PERIOD);

        if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
                exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON) {

            Optional<String> senmlPayload = getJsonSenmlResponse();

            if (senmlPayload.isPresent())
                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
        //Otherwise respond with the default textplain payload
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(updatedTemperatureValue), MediaTypeRegistry.TEXT_PLAIN);

    }

}

