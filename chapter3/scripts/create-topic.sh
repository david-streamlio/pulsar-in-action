#/bin/bash

docker exec -it pulsar /pulsar/bin/pulsar-admin tenants create manning

docker exec -it pulsar /pulsar/bin/pulsar-admin namespaces create manning/chapter3

docker exec -it pulsar  /pulsar/bin/pulsar-admin topics create persistent://manning/chapter3/my-topic

