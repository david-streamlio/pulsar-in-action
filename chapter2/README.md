# Build/Run Instructions
mvn package
export CLASSPATH=$(mvn dependency:build-classpath | grep -A1 classpath: | tail -n1)
java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter2.PulsarReaders
java -cp $CLASSPATH:target/classes com.manning.pulsar.chapter2.PulsarConsumers
