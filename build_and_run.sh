#!/bin/bash

mvn clean install

cd target
java -jar demo-0.0.1-SNAPSHOT.jar; cd ..