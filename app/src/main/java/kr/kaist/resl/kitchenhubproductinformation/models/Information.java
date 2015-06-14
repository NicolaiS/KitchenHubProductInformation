package kr.kaist.resl.kitchenhubproductinformation.models;

import java.util.List;

/**
 * Model of information from Product information service
 */

public class Information {

    private String urn = null;
    private String message = null;
    private Integer version = null;
    private List<Attribute> attributes = null;
    private List<Container> containers = null;

    public Information() {

    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

}
