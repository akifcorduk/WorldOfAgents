package group3;

import jade.core.AID;
import ontologies.Tribe;
import ontologies.Unit;

import java.util.ArrayList;


public class TribeRepresentation {
    private AID agentId;
    private Tribe tribe;
    private int gold;
    private int wood;
    private int food;
    private int stone;

    // All registered, active units
    protected ArrayList<Unit> registeredUnits;


    public void addGold(int amount) {
        gold += amount;
    }

    public void addStone(int amount) {
        gold += amount;
    }
    public void addFood(int amount) {
        gold += amount;
    }
    public void addWood(int amount) {
        gold += amount;
    }

    public void removeGold(int amount) {
        gold -= amount;
    }

    public void removeStone(int amount) {
        gold -= amount;
    }
    public void removeFood(int amount) {
        gold -= amount;
    }
    public void removeWood(int amount) {
        gold -= amount;
    }

    public AID getAgentId() {
        return agentId;
    }

    public void setAgentId(AID agentId) {
        this.agentId = agentId;
    }

    public Tribe getTribe() {
        return tribe;
    }

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getWood() {
        return wood;
    }

    public void setWood(int wood) {
        this.wood = wood;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getStone() {
        return stone;
    }

    public void setStone(int stone) {
        this.stone = stone;
    }




    TribeRepresentation(int id, String name, AID agentID){
        this.agentId = agentID;
        this.tribe = new Tribe();
        this.tribe.setID(id);
        this.tribe.setName(name);
        this.registeredUnits = new ArrayList<>();
    }

}