package com.aylien.pipeline.steps;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.openapitools.client.model.AcknowledgeResponse;
import org.openapitools.client.model.ProcessItem;
import org.openapitools.client.model.ProcessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageJobAcknowledge extends DoFn<Iterable<Integer>, AcknowledgeResponse> implements APIConsumer{

    private static final Logger LOG = LoggerFactory.getLogger(MessageJobAcknowledge.class);

    private final String endpoint;

    public MessageJobAcknowledge(String endpoint) {
        this.endpoint = endpoint;
    }

    @ProcessElement
    public void processElement(@Element Iterable<Integer> ids, OutputReceiver<AcknowledgeResponse> out) {
        LOG.info("Sending acknowledge massage for {}", ids);
        try {
            AcknowledgeResponse response = getClient(endpoint)
                    .ackPost(StreamSupport.stream(ids.spliterator(), true).collect(Collectors.toList()));

            LOG.info("Ack response: {}", response);
            out.output(response);
        }catch (Exception e){
            LOG.error("problem processing acknowledgement: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
