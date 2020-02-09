package group3;

import ontologies.Tribe;

public class MapElement {
    public enum ElementType{
        EMPTY,
        ORE,
        WOOD,
        TOWNHALL,
        FARM
    }

    protected ElementType type;
    protected TribeRepresentation owner;

    public MapElement(ElementType type){
        this.type = type;
        this.owner = null;
    }

}
