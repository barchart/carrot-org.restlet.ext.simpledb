package org.restlet.ext.simpledb.name;

public interface Name {

	interface Id {

		String VOLUME = "volume";
		String ENTRY = "entry";

		String DOMAIN = "domain";
		String ITEM = "item";
		String ATTRIBUTE = "attribute";

	}

	String NONE = "";

	String ROOT = "/";

	//

	// volume is a collection of domain shards

	String VOLUMES = //
	"/volumes";

	String VOLUME = //
	"/volumes/{volume}";

	String ENTRIES = //
	"/volumes/{volume}/entries";

	/* {entry} == firstId|secondId|thirdId|nnnnnnId */
	String ENTRY = //
	"/volumes/{volume}/entries/{entry}";

	//

	// via direct domain access

	String DOMAINS = //
	"/domains";

	String DOMAIN = //
	"/domains/{domain}";

	String ITEMS = //
	"/domains/{domain}/items";

	String ITEM = //
	"/domains/{domain}/items/{item}";

	String ATTRIBUTES = //
	"/domains/{domain}/items/{item}/attributes";

	String ATTRIBUTE = //
	"/domains/{domain}/items/{item}/attributes/{attribute}";

}
