package com.aylien.pipeline.steps;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.openapitools.client.ApiClient;
import org.openapitools.client.api.DefaultApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public interface APIConsumer {

    Logger LOG = LoggerFactory.getLogger(APIConsumer.class);

    Integer MAX_RETRIES = 3;

    Integer BACKOFF_WINDOW = 2;

    default DefaultApi getClient(String endpoint){
        ApiClient baseClient = new ApiClient();
        baseClient.setBasePath(endpoint);

        // FIXME: fine tune the retry mechanism
        baseClient.setHttpClient(new OkHttpClient.Builder().retryOnConnectionFailure(true).addInterceptor(chain -> {
            Request request = chain.request();

            Response response = chain.proceed(request);

            int tries = 1;

            while (!response.isSuccessful() && tries <= MAX_RETRIES) {

                LOG.info("Request failed {} times for {} ", tries, endpoint);

                tries += 1;

                try {
                    TimeUnit.SECONDS.sleep((1 + tries) * BACKOFF_WINDOW);
                } catch (InterruptedException e) {
                    LOG.error("Backoff period interrupted for {}", endpoint);
                    e.printStackTrace();
                }

                response = chain.proceed(request);
            }

            // otherwise just pass the original response on
            return response;
        }).build());

        return new DefaultApi(baseClient);
    }
}
