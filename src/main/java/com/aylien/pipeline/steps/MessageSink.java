package com.aylien.pipeline.steps;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.openapitools.client.model.ProcessItem;
import org.openapitools.client.model.ProcessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageSink extends DoFn<KV<Long,Iterable<ProcessItem>>, Iterable<Integer>> implements APIConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(MessageSink.class);

    private final String endpoint;

    public MessageSink(String endpoint) {
        this.endpoint = endpoint;
    }

    @ProcessElement
    public void processElement(@Element KV<Long,Iterable<ProcessItem>> items, OutputReceiver<Iterable<Integer>> out) {
        LOG.info("Processing batch #{}", items.getKey());
        try {
            ProcessResponse result = getClient(endpoint).processPost(
                    StreamSupport.stream(items.getValue().spliterator(), true).collect(Collectors.toList()));

            Iterable<Integer> ids = StreamSupport.stream(items.getValue().spliterator(), true)
                    .map(i -> i.getId()).collect(Collectors.toList());

            LOG.info("process result: {} for ids: {}", result, ids);

            out.output(ids);
        }catch (Exception e){
            LOG.error("problem processing messages: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
