package group3;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import ontologies.*;


public class AgPlatform3 extends Agent{
    private Codec codec = new SLCodec();
    private Ontology ontology = WoAOntology.getInstance();

    public final static String REGISTRATION = "RegistrationDesk";
    public final static String ENTITIY = "EntityManager";
    public final static String WORLD = "World";
    protected static ArrayList<Integer> registeredIDs;
    //protected static ArrayList<TribeRepresentation> registeredTribes;
    private EntityManager entityManager;

    public enum Phase {
        REGISTRATION,
        INITIALIZATION,
        GAME,
        CELEBRATION
    }



    Phase currentPhase = Phase.REGISTRATION;

    @Override
    protected void setup() {
        this.entityManager = new EntityManager();
        System.out.println(getLocalName() + " - has entered into the system");
        registeredIDs = new ArrayList<>();
        //registeredTribes = new ArrayList<>();
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);


        try {
            // Creates its own description
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd1 = new ServiceDescription();
            ServiceDescription sd2 = new ServiceDescription();
            ServiceDescription sd3 = new ServiceDescription();
//            sd.setName(this.getName());
            sd1.setName(REGISTRATION);
            sd1.setType(REGISTRATION);
            dfd.addServices(sd1);
            sd2.setName(WORLD);
            sd2.setType(WORLD);
            dfd.addServices(sd2);
            sd3.setName(ENTITIY);
            sd3.setType(ENTITIY);
            dfd.addServices(sd3);
            // Registers its description in the DF
            DFService.register(this, dfd);

            dfd = null;


            doWait(5000);

        } catch (FIPAException e) {
            e.printStackTrace();
        }

        Date start = new Date();

