package model;

import java.sql.Timestamp;

public class Outage {
    private int outageId;
    private String areaName;
    private String outageType;
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private String cause;
    private Timestamp reportTime;
    private Timestamp estimatedRestoreTime;
    private Timestamp actualRestoreTime;
    private String status; // REPORTED, TECHNICIAN_ASSIGNED, REPAIR_STARTED, POWER_RESTORED
    private Integer assignedTechnicianId; // nullable

    public Outage() {}

    public Outage(int outageId, String areaName, String outageType, String priority, String cause,
                  Timestamp reportTime, Timestamp estimatedRestoreTime, Timestamp actualRestoreTime,
                  String status, Integer assignedTechnicianId) {
        this.outageId = outageId;
        this.areaName = areaName;
        this.outageType = outageType;
        this.priority = priority;
        this.cause = cause;
        this.reportTime = reportTime;
        this.estimatedRestoreTime = estimatedRestoreTime;
        this.actualRestoreTime = actualRestoreTime;
        this.status = status;
        this.assignedTechnicianId = assignedTechnicianId;
    }

    public Outage(String areaName, String outageType, String priority, String cause,
                  Timestamp estimatedRestoreTime, String status) {
        this.areaName = areaName;
        this.outageType = outageType;
        this.priority = priority;
        this.cause = cause;
        this.reportTime = new Timestamp(System.currentTimeMillis());
        this.estimatedRestoreTime = estimatedRestoreTime;
        this.status = status;
    }

    public int getOutageId() {
        return outageId;
    }

    public void setOutageId(int outageId) {
        this.outageId = outageId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getOutageType() {
        return outageType;
    }

    public void setOutageType(String outageType) {
        this.outageType = outageType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public Timestamp getReportTime() {
        return reportTime;
    }

    public void setReportTime(Timestamp reportTime) {
        this.reportTime = reportTime;
    }

    public Timestamp getEstimatedRestoreTime() {
        return estimatedRestoreTime;
    }

    public void setEstimatedRestoreTime(Timestamp estimatedRestoreTime) {
        this.estimatedRestoreTime = estimatedRestoreTime;
    }

    public Timestamp getActualRestoreTime() {
        return actualRestoreTime;
    }

    public void setActualRestoreTime(Timestamp actualRestoreTime) {
        this.actualRestoreTime = actualRestoreTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAssignedTechnicianId() {
        return assignedTechnicianId;
    }

    public void setAssignedTechnicianId(Integer assignedTechnicianId) {
        this.assignedTechnicianId = assignedTechnicianId;
    }

    @Override
    public String toString() {
        return "Outage #" + outageId + " [" + areaName + " - " + status + "]";
    }
}
