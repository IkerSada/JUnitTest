package test;

import static org.junit.Assert.*;

import org.junit.Test;

import dataAccess.DataAccess;
import domain.Bidaiaria;
import domain.Driver;
import exceptions.erreklamazioaEbatzitaException;
import testOperations.TestDataAccess;

public class EgoeraEzarriBDBlackTest {

    // sut: system under test
    static DataAccess sut = new DataAccess();
    static TestDataAccess testDA = new TestDataAccess();

    @Test
    // sut.egoeraEzarri: Erreklamazioa already "ebatzita" → must throw erreklamazioaEbatzitaException
    public void test1_ErreklamazioaEbatzita() {
        String bidaiariaEmail = "bidaiaria@test.com";
        String driverEmail = "driver@test.com";
        int erreklamazioaId = -1;

        try {
            testDA.open();
            testDA.createBidaiaria(bidaiariaEmail, "Bidaiaria", "pass", 100f);
            testDA.createDriver(driverEmail, "Driver10");

            Bidaiaria bidaiaria = testDA.getBidaiaria(bidaiariaEmail);
            Driver driver = testDA.getDriver(driverEmail);

            erreklamazioaId = testDA.createSimpleErreklamazioa("onartu", bidaiaria, driver);
            testDA.close();

            sut.open();
            sut.egoeraEzarri(erreklamazioaId, "deuseztatu");
            sut.close();

            fail("Expected erreklamazioaEbatzitaException not thrown");

        } catch (erreklamazioaEbatzitaException e) {
            assertTrue(true); // expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            testDA.open();
            if (erreklamazioaId != -1) testDA.removeErreklamazioa(erreklamazioaId);
            testDA.removeBidaiaria(bidaiariaEmail);
            testDA.removeDriver(driverEmail);
            testDA.close();
        }
    }

    @Test
    // sut.egoeraEzarri: Erreklamazioa in state "itxaron" → changing to "itxaron" again is valid
    public void test2_EgoeraItxaron() {
        String bidaiariaEmail = "bidaiaria@test.com";
        String driverEmail = "driver@test.com";
        int erreklamazioaId = -1;

        try {
            testDA.open();
            testDA.createBidaiaria(bidaiariaEmail, "Bidaiaria", "pass", 100f);
            testDA.createDriver(driverEmail, "Driver11");

            Bidaiaria bidaiaria = testDA.getBidaiaria(bidaiariaEmail);
            Driver driver = testDA.getDriver(driverEmail);

            erreklamazioaId = testDA.createSimpleErreklamazioa("itxaron", bidaiaria, driver);
            testDA.close();

            sut.open();
            sut.egoeraEzarri(erreklamazioaId, "itxaron");

            assertTrue(true); // expected normal behavior

        } catch (erreklamazioaEbatzitaException e) {
            fail("Unexpected erreklamazioaEbatzitaException");
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            testDA.open();
            if (erreklamazioaId != -1) testDA.removeErreklamazioa(erreklamazioaId);
            testDA.removeBidaiaria(bidaiariaEmail);
            testDA.removeDriver(driverEmail);
            testDA.close();
        }
    }

    
    @Test
    public void test3_EgoeraDeuseztatu() {
        String bidaiariaEmail = "bidaiaria@test.com";
        String driverEmail = "driver@test.com";
        int erreklamazioaId = -1;

        try {
            testDA.open();
            testDA.createBidaiaria(bidaiariaEmail, "Bidaiaria", "pass", 100f);
            testDA.createDriver(driverEmail, "Driver12");

            Bidaiaria bidaiaria = testDA.getBidaiaria(bidaiariaEmail);
            Driver driver = testDA.getDriver(driverEmail);

            erreklamazioaId = testDA.createSimpleErreklamazioa("itxaron", bidaiaria, driver);
            testDA.close();

            sut.open();
            sut.egoeraEzarri(erreklamazioaId, "deuseztatu");
            
            // Verify the state actually changed
            testDA.open();
            String newState = testDA.getErreklamazioaEgoera(erreklamazioaId);
            testDA.close();
            
            assertEquals("deuseztatu", newState); // Verify the change

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            testDA.open();
            if (erreklamazioaId != -1) testDA.removeErreklamazioa(erreklamazioaId);
            testDA.removeBidaiaria(bidaiariaEmail);
            testDA.removeDriver(driverEmail);
            testDA.close();
        }
    }
   

    @Test
    // sut.egoeraEzarri: Erreklamazioa in state "itxaron" → change to an unrecognized state (e.g. "besteBat")
    public void test4_EgoeraBesteBat() {
        String bidaiariaEmail = "bidaiaria@test.com";
        String driverEmail = "driver@test.com";
        int erreklamazioaId = -1;

        try {
            testDA.open();
            testDA.createBidaiaria(bidaiariaEmail, "Bidaiaria", "pass", 100f);
            testDA.createDriver(driverEmail, "Driver13");

            Bidaiaria bidaiaria = testDA.getBidaiaria(bidaiariaEmail);
            Driver driver = testDA.getDriver(driverEmail);

            erreklamazioaId = testDA.createSimpleErreklamazioa("itxaron", bidaiaria, driver);
            testDA.close();

            sut.open();
            sut.egoeraEzarri(erreklamazioaId, "besteBat");

            assertTrue(true); // accepted as general case

        } catch (erreklamazioaEbatzitaException e) {
            fail("Unexpected erreklamazioaEbatzitaException");
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            testDA.open();
            if (erreklamazioaId != -1) testDA.removeErreklamazioa(erreklamazioaId);
            testDA.removeBidaiaria(bidaiariaEmail);
            testDA.removeDriver(driverEmail);
            testDA.close();
        }
    }
    
    @Test
    public void test5_NonExistentErreklamazioa() {
        try {
            sut.open();
            sut.egoeraEzarri(-999, "itxaron"); // Non-existent ID
            fail("Expected exception for non-existent erreklamazioa");
        } catch (Exception e) {
            // Expected behavior
        } finally {
            sut.close();
        }
    }

    @Test
    public void test6_NullState() {
        // Test how the system handles null state parameter
    }
    
    
    
}
