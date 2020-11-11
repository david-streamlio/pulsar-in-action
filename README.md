# Pulsar In Action
Code for the first 6 chapters of the Pulsar In Action book. You can find the code for Chapters 7 thru 11 in the 
https://github.com/david-streamlio/GottaEat repo, which has the implementation for the GottaEat food delivery example
references in the book.

## Build instructions
mvn package

## Run instructions
export CLASSPATH=$(mvn dependency:build-classpath | grep -A1 classpath: | tail -n1)
java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.consumers.BackAndForth
java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.consumers.FailoverSubscriptionDemo
java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.consumers.SharedSubscriptionDemo

java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.readers.EarliestReader
java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.readers.LatestReader
java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.readers.PulsarReaders
