package org.tinker.collectiontest;

import com.tinkerpop.blueprints.pgm.Vertex;

import org.tinker.concretetest.God;
import org.tuml.runtime.adaptor.GraphDb;
import org.tuml.runtime.adaptor.TinkerIdUtilFactory;
import org.tuml.runtime.adaptor.TransactionThreadEntityVar;
import org.tuml.runtime.domain.BaseTinker;
import org.tuml.runtime.domain.CompositionNode;

public class Nightmare extends BaseTinker implements CompositionNode {


	/** Constructor for Nightmare
	 * 
	 * @param compositeOwner 
	 */
	public Nightmare(God compositeOwner) {
		this.vertex = GraphDb.getDb().addVertex("dribble");
		createComponents();
		init(compositeOwner);
		TransactionThreadEntityVar.setNewEntity(this);
		defaultCreate();
	}
	
	/** Constructor for Nightmare
	 * 
	 * @param vertex 
	 */
	public Nightmare(Vertex vertex) {
		this.vertex=vertex;
	}
	
	/** Default constructor for Nightmare
	 */
	public Nightmare() {
	}
	
	/** Constructor for Nightmare
	 * 
	 * @param persistent 
	 */
	public Nightmare(Boolean persistent) {
		this.vertex = GraphDb.getDb().addVertex("dribble");
		TransactionThreadEntityVar.setNewEntity(this);
		defaultCreate();
	}

	@Override
	public void clearCache() {
		this.name = null;
		this.nameNonUnique = null;
	}
	
	public void createComponents() {
	}
	
	@Override
	public Long getId() {
		return TinkerIdUtilFactory.getIdUtil().getId(this.vertex);
	}
	
	@Override
	public int getObjectVersion() {
		return TinkerIdUtilFactory.getIdUtil().getVersion(this.vertex);
	}
	
	public void init(God compositeOwner) {
		this.z_internalAddToGod(owner);
		this.hasInitBeenCalled = true;
		initVariables();
	}
	
	public void initVariables() {
	}
	
	@Override
	public boolean isTinkerRoot() {
		return false;
	}
	
	@Override
	public void setId(Long id) {
		TinkerIdUtilFactory.getIdUtil().setId(this.vertex, id);
	}

}