package group3;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontologies.CreateUnit;
import ontologies.KillYourself;
import ontologies.Reason;
import ontologies.Tribe;
import ontologies.Unit;
import ontologies.WoAOntology;


public class AgUnit3 extends Agent {
    private Codec codec = new SLCodec();
    private Ontology ontology = WoAOntology.getInstance();
    private static int count = 0;
    private Tribe owner;


    @Override
    protected void setup() {
        System.out.println(this.getLocalName()+": Unit created.");

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        addBehaviour(new SimpleBehaviour() {
            boolean end=false;
            @Override
            public void action() {
                doWait(6000);
                createUnit();
                end = true;
            }

            @Override
            public boolean done() {
                return end;
            }
        });




        // If killYourself is sent to the unit, it deletes itself
        addBehaviour((new SimpleBehaviour() {
            @Override
            public void action() {
                ACLMessage msgKYS = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM).MatchProtocol("killYourself"));
                if (msgKYS != null) {
                    ContentElement ce = null;
                    try {
                        ce = getContentManager().extractContent(msgKYS);
                    } catch (Codec.CodecException e) {
                        e.printStackTrace();
                    } catch (UngroundedException e) {
                        e.printStackTrace();
                    } catch (OntologyException e) {
                        e.printStackTrace();
                    }
                    if (ce instanceof Action) {
                        Action agAction = (Action) ce;
                        Concept conc = agAction.getAction();
                        if (conc instanceof KillYourself) {
                            System.out.println(getLocalName() + " - killing himself now");
                            doDelete();
                        }


                    }
                }


            }

            @Override
            public boolean done() {
                return false;
            }
        }));

    }

    private void createUnit(){


        //request create unit
        addBehaviour(new SimpleBehaviour() {

            AID ag;
            boolean end = false;
            @Override
            public void action() {
                // Creates the description for the type of agent to be searched
                DFAgentDescription dfd = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("World");
                dfd.addServices(sd);
                try{
                    // It finds agents of the required type
                    DFAgentDescription[] res = new DFAgentDescription[20];
                    res = DFService.search(myAgent, dfd);
                    if(res != null){
                        ag = res[0].getName();

                        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                        msg.addReceiver(ag);
                        msg.setLanguage(codec.getName());
                        msg.setOntology(ontology.getName());
                        msg.setProtocol("requestCreateUnit");
                        CreateUnit createUnit = new CreateUnit();
                        Action agAction = new Action(ag, createUnit);

                        try {
                            // The ContentManager transforms the java objects into strings
                            getContentManager().fillContent(msg, agAction);
                            send(msg);
                            System.out.println(getLocalName() + " - requests to create unit");
                            end = true;
                        } catch (Codec.CodecException ce) {
                            ce.printStackTrace();
                        } catch (OntologyException oe) {
                            oe.printStackTrace();
                        }

                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean done() {
                return end;
            }
        });

        //check the response message for create unit request
        addBehaviour(new SimpleBehaviour(this) {
            boolean end;

            @Override
            public void action() {
                ACLMessage msgRefuse = receive(MessageTemplate.MatchPerformative(ACLMessage.REFUSE).MatchProtocol("refuseCreateUnit"));
                ACLMessage msgNotUnderstood = receive(MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD));
                ACLMessage msgAgree = receive(MessageTemplate.MatchPerformative(ACLMessage.AGREE).MatchProtocol("agreeCreateUnit"));

                if (msgRefuse != null) {
                    ContentElement contentElement = null;
                    try{
                        contentElement = getContentManager().extractContent(msgRefuse);
                        if(contentElement instanceof Action) {
                            Action agAction = (Action) contentElement;
                            Concept conc = agAction.getAction();
                            String reasonString = "";
                            if(conc instanceof Reason){

                                Reason reason = (Reason) conc;
                                reasonString = reason.getDescription() == "LOCATION" ? "Incorrect Location": "Insufficient Resources";

                            }
                            System.out.println(myAgent.getLocalName() + " - got refused to create a unit. Reason: " + reasonString);
                            end = true;
                        }

                    }
                    catch (Codec.CodecException e) {
                        e.printStackTrace();
                    } catch (OntologyException e) {
                        e.printStackTrace();
                    }


                }

                if (msgNotUnderstood != null) {
                    System.out.println(myAgent.getLocalName() + " - not understood by Platform");
                    end = true;
                }

                if (msgAgree != null) {
                    System.out.println(myAgent.getLocalName() + " - agreed by Platform to create a unit");
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
                ACLMessage msgInform = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM).MatchProtocol("informUnitCreation"));

                if (msgInform != null) {
                    try {
                        System.out.println(myAgent.getLocalName() + " - have created a unit");

                        ContentElement ce = null;
                        ce = getContentManager().extractContent(msgInform);
                        if (ce instanceof Action) {

                            Action informAction = (Action) ce;
                            Unit newUnit = (Unit) informAction.getAction();
                            //todo we can inform tribe about unit creation(however the platform informs it anyway)
                            end = true;
                        }
                    } catch (Codec.CodecException e) {
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
