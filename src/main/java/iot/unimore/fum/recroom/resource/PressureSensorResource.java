package iot.unimore.fum.recroom.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import iot.unimore.fum.recroom.descriptor.PressureSensorDescriptor;
import iot.unimore.fum.recroom.descriptor.ResourceDataListener;
import iot.unimore.fum.recroom.descriptor.SmartObjectResource;
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

public class PressureSensorResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(PressureSensorResource.class);

    private static final String OBJECT_TITLE = "PressureSensor";

    private static final Number SENSOR_VERSION = 0.1;

    //Resource Unit according to SenML Units Registry (http://www.iana.org/assignments/senml/senml.xhtml)
    private String UNIT = "Pa";

    private PressureSensorDescriptor pressureSensorDescriptor;

    private ObjectMapper objectMapper;

    private Double updatedPressureValue = 0.0;

    private String deviceId;

    public PressureSensorResource(String deviceId, String name, PressureSensorDescriptor pressureSensorDescriptor){

        super(name);

        if(pressureSensorDescriptor != null && deviceId != null){

            this.deviceId = deviceId;

            this.pressureSensorDescriptor = pressureSensorDescriptor;

            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", pressureSensorDescriptor.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_S.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));

        }
        else
            logger.error("Error -> NULL Pressure Descriptor !");

        this.pressureSensorDescriptor.addDataListener(new ResourceDataListener<Double>() {
            @Override
            public void onDataChanged(SmartObjectResource<Double> resource, Double updatedValue) {
                updatedPressureValue = updatedValue;
                changed();
            }
        });
    }

    private Optional<String> getJsonSenmlResponse(){

        try{

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s:%s", this.deviceId, this.getName()));
            senMLRecord.setBver(SENSOR_VERSION);
            senMLRecord.setU(UNIT);
            senMLRecord.setV(updatedPressureValue);
            senMLRecord.setT(System.currentTimeMillis());

            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        // the Max-Age value should match the update interval
        exchange.setMaxAge(PressureSensorDescriptor.UPDATE_PERIOD);

        //If the request specify the MediaType as JSON or JSON+SenML
        if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
                exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON){

            Optional<String> senmlPayload = getJsonSenmlResponse();

            if(senmlPayload.isPresent())
                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
        //Otherwise respond with the default textplain payload
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(updatedPressureValue), MediaTypeRegistry.TEXT_PLAIN);

    }

}
