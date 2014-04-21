package org.umlg.runtime.restlet;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ServerResource;
import org.umlg.runtime.adaptor.UMLG;
import org.umlg.runtime.domain.UmlgNode;
import org.umlg.runtime.restlet.util.UmlgURLDecoder;

import java.io.IOException;

/**
 * Date: 2012/12/26
 * Time: 6:01 PM
 */
public class UmlgMetaQueryServerResourceImpl extends ServerResource {

    @Override
    public Representation get() {
        try {
            String id = UmlgURLDecoder.decode((String) getRequestAttributes().get("contextId"));
            UmlgNode parentResource = UMLG.get().instantiateClassifier(id);
            Object metaNodeId = parentResource.getMetaNode().getId();
            String metaQueryUri = "riap://application/" + getRootRef().getLastSegment() + "/baseclassumlgs/" + UmlgURLDecoder.encode(metaNodeId.toString()) + "/classQuery";
            ClientResource service = new ClientResource(metaQueryUri);
            service.setNext(getContext().getServerDispatcher());
            try {
                String s = service.get().getText();
                return new JsonRepresentation(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            UMLG.get().rollback();
        }
    }

    @Override
    public Representation options() {
        try {
            String id = UmlgURLDecoder.decode((String) getRequestAttributes().get("contextId"));
            UmlgNode parentResource = UMLG.get().instantiateClassifier(id);
            Object metaNodeId = parentResource.getMetaNode().getId();
            String metaQueryUri = "riap://application/" + getRootRef().getLastSegment() + "/baseclassumlgs/" + UmlgURLDecoder.encode(metaNodeId.toString()) + "/classQuery";
            ClientResource service = new ClientResource(metaQueryUri);
            service.setNext(getContext().getServerDispatcher());
            try {
                String s = service.options().getText();
                return new JsonRepresentation(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            UMLG.get().rollback();
        }
    }

    @Override
    public Representation post(Representation entity) {
        String id = UmlgURLDecoder.decode((String) getRequestAttributes().get("contextId"));
        UmlgNode parentResource = UMLG.get().instantiateClassifier(id);
        Object metaNodeId = parentResource.getMetaNode().getId();
        String metaQueryUri = "riap://application/" + getRootRef().getLastSegment() + "/baseclassumlgs/" + UmlgURLDecoder.encode(metaNodeId.toString()) + "/classQuery";
        ClientResource service = new ClientResource(metaQueryUri);
        service.setNext(getContext().getServerDispatcher());
        return service.post(entity);
    }

    @Override
    public Representation put(Representation entity) {
        String id = UmlgURLDecoder.decode((String) getRequestAttributes().get("contextId"));
        UmlgNode parentResource = UMLG.get().instantiateClassifier(id);
        Object metaNodeId = parentResource.getMetaNode().getId();
        String metaQueryUri = "riap://application/" + getRootRef().getLastSegment() + "/baseclassumlgs/" + UmlgURLDecoder.encode(metaNodeId.toString()) + "/classQuery";
        ClientResource service = new ClientResource(metaQueryUri);
        service.setNext(getContext().getServerDispatcher());
        return service.put(entity);
    }
}