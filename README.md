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

* One or more computers running Java 8 and Tomcat 7 or 8
* A Cassandra cluster running Cassandra 2.1.x


## Qakka Internals

You can learn more about Qakka internals by reading this [Google Slides presentation](https://docs.google.com/presentation/d/1j1w8txLlmVg6Ndiq1gsmF_3g8tl5flSjgmko4UxH6Gc/edit?usp=sharing).

Qakka was developed as for this JIRA Issue: [USERGRID-1318](https://issues.apache.org/jira/browse/USERGRID-1318).


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

