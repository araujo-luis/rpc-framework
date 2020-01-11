# rpc-framework

## Skeleton generation on Java

It'd be possible to generate automatically from the server-side code (the skeleton). There is a part that is general, and therefore can be generated automatically, and a part that is specific to the application (the implementation of the interface methods) that cannot be generated.

This project designs a mechanism in order to generate skeleton code. Afterwards, the specific part of the application can be overwritten and add the necessary code. In addition to the stub, it generates the skeleton code.
