
package com.aylien.pipeline;

import com.aylien.pipeline.steps.MessageFormatter;
import com.aylien.pipeline.steps.MessageJobAcknowledge;
import com.aylien.pipeline.steps.MessageSink;
import com.aylien.pipeline.steps.MessageSupplier;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.WithKeys;
import org.openapitools.client.model.AcknowledgeResponse;
import org.openapitools.client.model.ProcessItem;

public class MessageProcessor {

  public interface MessageProcessorOptions extends PipelineOptions {

    @Description("Path of the file to read from")
    @Default.String("http://localhost:5000/")
    String getSource();

    void setSource(String value);

    @Description("Message batch size")
    @Required
    Integer getBatchSize();

    void setBatchSize(Integer value);

    @Description("Path of the file to write to")
    @Default.String("http://localhost:5000/")
    String getSink();

    void setSink(String value);
  }

  static void runMessageBatch(MessageProcessorOptions options) {
    Pipeline p = Pipeline.create(options);

    // Each batch must be uniquely identified by its work package so fine grained computations can be aggregated
    Long jobId = System.currentTimeMillis(); //FIXME: better yet, use UUID (and proper key serializer, etc...)

    p.apply("Fetch message batch", new MessageSupplier(options.getSource(), options.getBatchSize()))
        .apply("Format message", ParDo.of(new MessageFormatter())).setCoder(AvroCoder.of(ProcessItem.class))
        .apply("Assign Correlation ID", WithKeys.of(jobId))
        .apply("Collect items", GroupByKey.create())
        .apply("Process formatted messages", ParDo.of(new MessageSink(options.getSink())))
        .apply("Acknowledge Job completion", ParDo.of(new MessageJobAcknowledge(options.getSink())))
            .setCoder(AvroCoder.of(AcknowledgeResponse.class));

    p.run().waitUntilFinish();
  }

  public static void main(String[] args) {
    MessageProcessorOptions options =
        PipelineOptionsFactory.fromArgs(args).as(MessageProcessorOptions.class);

    runMessageBatch(options);
  }
}
