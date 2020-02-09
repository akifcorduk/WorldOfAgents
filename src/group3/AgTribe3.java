package group3;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

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
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontologies.*;

@SuppressWarnings("serial")
public class AgTribe3 extends Agent {

    private Codec codec = new SLCodec();
    private Ontology ontology = WoAOntology.getInstance();

    public final static String REGISTRATION = "RegistrationDesk";
    public final static int ID = 3;

    protected static ArrayList<Unit> registeredUnits;
    protected static boolean confirmRegister = false;
    protected static boolean registered = false;
    public static String name;


    @Override
    protected void setup() {
        System.out.println(getLocalName() + " - has entered");

        registeredUnits = new ArrayList<>();

        // Register of the codec and the ontology to be used in the ContentManager
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        // From this moment, it will be searching agents for registration
        final Date registerTime = new Date();

        addBehaviour(new SimpleBehaviour(this) {
            boolean end = false;
            AID ag;

            @Override
            public void action() {
                // Creates the description for the type of agent to be searched
                DFAgentDescription dfd = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType(REGISTRATION);
                dfd.addServices(sd);

                try {
                    if ((new Date()).getTime() - registerTime.getTime() >= 60000) {
                        end = true;
                    }

                    // It finds agents of the required type
                    DFAgentDescription[] res = new DFAgentDescription[20];
                    res = DFService.search(myAgent, dfd);

                    if (res.length > 0 && !registered) {
                        ag = res[0].getName();

                        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                        msg.addReceiver(ag);
                        msg.setLanguage(codec.getName());
                        msg.setOntology(ontology.getName());
                        msg.setProtocol("requestRegistration");

                        TribeRegistration tribeRegistration = new TribeRegistration();
                        tribeRegistration.setID(ID);
                        // As it is an action and the encoding language the SL, it must be wrapped
                        // into an Action
                        Action agAction = new Action(ag, tribeRegistration);

                        try {
                            // The ContentManager transforms the java objects into strings
                            getContentManager().fillContent(msg, agAction);
                            send(msg);
                            System.out.println(getLocalName() + " - request to register");
                        } catch (CodecException ce) {
                            ce.printStackTrace();
                        } catch (OntologyException oe) {
                            oe.printStackTrace();
                        }

                        doWait(5000);
                    } else {
                        doWait(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public boolean done() {
                return registered || end;
            }

        });

        addBehaviour(new SimpleBehaviour(this) {
            boolean end = false;

            @Override
            public void action() {
                ACLMessage msgRefuse = receive(MessageTemplate.MatchPerformative(ACLMessage.REFUSE).MatchProtocol("refuseRegistration"));
                ACLMessage msgNotUnderstood = receive(MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD));
                ACLMessage msgAgree = receive(MessageTemplate.MatchPerformative(ACLMessage.AGREE).MatchProtocol("agreeRegistration"));

                if (msgRefuse != null) {
                    System.out.println(myAgent.getLocalName() + " - got refused by Platform");
                    end = true;
                }

                if (msgNotUnderstood != null) {
                    System.out.println(myAgent.getLocalName() + " - not understood by Platform");
                    end = true;
                }

                if (msgAgree != null) {
                    System.out.println(myAgent.getLocalName() + " - agreed by Playform to register");
                    registered = true;
                    end = true;
                }
            }

            @Override
            public boolean done() {
                return end;
            }
        });

        addBehaviour(new SimpleBehaviour(this) {
            boolean end = false;

            @Override
            public void action() {
                ACLMessage msgInform = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM).MatchProtocol("InformName"));

                if (msgInform != null) {
                    try {
                        System.out.println(myAgent.getLocalName() + " - is registered");
                        confirmRegister = true;

                        ContentElement ce = null;
                        ce = getContentManager().extractContent(msgInform);
                        if (ce instanceof Action) {

                            Action informAction = (Action) ce;
                            Tribe myTribe = (Tribe) informAction.getAction();
                            name = myTribe.getName();
                            end = true;
                        }
                    } catch (CodecException e) {
                        e.printStackTrace();
                    } catch (OntologyException e) {
                        e.printStackTrace();
                    }

                }

            }
            @Override
            public boolean done() {
                return end;
            }
        });


        //This behaviour checks for initial allocation messages from the platform
        addBehaviour(new SimpleBehaviour(this) {
            boolean end = false;

            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM).MatchProtocol("informInit"));
                ContentElement contentElement;
                if (msg != null) {
                    try {
                        contentElement = getContentManager().extractContent(msg);
                        if (contentElement instanceof Action) {
                            Action agAction = (Action) contentElement;
                            Concept conc = agAction.getAction();
                            if (conc instanceof Allocate) {

                                Iterator iterator = ((Allocate) conc).getAllInitialResources();
                                String output = myAgent.getLocalName() + ": Received ";
                                while (iterator.hasNext()) {
                                    Resource res = (Resource) iterator.next();
                                    output += res.getAmount() + " units of " + res.getType() + ", ";
                                }
                                output += "and " + ((Allocate) conc).getInitialUnits().size() + " units.";

                                iterator = ((Allocate) conc).getAllInitialUnits();
                                while (iterator.hasNext()) {
                                    registeredUnits.add((Unit) iterator.next());
                                }
                                System.out.println(output);
                                end = true;
                            }
                        }
                    } catch (CodecException e) {
                        e.printStackTrace();
                    } catch (OntologyException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public boolean done() {
                return end;
            }
        });

    }
}