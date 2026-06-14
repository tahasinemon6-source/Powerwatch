package model;

public class Infrastructure {
    private int infrastructureId;
    private String name;
    private String areaName;
    private String type; // "Hospital", "Police Station", "Fire Station"

    public Infrastructure() {}

    public Infrastructure(int infrastructureId, String name, String areaName, String type) {
        this.infrastructureId = infrastructureId;
        this.name = name;
        this.areaName = areaName;
        this.type = type;
    }

    public Infrastructure(String name, String areaName, String type) {
        this.name = name;
        this.areaName = areaName;
        this.type = type;
    }

    public int getInfrastructureId() {
        return infrastructureId;
    }

    public void setInfrastructureId(int infrastructureId) {
        this.infrastructureId = infrastructureId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name + " (" + type + " in " + areaName + ")";
    }
}
