package iot.unimore.fum.recroom.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import iot.unimore.fum.recroom.descriptor.LightTherapyParameterDescriptor;
import iot.unimore.fum.recroom.descriptor.PressureSensorDescriptor;
import iot.unimore.fum.recroom.descriptor.ResourceDataListener;
import iot.unimore.fum.recroom.descriptor.SmartObjectResource;
import iot.unimore.fum.recroom.model.BootWrapModel;
import iot.unimore.fum.recroom.model.LightTherapyModel;
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

public class LightTherapyParameterResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(LightTherapyParameterResource.class);

    private static final String OBJECT_TITLE = "LightTherapyParameter";

    private static final Number VERSION = 0.1;

    private String UNIT = "lx";

    private LightTherapyParameterDescriptor lightTherapyParameterDescriptor;

    private LightTherapyModel lightTherapyModelValue;

    private ObjectMapper objectMapper;

    private String deviceId;

    public LightTherapyParameterResource(String deviceId, String name, LightTherapyParameterDescriptor lightTherapyParameterDescriptor) {

        super(name);

        if (lightTherapyParameterDescriptor != null && deviceId != null) {

            this.deviceId = deviceId;

            this.lightTherapyParameterDescriptor = lightTherapyParameterDescriptor;
            this.lightTherapyModelValue = lightTherapyParameterDescriptor.loadUpdatedValue();

            //Jackson Object Mapper + Ignore Null Fields in order to properly generate the SenML Payload
            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", lightTherapyParameterDescriptor.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_P.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));
        } else
            logger.error("Error -> NULL Raw Reference !");

        this.lightTherapyParameterDescriptor.addDataListener(new ResourceDataListener<LightTherapyModel>() {
            @Override
            public void onDataChanged(SmartObjectResource<LightTherapyModel> resource, LightTherapyModel updatedValue) {
                lightTherapyModelValue = updatedValue;
                changed();
            }
        });

    }

    private Optional<String> getJsonSenmlResponse() {

        try {

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord baseRecord = new SenMLRecord();
            baseRecord.setBn(String.format("%s:%s", this.deviceId, this.getName()));
            baseRecord.setBver(VERSION);

            SenMLRecord operationalModeRecord = new SenMLRecord();
            operationalModeRecord.setN("operational_mode");
            operationalModeRecord.setVs(lightTherapyModelValue.getOperationalMode());

            SenMLRecord lenghtOfLightRecord = new SenMLRecord();
            lenghtOfLightRecord.setN("lenght_of_light");
            lenghtOfLightRecord.setU(UNIT);
            lenghtOfLightRecord.setV(lightTherapyModelValue.getLenghtOfLight());

            SenMLRecord timeOfLightRecord = new SenMLRecord();
            timeOfLightRecord.setN("time_of_light");
            timeOfLightRecord.setV(lightTherapyModelValue.getTimeOfLight());

            senMLPack.add(baseRecord);
            senMLPack.add(operationalModeRecord);
            senMLPack.add(lenghtOfLightRecord);
            senMLPack.add(timeOfLightRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        try {

            if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON) {

                Optional<String> senmlPayload = getJsonSenmlResponse();

                if (senmlPayload.isPresent())
                    exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
                else
                    exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
            }

            else
                exchange.respond(CoAP.ResponseCode.CONTENT, this.objectMapper.writeValueAsBytes(lightTherapyModelValue), MediaTypeRegistry.TEXT_PLAIN);

        } catch (Exception e) {
            e.printStackTrace();
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void handlePUT(CoapExchange exchange) {

        try{
            String receivedPayload = new String(exchange.getRequestPayload());

            if(receivedPayload != null && receivedPayload.length()>0) {
                logger.info("PUT REQUEST ACCEPTED.... Processing... ");
                if(receivedPayload.equals(LightTherapyModel.DEFAULT_OPERATIONAL_MODE)){
                    this.lightTherapyModelValue.setOperationalMode("light_default_mode");
                    this.lightTherapyModelValue.setTimeOfLight(10000);
                    this.lightTherapyModelValue.setLenghtOfLight(660);
                    exchange.respond(CoAP.ResponseCode.CHANGED);
                    changed();
                } else if(receivedPayload.equals(BootWrapModel.ALTERNATIVE_OPERATIONAL_MODE)){
                    this.lightTherapyModelValue.setOperationalMode("light_alternative_mode");
                    this.lightTherapyModelValue.setTimeOfLight(20000);
                    this.lightTherapyModelValue.setLenghtOfLight(760);
                    exchange.respond(CoAP.ResponseCode.CHANGED);
                    changed();
                } else
                    exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);


        } catch(Exception e){
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }
}

