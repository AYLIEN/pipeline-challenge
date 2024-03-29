# Hello

Hello, thank you for taking this challenge. The goal here is to get a sense of your understanding of designing data pipelines. We are not looking for the perfect solution, as that would definitely require a lot of time and effort. Rather we are looking for cues to the depth of your understanding. We would appreciate it if you would help us in that by providing us with comments and explanations behind what you did so we understand your thought process.

We wish you all the best and good luck!

Note: This repository is archived to avoid pull-requests, since pull-requests will be public. Please zip your folder and send it over to your correspondence at AYLIEN instead.

# Challenge

We are looking to design a high-throughput data streaming pipeline which pulls messages from an HTTP endpoint, transforms them, and sends them for processing to another endpoint. In the end, the service will acknowledge the successful processing of each message. The HTTP endpoints for pulling messages, processing them and acknowledging them are provided, as well as a metrics endpoint so you can see how your pipeline is doing. More details on the API are provided below, and you can find the specification in `messages-openapi.yaml`.

You are supposed to use **Scala (preferred) or Java** programming language. You can use any libraries and frameworks of your choice. Please **provide instructions** for running your project.

In short, these are the steps you need to implement:
1. Pull messages
2. Transform them in a way that is digestible by the `/process` endpoint (example request below)
    - Concatenate title and body into `text`
    - Count characters and words into `characters_count` and `words_count`, respectively
    - Flatten social share counts for each network and sum them up (`facebook_shares: 123`, `twitter_shares: 50`, `reddit_shares: 5` and `sum_shares: 178`)
3. Send them for processing
    - In case of a failure, retry them
4. Acknowledge successfully processed messages

There is no "strict" validation of your transformed messages, we mostly care about how you will integrate such transformation into your code.

**Please note that we expect a streaming pipeline that can run indefinitely and process as many requests as possible until stopped.**

# Tools

There are a set of tools already prepared for you to work with for this task. You can start these tools using [`docker-compose`](https://docs.docker.com/compose/install/).

```
docker-compose up
```

## Messages API

This is a simple HTTP endpoint which you can pull messages from, acknowledge messages and send items for processing to. It is available at `localhost:5000`. The API schema is specified in `messages-openapi.yaml` using OpenAPI specification format.

To receive a maximum of `10` messages at any time, send this request:

```
curl http://localhost:5000/messages?max_messages=10
```

The number of messages returned will be less than or equal to 10 in this case. You may receive an empty array if no messages are available.
The `max_messages` parameter is mandatory and must be provided. For more information on the schema of requests and responses, see `messages-openapi.yaml`.

To send an item for processing:

```
curl -XPOST http://localhost:5000/process -H 'Content-Type: application/json' -d '[{
  "text": "title + body here",
  "words_count": 123,
  "characters_count": 500,
  "keywords": ["as", "they", "were"],
  "facebook_shares": 123,
  "twitter_shares": 50,
  "reddit_shares": 5,
  "sum_shares": 178
}]'
```
Processing items take a while, depending on the batch size (number of items in the array you provide). This time grows logarithmically with batch size. Processing may fail for a batch as a whole.

After an item has been processed **successfully**, you should acknowledge that the message has been processed by sending the IDs you received from the `/messages` endpoint to `/ack`:

```
curl -XPOST http://localhost:5000/ack -H 'Content-Type: application/json' -d "[1, 2, 3]"
```

A message has up to a minute to be acknowledged and is considered "dead" after that time. Dead messages do not show up in latency results but instead, show up in the "dead" count. The response from `/ack` will include a list of dead message IDs.

To see metrics:

```
curl http://localhost:5000/metrics
```

The metrics for Messages API are stored in the Redis instance defined in docker-compose. This redis instance is accessible at `localhost:6379` when using `docker-compose up`. You can use this Redis instance, but avoid writing to these hash keys: `sent`, `last_id` and `acked`.
