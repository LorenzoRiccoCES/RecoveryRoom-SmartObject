package iot.unimore.fum.recroom.resource;

import com.google.gson.Gson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import iot.unimore.fum.recroom.descriptor.BootWrapParameterDescriptor;
import iot.unimore.fum.recroom.descriptor.PressureSensorDescriptor;
import iot.unimore.fum.recroom.descriptor.ResourceDataListener;
import iot.unimore.fum.recroom.descriptor.SmartObjectResource;
import iot.unimore.fum.recroom.model.BootWrapModel;
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

public class BootWrapParameterResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(BootWrapParameterResource.class);

    private static final String OBJECT_TITLE = "BootWrapParameter";

    private static final Number VERSION = 0.1;

    private BootWrapParameterDescriptor bootWrapParameterDescriptor;

    private PressureSensorDescriptor pressureSensorDescriptor;

    private BootWrapModel bootWrapModelValue;

    private ObjectMapper objectMapper;

    private String deviceId;

    public BootWrapParameterResource(String deviceId, String name, BootWrapParameterDescriptor bootWrapParameterDescriptor) {

        super(name);

        if (bootWrapParameterDescriptor != null && deviceId != null) {

            this.deviceId = deviceId;

            this.bootWrapParameterDescriptor = bootWrapParameterDescriptor;
            this.bootWrapModelValue = bootWrapParameterDescriptor.loadUpdatedValue();

            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", bootWrapParameterDescriptor.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_P.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));
        } else
            logger.error("Error -> NULL Pressure Reference !");

        this.bootWrapParameterDescriptor.addDataListener(new ResourceDataListener<BootWrapModel>() {
            @Override
            public void onDataChanged(SmartObjectResource<BootWrapModel> resource, BootWrapModel updatedValue) {
                bootWrapModelValue = updatedValue;
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
            operationalModeRecord.setVs(bootWrapModelValue.getOperationalMode());


            SenMLRecord timeOfPressureRecord = new SenMLRecord();
            timeOfPressureRecord.setN("time_of_pressure");
            timeOfPressureRecord.setV(bootWrapModelValue.getTimeOfPressure());

            senMLPack.add(baseRecord);
            senMLPack.add(operationalModeRecord);
            senMLPack.add(timeOfPressureRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        try {
            //If the request specify the MediaType as JSON or JSON+SenML
            if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON) {

                Optional<String> senmlPayload = getJsonSenmlResponse();

                if (senmlPayload.isPresent())
                    exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
                else
                    exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
            }
            //Otherwise respond with the default textplain payload
            else
                exchange.respond(CoAP.ResponseCode.CONTENT, this.objectMapper.writeValueAsBytes(bootWrapModelValue), MediaTypeRegistry.TEXT_PLAIN);

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
                if(receivedPayload.equals(BootWrapModel.DEFAULT_OPERATIONAL_MODE)){
                    this.bootWrapModelValue.setTimeOfPressure(10000);
                    this.bootWrapModelValue.setOperationalMode("bootwrap_default_mode");
                    exchange.respond(CoAP.ResponseCode.CHANGED);
                    changed();
                } else if(receivedPayload.equals(BootWrapModel.ALTERNATIVE_OPERATIONAL_MODE)){
                    this.bootWrapModelValue.setTimeOfPressure(20000);
                    this.bootWrapModelValue.setOperationalMode("bootwrap_alternative_mode");
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

