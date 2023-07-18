# migration of assets from old yaml, groovy and json formats

* Copy files to be migrated into: `src/test/resources/assets-tobe-migrated` according to their type
* Run Migrator.java as a test, by default test annotation is commented out
* Verify logs 
* Refresh `src/test/resources/` 
* Last line in log gives the location of the release.json that can be imported via composer