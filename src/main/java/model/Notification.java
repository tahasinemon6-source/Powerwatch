package model;

import java.sql.Timestamp;

public class Notification {
    private int notificationId;
    private Integer outageId; // can be null
    private String message;
    private Timestamp createdTime;

    public Notification() {}

    public Notification(int notificationId, Integer outageId, String message, Timestamp createdTime) {
        this.notificationId = notificationId;
        this.outageId = outageId;
        this.message = message;
        this.createdTime = createdTime;
    }

    public Notification(Integer outageId, String message) {
        this.outageId = outageId;
        this.message = message;
        this.createdTime = new Timestamp(System.currentTimeMillis());
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public Integer getOutageId() {
        return outageId;
    }

    public void setOutageId(Integer outageId) {
        this.outageId = outageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "[" + createdTime + "] " + message;
    }
}
