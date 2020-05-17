package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm14.trafsim.model.entities.IConsumer;

public interface IPedestrianUIMain {
  void addPedestrian(IPedestrian pedestrian);
  void updatePedestrian(IPedestrian pedestrian);
  void removePedestrian(IPedestrian pedestrian);
  void addCar(ICar car, IConsumer section);
  void updateCar(ICar car, IConsumer section);
  void removeCar(ICar car, IConsumer section);
}
