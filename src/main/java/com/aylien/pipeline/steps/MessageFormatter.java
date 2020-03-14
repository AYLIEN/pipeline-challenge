package com.aylien.pipeline.steps;

import org.apache.beam.sdk.transforms.DoFn;
import org.openapitools.client.model.Message;
import org.openapitools.client.model.ProcessItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MessageFormatter extends DoFn<Message, ProcessItem> {

    private static final Logger LOG = LoggerFactory.getLogger(MessageSupplier.class);

    @ProcessElement
    public void processElement(@Element Message message, OutputReceiver<ProcessItem> out) {

        LOG.info("Processing: {}", message);

        Optional<Integer> maybeId = Optional.ofNullable(message.getId());

        maybeId.ifPresent(id -> {
            Optional<String> maybeTitle = Optional.ofNullable(message.getTitle());
            Optional<String> maybeBody = Optional.ofNullable(message.getBody());

            ProcessItem result = new ProcessItem();
            result.setId(id);
            result.setKeywords(message.getKeywords());
            result.setText(String.format("%s\n%s", maybeTitle.orElse("no title"), maybeBody.orElse("no body")));

            result.setCharactersCount(maybeBody.orElse("").length());
            result.setWordsCount(maybeBody.orElse("").split("\\s+").length);

            result.setSumShares(message.getSocialShareCounts().stream()
                    .map(s -> s.getCount()).reduce(0, Integer::sum));
            result.setFacebookShares(message.getSocialShareCounts().stream()
                    .filter(s -> "facebook".equals(s.getNetwork())).map(s -> s.getCount()).reduce(0, Integer::sum));
            result.setTwiterShares(message.getSocialShareCounts().stream()
                    .filter(s -> "twitter".equals(s.getNetwork())).map(s -> s.getCount()).reduce(0, Integer::sum));
            result.setRedditShares(message.getSocialShareCounts().stream()
                    .filter(s -> "reddit".equals(s.getNetwork())).map(s -> s.getCount()).reduce(0, Integer::sum));

            LOG.info("Done: {}", result);
            out.output(result);
        });

        if(!maybeId.isPresent()){
            LOG.warn("Skipping message {} due to missing ID", message);
        }
    }
}
