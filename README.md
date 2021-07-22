# Event Counter
###

### How to build or test the source code
0. Setting up event generation binary as event source using netcat
```
chmod +x blackbox.macosx
./blackbox.macosx | nc -lk 9999 
```
2. Build assembly and execute run_eventcounter.sh
```
sbt "project eventCounter" clean assembly

./run_eventcount.sh
```
3. The output is generated in the app.log
```
tail app.log
```

### Event Counter Endpoint Websocket :
A websocket end point that refresh every 10 seconds

```
ws://0.0.0.0:53248/eventData/ws1
```
An example HTML page invoking this websocket end point is following directory.
Load this file directly in the browser.

./front/public/TestWS.html

### Event Counter HTTP Endpoint 
For the same event data
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
* Profiling (Apache Bench, VisualVM)
* Exhuastive Test cases and Coverage

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
        
- Config: 
    - Loads EnvConfig (env.conf) from resource 
    - Loads external dev.conf / test.conf 
    - Loads database / api details from external conf

