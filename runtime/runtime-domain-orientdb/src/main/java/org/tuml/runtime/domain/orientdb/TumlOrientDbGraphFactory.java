package org.tuml.runtime.domain.orientdb;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.tuml.runtime.adaptor.TransactionThreadEntityVar;
import org.tuml.runtime.adaptor.TumlGraph;
import org.tuml.runtime.adaptor.TumlGraphFactory;

import java.io.File;

public class TumlOrientDbGraphFactory implements TumlGraphFactory {
	
	public static TumlOrientDbGraphFactory INSTANCE = new TumlOrientDbGraphFactory();
	
	private TumlOrientDbGraphFactory() {
	}
	
	public static TumlGraphFactory getInstance() {
		return INSTANCE;
	}
	
	@Override
	public TumlGraph getTumlGraph(String url) {
		File f = new File(url);
		OrientGraph db = new OrientGraph("local:" + f.getAbsolutePath());
		TransactionThreadEntityVar.remove();
		TumlGraph nakedGraph = new TumlOrientDbGraph(db);
		nakedGraph.addRoot();
		nakedGraph.registerListeners();		
		nakedGraph.commit();
		return nakedGraph;
	}

}
