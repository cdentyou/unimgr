package org.mef.nrp.impl;

import java.util.List;

/**
 * Runs activation over multiple @ drivers.
 *
 * @author bartosz.michalik@amartus.com
 */
public class ActivationTransaction {
    private List<ActivationDriver> drivers;


    public void addDriver(ActivationDriver driver) {
        drivers.add(driver);
    }

    public void activate() {
        sortDrivers();
        try {
            for(ActivationDriver d: drivers) { d.activate(); }
            commit();
        } catch (Exception e) {
            //TODO add logging
            rollback();
        }
    }


    public void deactivate() {
        sortDrivers();
        try {
            for(ActivationDriver d: drivers) { d.deactivate(); }
            commit();
        } catch (Exception e) {
            //TODO add logging
            rollback();
        }
    }

    private void commit() {
        drivers.stream().forEach(ActivationDriver::commit);
    }

    private void rollback() {
        drivers.stream().forEach(ActivationDriver::rollback);
    }

    private void sortDrivers() {
        drivers.sort((a,b) -> a.priority() - b.priority());
    }

}
