# Qakka

Standalone version of Usergrid's Qakka queue service.

## Introducting Qakka

Qakka is a standalone version of the distributed and multi-region queue system that it built-into Apache Usergrid. It is a Java web application that combines the Usergrid Actor System and Queue modules with a Jersey JAX-RS based REST API. 

__Qakka Features__

* Multiple named Queues
* Distributed and Multi-region
    * Queue messages can be sent to multiple regions
    * Queue reads always from local region
* Inflight Queue messages time-out if not “acked”
* Schemaless Queue message payloads, JSON or BLOB with any content-type
* REST & Java APIs


## Qakka System Requirements

Minimal requires for Qakka in one region or data center are:

* One or more computers running Java 8 and Apache Tomcat 7 or 8
* A Apache Cassandra cluster running Cassandra 2.1.x


## Qakka Internals

You can learn more about Qakka internals by reading this [Google Slides presentation](https://docs.google.com/presentation/d/1j1w8txLlmVg6Ndiq1gsmF_3g8tl5flSjgmko4UxH6Gc/edit?usp=sharing).

Qakka was developed as for this JIRA Issue: [USERGRID-1318](https://issues.apache.org/jira/browse/USERGRID-1318).


## How to use Qakka

We don't have any examples of Qakka REST API use yet, but we do have Swagger generated docs:

* [Qakka REST API docs](http://petstore.swagger.io/?url=https://raw.githubusercontent.com/apache/usergrid-qakka/master/docs/swagger.json)


## How to build Qakka

Qakka is unreleased software and if you want to use it, you will have to built it. Here are the steps to build Qakka:

You will need Java 8 JDK and Maven 3 to build Qakka. You will also need Cassandra 2.1.x if you want to run the JUnit tests.

* Build the Usergrid "Stack":
    * Clone the Apache Usergrid repo and run `mvn -DskipTests=true install` in the  `/stack` directory.
    * This will place the Usergrid jar fies into your local Maven repo so that Qakka can find them.
    
* Build Qakka
    * Clone the Apache Usergrid Qakka repo and run `mvn -DskipTests=true install` in Qakka's directory.
    
If you want to run the Qakka JUnit tests, you will need to have Cassandra 2.1.x running on your computer with the default setup (e.g. on port 9160). You can run the tests with `mvn tests`. The tests in Qakka only hit the REST API; there are more Qakka tests in the Usergrid Queue module.


## Qakka Installation

We don't have installation documentation or scripts yet, but here's an overview of what you'll have to do
to setup Qakka:

* In each region, setup your Tomcat computers
    * Install Java 8
    * Install Tomcat 7 or 8
    * Place the WAR file in the Tomcat's webapps directory
    * Place your qakka.properties file in your Tomcat lib directory
    * If you more than one Tomcat, you probably want to add a Load Balancer, e.g. AWS ELB
    
* In each region, setup your Cassandra computers
    * Install Java 8
    * Install Cassandra 2.1.x
    * Verify that your cluster is working
    
There is an example [qakka.properties](https://github.com/apache/usergrid-qakka/blob/master/docs/qakka-example.properties) file that contains some information about how to configure Qakka, its Cassandra connections and its Usergrid Clustering.

