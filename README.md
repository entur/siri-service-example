# SIRI Client/Server

Example-implementation of a SIRI Producer/Consumer with simple Publish/Subscribe support. The consumer creates a set of SIRI subscriptions with the publisher, and the producer then posts periodic heartbeats and mock-data to the consumer for each subscription.

Note: This is not intended to be used as-is, but demonstrates the concepts and basic behaviour of a SIRI Publish/Subscribe-service.

## Producer
- Starts a SIRI server (on port `8080`) that accepts SIRI SubscriptionRequest
- Keeps subscriptions alive by posting periodic heartbeats.
- Periodically creates Mock SIRI-data that is published to relevant subscriptions.
- Parameters can be overridden bye specifying them on the commandline when starting up.
    - E.g. adding `--verbose.xml=true` to the startup-command will log all outbound xml

## Consumer
- Starts a SIRI client (on port `8081`).
- Initializes subscriptions towards the above.
- Logs received data/heartbeats
- Simple monitoring that subscriptions are healthy, and restarts subscriptions when necessary.
- Parameters can be overridden bye specifying them on the commandline when starting up.
  - E.g. adding `--verbose.xml=true` to the startup-command will log all inbound xml

## Usage
Multimodule project that can be built from root.

Default properties may be overidden by specifying new values when starting the applications. They should be specified in the format `--<property.name>=<property.value>`. This will then override the property set in `application.properties`.

E.g. to start the applications with verbose xml output, the parameter `verbose.xml` should be set to `true`. This can be specified on the commandline like.
```
java -jar siri-producer/target/siri-producer-0.0.1-SNAPSHOT.jar --verbose.xml=true
```

### Build
```
mvn clean package
```
### Start producer
``` 
java -jar siri-producer/target/siri-producer-0.0.1-SNAPSHOT.jar
```
### Start consumer
``` 
java -jar siri-consumer/target/siri-consumer-0.0.1-SNAPSHOT.jar 
```

