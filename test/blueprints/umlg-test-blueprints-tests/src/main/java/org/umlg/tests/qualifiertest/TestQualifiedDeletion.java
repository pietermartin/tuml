package org.umlg.tests.qualifiertest;

import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.junit.Assert;
import org.junit.Test;
import org.umlg.componenttest.Space;
import org.umlg.componenttest.SpaceTime;
import org.umlg.componenttest.Time;
import org.umlg.concretetest.God;
import org.umlg.concretetest.Universe;
import org.umlg.qualifiertest.Many1;
import org.umlg.qualifiertest.Many2;
import org.umlg.qualifiertest.Nature;
import org.umlg.runtime.collection.UmlgSet;
import org.umlg.runtime.test.BaseLocalDbTest;
import org.umlg.runtime.util.Pair;

import java.util.Set;

public class TestQualifiedDeletion extends BaseLocalDbTest {

    @Test
    public void testTitanException() {
        God god = new God(true);
        god.setName("THEGOD");
        Universe universe1 = new Universe(god);
        universe1.setName("universe1");
        SpaceTime st = new SpaceTime(universe1);
        Space s = new Space(st);
        Time t = new Time(st);
        Many1 many11 = new Many1(god);
        many11.setName("many11");
        Many2 many21 = new Many2(god);
        many21.setName("many21");
        many11.addToMany2(many21);

        db.commit();
        Many1 many1Test = new Many1(many11.getVertex());
        many1Test.delete();
        db.commit();

    }

	@SuppressWarnings("unused")
	@Test
	public void testDeletionManyToMany() {
		God god = new God(true);
		god.setName("THEGOD");
		Universe universe1 = new Universe(god);
		universe1.setName("universe1");
		SpaceTime st = new SpaceTime(universe1);
		Space s = new Space(st);
		Time t = new Time(st);

		Many1 many11 = new Many1(god);
		many11.setName("many11");
		Many1 many12 = new Many1(god);
		many12.setName("many12");
		Many1 many13 = new Many1(god);
		many13.setName("many13");
		Many1 many14 = new Many1(god);
		many14.setName("many14");

		Many2 many21 = new Many2(god);
		many21.setName("many21");
		Many2 many22 = new Many2(god);
		many22.setName("many22");
		Many2 many23 = new Many2(god);
		many23.setName("many23");
		Many2 many24 = new Many2(god);
		many24.setName("many24");

		many11.addToMany2(many21);
		many11.addToMany2(many22);
		many11.addToMany2(many23);
		many11.addToMany2(many24);

		many12.addToMany2(many21);
		many12.addToMany2(many22);
		many12.addToMany2(many23);
		many12.addToMany2(many24);

		many13.addToMany2(many21);
		many13.addToMany2(many22);
		many13.addToMany2(many23);
		many13.addToMany2(many24);

		many14.addToMany2(many21);
		many14.addToMany2(many22);
		many14.addToMany2(many23);
		many14.addToMany2(many24);

        db.commit();
		Assert.assertEquals(12, countVertices());
		Assert.assertEquals(28, countEdges());
		Many2 many2Test = new Many2(many21.getVertex());
		Assert.assertEquals(1, many2Test.getMany1ForMany1Qualifier1(Pair.of(Compare.eq, "many11")).size());

		Many1 many1Test = new Many1(many11.getVertex());
		many1Test.delete();
        db.commit();

		many2Test = new Many2(many21.getVertex());
		Assert.assertTrue(many2Test.getMany1ForMany1Qualifier1(Pair.of(Compare.eq, "many11")).isEmpty());
	}

