package com.siemens.bt.jazz.services.WorkItemBulkMover.rtc.models;


public class WorkItem {
    private int id;
    private String title;
    private String uri;

    public WorkItem(int id, String title, String uri) {
        this.id = id;
        this.title = title;
        this.uri = uri;
    }

    public int getId() {
        return id;
    }
}
