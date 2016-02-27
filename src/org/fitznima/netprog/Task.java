package org.fitznima.netprog;

/**
 * Created by FitzRoi on 2/26/16.
 */
public class Task {
    private String name;
    private String startTime;
    private String endTime;

    public Task(){}
    public Task(String name, String startTime, String endTime){
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void print(){
        System.out.print("Task: " + getName() +"; startTime:" + startTime +"; endTime: " + endTime +"\n");
    }
}
