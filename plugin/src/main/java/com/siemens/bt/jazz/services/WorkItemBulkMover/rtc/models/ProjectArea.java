package com.siemens.bt.jazz.services.WorkItemBulkMover.rtc.models;

public class ProjectArea {
    private String id;
    private String name;

    public ProjectArea() {}
    public ProjectArea(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
