package org.tuml;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import java.util.Set;
import java.util.UUID;

import org.tuml.runtime.adaptor.GraphDb;
import org.tuml.runtime.adaptor.TinkerIdUtilFactory;
import org.tuml.runtime.collection.TinkerSet;
import org.tuml.runtime.collection.TinkerSetImpl;
import org.tuml.runtime.collection.TumlRuntimeProperty;
import org.tuml.runtime.domain.BaseTinker;
import org.tuml.runtime.domain.TinkerNode;

public class InterfaceRealization1 extends BaseTinker implements TinkerNode, Interface1 {
	private TinkerSet<String> name;
	private TinkerSet<Interface2> interface2;

	/** Constructor for InterfaceRealization1
	 * 
	 * @param vertex 
	 */
	public InterfaceRealization1(Vertex vertex) {
		this.vertex=vertex;
		initialiseProperties();
	}
	
	/** Default constructor for InterfaceRealization1
	 */
	public InterfaceRealization1() {
	}
	
	/** Constructor for InterfaceRealization1
	 * 
	 * @param persistent 
	 */
	public InterfaceRealization1(Boolean persistent) {
		this.vertex = GraphDb.getDb().addVertex("dribble");
		defaultCreate();
		initialiseProperties();
		createComponents();
		Edge edge = GraphDb.getDb().addEdge(null, GraphDb.getDb().getRoot(), this.vertex, "root");
		edge.setProperty("inClass", this.getClass().getName());
	}

	public void addToInterface2(Interface2 interface2) {
		if ( interface2 != null ) {
			this.interface2.add(interface2);
		}
	}
	
	public void addToInterface2(Set<Interface2> interface2) {
		if ( !interface2.isEmpty() ) {
			this.interface2.addAll(interface2);
		}
	}
	
	public void addToName(String name) {
		if ( name != null ) {
			this.name.add(name);
		}
	}
	
	public void clearInterface2() {
		this.interface2.clear();
	}
	
	public void clearName() {
		this.name.clear();
	}
	
	public void createComponents() {
	}
	
	@Override
	public void delete() {
	}
	
	@Override
	public Long getId() {
		return TinkerIdUtilFactory.getIdUtil().getId(this.vertex);
	}
	
	public TinkerSet<Interface2> getInterface2() {
		return this.interface2;
	}
	
	public String getName() {
		TinkerSet<String> tmp = this.name;
		if ( !tmp.isEmpty() ) {
			return tmp.iterator().next();
		} else {
			return null;
		}
	}
	
	@Override
	public int getObjectVersion() {
		return TinkerIdUtilFactory.getIdUtil().getVersion(this.vertex);
	}
	
	@Override
	public String getUid() {
		String uid = (String) this.vertex.getProperty("uid");
		if ( uid==null || uid.trim().length()==0 ) {
			uid=UUID.randomUUID().toString();
			this.vertex.setProperty("uid", uid);
		}
		return uid;
	}
	
	public void initVariables() {
	}
	
	@Override
	public void initialiseProperties() {
		this.name =  new TinkerSetImpl<String>(this, InterfaceRealization1RuntimePropertyEnum.name);
		this.interface2 =  new TinkerSetImpl<Interface2>(this, InterfaceRealization1RuntimePropertyEnum.interface2);
	}
	
	@Override
	public void initialiseProperty(TumlRuntimeProperty tumlRuntimeProperty) {
		switch ( (InterfaceRealization1RuntimePropertyEnum.fromLabel(tumlRuntimeProperty.getLabel())) ) {
			case interface2:
				this.interface2 =  new TinkerSetImpl<Interface2>(this, InterfaceRealization1RuntimePropertyEnum.interface2);
			break;
		
			case name:
				this.name =  new TinkerSetImpl<String>(this, InterfaceRealization1RuntimePropertyEnum.name);
			break;
		
		}
	}
	
	@Override
	public boolean isTinkerRoot() {
		return true;
	}
	
	public void removeFromInterface2(Interface2 interface2) {
		if ( interface2 != null ) {
			this.interface2.remove(interface2);
		}
	}
	
	public void removeFromInterface2(Set<Interface2> interface2) {
		if ( !interface2.isEmpty() ) {
			this.interface2.removeAll(interface2);
		}
	}
	
	public void removeFromName(Set<String> name) {
		if ( !name.isEmpty() ) {
			this.name.removeAll(name);
		}
	}
	
	public void removeFromName(String name) {
		if ( name != null ) {
			this.name.remove(name);
		}
	}
	
	@Override
	public void setId(Long id) {
		TinkerIdUtilFactory.getIdUtil().setId(this.vertex, id);
	}
	
	public void setInterface2(Set<Interface2> interface2) {
		clearInterface2();
		addToInterface2(interface2);
	}
	
	public void setName(String name) {
		clearName();
		addToName(name);
	}

	public enum InterfaceRealization1RuntimePropertyEnum implements TumlRuntimeProperty {
		name(true,false,"org__tuml__Interface1__name",false,false,true,false,1,1),
		interface2(true,true,"A_<interface1>_<interface2>",false,true,false,false,-1,0);
		private boolean controllingSide;
		private boolean composite;
		private String label;
		private boolean oneToOne;
		private boolean oneToMany;
		private boolean manyToOne;
		private boolean manyToMany;
		private int upper;
		private int lower;
		/** Constructor for InterfaceRealization1RuntimePropertyEnum
		 * 
		 * @param controllingSide 
		 * @param composite 
		 * @param label 
		 * @param oneToOne 
		 * @param oneToMany 
		 * @param manyToOne 
		 * @param manyToMany 
		 * @param upper 
		 * @param lower 
		 */
		private InterfaceRealization1RuntimePropertyEnum(boolean controllingSide, boolean composite, String label, boolean oneToOne, boolean oneToMany, boolean manyToOne, boolean manyToMany, int upper, int lower) {
			this.controllingSide = controllingSide;
			this.composite = composite;
			this.label = label;
			this.oneToOne = oneToOne;
			this.oneToMany = oneToMany;
			this.manyToOne = manyToOne;
			this.manyToMany = manyToMany;
			this.upper = upper;
			this.lower = lower;
		}
	
		static public InterfaceRealization1RuntimePropertyEnum fromLabel(String label) {
			if ( name.getLabel().equals(label) ) {
				return name;
			}
			if ( interface2.getLabel().equals(label) ) {
				return interface2;
			}
			throw new IllegalStateException();
		}
		
		public String getLabel() {
			return this.label;
		}
		
		public int getLower() {
			return this.lower;
		}
		
		public int getUpper() {
			return this.upper;
		}
		
		public boolean isComposite() {
			return this.composite;
		}
		
		public boolean isControllingSide() {
			return this.controllingSide;
		}
		
		public boolean isManyToMany() {
			return this.manyToMany;
		}
		
		public boolean isManyToOne() {
			return this.manyToOne;
		}
		
		public boolean isOneToMany() {
			return this.oneToMany;
		}
		
		public boolean isOneToOne() {
			return this.oneToOne;
		}
		
		@Override
		public boolean isValid(int elementCount) {
			return (getUpper() == -1 || elementCount <= getUpper()) && elementCount >= getLower();
		}
	
	}
}