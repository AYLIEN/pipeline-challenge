package com.aylien.pipeline.steps;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.openapitools.client.model.Message;
import org.openapitools.client.model.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MessageSupplier extends PTransform<PBegin, PCollection<Message>> implements APIConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(MessageSupplier.class);

    private final String endpoint;
    private final Integer batchSize;


    public MessageSupplier(String endpoint, Integer batchSize) {
        this.endpoint = endpoint;
        this.batchSize = batchSize;

        LOG.debug("Created Message Supplier for {} with batch size {}", endpoint, batchSize);
    }

    @Override
    public PCollection<Message> expand(PBegin input) {
        LOG.info("Fetching batch...");

        List<Message> found = new LinkedList<>();
        try  {
            Messages batch = getClient(endpoint).messagesGet(this.batchSize);

            // AVRO Codec does not handle optional arrays
            found.addAll(Optional.of(batch.getMessages().stream().map(m -> {
                if(m.getSocialShareCounts() == null){
                    m.setSocialShareCounts(Collections.emptyList());
                }
                return m;
            }).collect(Collectors.toList())).orElse(Collections.emptyList()));

            LOG.info("{} messages found", found.size());
        }catch (Exception e){
            LOG.error("problem fetching messages: {}", e.getMessage());
            e.printStackTrace();
        }

        return Create.of(found).withCoder(AvroCoder.of(Message.class)).expand(input);
    }
}
