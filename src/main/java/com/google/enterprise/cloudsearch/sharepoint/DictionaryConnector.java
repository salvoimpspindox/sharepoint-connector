package com.google.enterprise.cloudsearch.sharepoint;

import com.google.enterprise.cloudsearch.sdk.indexing.IndexingApplication;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingConnector;
import com.google.enterprise.cloudsearch.sdk.indexing.template.FullTraversalConnector;
import com.google.enterprise.cloudsearch.sdk.indexing.template.Repository;

public class DictionaryConnector {

	public static void main(String[] args) throws InterruptedException {
		Repository repository = new DictionaryRepository();
		IndexingConnector connector = new FullTraversalConnector(repository);
		IndexingApplication application = new IndexingApplication.Builder(connector, args).build();
		application.start();
	}

}
