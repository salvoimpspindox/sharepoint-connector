package com.google.enterprise.cloudsearch.sharepoint;

import static com.google.enterprise.cloudsearch.sdk.indexing.IndexingItemBuilder.FieldOrValue.withValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import com.google.api.services.cloudsearch.v1.model.Item;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterable;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterableImpl;
import com.google.enterprise.cloudsearch.sdk.RepositoryException;
import com.google.enterprise.cloudsearch.sdk.StartupException;
import com.google.enterprise.cloudsearch.sdk.config.Configuration;
import com.google.enterprise.cloudsearch.sdk.indexing.Acl;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingItemBuilder;
import com.google.enterprise.cloudsearch.sdk.indexing.template.ApiOperation;
import com.google.enterprise.cloudsearch.sdk.indexing.template.Repository;
import com.google.enterprise.cloudsearch.sdk.indexing.template.RepositoryContext;
import com.google.enterprise.cloudsearch.sdk.indexing.template.RepositoryDoc;

public class DictionaryRepository implements Repository {

	private static final Logger log = Logger.getLogger(DictionaryRepository.class.getName());

	private static final Acl DOMAIN_PUBLIC_ACL = new Acl.Builder()
			.setReaders(ImmutableList.of(Acl.getCustomerPrincipal())).build();

	String dictionaryFilePath;
	String separator = "#";
	String synonymsSeparator = ",";

	DictionaryRepository() {
	}

	@Override
	public void init(RepositoryContext context) {
		log.info("Initializing repository");
		dictionaryFilePath = Configuration.getString("dictionary.file", null).get();
		if (dictionaryFilePath == null) {
			throw new StartupException("Missing dictionary.file parameter in configuration");
		}
		if (Files.notExists(Paths.get(dictionaryFilePath))) {
			throw new StartupException("Dictionary file does not exist.");
		}
	}

	@Override
	public void close() {
		log.info("Closing repository");
	}

	@Override
	public CheckpointCloseableIterable<ApiOperation> getAllDocs(byte[] checkpoint) throws RepositoryException {
		log.info("Retrieving all documents.");

		try {
			Scanner scanner = new Scanner(new File(dictionaryFilePath));
			List<ApiOperation> allDocs = new ArrayList<>();
			while (scanner.hasNextLine()) {
				String record = scanner.nextLine();
				allDocs.add(buildDocument(record.split(separator)[0],
						Arrays.asList(record.split(separator)[1].split(synonymsSeparator))));
			}
			scanner.close();

			return new CheckpointCloseableIterableImpl.Builder<>(allDocs).build();
		} catch (IOException e) {
			throw new RepositoryException.Builder().setCause(e).setErrorType(RepositoryException.ErrorType.CLIENT_ERROR)
					.build();
		}
	}

	private ApiOperation buildDocument(String term, List<String> synonyms) {
		Multimap<String, Object> structuredData = ArrayListMultimap.create();
		structuredData.put("_term", term);
		structuredData.putAll("_synonym", synonyms);
		structuredData.put("_onlyApplicableForAttachedSearcapplications", true);

		log.info("CREATING DICTIONARY_ENTRY -> " + term + ":" + String.join(",", synonyms));

		String itemName = String.format("dictionary/%s", term);

		// Using the SDK item builder class to create the item
		Item item = IndexingItemBuilder.fromConfiguration(itemName)
				.setItemType(IndexingItemBuilder.ItemType.CONTENT_ITEM).setObjectType(withValue("_dictionaryEntry"))
				.setVersion(String.valueOf(new Date().getTime()).getBytes()).setValues(structuredData)
				.setAcl(DOMAIN_PUBLIC_ACL).build();

		// Create the fully formed document
		return new RepositoryDoc.Builder().setItem(item).build();
	}

	@Override
	public CheckpointCloseableIterable<ApiOperation> getChanges(byte[] checkpoint) {
		return null;
	}

	@Override
	public CheckpointCloseableIterable<ApiOperation> getIds(byte[] checkpoint) {
		return null;
	}

	@Override
	public ApiOperation getDoc(Item item) {
		return null;
	}

	@Override
	public boolean exists(Item item) {
		return false;
	}
}
