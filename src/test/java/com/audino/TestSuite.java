package com.audino;

import com.audino.model.MedicationTest;
import com.audino.model.PatientTest;
import com.audino.service.DataServiceTest;
import com.audino.service.InteractionEngineTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for the Audino application.
 * Runs all major unit tests.
 */
@Suite
@SuiteDisplayName("Audino Application Test Suite")
@SelectClasses({
    PatientTest.class,
    MedicationTest.class,
    DataServiceTest.class,
    InteractionEngineTest.class
})
public class TestSuite {
    // This class remains empty. It's used only as a holder for the above annotations.
}