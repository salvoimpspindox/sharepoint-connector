#!/bin/bash
pkill -F importer.pid
java -D'java.util.logging.config.file'=logging.properties -jar google-cloudsearch-sharepoint-connector-v1-0.0.5.jar & 
IMPORTER_PID=$!
echo IMPORTER_PID
echo IMPORTER_PID > importer.pid