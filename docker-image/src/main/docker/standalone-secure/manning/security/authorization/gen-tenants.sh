#!/bin/bash


/pulsar/bin/pulsar-admin tenants create my-tenant \
  --admin-roles admin \
  --allowed-clusters us-east
  

/pulsar/bin/pulsar-admin namespaces grant-permission test-tenant/ns1 \
  --actions produce,consume \
  --role 'my.role.*'
