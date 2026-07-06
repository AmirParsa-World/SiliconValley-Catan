package model;

public class Sector {

    private final ResourceType resourceType;
    private final int activationNumber;
    private boolean isBlocked;


    public Sector ( ResourceType resourceType, int activationNumber ){

        this.resourceType = resourceType;
        this.activationNumber = activationNumber;
//        if (resourceType.equals(ResourceType.REGULATORY))
//            this.isBlocked = true;
//        else
//            this.isBlocked = false;
        this.isBlocked = (resourceType == ResourceType.REGULATORY); // very clearer.


    }
    // here are our getters.
    public ResourceType getResourceType(){
        return this.resourceType;
    }

    public int getActivationNumber() {
        return this.activationNumber;
    }

    public boolean isBlocked() {
        return this.isBlocked;
    }


    // and at the end, we should have 2 methods that change block state.
    public void block() {
        this.isBlocked = true;
    }

    public void unblock() {
        this.isBlocked = false;
    }
}
