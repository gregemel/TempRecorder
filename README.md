# TempRecorder
A REST endpoint written in Scala, Akka http, and MongoDb.
Working code where a REST endpoint receives and records info like current temp and humidity from weather station clients.

# Purpose
This project exists for me to practice building Akka components in Scala.
It includes working code for an http micro-service that logs temperature readings into a local MongoDB instance.
This is very much a work in progress and lacks fundamental engineering like like security, test automation, and deployments.

### TempRecorder 
TempRecorder is an http endpoint that reads and writes from a local MongoDB instance. 
The endpoint uses http Akka and the MongoDB Scala Driver.
Working code marshals json from http Akka, into a domain model object, then persists in MongoDB as json again.
At a bare minimum, a REST microservice needs to handle http post/get verbs and read/writes to the database.
This project meets that basic goal, even though it is not full entity crud.

## Code
The code is currently structured in model, controller, repository type layouts, similar to REST services written in conventional java/spring.
This structure will likely changing as I become more familiar with idiomatic Scala, Akka,  and Actors.  

### Controller
The controller defines the port binding and uri routes 

### handler
Handles http events routed from the controller, like POST and GET.

### Model
A case class is used to construct the temperature record.

### Repository
Repository fetches and saves temperature records using mongo.

This project represents an exploration and experimentation.  It is not (yet) meant to be a good example of code quality, style, or best practices.

## POST to record temperature at a location
* curl -H "Content-Type: application/json" -X POST -d '{"location":"basement","dateTime":"2018-02-16T22:12:00","temp":72}' http://localhost:8080/record-temp

_temp recorded! Temperature(basement,2018-02-16T22:12:00,72) log size=1_

## GET lookup based on temp... ???
* curl http://localhost:8080/temperature/72

_completed getting (Temperature(basement,2018-02-16T22:12:00,72))_

## steps to setup mongodb instance
* sudo service mongod start

## coming soon...
* test automation
* build pipelines
* AWS: SQS and SNS
* more...