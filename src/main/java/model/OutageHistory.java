package model;

import java.sql.Timestamp;

public class OutageHistory {
    private int historyId;
    private int outageId;
    private String oldStatus;
    private String newStatus;
    private String updatedBy;
    private Timestamp updateTime;

    public OutageHistory() {}

    public OutageHistory(int historyId, int outageId, String oldStatus, String newStatus, String updatedBy, Timestamp updateTime) {
        this.historyId = historyId;
        this.outageId = outageId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.updatedBy = updatedBy;
        this.updateTime = updateTime;
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getOutageId() {
        return outageId;
    }

    public void setOutageId(int outageId) {
        this.outageId = outageId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return oldStatus + " -> " + newStatus + " (by " + updatedBy + " on " + updateTime + ")";
    }
}
