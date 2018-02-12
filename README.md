# TempRecorder
A REST endpoint for recording temperature readings from connected weather stations

##POST
* curl -H "Content-Type: application/json" -X POST -d '{"items":[{"location":"xyz","temp":70}]}' http://localhost:8080/record-temp

##GET
* curl http://localhost:8080&item=0

