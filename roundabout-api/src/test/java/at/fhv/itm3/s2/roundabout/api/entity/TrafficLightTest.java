package at.fhv.itm3.s2.roundabout.api.entity;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrafficLightTest {

    long DEFAULT_RED_PHASE = 20;
    long DEFAUTL_GEEN_PHASE = 20;

    @Test(expected = IllegalStateException.class)
    public void changeStateOfInactiveTrafficLight() {
        TrafficLight trafficLight = new TrafficLight(false, DEFAUTL_GEEN_PHASE, DEFAULT_RED_PHASE,  null);
        trafficLight.setFreeToGo(true);
    }

    @Test
    public void initialState() {
        TrafficLight trafficLight = new TrafficLight(true, DEFAUTL_GEEN_PHASE, DEFAULT_RED_PHASE,  null);
        assertTrue(trafficLight.isFreeToGo());
    }

    @Test
    public void setState() {
        TrafficLight trafficLight = new TrafficLight(true, DEFAUTL_GEEN_PHASE, DEFAULT_RED_PHASE,  null);
        trafficLight.setFreeToGo(false);
        assertFalse(trafficLight.isFreeToGo());
    }

    @Test
    public void isNotActive() {
        TrafficLight trafficLight = new TrafficLight(false, DEFAUTL_GEEN_PHASE, DEFAULT_RED_PHASE,  null);
        assertFalse(trafficLight.isActive());
    }
}
