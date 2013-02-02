package org.tuml.restlet.client.json;

/**
 * Date: 2013/02/02
 * Time: 12:24 PM
 */
public class PropertyJson {

    private String name;
    private Object value;

    public PropertyJson(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String toJson() {
        return "\"" + this.name + "\": \"" + this.value + "\"";
    }
}
