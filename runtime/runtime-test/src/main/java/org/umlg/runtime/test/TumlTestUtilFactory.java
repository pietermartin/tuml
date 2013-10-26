package org.umlg.runtime.test;

import org.umlg.runtime.adaptor.UmlgAdaptorImplementation;
import org.umlg.runtime.util.UmlgUtil;

import java.lang.reflect.Method;

/**
 * Date: 2013/01/09
 * Time: 7:23 AM
 */
public class TumlTestUtilFactory {

    private static TumlTestUtil tumlTestUtil;

    @SuppressWarnings("unchecked")
    public static TumlTestUtil getTestUtil() {
        if (tumlTestUtil == null) {
            try {
                UmlgAdaptorImplementation umlgAdaptorImplementation = UmlgAdaptorImplementation.fromName(UmlgUtil.getBlueprintsImplementation());
                Class factory = Class.forName(umlgAdaptorImplementation.getTumlTestUtil());
                Method m = factory.getDeclaredMethod("getInstance", new Class[0]);
                tumlTestUtil = (TumlTestUtil) m.invoke(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return tumlTestUtil;
    }

}