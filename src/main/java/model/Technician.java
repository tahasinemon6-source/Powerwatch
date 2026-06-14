package model;

public class Technician {
    private int technicianId;
    private String technicianName;
    private String phone;
    private String teamName;
    private String availabilityStatus; // "AVAILABLE", "BUSY", "OFFLINE"

    public Technician() {}

    public Technician(int technicianId, String technicianName, String phone, String teamName, String availabilityStatus) {
        this.technicianId = technicianId;
        this.technicianName = technicianName;
        this.phone = phone;
        this.teamName = teamName;
        this.availabilityStatus = availabilityStatus;
    }

    public Technician(String technicianName, String phone, String teamName, String availabilityStatus) {
        this.technicianName = technicianName;
        this.phone = phone;
        this.teamName = teamName;
        this.availabilityStatus = availabilityStatus;
    }

    public int getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(int technicianId) {
        this.technicianId = technicianId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    @Override
    public String toString() {
        return technicianName + " (" + teamName + " - " + availabilityStatus + ")";
    }
}
