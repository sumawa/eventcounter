mkdir -p output
EC_ENV=dev EC_HOME=$(pwd) java  -classpath ./event-counter/target/scala-2.12/event-counter-assembly-0.1.0.jar com.sa.events.EventCounterMain > app.log &
