# nerd4j-utils
This library contains utility classes common to all nerd4j projects but also generic enough to be used in any code.

## Content of this library
The classes of this library are divided into several concepts:

* Dependency: classes and interfaces to define dependencies and organize them into a dependency tree.
* I10n: utility classes to handle Locale and multi language support.
* IO: utility classes to simplify operations on file system.
* Math: currently contains just the class `PrimeSieve` that provides some useful operations on prime numbers.
* Net: factory classes used to build Apache Http Clients in a fast and easy way.
* Resource: classes and interfaces to handle resource paths in an easy and configurable way.
* Security: wrappers over the `java.security.MessageDigest` to easily generate hash codes and authentication tokens.
* SMTP: wrappers over the `javax.mail` and the `freemarker` frameworks for easily build and send simple text and templated emails.
* Thread: utility classes to handle thread pools and concurrency over bounded resource pools.
* Time: currently contains just the class `SimpleDateHandler` that provides an easy way to handle with objects of type `java.util.Date`.


This library is also available on Maven Central [here] (http://search.maven.org/#artifactdetails|org.nerd4j|nerd4j-utils|1.0.0|jar "Maven Central: nerd4j-utils") and can be used with the following dependecy declaration:
```xml
<dependency>
 <groupId>org.nerd4j</groupId>
 <artifactId>nerd4j-utils</artifactId>
 <version>1.0.0</version>
</dependency>
```
