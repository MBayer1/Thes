package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RepulsiveForceAgainstOtherPedestrians {

    final private Double sigma = 30.0; // in centimeter
    final private Double VAlphaBeta = 210.0; // (cm / s)^2
    SupportiveCalculations calculations = new SupportiveCalculations();
    LinkedList<PedestrianStreetSection> listOfCheckedStreets = new LinkedList<>();

    public Vector2d getRepulsiveForceAgainstAllOtherPedestrians(    RoundaboutSimulationModel model,
                                                                    Pedestrian pedestrian,
                                                                    PedestrianPoint destination){

        Vector2d vacDestination = new Vector2d(destination.getX(), destination.getY());
        Vector2d sumForce = new Vector2d(0,0);

        // run through all previous and following connected street sections up to 8m distance
        // from current position of alpha pedestrian
        GetAllPedestrianFromStreet( model, pedestrian, vacDestination, new LinkedList<> (),
                (PedestrianStreetSection)pedestrian.getCurrentSection().getStreetSection(), sumForce);
        GetAllPedestrianFromPreviousStreets( model, pedestrian, vacDestination, sumForce );
        GetAllPedestrianFromFollowingStreets ( model, pedestrian, vacDestination, sumForce );

        return sumForce;
    }


    void GetAllPedestrianFromStreet(RoundaboutSimulationModel model,
                                    Pedestrian pedestrian,
                                    Vector2d destination,
                                    List<IConsumer> listOfStreetSectionsInRange,
                                    PedestrianStreetSection section,
                                    Vector2d sumForce){
        boolean noPedestrian = true;
        boolean pedestrianOutOfRange = false;
        for(IPedestrian pedestrianBeta: section.getPedestrianQueue()){
            if( !(pedestrianBeta instanceof Pedestrian) ){
                throw new IllegalArgumentException("Pedestrian is not an instance of Pedestrian");
            }
            noPedestrian = false;
            // calculate forces
            if( (section).getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_SINK) ||
                    checkPedestrianInRange( model, pedestrian,(Pedestrian)pedestrianBeta)){
                sumForce.add(calculateActualRepulsiveForceAgainstOtherPedestrian( model, destination, pedestrian,(Pedestrian)pedestrianBeta) );
            } else {
                pedestrianOutOfRange = true;
            }
        }
        listOfCheckedStreets.add( section );

        if ( noPedestrian || ! pedestrianOutOfRange ) {
            // no Pedestrian to estimate distance or all existing pedestrian where in range
            listOfStreetSectionsInRange.add( section );
        }
    }

    public void GetAllPedestrianFromPreviousStreets(RoundaboutSimulationModel model,
                                                    Pedestrian pedestrian,
                                                    Vector2d destination,
                                                    Vector2d sumForce) {
        IConsumer currentStreetSection = pedestrian.getCurrentSection().getStreetSection();
        if(!(currentStreetSection instanceof PedestrianStreetSection)){
            throw new IllegalArgumentException("Consumer is not an instance of PedestrianStreetSection");
        }

        List<IConsumer> listOfStreetSectionsInRange = new ArrayList<>();
        listOfStreetSectionsInRange.add(currentStreetSection);

        while( !listOfStreetSectionsInRange.isEmpty() ){
            currentStreetSection = listOfStreetSectionsInRange.remove(listOfStreetSectionsInRange.size()-1);
            List<PedestrianConnectedStreetSections> previousConnector = ((PedestrianStreetSection)currentStreetSection).getPreviousStreetConnector();

            for( PedestrianConnectedStreetSections previousStreetSectionPair : previousConnector ) {
                if( previousStreetSectionPair.getFromStreetSection().equals(currentStreetSection) ) {
                    IConsumer previousSection = previousStreetSectionPair.getToStreetSection(); // from is always current section

                    if (previousSection == null && previousStreetSectionPair.getToSource() != null) continue; // is a source
                    if( !(previousSection instanceof PedestrianStreetSection) ){
                        throw new IllegalArgumentException("Section is not an instance of PedestrianStreetSection");
                    }

                    if ( listOfCheckedStreets.contains(previousSection) ) continue;

                    GetAllPedestrianFromStreet( model, pedestrian, destination, listOfStreetSectionsInRange, (PedestrianStreetSection)previousSection, sumForce);
                }
            }
        }
    }

    void GetAllPedestrianFromFollowingStreets ( RoundaboutSimulationModel model,
                                                       Pedestrian pedestrian,
                                                       Vector2d destination,
                                                       Vector2d sumForce) {
        IConsumer currentStreetSection = pedestrian.getCurrentSection().getStreetSection();
        if(!(currentStreetSection instanceof PedestrianStreetSection)){
        throw new IllegalArgumentException("Consumer is not an instance of PedestrianStreetSection");
        }

        List<IConsumer> listOfStreetSectionsInRange = new ArrayList<>();
        listOfStreetSectionsInRange.add(currentStreetSection);

        while( !listOfStreetSectionsInRange.isEmpty() ){
            currentStreetSection = listOfStreetSectionsInRange.remove(listOfStreetSectionsInRange.size()-1);
            List<PedestrianConnectedStreetSections>  nextConnector = ((PedestrianStreetSection)currentStreetSection).getNextStreetConnector();

            for( PedestrianConnectedStreetSections nextStreetSectionPair : nextConnector ) {
                if( nextStreetSectionPair.getFromStreetSection().equals(currentStreetSection) ) {
                    IConsumer nextSection = nextStreetSectionPair.getFromStreetSection();

                    if( !(nextSection instanceof PedestrianStreetSection) ){
                        throw new IllegalArgumentException("Section is not an instance of PedestrianStreetSection");
                    }

                    if (  listOfCheckedStreets.contains(nextSection) ) continue;

                    GetAllPedestrianFromStreet( model, pedestrian, destination, listOfStreetSectionsInRange, (PedestrianStreetSection)nextSection, sumForce);
                }
            }
        }
    }

    Vector2d calculateActualRepulsiveForceAgainstOtherPedestrian(RoundaboutSimulationModel model,
                                                                        Vector2d destination,
                                                                        Pedestrian pedestrian,
                                                                        Pedestrian pedestrianBeta){
        Double weightingFactor;
        Vector2d force = getRepulsiveForceAgainstOtherPedestrian(model, pedestrian, pedestrianBeta);

        // Check Field of View --> 170°
        if (calculations.val1BiggerOrAlmostEqual(destination.dot(force),  //A ⋅ B = ||A|| * ||B|| * cos θ
                destination.length() * force.length() * Math.cos(model.pedestrianFieldOfViewDegree / 2))) { // half as form center of angel as this is the "nose position" of alpha
            weightingFactor = model.getPedestrianFieldOfViewWeakeningFactor;
        } else {
            weightingFactor = 0.0;
        }

        force.scale(weightingFactor);
        return force;
    }

    boolean checkPedestrianInRange( RoundaboutSimulationModel model, Pedestrian pedestrian, Pedestrian pedestrianBeta){
        if ( calculations.almostEqual( Point2D.distance(   pedestrian.getCurrentGlobalPosition().getX(),
                                                           pedestrian.getCurrentGlobalPosition().getY(),
                                                           pedestrianBeta.getCurrentGlobalPosition().getX(),
                                                           pedestrianBeta.getCurrentGlobalPosition().getY()) ,
                                                           model.pedestrianFieldOfViewRadius)) {
            return true;
        }
        return false;
    }

    Vector2d getRepulsiveForceAgainstOtherPedestrian(  RoundaboutSimulationModel model,
                                                              IPedestrian pedestrianAlpha, IPedestrian pedestrianBeta){

        if ( !(pedestrianAlpha instanceof Pedestrian) || !(pedestrianBeta instanceof Pedestrian) ) {
            throw new IllegalArgumentException("One of the pedestrian is not an instance of Pedestrian");
        }

        //vectorBetweenBothPedestrian
        PedestrianPoint posBeta = pedestrianBeta.getCurrentGlobalPosition();
        Vector2d vectorBetweenBothPedestrian = calculations.getVector(
                                pedestrianAlpha.getCurrentGlobalPosition().getX(), pedestrianAlpha.getCurrentGlobalPosition().getY(),
                                posBeta.getX(), posBeta.getY());

        //preferredDirectionOfBeta = eBeta
        Vector2d vecPosBeta = new Vector2d(posBeta.getX(), posBeta.getY());
        PedestrianPoint nextAimBeta = pedestrianBeta.getNextSubGoal();
        Vector2d vecNextAimBeta = new Vector2d(nextAimBeta.getX(), nextAimBeta.getY());

        Vector2d preferredDirectionOfBeta = vecPosBeta;
        preferredDirectionOfBeta.sub(vecNextAimBeta);       // t is in the estimated future. when reaching destination (expected)
        Double nextAimBetaLength = preferredDirectionOfBeta.length();
        preferredDirectionOfBeta.scale(1/nextAimBetaLength);

        //Traveled path of the walker β within ∆t
        Double traveledPathWithinTOfBeta = nextAimBetaLength;

        //small half axis of the ellipse
        Vector2d betaData = preferredDirectionOfBeta;
        betaData.scale(traveledPathWithinTOfBeta);
        Vector2d nextDestinationVectorAlphaSubTravelPathBeta = vectorBetweenBothPedestrian;
        nextDestinationVectorAlphaSubTravelPathBeta.sub(betaData);

        Double smallHalfAxisOfEllipse = Math.sqrt(  Math.pow(vectorBetweenBothPedestrian.length() + nextDestinationVectorAlphaSubTravelPathBeta.length(),2)) -
                                                    Math.pow(traveledPathWithinTOfBeta,2);

        // Repulsive force against other pedestrians
        // V_alphaBeta(t0)* e^(-b/sigma)
        Double exponent = smallHalfAxisOfEllipse/-2;  // is 2b --> and we need b
        exponent /= sigma;
        exponent = Math.exp(exponent);
        Double repulsiveForce = VAlphaBeta * exponent;

        vectorBetweenBothPedestrian.scale(repulsiveForce);
        vectorBetweenBothPedestrian.negate();

        return vectorBetweenBothPedestrian;
    }
}
