package org.umlg.runtime.domain.activity;

import com.tinkerpop.blueprints.Vertex;
import org.umlg.runtime.collection.Qualifier;
import org.umlg.runtime.collection.TinkerSet;
import org.umlg.runtime.collection.UmlgRuntimeProperty;
import org.umlg.runtime.domain.UmlgMetaNode;
import org.umlg.runtime.domain.UmlgNode;
import org.umlg.runtime.validation.UmlgConstraintViolation;

import java.util.List;
import java.util.Map;

public class CallEvent extends Event {

	private static final long serialVersionUID = -467486969342220483L;

	public CallEvent(String name) {
		super(name);
	}

	public CallEvent(Vertex vertex) {
		super(vertex);
	}

	@Override
	public void initialiseProperties() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialiseProperty(UmlgRuntimeProperty umlgRuntimeProperty, boolean inverse) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public UmlgRuntimeProperty inverseAdder(UmlgRuntimeProperty umlgRuntimeProperty, boolean inverse, UmlgNode umlgNode) {
        //To change body of implemented methods use File | Settings | File Templates.
        return null;
    }

    @Override
    public void initVariables() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void delete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Qualifier> getQualifiers(UmlgRuntimeProperty umlgRuntimeProperty, UmlgNode node, boolean inverse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSize(UmlgRuntimeProperty umlgRuntimeProperty) {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public Object getId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String toJson(Boolean deep) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String toJsonWithoutCompositeParent(Boolean deep) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public String toJson() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromJson(String json) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromJson(Map<String, Object> propertyMap) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void fromJsonDataTypeAndComposite(Map<String, Object> propertyMap) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void fromJsonNonCompositeOne(Map<String, Object> propertyMap) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public <E> TinkerSet<E> asSet() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public List<UmlgConstraintViolation> validateMultiplicities() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<UmlgConstraintViolation> checkClassConstraints() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public String toJsonWithoutCompositeParent() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public String getMetaDataAsJson() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public UmlgNode getOwningObject() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public boolean hasOnlyOneCompositeParent() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addEdgeToMetaNode() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UmlgMetaNode getMetaNode() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public List<UmlgNode> getPathToCompositionalRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQualifiedName() {
		// TODO Auto-generated method stub
		return null;
	}

}
