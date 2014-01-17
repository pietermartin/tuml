package com.rorotika.test;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.*;
import org.umlg.Many;
import org.umlg.One;
import org.umlg.root.Root;
import org.umlg.runtime.adaptor.GraphDb;
import org.umlg.runtime.adaptor.UmlgGraph;
import org.umlg.runtime.adaptor.UmlgGraphManager;
import org.umlg.runtime.validation.TumlConstraintViolationException;

import java.io.IOException;
import java.net.URL;
import java.util.logging.LogManager;

/**
 * Date: 2014/01/15
 * Time: 10:17 PM
 */
public class TestDemo {

    private UmlgGraph db;

    @Before
    public void before() {
        this.db = GraphDb.getDb();
    }

    @After
    public void after() {
        db.drop();
    }

    @Test
    public void testDemo() {
        One one1 = new One(true);
        //MaxLength validation ensures name's length must be <= 5
        one1.setName("12345");
        one1.setDate(new LocalDate());
        Many many1 = new Many(one1);
        //MinLength validation ensures name's length must be >= 5
        many1.setName("12345");
        many1.setDateTime(new DateTime());
        Many many2 = new Many(one1);
        many2.setName("12345");
        many2.setDateTime(new DateTime());
        Many many3 = new Many(one1);
        many3.setName("12345");
        many3.setDateTime(new DateTime());
        Many many4 = new Many(one1);
        many4.setName("12345");
        many4.setDateTime(new DateTime());
        Many many5 = new Many(one1);
        many5.setName("12345");
        many5.setDateTime(new DateTime());
        db.commit();

        Assert.assertEquals(1, Root.INSTANCE.getOne().size());
        Assert.assertEquals(5, one1.getMany().size());
    }

    @Test(expected = TumlConstraintViolationException.class)
    public void testDemoValidation() {
        try {
            One one1 = new One(true);
            //MaxLength validation ensures name's length must be <= 5
            one1.setName("12345");
            one1.setDate(new LocalDate());
            Many many1 = new Many(one1);
            //MinLength validation ensures name's length must be >= 5
            many1.setName("1234");
            many1.setDateTime(new DateTime());
            db.commit();
            Assert.assertEquals(1, Root.INSTANCE.getOne().size());
            Assert.assertEquals(5, one1.getMany().size());
        } finally {
            db.rollback();
        }
    }
}
