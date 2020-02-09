package ontologies;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Allocate
* @author ontology bean generator
* @version 2018/04/28, 18:40:42
*/
public class Allocate implements AgentAction {

   /**
* Protege name: initialUnits
   */
   private List initialUnits = new ArrayList();
   public void addInitialUnits(Unit elem) { 
     List oldList = this.initialUnits;
     initialUnits.add(elem);
   }
   public boolean removeInitialUnits(Unit elem) {
     List oldList = this.initialUnits;
     boolean result = initialUnits.remove(elem);
     return result;
   }
   public void clearAllInitialUnits() {
     List oldList = this.initialUnits;
     initialUnits.clear();
   }
   public Iterator getAllInitialUnits() {return initialUnits.iterator(); }
   public List getInitialUnits() {return initialUnits; }
   public void setInitialUnits(List l) {initialUnits = l; }

   /**
* Protege name: initialResources
   */
   private List initialResources = new ArrayList();
   public void addInitialResources(Resource elem) { 
     List oldList = this.initialResources;
     initialResources.add(elem);
   }
   public boolean removeInitialResources(Resource elem) {
     List oldList = this.initialResources;
     boolean result = initialResources.remove(elem);
     return result;
   }
   public void clearAllInitialResources() {
     List oldList = this.initialResources;
     initialResources.clear();
   }
   public Iterator getAllInitialResources() {return initialResources.iterator(); }
   public List getInitialResources() {return initialResources; }
   public void setInitialResources(List l) {initialResources = l; }

}