        addBehaviour(new CyclicBehaviour(this) {

        	@Override
            public void action() {

                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REFUSE).MatchProtocol("requestRegistration"));
                if (((new Date()).getTime() - start.getTime() >= 20000)&& currentPhase == Phase.REGISTRATION) {
                	currentPhase = Phase.INITIALIZATION;
                	return;
                }
                if(msg != null){
                    try {
                        ContentElement contentElement = null;
                        if(msg.getPerformative() == ACLMessage.REQUEST){
                            contentElement = getContentManager().extractContent(msg);
                                if(contentElement instanceof Action) {
                                    Action agAction = (Action) contentElement;
                                    Concept conc =  agAction.getAction();

                                    if(conc instanceof TribeRegistration){
                                        System.out.println(myAgent.getLocalName() + " - received registration request from " + (msg.getSender()).getLocalName());
                                        int id = ((TribeRegistration) conc).getID();
                                        if(!registeredIDs.contains(id)) {
                                            registeredIDs.add(id);

                                            entityManager.registeredTribes.add(new TribeRepresentation(id, "Tribe_" + id, msg.getSender()));
                                            ACLMessage reply = msg.createReply();

                                            reply.setLanguage(codec.getName());
                                            reply.setOntology(ontology.getName());
                                            reply.setProtocol("agreeRegistration");

                                            reply.setPerformative(ACLMessage.AGREE);

                                            myAgent.send(reply);
                                            System.out.println(myAgent.getLocalName() + " - agreed to register tribe with id " + id );

                                            doWait(1000);


                                            ACLMessage msgInform = new ACLMessage(ACLMessage.INFORM);
                                            msgInform.addReceiver(msg.getSender());
                                            msgInform.setLanguage(codec.getName());
                                            msgInform.setOntology(ontology.getName());
                                            msgInform.setProtocol("InformName");


                                            Tribe tribe = new Tribe();
                                            tribe.setName("Tribe" + id);
                                            // As it is an action and the encoding language the SL, it must be wrapped
                                            // into an Action
                                            Action informAction = new Action(msg.getSender(), tribe);

                                            try {
                                                // The ContentManager transforms the java objects into strings
                                                getContentManager().fillContent(msgInform, informAction);
                                                send(msgInform);
                                                System.out.println(msg.getSender() + " - informed");
                                            } catch (CodecException ce) {
                                                ce.printStackTrace();
                                            } catch (OntologyException oe) {
                                                oe.printStackTrace();
                                            }




                                        }else{
                                            ACLMessage reply = msg.createReply();

                                            reply.setLanguage(codec.getName());
                                            reply.setOntology(ontology.getName());
                                            reply.setProtocol("refuseRegistration");
                                            reply.setPerformative(ACLMessage.REFUSE);

                                            myAgent.send(reply);
                                            System.out.println(myAgent.getLocalName() + " - refused to register tribe with id " + id );
                                        }
                                    }
                                }


                        }
                    } catch (CodecException e) {
                        e.printStackTrace();
                    } catch (OntologyException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        // Behaviour that initializes the game after the phase was changed to initialization
        // Maybe it's not good to always check it in a behaviour, maybe we can move the function call to a different place
        // For now, i.e. for debug/test purposes, this function is just once executed after 7 seconds.
        addBehaviour(new SimpleBehaviour() {

                         @Override
                         public void action() {

                             if (currentPhase == Phase.INITIALIZATION){
                                // doWait(7000);
                                currentPhase = Phase.GAME; // Just for debug purposes
                                initializeGame();
                             }
                         }

                         @Override
                         public boolean done() {
                             return false;
                         }
                     }

        );

        //unit creation behavior
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST).MatchProtocol("requestCreateUnit"));
                if(msg != null) {
                    try {
                        ContentElement contentElement = null;
                        contentElement = getContentManager().extractContent(msg);
                        if(contentElement instanceof Action) {
                            Action agAction = (Action) contentElement;
                            Concept conc = agAction.getAction();

                            if (conc instanceof CreateUnit) {
                                System.out.println(myAgent.getLocalName() + " - received create unit request from " + (msg.getSender()).getLocalName());
                                AID unitID = msg.getSender();
                                EntityManager.UnitCreation response = entityManager.createUnit(unitID,getContainerController());
                                if(response.agentID != null){
                                    ACLMessage reply = msg.createReply();

                                    reply.setLanguage(codec.getName());
                                    reply.setOntology(ontology.getName());
                                    reply.setProtocol("agreeCreateUnit");
                                    reply.setPerformative(ACLMessage.AGREE);

                                    myAgent.send(reply);
                                    System.out.println(myAgent.getLocalName() + " - agreed to create unit"  );

                                    ACLMessage msgInform = new ACLMessage(ACLMessage.INFORM);
                                    msgInform.addReceiver(msg.getSender());
                                    msgInform.setLanguage(codec.getName());
                                    msgInform.setOntology(ontology.getName());
                                    msgInform.setProtocol("informUnitCreation");

                                    Action informAction = new Action(msg.getSender(), response.unit);
                                    getContentManager().fillContent(msgInform, informAction);
                                        // The ContentManager transforms the java objects into strings
                                    send(msgInform);
                                    System.out.println(msg.getSender() + " - informed about unit creation");

                                }
                                else{
                                    Reason reason = new Reason();
                                    reason.setDescription(response.reason);
                                    Action returnAction = new Action(msg.getSender(),reason);


                                    ACLMessage reply = msg.createReply();

                                    reply.setLanguage(codec.getName());
                                    reply.setOntology(ontology.getName());
                                    reply.setProtocol("refuseCreateUnit");
                                    reply.setPerformative(ACLMessage.REFUSE);
                                    getContentManager().fillContent(reply,returnAction);

                                    myAgent.send(reply);
                                    System.out.println(myAgent.getLocalName() + " - refused to create unit with the reason: )" + response.reason);
                                }


                            }
                        }

                    } catch (Exception e){
                        System.out.println(e.getStackTrace());
                    }
                }
            }
        });

    }

    private boolean killUnit(AID aid){
        // check if unit exists; should as well check if agent is really a unit


        // unsubscribe unit from informs if it is subscribed

        if (entityManager.subscribedAgents.contains(aid)){
            for (int i=0; i<entityManager.subscribedAgents.size(); i++){
                if (entityManager.subscribedAgents.get(i) == aid){
                    entityManager.subscribedAgents.remove(i);
                    break;
                    // Be aware: If an agent is subscribed multiple times, it only gets deleted once
                    // So a agent should be able to subscribe only once
                }
            }}

        // send kill to unit
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(aid);
        msg.setLanguage(codec.getName());
        msg.setOntology(ontology.getName());
        // Check if Protocol name is correct!!
        msg.setProtocol("killUnit");
        KillYourself killYourselfPlease = new KillYourself();

        // As it is an action and the encoding language the SL, it must be wrapped
        // into an Action
        Action agAction = new Action(aid, killYourselfPlease);

        try {
            // The ContentManager transforms the java objects into strings
            getContentManager().fillContent(msg, agAction);
            send(msg);
            System.out.println(this.getLocalName() + " - inform unit "+aid.getLocalName()+" to kill itself");
        } catch (CodecException ce) {
            ce.printStackTrace();
            return false;
        } catch (OntologyException oe) {
            oe.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Informs all agents that are entitled to be informed about a specific change
     * @return success
     */
    private boolean informChange(/*Type of change*/){
        ArrayList<AID> receivers = new ArrayList<>();
       // Check  which subscribers are entitled to receive this information

        // for debug purposes, send information to all subscribers
        for (AID aid: entityManager.subscribedAgents) {
            receivers.add(aid);
        }

        // Sends the information to the receivers that were selected
       informChange(receivers /*, Type of change*/);
        return true;
    }

    /**
     * Send one update to one specific agent.
     * @param receiver Agent
     * @return success
     */
    private boolean informChange(ArrayList<AID> receiver /*, Type of change*/){



        return true;
    }



    /**
     * Initialization function that is executed after the registration deadline is reached.
     * Map and everything needed is generated. This probably should include starting of all other needed agents.
     * After termination of the initialization the game is started.
     * @return
     */
    private boolean initializeGame(){
        System.out.println(this.getLocalName() + "Initialization phase started.");
        // Iterate over tribes and assign initial resources to each
        for (Integer tribeID :this.registeredIDs){
            // Position has to be specified later depending on amount of players, size of map etc.
            Position position = new Position();
            position.setX(0);
            position.setY(0);
            initialAllocation(tribeID, position);
        }


        System.out.println(this.getLocalName() + "Initialization phase finished.");
        return true;
    }



    /**
     * Assign the initial resources to a tribe. This means initial units and resources (gold, stone, wood).
     * Afterwards the tribe should be informed.
     * The amount of units and resources should be specified in the configuration file.
     * @param tribeId Id of the tribe to whom the resources are allocated
     * @return success
     */
    private boolean initialAllocation(int tribeId, Position position){

        // Initial resources defined in test case. They should be fetched out of the configuration file.
        int amountUnits = 1;
        int amountGold = 125;
        int amountStone = 75;
        int amountFood = 3;
        int amountWood = 50;

        // Create concepts for resources
        Resource gold = new Resource();
        gold.setAmount(amountGold);
        gold.setType("gold");

        Resource stone = new Resource();
        stone.setAmount(amountStone);
        stone.setType("stone");

        Resource food = new Resource();
        food.setAmount(amountFood);
        food.setType("food");

        Resource wood = new Resource();
        wood.setAmount(amountGold);
        wood.setType("gold");


        // Assign initial resources in worldManager

        // Create units

        // Inform tribe.
        AID ag = null;
        Tribe receivingTribe = null;
        TribeRepresentation ownerTribe = null;
        for (TribeRepresentation tribe: entityManager.registeredTribes){
            if (tribe.getTribe().getID() == tribeId){
                ag = tribe.getAgentId();
                receivingTribe = tribe.getTribe();
                ownerTribe = tribe;
                tribe.addFood(food.getAmount());
                tribe.addGold(gold.getAmount());
                tribe.addStone(stone.getAmount());
                tribe.addWood(wood.getAmount());
            }
        }
        if (ag == null || receivingTribe == null) {
            // Tribe with tribeId isn't registered
            System.out.println("Error: Tribe with this ID isn't registered.");
            return false;
        }

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(ag);
        msg.setLanguage(codec.getName());
        msg.setOntology(ontology.getName());
        msg.setProtocol("informInit");
        Allocate allocation = new Allocate();


        allocation.addInitialResources(gold);
        allocation.addInitialResources(stone);
        allocation.addInitialResources(food);


        // In the following part the units agents are created
        AgentContainer container = this.getContainerController();
        AgentController agents;
        String unitAgentName;

        Unit newUnit;
        for (int i =0; i< amountUnits; i++)
        {
            newUnit = new Unit();
            newUnit.setPosition(position);
            try {
                unitAgentName =receivingTribe.getName()+"Unit"+i;
                agents = container.createNewAgent(unitAgentName, "group3.AgUnit3", null);
                agents.start();
                // TODO: Configure unit, i.e. set owner of unit and register unit in world manager
                // TODO: Check if this gets the correct AID, I'm not really sure
                newUnit.setUnitID(new AID(agents.getName()));
                newUnit.setUnitOwner(receivingTribe);
                ownerTribe.registeredUnits.add(newUnit);
                allocation.addInitialUnits(newUnit);
                //killUnit(newUnit.getUnitID());
            } catch (StaleProxyException e) {
                e.printStackTrace();

        }}




        // As it is an action and the encoding language the SL, it must be wrapped
        // into an Action
        Action agAction = new Action(ag, allocation);

        try {
            // The ContentManager transforms the java objects into strings
            getContentManager().fillContent(msg, agAction);

            send(msg);
            System.out.println(this.getLocalName() + " - inform tribe about intital resources");
        } catch (CodecException ce) {
            ce.printStackTrace();
        } catch (OntologyException oe) {
            oe.printStackTrace();
        }





        return true;
    }

}
