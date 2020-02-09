package group3;

import jade.core.AID;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import ontologies.Position;
import ontologies.Tribe;
import ontologies.Unit;

import java.util.ArrayList;

public class EntityManager {

    // Registered Tribes
    protected ArrayList<TribeRepresentation> registeredTribes;

    protected ArrayList<AID> subscribedAgents;

    // Representation of the board/map
    // TODO: Add representation of board, like array
    private Map map;
    private static int unitFood;
    private static int unitGold;
    //
    public class UnitCreation{
        protected AID agentID = null;
        protected String reason = null;
        protected Unit unit = null;
    }



    /**
     * Constructur
     */
    public EntityManager() {
        this.registeredTribes = new ArrayList<>();
        this.subscribedAgents = new ArrayList<>();
        this.map = new Map(10,10);
        unitFood = 0;
        unitGold = 0;
    }

    protected UnitCreation createUnit(AID unitID, ContainerController container){
        UnitCreation response = new UnitCreation();
        for(TribeRepresentation tribe: registeredTribes){
            for(Unit unit : tribe.registeredUnits){
                if(unit.getUnitID().getName() == unitID.getName()){
                    if(tribe.getFood()>= unitFood && tribe.getGold() >= unitGold){
                        Position unitPostion = unit.getPosition();
                        MapElement mapElement = map.getMapElement(unitPostion);
                        //if(mapElement.type == MapElement.ElementType.TOWNHALL && mapElement.owner == tribe){
                        if(true){
                            AgentController agents;
                            String unitAgentName;
                            Unit newUnit = new Unit();
                            newUnit.setPosition(unitPostion);
                            try {
                                unitAgentName =tribe.getTribe().getName()+"Unit"+tribe.registeredUnits.size(); //todo find a proper unit name
                                agents = container.createNewAgent(unitAgentName, "group3.AgUnit3", null);

                                newUnit.setUnitID(new AID(agents.getName()));
                                newUnit.setUnitOwner(tribe.getTribe());

                                response.agentID = newUnit.getUnitID();
                                response.unit = newUnit;
                                tribe.registeredUnits.add(newUnit);
                                tribe.removeFood(unitFood);
                                tribe.removeGold(unitGold);
                                agents.start();

                            } catch (StaleProxyException e) {
                                e.printStackTrace();

                            }
                            // create agent
                            // get aıd
                            // return aıd
                            return response;
                        }
                        else{
                            response.reason="LOCATION";
                            return response;
                        }

                    }else{
                        response.reason="RESOURCES";
                        return response;
                    }
                }
            }
        }
        return null;
    }


}
