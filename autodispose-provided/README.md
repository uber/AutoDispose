AutoDispose-provided
====================

This module solely exists to separate out `@PackageNonnull` to allow for it to be a `compileOnly` 
dependency of the other projects. This allows it to be useful still for static analysis, while not 
imposing the jsr305 artifact as a full compile dependency on the consumer.
