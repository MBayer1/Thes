package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm3.s2.roundabout.api.entity.IDriverBehaviour;

public class DriverBehaviour implements IDriverBehaviour {

    private double speed;
    private double minDistanceToNextCar;
    private double maxDistanceToNextCar;
    private double mergeFactor;
    private double accelerationTime;

    public DriverBehaviour(double speed, double minDistanceToNextCar, double maxDistanceToNextCar, double mergeFactor, double accelerationTime)
    throws IllegalArgumentException {
        setSpeed(speed);
        setMinDistanceToNextCar(minDistanceToNextCar);
        setMaxDistanceToNextCar(maxDistanceToNextCar);
        setMergeFactor(mergeFactor);
        setAccelerationTime(accelerationTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSpeed() {
        return speed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpeed(double speed)
    throws IllegalArgumentException {
        if (speed >= 0) {
            this.speed = speed;
        } else {
            throw new IllegalArgumentException("Speed should be greater or equal than 0");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMinDistanceToNextCar() {
        return minDistanceToNextCar;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMinDistanceToNextCar(double minDistanceToNextCar)
    throws IllegalArgumentException {
        if (minDistanceToNextCar > 0) {
            this.minDistanceToNextCar = minDistanceToNextCar;
        } else {
            throw new IllegalArgumentException("Min distance must be positive");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaxDistanceToNextCar() {
        return maxDistanceToNextCar;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxDistanceToNextCar(double maxDistanceToNextCar)
    throws IllegalArgumentException {
        if (maxDistanceToNextCar > 0) {
            this.maxDistanceToNextCar = maxDistanceToNextCar;
        } else {
            throw new IllegalArgumentException("Max distance must be positive");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMergeFactor() {
        return mergeFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMergeFactor(double mergeFactor)
    throws IllegalArgumentException {
        if (mergeFactor >= 1) {
            this.mergeFactor = mergeFactor;
        } else {
            throw new IllegalArgumentException("Merge factor must be >= 1");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAccelerationTime() {
        return accelerationTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAccelerationTime(double accelerationTime)
    throws IllegalArgumentException {
        if (accelerationTime > 0) {
            this.accelerationTime = accelerationTime;
        } else {
            throw new IllegalArgumentException("Cars can solely drive forward, acceleration factor has to be positive.");
        }
    }
}
