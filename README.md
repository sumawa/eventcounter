# Event Counter
###
#### Libraries used
* http4s: a Type safe, functional, streaming HTTP for Scala (Http4s deals with I/O using cats-effect)
* cats/cats-effect: high-performance, asynchronous, composable framework for building real-world applications in a purely functional style
* doobie: a pure functional JDBC layer for Scala and Cats. It provides a functional way to construct programs that use JDBC.
* fs2: purely functional, effectful, and polymorphic stream processing library
* others: circe (JSON library) pureconfig (for loading configuration files)

#### Possible Improvements:

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

### How to build or test the source code

1. Build assembly and execute run_eventcounter.sh
```
sbt "project eventCounter" clean assembly
./run_eventcount.sh
```
2. The output is generated in the app.log
```
tail app.log
```
3. Test
```
sbt  "project eventCounter" clean test
```

### Event Counter API 1 "":
Desc
```
```

### Event Counter API 2 "":
Desc
```
```
