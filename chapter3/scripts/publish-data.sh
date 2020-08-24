#/bin/bash

i="0"

while [ $i -lt 9999999 ]
do
  docker exec -it pulsar /pulsar/bin/pulsar-client produce -m "Message $i" persistent://manning/chapter3/my-topic
i=$[$i+1]
done