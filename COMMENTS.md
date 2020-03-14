# Author's Comments

The solution attempts to leverage the state of the art of Dataflow technology by looking for cues on the test requirements. For example, it uses tried and tested techniques for generating sources from API specifications with the modern OpenAPI standard supplied.

The project's core relies on [Apache Beam](https://beam.apache.org/). It is a portable, massive parallel processing based, Dataflow library. The Java programming language provides a mature development suite with a wide range of tooling support. Nonetheless, this technology stack is also available for Pyhton and Go is to suit the team's needs.

## Requirements

The project was built using [JDK 1.8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) with [Maven 3.6.3](https://maven.apache.org/install.html)

## Running the Job

With the docker composition running:

```
$ mvn compile exec:java -Dexec.mainClass=com.aylien.pipeline.MessageProcessor \
     -Dexec.args="--runner=FlinkRunner --batchSize=${requiredSize}" -Pflink-runner
```

The docker composition includes [Apache Flink](https://flink.apache.org/) as a test bed. If your environment has access to AWS or GCloud clients (and the supplied accounts can start Kinesis or Dataflow jobs) you can experiment with the solution on a cloud environment by just selecting a different profile ([see examples](https://beam.apache.org/get-started/quickstart-java/)).