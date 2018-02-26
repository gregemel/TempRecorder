# TempRecorder
A REST endpoint written in Scala, Akka http, for recording temperature readings from connected weather stations

# Purpose
The project exists for me to practice Scala enterprise development.  
TempRecorder is an http endpoint that reads and writes from a local MongoDB instance. 
The endpoint uses http Akka and the MongoDB Scala Driver.
At a bare minimum, a micro-service needs to handle http post/get verbs and read/write to the database.  This project meets that basic goal.

This project represents an exploration and experimentation.  It is not (yet) meant to be a good example of code quality, style, or best practices.

This is still much missing like security, test automation, and deployment.

## POST to record temperature at a location
* curl -H "Content-Type: application/json" -X POST -d '{"location":"basement","dateTime":"2018-02-16T22:12:00","temp":72}' http://localhost:8080/record-temp

_temp recorded! Temperature(basement,2018-02-16T22:12:00,72) log size=1_

## GET lookup based on temp... ???
* curl http://localhost:8080/temperature/72

_completed getting (Temperature(basement,2018-02-16T22:12:00,72))_

## steps to setup mongodb instance
* sudo service mongod start

## coming soon...
* unit tests
* integration tests
* AWS: SQS and SNS
* more...