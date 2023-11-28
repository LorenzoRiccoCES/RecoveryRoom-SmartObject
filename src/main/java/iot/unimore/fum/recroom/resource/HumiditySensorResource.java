package iot.unimore.fum.recroom.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import iot.unimore.fum.recroom.descriptor.HumiditySensorDescriptor;
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

public class HumiditySensorResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(HumiditySensorResource.class);

    private static final String OBJECT_TITLE = "HumiditySensor";

    private static final Number SENSOR_VERSION = 0.1;

    //Resource Unit according to SenML Units Registry (http://www.iana.org/assignments/senml/senml.xhtml)
    private String UNIT = "%RH";

    private HumiditySensorDescriptor humiditySensorDescriptor;

    private ObjectMapper objectMapper;

    private Double updatedHumidityValue = 0.0;

    private String deviceId;

    public HumiditySensorResource(String deviceId, String name, HumiditySensorDescriptor humiditySensorDescriptor){

        super(name);

        if(humiditySensorDescriptor != null && deviceId != null){

            this.deviceId = deviceId;

            this.humiditySensorDescriptor = humiditySensorDescriptor;

            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", humiditySensorDescriptor.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_S.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));
        }
        else
            logger.error("Error -> NULL Humidity Descriptor !");

        this.humiditySensorDescriptor.addDataListener(new ResourceDataListener<Double>() {
            @Override
            public void onDataChanged(SmartObjectResource<Double> resource, Double updatedValue) {
                updatedHumidityValue = updatedValue;
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
            senMLRecord.setV(updatedHumidityValue);
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
        exchange.setMaxAge(HumiditySensorDescriptor.UPDATE_PERIOD);

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
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(updatedHumidityValue), MediaTypeRegistry.TEXT_PLAIN);

    }

}
