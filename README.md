# Pulsar In Action
Code for the first 6 chapters of the Pulsar In Action book. You can find the code for Chapters 7 thru 11 in the 
https://github.com/david-streamlio/GottaEat repo, which has the implementation for the GottaEat food delivery example
references in the book.

## Build Instructions

This project leverages the [Continuous delivery friendly version of Maven](https://maven.apache.org/maven-ci-friendly.html), which allows us to dynamically specify the project version
via a property, e.g.

`mvn -Drevision=1.0.0 clean install`


When Maven install (or deploy) ours artifacts, pom.xml are copied without modification (${revision} properties are not replaced by their resolved value). If you try to depend on one of these artifacts it will fail because ${revision} will not be set in this outside project. In order to fix this issue, you will need to download and install the [unique-revision-maven-filtering](https://jeanchristophegay.com/en/posts/maven-unique-version-multi-modules-build/) Maven extension, and place it in your ${MAVEN_HOME}/lib folder

## Run instructions
`export CLASSPATH=$(mvn dependency:build-classpath | grep -A1 classpath: | tail -n1)`
`java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.consumers.BackAndForth`

`java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.consumers.FailoverSubscriptionDemo`

`java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.consumers.SharedSubscriptionDemo`

`java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.readers.EarliestReader`

`java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.readers.LatestReader`

`java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter3.readers.PulsarReaders`
