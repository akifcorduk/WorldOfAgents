package ontologies;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Position
* @author ontology bean generator
* @version 2018/04/28, 18:40:42
*/
public class Position implements Concept {

   /**
* Protege name: y
   */
   private int y;
   public void setY(int value) { 
    this.y=value;
   }
   public int getY() {
     return this.y;
   }

   /**
* Protege name: x
   */
   private int x;
   public void setX(int value) { 
    this.x=value;
   }
   public int getX() {
     return this.x;
   }

}
