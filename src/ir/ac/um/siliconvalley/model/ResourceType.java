package ir.ac.um.siliconvalley.model;

public enum ResourceType {

    DATA("Data Valley"),
    PATENT("IP Quarter"),
    CLOUD("Cloud Campus"),
    CAPITAL("Fintech District"),
    TALENT("AI Hub"),
    REGULATORY("Regulatory Zone"); // the blocker's place in the map.


    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}