	@SuppressWarnings("unused")
	@Test
	public void testDeletionManyToManyList() {
		God god = new God(true);
		god.setName("THEGOD");

		Many1 many11 = new Many1(god);
		many11.setName("many11");
		Many1 many12 = new Many1(god);
		many12.setName("many12");
		Many1 many13 = new Many1(god);
		many13.setName("many13");
		Many1 many14 = new Many1(god);
		many14.setName("many14");

		Many2 many21 = new Many2(god);
		many21.setName("many21");
		Many2 many22 = new Many2(god);
		many22.setName("many22");
		Many2 many23 = new Many2(god);
		many23.setName("many23");
		Many2 many24 = new Many2(god);
		many24.setName("many24");

		many11.addToMany2List(many21);
		many11.addToMany2List(many22);
		many11.addToMany2List(many23);
		many11.addToMany2List(many24);

		many12.addToMany2List(many21);
		many12.addToMany2List(many22);
		many12.addToMany2List(many23);
		many12.addToMany2List(many24);

		many13.addToMany2List(many21);
		many13.addToMany2List(many22);
		many13.addToMany2List(many23);
		many13.addToMany2List(many24);

		many14.addToMany2List(many21);
		many14.addToMany2List(many22);
		many14.addToMany2List(many23);
		many14.addToMany2List(many24);

        db.commit();
        Assert.assertEquals(8, countVertices());
        Assert.assertEquals(24, countEdges());
		Many2 many2Test = new Many2(many21.getVertex());
		Assert.assertEquals(1, many2Test.getMany1ListForListQualifier1(Pair.of(Compare.eq, "many11")).size());

		Many1 many1Test = new Many1(many11.getVertex());
		many1Test.delete();
        db.commit();

		many2Test = new Many2(many21.getVertex());
		Assert.assertTrue(many2Test.getMany1ListForListQualifier1(Pair.of(Compare.eq, "many11")).isEmpty());

		Assert.assertEquals("many12", many2Test.getMany1List().get(0).getName());
		Assert.assertEquals("many13", many2Test.getMany1List().get(1).getName());
		Assert.assertEquals("many14", many2Test.getMany1List().get(2).getName());
	}

	@Test
	public void testQualifiedManyDeletion() {
		God god = new God(true);
		god.setName("THEGOD");
		Nature nature = new Nature(true);
		nature.setName1("name1_0");
		nature.setName2("xxx");
        nature.setNameUnique("a");
		nature.addToGod(god);
        db.commit();

		Assert.assertEquals(1, countVertices());
		Assert.assertEquals(1, countEdges());

		nature = new Nature(true);
		nature.setName1("name1_1");
		nature.setName2("xxx");
        nature.setNameUnique("b");
		nature.addToGod(god);
        db.commit();

		Assert.assertEquals(2, countVertices());
		Assert.assertEquals(2, countEdges());

		nature = new Nature(true);
		nature.setName1("name1_2");
		nature.setName2("xxx");
        nature.setNameUnique("c");
		nature.addToGod(god);
        db.commit();

		Assert.assertEquals(3, countVertices());
		Assert.assertEquals(3, countEdges());

		nature = new Nature(true);
		nature.setName1("name1_3");
		nature.setName2("xxx");
        nature.setNameUnique("d");
		nature.addToGod(god);
        db.commit();

		Assert.assertEquals(4, countVertices());
		Assert.assertEquals(4, countEdges());

		nature = new Nature(true);
		nature.setName1("name1_4");
		nature.setName2("yyy");
        nature.setNameUnique("e");
        nature.addToGod(god);
        db.commit();

		Assert.assertEquals(5, countVertices());
		Assert.assertEquals(5, countEdges());

		God godTest = new God(god.getVertex());
		Set<Nature> natureForQualifier2 = godTest.getNatureForQualifier2(Pair.of(Compare.eq, "xxx"));
		Assert.assertEquals(4, natureForQualifier2.size());
		natureForQualifier2 = godTest.getNatureForQualifier2(Pair.of(Compare.eq, "yyy"));
		Assert.assertEquals(1, natureForQualifier2.size());
        db.commit();

		God godTest2 = new God(god.getVertex());
		UmlgSet<Nature> natures = godTest2.getNatureForQualifier2(Pair.of(Compare.eq, "xxx"));
		for (Nature nature2 : natures) {
			nature2.delete();
			break;
		}
        db.commit();

		Assert.assertEquals(4, countVertices());
		Assert.assertEquals(4, countEdges());

		God godTest3 = new God(god.getVertex());
		natures = godTest3.getNatureForQualifier2(Pair.of(Compare.eq, "xxx"));
		Assert.assertEquals(3, natures.size());
		Assert.assertEquals(1, godTest2.getNatureForQualifier2(Pair.of(Compare.eq, "yyy")).size());

	}

}
