#!/bin/bash

blank=""
edgestatus="UP"
toolkitstatus="UP"
ristatus="UP"
validatorstatus="UP"
statustxt="/opt/notify/status.txt"

date > $statustxt

dig ttpedge.sitenv.org cert >> $statustxt
curl -I -k http://ttpedge.sitenv.org:11080/xdstools2/ >> $statustxt
dig ttpds2.sitenv.org cert >> $statustxt
curl -I -k http://ttpds.sitenv.org:8080/referenceccdaservice/ >> $statustxt

status="$(dig ttpedge.sitenv.org cert | grep PKIX)"
if [ "$status" == "$blank" ]
then
   edgestatus="DOWN"
fi
status="$(curl -I -k http://ttpedge.sitenv.org:11080/xdstools2/ | grep 200)"
if [ "$status" == "$blank" ]
then
   toolkitstatus="DOWN"
fi
status="$(dig ttpds2.sitenv.org cert | grep PKIX)"
if [ "$status" == "$blank" ]
then
   ristatus="DOWN"
fi
status="$(curl -I -k http://ttpds.sitenv.org:8080/referenceccdaservice/ | grep 405)"
if [ "$status" == "$blank" ]
then
   validatorstatus="DOWN"
fi
sudo java -jar /opt/notify/notifier.jar "sandeep17199117@gmail.com;srini.adhi@gmail.com;sut.example@gmail.com" "EdgeDNS $edgestatus Toolkit $toolkitstatus DirectRIDNS $ristatus Validator $validatorstatus"
