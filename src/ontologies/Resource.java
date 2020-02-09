package ontologies;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Resource
* @author ontology bean generator
* @version 2018/04/28, 18:40:42
*/
public class Resource implements Concept {

   /**
* Protege name: amount
   */
   private int amount;
   public void setAmount(int value) { 
    this.amount=value;
   }
   public int getAmount() {
     return this.amount;
   }

   /**
* Protege name: type
   */
   private String type;
   public void setType(String value) { 
    this.type=value;
   }
   public String getType() {
     return this.type;
   }

}
