package org.umlg.runtime.adaptor;

import org.apache.commons.io.FileUtils;
import org.umlg.runtime.util.UmlgAdaptorImplementation;
import org.umlg.runtime.util.UmlgProperties;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Date: 2012/12/29
 * Time: 9:23 PM
 */
public class TumlGraphManager {

    public static TumlGraphManager INSTANCE = new TumlGraphManager();
    private static final Logger logger = Logger.getLogger(TumlGraphManager.class.getPackage().getName());

    private TumlGraphManager() {

    }

    public TumlGraph startupGraph() {
        try {
            String dbUrl = UmlgProperties.INSTANCE.getTumlDbLocation();
            UmlgAdaptorImplementation umlgAdaptorImplementation = UmlgAdaptorImplementation.fromName(UmlgProperties.INSTANCE.getTinkerImplementation());
            @SuppressWarnings("unchecked")
            Class<UmlgGraphFactory> factory = (Class<UmlgGraphFactory>) Class.forName(umlgAdaptorImplementation.getTumlGraphFactory());
            Method m = factory.getDeclaredMethod("getInstance", new Class[0]);
            UmlgGraphFactory nakedGraphFactory = (UmlgGraphFactory) m.invoke(null);
            TumlGraph tumlGraph = nakedGraphFactory.getTumlGraph(dbUrl);
            return tumlGraph;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        try {
            UmlgAdaptorImplementation umlgAdaptorImplementation = UmlgAdaptorImplementation.fromName(UmlgProperties.INSTANCE.getTinkerImplementation());
            @SuppressWarnings("unchecked")
            Class<UmlgGraphFactory> factory = (Class<UmlgGraphFactory>) Class.forName(umlgAdaptorImplementation.getTumlGraphFactory());
            Method m = factory.getDeclaredMethod("getInstance", new Class[0]);
            UmlgGraphFactory nakedGraphFactory = (UmlgGraphFactory) m.invoke(null);
            nakedGraphFactory.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteGraph() {
        try {
            UmlgAdaptorImplementation umlgAdaptorImplementation = UmlgAdaptorImplementation.fromName(UmlgProperties.INSTANCE.getTinkerImplementation());
            @SuppressWarnings("unchecked")
            Class<UmlgGraphFactory> factory = (Class<UmlgGraphFactory>) Class.forName(umlgAdaptorImplementation.getTumlGraphFactory());
            Method m = factory.getDeclaredMethod("getInstance", new Class[0]);
            UmlgGraphFactory nakedGraphFactory = (UmlgGraphFactory) m.invoke(null);
            nakedGraphFactory.drop();
            //Delete the files
            String dbUrl = UmlgProperties.INSTANCE.getTumlDbLocation();
            String parsedUrl = dbUrl;
            if (dbUrl.startsWith("local:")) {
                parsedUrl = dbUrl.replace("local:", "");
            }
            File dir = new File(parsedUrl);
            if (dir.exists()) {
                try {
                    logger.info(String.format("Deleting dir %s", new Object[]{dir.getAbsolutePath()}));
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void backupGraph() {
        try {
            String dbUrl = UmlgProperties.INSTANCE.getTumlDbLocation();
            String parsedUrl = dbUrl;
            if (dbUrl.startsWith("local:")) {
                parsedUrl = dbUrl.replace("local:", "");
            }
            File dir = new File(parsedUrl);
            if (dir.exists()) {
                try {
                    File backupDir = new File(dir.getParent(), dir.getName() + "-" + new SimpleDateFormat("ddMMyyyy_mmss").format(new Date()));
                    logger.info(String.format("Moving dir %s to %s", new Object[]{dir.getAbsolutePath(), backupDir.getAbsolutePath()}));
                    FileUtils.moveDirectory(dir, backupDir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
