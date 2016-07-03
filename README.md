# Spring Boot Rest Example

I made this project to know more about how to make a REST application with
Spring boot (MVC + Data).
The objective of this small project was to design a RESTful web service
storing transactions and returning information about them.

## Quick start

To build and run this Spring Boot application use:

```shell
mvn spring:boot-run
```

Beside the integration tests, you can get a quick overview of the
functionalities by using a bash script (you need to install `curl`)

```shell
sh ./src/test/resources/test.sh
```

It should give the following output

```
$ sh ./src/test/resources/test.sh
PUT /transactionservice/transaction/10 { amount: 5000, type: cars } =>
{"status":"ok"}

PUT /transactionservice/transaction/11 { amount: 10000, type: shopping, parent_id: 10 } =>
{"status":"ok"}

GET /transactionservice/types/cars =>
[10]

GET /transactionservice/sum/10 =>
{"sum":15000.0}

GET /transactionservice/sum/11 =>
{"sum":10000.0}

PUT /transactionservice/transaction/10 { amount: 5000, type: cars, parent_id: 11 } =>
{"status":"error"}

DELETE /transactionservice/transaction/11 =>


DELETE /transactionservice/transaction/10 =>
```

The internal state of the H2 database can be checked through the H2 console
http://localhost:8080/h2-console/

## Design choices

In this project, transactions are represented as trees (cf
`/transactionservice/sum/:id` for application examples) even if it is not
really close to what we would have in real banking systems.

The objective was to allow to demonstrate how to design a robust system,
in this case by detecting requests that would cause an inconsistent state
of the system, for example by introducing cycles in the transaction tree,
thus making it into a graph.

I chose H2 with disk persistence for database but the use of Liquibase and
Spring Data allow to use any other base just by adding a new Spring profile
and its corresponding YAML configuration file (in `src/resources/config`).

The base can be cleaned by deleting the `target` folder with `mvn clean`. In
order to allow to run the integration tests while the application is running,
I wrote a specific configuration file to deploy the application or another
port (`10344`) and by using an in-memory H2 base.

## Discussion

The code is very much inspired by the work I did using the JHipster project
(https://jhipster.github.io/) in the past months. This time I decided to not
use the generator at all, but to start an application from scratch, while
still keeping a similar structure.

In order to learn new things, I decided to use the
Jackson annotation `@JsonView` to allow for discrepancies between the model
entities and the JSON format used in the REST requests and also wrapper
objects for the PUT and POST requests.

I also decided to have rather comprehensive integratioon tests, thus not
only testing the model classes but also the REST services themselves
(cf `org.jbourdon.springRestExample.web.rest.TransactionResourceIntTest` for
more details)
