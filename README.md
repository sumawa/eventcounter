# Event Counter
###

### Design and execution points

- The event source process is run using netcat
```
chmod +x blackbox.macosx
./blackbox.macosx | nc -lk 9999 
``` 

It emits data like

```
{ "event_type": "foo", "data": "amet", "timestamp": 1627008128 }
{ "R= ??c?z?
{ "event_type": "bar", "data": "ipsum", "timestamp": 1627008128 }
{ "event_type": "bar", "data": "lorem", "timestamp": 1627008128 }
{ "event_type": "baz", "data": "sit", "timestamp": 1627008128 }
```  

- Goal is to compute windowed word count grouped by event_type, something like this

```
http://localhost:53248/eventData

[
  {
    "eventType" : "bar",
    "count" : 60
  },
  {
    "eventType" : "foo",
    "count" : 61
  },
  {
    "eventType" : "baz",
    "count" : 55
  }
]
```

- The event counter application is built in the form of assembly and executed with run
```
sbt "project eventCounter" clean assembly

./run_eventcount.sh
```
- The output is generated in the app.log
```
tail app.log
```
- It starts 
  * an http4s BlazeServer serving http requests 
  * and internally a scheduled stream emitter which opens client TCP socket and read chunks from the server.

#### Note:
- This arrangement is just for demo, in the ideal world an http service and stream processor will be independent components run and managed separately. 

#### About com.sa.events.domain.EventDataService
- The streaming logic is defined in the form of 
  * Event source: Reads a certain amount of chunks from server and group data by event_type
  * Event processing pipe: processing (aggregating grouped count into a state)
  * Event persist pipe: updating current aggregate into DB 
  
### End points exposed ###
#### Event Counter Endpoint Websocket :

- A websocket end point that refresh every 10 seconds (defined in com.sa.events.api.EventWSRoutes)

```
ws://0.0.0.0:53248/eventData/ws
```
An example HTML page invoking this websocket end point is following directory.
To test this endpoint, load this file directly in the browser.
```
./front/public/TestWS.html
```
#### Event Counter HTTP Endpoint 
For the same event data, a regular HTTP GET
```
http://localhost:53248/eventData

[
  {
    "eventType" : "bar",
    "count" : 60
  },
  {
    "eventType" : "foo",
    "count" : 61
  },
  {
    "eventType" : "baz",
    "count" : 55
  }
]
```
### Shutting down
```
./kill_eventcount.sh
```

#### Libraries used
* http4s: a Type safe, functional, streaming HTTP for Scala (Http4s deals with I/O using cats-effect)
* cats/cats-effect: high-performance, asynchronous, composable framework for building real-world applications in a purely functional style
* doobie: a pure functional JDBC layer for Scala and Cats. It provides a functional way to construct programs that use JDBC.
* fs2: purely functional, effectful, and polymorphic stream processing library
* others: circe (JSON library) pureconfig (for loading configuration files)

#### Possible Improvements:

* Http Service and Event processing daemon can be separate projects deployed independently
* More externally configurable properties, esp for scheduled activities like socket read and websocket push
* Profiling (Apache Bench, VisualVM)
* Exhuastive Test cases and Coverage (Need more time to write test cases)
* There is a possible bug in "execute" defined in EventDataService, as the "release" may not happen after use. 

#### Artefacts:
- EventCounterMain: 
    - entry point for the application
    - sets up routes
    - initializes repository and services
    - starts server
    
- Routes: 
    - EventWSRorutes for HTTP routes, just Request => Response part
    - Defines API endpoints
         
- Domain: 
    - EventData: case class with implicit decoder/encoder
    - EventDataService: The core logic of 
      * reading event source and processing data as stream
      * persist currently aggregated event count data in either DB or redis
        
- Config: 
    - Loads EnvConfig (env.conf) from resource 
    - Loads external dev.conf / test.conf 
    - Loads database / api details from external conf

