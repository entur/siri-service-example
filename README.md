# SIRI Client/Server

Example-implementation of a SIRI Producer/Consumer with simple Publish/Subscribe support. The consumer creates a set of SIRI subscriptions with the publisher, and the producer then posts periodic heartbeats and mock-data to the consumer for each subscription.

Note: This is not intended to be used as-is, but demonstrates the concepts and basic behaviour of a SIRI Publish/Subscribe-service.

## Producer
- Starts a SIRI server (on port `8080`) that accepts SIRI SubscriptionRequest
- Keeps subscriptions alive by posting periodic heartbeats.
- Periodically creates Mock SIRI-data that is published to relevant subscriptions.
- Set `verbose.xml=true` in _application.properties_ to print all outbound XML

## Consumer
- Starts a SIRI client (on port `8081`).
- Initializes subscriptions towards the above.
- Logs received data/heartbeats
- Simple monitoring that subscriptions are healthy, and restarts subscriptions when necessary.
- Set `verbose.xml=true` in _application.properties_ to print all in~~~~bound XML

## Usage
Multimodule project that can be built from root.

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

