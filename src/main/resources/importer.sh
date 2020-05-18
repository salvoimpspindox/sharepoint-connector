#!/bin/bash
java -D'java.util.logging.config.file'=logging.properties -jar -Xmx3048m google-cloudsearch-sharepoint-connector-v1-0.0.5.jar &
IMPORTER_PID=$!
echo IMPORTER_PID
echo IMPORTER_PID > ../importer.pid