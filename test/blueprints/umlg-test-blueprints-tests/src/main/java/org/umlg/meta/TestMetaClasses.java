package org.umlg.meta;

import org.junit.Assert;
import org.junit.Test;
import org.umlg.concretetest.Angel;
import org.umlg.concretetest.God;
import org.umlg.concretetest.meta.AngelMeta;
import org.umlg.concretetest.meta.GodMeta;
import org.umlg.runtime.test.BaseLocalDbTest;

/**
 * Date: 2012/12/27
 * Time: 8:50 AM
 */
public class TestMetaClasses extends BaseLocalDbTest {

    @Test
    public void testMetaPersistence() {
        God g = new God(true);
        g.setName("g");
        Angel a = new Angel(g);
        db.commit();
        Assert.assertEquals(2, countVertices());
        Assert.assertNotNull(g.getMetaNode());
        Assert.assertNotNull(a.getMetaNode());
        Assert.assertTrue(g.getMetaNode() instanceof  GodMeta);
        Assert.assertTrue(a.getMetaNode() instanceof  AngelMeta);
    }
}
