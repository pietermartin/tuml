package org.tuml.testrestletminimal;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.tuml.framework.Visitor;
import org.tuml.generation.JavaGenerator;
import org.tuml.javageneration.DefaultVisitors;
import org.tuml.javageneration.TumlLibVisitors;
import org.tuml.restlet.generation.RestletVisitors;

public class GenerateTestRestletMinimal {
	public static void main(String[] args) throws URISyntaxException {
		JavaGenerator javaGenerator = new JavaGenerator();
        File modelFile = new File(Thread.currentThread().getContextClassLoader().getResource("test-restlet-minimal.uml").toURI());
		javaGenerator
				.generate(modelFile, new File("./test/test-restlet-minimal"), RestletVisitors.getDefaultJavaVisitors());
	}
}