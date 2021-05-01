#/bin/bash -v

echo "Creating the topic"
/pulsar/bin/pulsar-admin tenants create orders
/pulsar/bin/pulsar-admin namespaces create orders/inbound
/pulsar/bin/pulsar-admin topics create persistent://orders/inbound/food-orders

/pulsar/manning/scripts/schema-tool.sh schema -config /pulsar/manning/schemas/food-order-schema-config.json -schema /pulsar/manning/schemas/idl-generated-food-order.avsc -type AVRO

echo "Uploading the schema"
/pulsar/bin/pulsar-admin schemas upload --filename /pulsar/manning/schemas/food-order-schema-config.json persistent://orders/inbound/food-orders

/pulsar/bin/pulsar-admin namespaces set-schema-validation-enforce --enable orders/inbound

echo "Confirm the schema"
/pulsar/bin/pulsar-admin schemas get persistent://orders/inbound/food-orders