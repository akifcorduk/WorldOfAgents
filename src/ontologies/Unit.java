package ontologies;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Unit
* @author ontology bean generator
* @version 2018/04/28, 18:40:42
*/
public class Unit implements Concept {

   /**
* Protege name: unitOwner
   */
   private Tribe unitOwner;
   public void setUnitOwner(Tribe value) { 
    this.unitOwner=value;
   }
   public Tribe getUnitOwner() {
     return this.unitOwner;
   }

   /**
* Protege name: position
   */
   private Position position;
   public void setPosition(Position value) { 
    this.position=value;
   }
   public Position getPosition() {
     return this.position;
   }

   /**
* Protege name: unitID
   */
   private AID unitID;
   public void setUnitID(AID value) { 
    this.unitID=value;
   }
   public AID getUnitID() {
     return this.unitID;
   }

}
