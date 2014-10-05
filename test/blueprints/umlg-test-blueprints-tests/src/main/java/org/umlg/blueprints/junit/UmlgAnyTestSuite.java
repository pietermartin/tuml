package org.umlg.blueprints.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.umlg.tests.batch.TestBatchMode;
import org.umlg.tests.bulkcollection.TestBulkCollection;
import org.umlg.tests.collectiontest.OrderedSetTestTest;
import org.umlg.tests.indexing.TestIndexing;
import org.umlg.tests.indexing.TestIndexingDataType;
import org.umlg.tests.ocl.ocloperator.OclTestToLowerCase;

/**
 * Date: 2013/10/19
 * Time: 10:06 AM
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestBulkCollection.class
})
public class UmlgAnyTestSuite {
}
