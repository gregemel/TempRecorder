# TempRecorder
A REST endpoint for recording temperature readings from connected weather stations

## POST to record temperature at a location
* curl -H "Content-Type: application/json" -X POST -d '{"location":"xyz","temp":70}' http://localhost:8080/record-temp

_temp recorded! Temperature(xyz,70) log size=1_

## GET lookup based on temp... ???
* curl http://localhost:8080/item/70

_{"location":"xyz","temp":70}_
