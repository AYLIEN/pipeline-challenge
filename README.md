# Hello

Hello, thank you for taking this challenge. The goal here is to get a sense of your understanding of designing data pipelines. We are not looking for the perfect solution as that would definitely require a lot of time and effort, rather we are looking for cues to the depth of your understanding. We would appreciate it if you would help us in that by providing us with comments and explanations behind what you did so we understand your thought process.

Wish you all the best and good luck!

# Challenge

We are looking to design a high-throughput data pipeline which pulls messages from an HTTP endpoint and indexes them in Elasticsearch for further searching. The HTTP endpoint for pulling messages is provided, as well as the tools necessary (Elasticsearch + Kibana). You do not need to provide an API to search over elasticsearch, the pipeline is enough and we will be looking at the data directly, but you should consider how to effectively index the documents so that they can be searched. After receiving messages and indexing them, you have to "acknowledge" those requests by sending a request to the Messages API with the message ID that has been processed successfully. The messages API provides metrics on the time it took to process each message.


The language or framework your work with is up to you entirely. Please provide instructions for running your project. We will run it along the messages API so we can see your pipeline processing messages and the latency metrics.

# Tools

There are a set of tools already prepared for you to work with for this task. You can start these tools using [`docker-compose`](https://docs.docker.com/compose/install/).

```
docker-compose up
```

## Elasticsearch & Kibana

Documents will be indexed in Elasticsearch. The instance is accessible at `localhost:9200` after running docker-compose. Kibana is a dashboard on top of Elasticsearch that helps you with interacting with the instance through a graphical user interface. You do not have to use Kibana but it is there to make it easier for you to debug and troubleshoot. Kibana is accessible at `localhost:5601`.

Useful resources:

- [Elasticsearch Clients Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started.html)
- [Elasticsearch documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started.html)
- [Elasticsearch REST API reference](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html)

You will be mostly working with the [Document APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs.html), and maybe [Index APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices.html). For troubleshooting you may find the [cat APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat.html) useful. You can use all of these APIs from the Kibana dashboard's Dev Tools section (look for the wrench icon on the sidebar).

## Messages API

This is a simple HTTP endpoint which you can pull messages from available at `localhost:5000`. The API schema is specified in `messages-openapi.yaml` using OpenAPI specification format.

To receive a maximum of `10` messages at any time, send this request:

```
curl http://localhost:5000/messages?max_messages=10
```

The number of messages returned will be less than or equal to `10` in this case. You may receive an empty array if no messages are available.

The `max_messages` parameter is mandatory and must be provided. For more information on the schema of requests and response see `messages-openapi.yaml`.

To acknowledge messages:

```
curl -XPOST http://localhost:5000/ack -H 'Content-Type: application/json' -d "[1, 2, 3]"
```

A message has up to a minute to be acknowledged, after that the message is considered "dead". Dead messages do not show up in latency results but instead show up in the "dead" count.

To see metrics:

```
curl http://localhost:5000/metrics
```

The metrics for Messages API are stored in the redis instance defined in docker-compose. This redis instance is accessible at `localhost:6379` when using `docker-compose up`. You are free to use this redis instance, but avoid writing to these hash keys: `sent`, `last_id` and `acked`.
