# TempRecorder
A REST endpoint written in Scala, Akka http, for recording temperature readings from connected weather stations

## POST to record temperature at a location
* curl -H "Content-Type: application/json" -X POST -d '{"location":"basement","dateTime":"2018-02-16T22:12:00","temp":72}' http://localhost:8080/record-temp

_temp recorded! Temperature(basement,2018-02-16T22:12:00,72) log size=1_

## GET lookup based on temp... ???
* curl http://localhost:8080/temperature/72

_completed getting (Temperature(basement,2018-02-16T22:12:00,72))_


## steps to setup mongodb instance
* $ mongo

_MongoDB shell version v3.6.2_
_connecting to: mongodb://127.0.0.1:27017_
_MongoDB server version: 3.6.2_


* use tempuratureLog

_switched to db tempuratureLog_

* db.createCollection("temps")

_{ "ok" : 1 }_

* db.temps.insert({location:"basement",dateTime:"2018-02-16T22:12:00",temp:72})

_WriteResult({ "nInserted" : 1 })_



## coming soon...
* unit tests
* integration tests
* more...