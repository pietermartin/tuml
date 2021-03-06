package org.umlg.runtime.adaptor;

import org.umlg.runtime.util.UmlgUtil;

import java.lang.reflect.Method;

/**
 * Date: 2013/02/08
 * Time: 8:29 PM
 */
public class UmlgExceptionUtilFactory {

    private static UmlgExceptionUtil umlgExceptionUtil;

    @SuppressWarnings("unchecked")
    public static UmlgExceptionUtil getTumlExceptionUtil() {
        if (umlgExceptionUtil == null) {
            try {
                UmlgAdaptorImplementation umlgAdaptorImplementation = UmlgAdaptorImplementation.fromName(UmlgUtil.getBlueprintsImplementation());
                Class<UmlgExceptionUtil> factory = (Class<UmlgExceptionUtil>) Class.forName(umlgAdaptorImplementation.getUmlgExceptionUtil());
                Method m = factory.getDeclaredMethod("getInstance", new Class[0]);
                umlgExceptionUtil = (UmlgExceptionUtil) m.invoke(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return umlgExceptionUtil;
    }
}
