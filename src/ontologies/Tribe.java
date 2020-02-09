package ontologies;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Tribe
* @author ontology bean generator
* @version 2018/04/28, 18:40:42
*/
public class Tribe implements Concept {

   /**
* Protege name: ID
   */
   private int iD;
   public void setID(int value) { 
    this.iD=value;
   }
   public int getID() {
     return this.iD;
   }

   /**
* Protege name: Name
   */
   private String name;
   public void setName(String value) { 
    this.name=value;
   }
   public String getName() {
     return this.name;
   }

}
