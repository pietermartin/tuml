package org.tuml.tumltest;

import org.tuml.javageneration.JavaGenerator;

public class TestAuditGeneration {

	public static void main(String[] args) {
		JavaGenerator.main(new String[]{"src/main/model/tinker-test.uml", "/home/pieter/workspace-tuml/tuml/test/tuml-test-audit", "true"});
	}
	
}
