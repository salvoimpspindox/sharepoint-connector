#!/bin/bash
java -D'java.util.logging.config.file'=logging.properties -cp google-cloudsearch-sharepoint-connector-v1-0.0.5.jar com.google.enterprise.cloudsearch.sharepoint.DictionaryConnector &