package org.tuml.runtime.restlet;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.service.StatusService;
import org.tuml.runtime.validation.TumlConstraintViolationException;

import java.util.Map;
import java.util.TreeMap;

/**
 * Date: 2013/02/08
 * Time: 3:21 PM
 */
public class ErrorStatusService extends StatusService {

    @Override
    public Representation getRepresentation(Status status, Request request, Response response) {
        StringBuilder sb = new StringBuilder();
        Throwable throwable = status.getThrowable();
             sb.append(throwable.getMessage());
        return new StringRepresentation(sb.toString());
    }

}
