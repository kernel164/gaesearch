<font color='red'>Documentation in progress</font>

### Lib Dependencies ###
  1. [commons-lang-2.4.jar](http://commons.apache.org/lang/)
  1. [google-collect-1.0-rc2.jar](http://code.google.com/p/google-collections/)
  1. [commons-logging.jar](http://commons.apache.org/logging/)

### Sample ###
  1. Simple Search Capable Model
```
package org.gaesearch.sample;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
@SearchCapable
public class Home {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@SearchKey // Note: currently only supports Long id key
	private Long id;

	@Persistent
	@SearchField("address")
	private String address1;

	@Persistent
	@SearchField("address")
	private String address2;

	@Persistent
	@SearchField("address")
	private String address3;

	@Persistent
	@SearchField
	private String area;

	@Persistent
	@SearchField
	private String city;

	// getter & setters
}
```
  1. Setup Simple Index Service
```
public class SimpleIndexService {
	private IndexSearchEngine indexSearchEngine = new GIndexSearchEngine("org.gaesearch.sample.Home");

	// object to be indexed.
	public void index(Object obj) {
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put(IndexSearchEngine.OBJECT, obj);
		settings.put(IndexSearchEngine.PMF, ObjectFactory.getPMF()); // set PMF

		// do indexing.
		indexSearchEngine.index(settings);
	}

        // object to be unindexed.
	public void unIndex(Object obj) {
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put(IndexSearchEngine.OBJECT, obj);
		settings.put(IndexSearchEngine.PMF, ObjectFactory.getPMF()); // set PMF

		// do unindexing.
		indexSearchEngine.unIndex(settings);
	}
}
```
  1. Simple Search Service
```
public class SimpleSearchService {
	private IndexSearchEngine indexSearchEngine = new GIndexSearchEngine("org.gaesearch.sample.Home");

	public SearchServiceImpl() {
	    // set persistence manager factory.
	    this.indexSearchEngine.setPersistenceManagerFactory(ObjectFactory.getPMF()); // set PMF
	}

	public List<Long> searchAddress(String text) {
		return indexSearchEngine.search(getSettings("address", text));
	}

	public List<Long> searchCity(String text) {
		return indexSearchEngine.search(getSettings("city", text));
	}

	public List<Long> searchAll(String text) {
		return indexSearchEngine.search(getSettings(null, text));
	}

	private Map<String, Object> getSettings(String type, String text) {
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put(IndexSearchEngine.OBJECT, new SearchBy(Home.class, type, text));
		return settings;
	}
}
```