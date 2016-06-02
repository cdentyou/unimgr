package org.mef.nrp.impl;

import org.junit.Before;
import org.junit.Test;
import org.mef.nrp.api.ActivationDriver;
import org.mef.nrp.api.ActivationDriverAmbiguousException;
import org.mef.nrp.api.ActivationDriverNotFoundException;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.FcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.FcPortBuilder;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import static org.opendaylight.unimgr.utils.ActivationDriverMocks.prepareDriver;

/**
 * @author bartosz.michalik@amartus.com
 */
public class ActivationDriverRepoServiceImplTest {
    private ActivationDriverRepoServiceImpl driverRepo;

    @Before
    public void setUp() throws Exception {
        driverRepo = new ActivationDriverRepoServiceImpl();

    }

    @Test(expected = ActivationDriverNotFoundException.class)
    public void testEmpty() throws Exception {
        final FcPort port = new FcPortBuilder().setId("a").build();
        driverRepo.getDriver(port, null);
    }


    @Test(expected = ActivationDriverAmbiguousException.class)
    public void testConflict() throws Exception {

        final ActivationDriver driver = mock(ActivationDriver.class);

        driverRepo.setDriverBuilders(Arrays.asList(
                prepareDriver(p -> driver), prepareDriver(p -> driver)
        ));

        final FcPort port = new FcPortBuilder().setId("a").build();
        driverRepo.getDriver(port, null);
    }

    @Test
    public void testMatching() throws Exception {

        final ActivationDriver driver = mock(ActivationDriver.class);

        driverRepo.setDriverBuilders(Collections.singletonList(
                prepareDriver(p -> driver)
        ));

        final FcPort port = new FcPortBuilder().setId("a").build();
        final ActivationDriver driverFromRepo = driverRepo.getDriver(port, null);
        assertEquals(driver, driverFromRepo);
    }




}