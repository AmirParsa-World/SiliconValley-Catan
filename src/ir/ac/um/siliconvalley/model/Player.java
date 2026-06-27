package ir.ac.um.siliconvalley.model;

public class Player {

  
    private final String name;
    private final Map<ResourceType, Integer> resources;
    private final List<CompanyStructure> structures;

    public Player(String name) {
        this.name = name;
        this.resources = new EnumMap<>(ResourceType.class);
        this.structures = new ArrayList<>();

        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0);
        }
    }

    public String getName() {
        return name;
    }

    public Map<ResourceType, Integer> getResources() {
        return resources;
    }

    public List<CompanyStructure> getStructures() {
        return structures;
    }

    
    public void addResource(ResourceType type, int amount) {
        resources.put(type, resources.get(type) + amount);
    }

    public boolean removeResource(ResourceType type, int amount) {

        if (resources.get(type) < amount) {
            return false;
        }

        resources.put(type, resources.get(type) - amount);
        return true;
    }

    public int getResource(ResourceType type) {
        return resources.get(type);
    }

    public int getTotalResources() {

        int total = 0;

        for (int amount : resources.values()) {
            total += amount;
        }

        return total;
    }

    public void addStructure(CompanyStructure structure) {
        structures.add(structure);
    }

    public int getVictoryPoints() {

        int points = 0;

        for (CompanyStructure structure : structures) {
            points += structure.getVictoryPoints();
        }

        return points;
    }

    @Override
    public String toString() {
        return name +
                " | Points: " + getVictoryPoints() +
                " | Resources: " + resources;
    }
}
