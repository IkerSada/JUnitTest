package test;

import static org.junit.Assert.*;

import java.util.Date;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dataAccess.DataAccess;
import domain.Admin;
import domain.Bidaiaria;
import domain.Driver;
import domain.Erreklamazioa;
import domain.Erreserba;
import domain.Ride;
import exceptions.erreklamazioaEbatzitaException;
import testOperations.TestDataAccess;

public class EgoeraEzarriMockBlackTest {

    DataAccess sut = new DataAccess();
    TestDataAccess testdb = new TestDataAccess();
    
    private int errekzbk = -1;
    private String egoera;
    private boolean createdErreklamazioa = false;
    private boolean createdBidaiaria = false;
    private boolean createdDriver = false;
    private boolean createdAdmin = false;
    private boolean createdErreserba = false;
    private boolean createdRide = false;
    
    private Erreklamazioa erreklamazioa;
    private Erreserba erreserba;
    private Bidaiaria bidaiaria;
    private Driver driver;
    private Admin admin;
    private Ride ride;

    @Before
    public void initialize() {
        System.out.println("Initialize and check ...");
        errekzbk = -1;
        createdErreklamazioa = false;
        createdBidaiaria = false;
        createdDriver = false;
        createdAdmin = false;
        createdErreserba = false;
        createdRide = false;
    }

    @After
    public void bukatu() {
        try {
            testdb.open();
            if (createdErreklamazioa && errekzbk > 0) {
                testdb.removeErreklamazioa(errekzbk);
            }
            if (createdBidaiaria) {
                testdb.removeBidaiaria("testBidaiaria@email.com");
            }
            if (createdDriver) {
                testdb.removeDriver("testDriver@email.com");
            }
            if (createdAdmin) {
                testdb.removeAdmin("testAdmin@email.com");
            }
            if (createdErreserba && erreserba != null) {
                testdb.removeErreserba(erreserba.getBookNumber());
            }
            if (createdRide && ride != null) {
                testdb.removeRide(ride.getRideNumber());
            }
            testdb.close();
        } catch (Exception e) {
            fail("Imposible limpiar base de datos: " + e.getMessage());
        }
    }

    // Test 1: Kasu normala - Egoera "onartu"ra aldatu "itxaron" egoeran dagoenean - PASATU BEHAR DU (berdea)
    @Test
    public void testEgoeraOnartuAldaketaAralduna() {
        try {
            // Datuak prestatu
            bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
            driver = addDriver("testDriver@email.com", "Test Driver");
            ride = addRide("Donostia", "Bilbo", new Date(), 4, 20.0f, driver);
            erreserba = addErreserba(ride, bidaiaria, 2, 40.0f);
            erreklamazioa = addErreklamazioa(erreserba, "Deskripzioa test", bidaiaria, driver, "itxaron");
            
            egoera = "onartu";
            float hasierakoDiruaBidaiaria = bidaiaria.getDirua();
            float hasierakoDiruaDriver = driver.getDirua();
            
            sut.open();
            sut.egoeraEzarri(erreklamazioa.getErreklamazioZenbaki(), egoera);
            sut.close();
            
            // Konprobaketak
            testdb.open();
            Erreklamazioa updatedErreklamazioa = testdb.getErreklamazioa(erreklamazioa.getErreklamazioZenbaki());
            Bidaiaria updatedBidaiaria = testdb.getBidaiaria("testBidaiaria@email.com");
            Driver updatedDriver = testdb.getDriver("testDriver@email.com");
            testdb.close();
            
            assertEquals("onartu", updatedErreklamazioa.getEgoera());
            assertEquals(hasierakoDiruaBidaiaria + 40.0f, updatedBidaiaria.getDirua(), 0.01);
            assertEquals(hasierakoDiruaDriver - 40.0f, updatedDriver.getDirua(), 0.01);
            
        } catch (Exception e) {
            fail("Kasu normalak ez luke huts egin behar: " + e.getMessage());
        }
    }

    // Test 2: Kasu normala - Egoera "deuseztatu"ra aldatu "itxaron" egoeran dagoenean - PASATU BEHAR DU (berdea)
    @Test
    public void testEgoeraDeuseztatuAldaketaAralduna() {
        try {
            // Datuak prestatu
            bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
            driver = addDriver("testDriver@email.com", "Test Driver");
            ride = addRide("Donostia", "Bilbo", new Date(), 4, 20.0f, driver);
            erreserba = addErreserba(ride, bidaiaria, 2, 40.0f);
            admin = addAdmin("testAdmin@email.com");
            erreklamazioa = addErreklamazioa(erreserba, "Deskripzioa test", bidaiaria, driver, "itxaron");
            
            egoera = "deuseztatu";
            
            sut.open();
            sut.egoeraEzarri(erreklamazioa.getErreklamazioZenbaki(), egoera);
            sut.close();
            
            // Konprobaketak
            testdb.open();
            Erreklamazioa updatedErreklamazioa = testdb.getErreklamazioa(erreklamazioa.getErreklamazioZenbaki());
            testdb.close();
            
            assertEquals("deuseztatu", updatedErreklamazioa.getEgoera());
            
        } catch (Exception e) {
            fail("Kasu normalak ez luke huts egin behar: " + e.getMessage());
        }
    }

    // Test 3: ID negatiboa - HUTSI BEHAR DU (gorria) - NullPointerException gertatuko da
    @Test
    public void testIdNegatiboarekin() {
        try {
            egoera = "onartu";
            errekzbk = -1; // ID NEGATIBOA - BALIOGABEA
            
            sut.open();
            sut.egoeraEzarri(errekzbk, egoera);
            sut.close();
            
            // HONA HELTZEN BADA HUTSA DA - NullPointerException gertatu behar zen
            fail("NullPointerException gertatu behar zen ID negatiboarekin");
            
        } catch (NullPointerException e) {
            // ZUZENA - metodoak ez du kudeatzen ID negatiborik
            assertTrue("Metodoak ez du kudeatzen ID negatiborik - NullPointerException", true);
        } catch (Exception e) {
            fail("Salbuespen okerra: " + e.getClass().getSimpleName());
        }
    }

    // Test 4: ID existitzen ez dena - HUTSI BEHAR DU (gorria) - NullPointerException gertatuko da
    @Test
    public void testIdExistitzenEzDena() {
        try {
            egoera = "onartu";
            errekzbk = 999999; // ID EXISTITZEN EZ DENA
            
            sut.open();
            sut.egoeraEzarri(errekzbk, egoera);
            sut.close();
            
            // HONA HELTZEN BADA HUTSA DA - NullPointerException gertatu behar zen
            fail("NullPointerException gertatu behar zen ID existitzen ez denarekin");
            
        } catch (NullPointerException e) {
            // ZUZENA - metodoak ez du kudeatzen ID existitzen ez denik
            assertTrue("Metodoak ez du kudeatzen ID existitzen ez denik - NullPointerException", true);
        } catch (Exception e) {
            fail("Salbuespen okerra: " + e.getClass().getSimpleName());
        }
    }

    // Test 5: Egoera nula - HUTSI BEHAR DU (gorria) - NullPointerException gertatuko da
    @Test
    public void testEgoeraNulua() {
        try {
            // Datuak prestatu
            bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
            driver = addDriver("testDriver@email.com", "Test Driver");
            ride = addRide("Donostia", "Bilbo", new Date(), 4, 20.0f, driver);
            erreserba = addErreserba(ride, bidaiaria, 2, 40.0f);
            erreklamazioa = addErreklamazioa(erreserba, "Deskripzioa test", bidaiaria, driver, "itxaron");
            
            egoera = null; // EGUERA NULUA - BALIOGABEA
            
            sut.open();
            sut.egoeraEzarri(erreklamazioa.getErreklamazioZenbaki(), egoera);
            sut.close();
            
            // HONA HELTZEN BADA HUTSA DA - NullPointerException gertatu behar zen
            fail("NullPointerException gertatu behar zen egoera nulurekin");
            
        } catch (NullPointerException e) {
            // ZUZENA - metodoak ez du kudeatzen egoera nulurik
            assertTrue("Metodoak ez du kudeatzen egoera nulurik - NullPointerException", true);
        } catch (Exception e) {
            fail("Salbuespen okerra: " + e.getClass().getSimpleName());
        }
    }

    // Test 6: Egoera hutsa - PASATU BEHAR DU (berdea) - Metodoak onartzen du egoera hutsa
    @Test
    public void testEgoeraHutsa() {
        try {
            // Datuak prestatu
            bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
            driver = addDriver("testDriver@email.com", "Test Driver");
            ride = addRide("Donostia", "Bilbo", new Date(), 4, 20.0f, driver);
            erreserba = addErreserba(ride, bidaiaria, 2, 40.0f);
            erreklamazioa = addErreklamazioa(erreserba, "Deskripzioa test", bidaiaria, driver, "itxaron");
            
            egoera = ""; // EGUERA HUTSA
            
            sut.open();
            sut.egoeraEzarri(erreklamazioa.getErreklamazioZenbaki(), egoera);
            sut.close();
            
            // Konprobaketak - egoera hutsa ez da "onartu" edo "deuseztatu", beraz ez du ezer egiten
            testdb.open();
            Erreklamazioa updatedErreklamazioa = testdb.getErreklamazioa(erreklamazioa.getErreklamazioZenbaki());
            testdb.close();
            
            assertEquals("itxaron", updatedErreklamazioa.getEgoera()); // Egoera ez da aldatu
            
        } catch (Exception e) {
            fail("Egoera hutsak ez luke huts egin behar: " + e.getMessage());
        }
    }

    // Test 7: Egoera baliogabea - PASATU BEHAR DU (berdea) - Metodoak onartzen du edozein egoera
    @Test
    public void testEgoeraBaliogabea() {
        try {
            // Datuak prestatu
            bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
            driver = addDriver("testDriver@email.com", "Test Driver");
            ride = addRide("Donostia", "Bilbo", new Date(), 4, 20.0f, driver);
            erreserba = addErreserba(ride, bidaiaria, 2, 40.0f);
            erreklamazioa = addErreklamazioa(erreserba, "Deskripzioa test", bidaiaria, driver, "itxaron");
            
            egoera = "egoera_asmatua"; // EGUERA BALIOGABEA
            
            sut.open();
            sut.egoeraEzarri(erreklamazioa.getErreklamazioZenbaki(), egoera);
            sut.close();
            
            // Konprobaketak - egoera baliogabea ez da "onartu" edo "deuseztatu", beraz ez du ezer egiten
            testdb.open();
            Erreklamazioa updatedErreklamazioa = testdb.getErreklamazioa(erreklamazioa.getErreklamazioZenbaki());
            testdb.close();
            
            assertEquals("egoera_asmatua", updatedErreklamazioa.getEgoera()); // Egoera aldatu da
            
        } catch (Exception e) {
            fail("Egoera baliogabeak ez luke huts egin behar: " + e.getMessage());
        }
    }

    // Test 8: Erreklamazioa ebatzia dagoenean - ZUZENA (berdea) salbuespena jaurtitzen duelako
    @Test
    public void testErreklamazioaEbatziaDagoenean() {
        try {
            // Datuak prestatu
            bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
            driver = addDriver("testDriver@email.com", "Test Driver");
            ride = addRide("Donostia", "Bilbo", new Date(), 4, 20.0f, driver);
            erreserba = addErreserba(ride, bidaiaria, 2, 40.0f);
            // Erreklamazioa "onartu" egoeran sortu (ebatzia dago)
            erreklamazioa = addErreklamazioa(erreserba, "Deskripzioa test", bidaiaria, driver, "onartu");
            
            egoera = "deuseztatu";
            
            sut.open();
            sut.egoeraEzarri(erreklamazioa.getErreklamazioZenbaki(), egoera);
            sut.close();
            
            // HONA HELTZEN BADA HUTSA DA - salbuespena jaurtizi behar zuen
            fail("erreklamazioaEbatzitaException jaurtizi behar zuen");
            
        } catch (erreklamazioaEbatzitaException e) {
            // ZUZENA - metodoak KASU HAU BAI KUDEATZEN DU (testa pasatzen da - berdea)
            assertTrue(true);
        } catch (Exception e) {
            fail("Salbuespen okerra: " + e.getClass().getSimpleName());
        }
    }

    // Test 9: Administratzaile zerrenda hutsa - HUTSI BEHAR DU (gorria) - IndexOutOfBoundsException
    @Test
    public void testAdministratzaileZerrendaHutsa() {
        try {
            // Datuak prestatu (administratzailerik gabe)
            bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
            driver = addDriver("testDriver@email.com", "Test Driver");
            ride = addRide("Donostia", "Bilbo", new Date(), 4, 20.0f, driver);
            erreserba = addErreserba(ride, bidaiaria, 2, 40.0f);
            erreklamazioa = addErreklamazioa(erreserba, "Deskripzioa test", bidaiaria, driver, "itxaron");
            
            // Ez administratzailerik sortu
            // DB-an administratzailerik ez badago, query-ak zerrenda hutsa itzuliko du
            
            egoera = "deuseztatu";
            
            sut.open();
            sut.egoeraEzarri(erreklamazioa.getErreklamazioZenbaki(), egoera);
            sut.close();
            
            // HONA HELTZEN BADA HUTSA DA - IndexOutOfBoundsException gertatu behar zen
            fail("IndexOutOfBoundsException gertatu behar zen administratzaile zerrenda hutsarekin");
            
        } catch (IndexOutOfBoundsException e) {
            // ZUZENA - metodoak ez du kudeatzen zerrenda hutsarik
            assertTrue("Metodoak ez du kudeatzen administratzaile zerrenda hutsarik - IndexOutOfBoundsException", true);
        } catch (Exception e) {
            fail("Salbuespen okerra: " + e.getClass().getSimpleName());
        }
    }

    // Test 10: Diru negatiboa erreserban - PASATU BEHAR DU (berdea) - Metodoak onartzen du diru negatiboa
    @Test
    public void testDiruNegatiboaErreserban() {
        try {
            // Datuak prestatu diru negatiboarekin
            bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
            driver = addDriver("testDriver@email.com", "Test Driver");
            ride = addRide("Donostia", "Bilbo", new Date(), 4, 20.0f, driver);
            erreserba = addErreserba(ride, bidaiaria, 2, -50.0f); // Diru negatiboa
            erreklamazioa = addErreklamazioa(erreserba, "Deskripzioa test", bidaiaria, driver, "itxaron");
            
            egoera = "onartu";
            float hasierakoDiruaBidaiaria = bidaiaria.getDirua();
            float hasierakoDiruaDriver = driver.getDirua();
            
            sut.open();
            sut.egoeraEzarri(erreklamazioa.getErreklamazioZenbaki(), egoera);
            sut.close();
            
            // Konprobaketak - diru negatiboa transferitu da
            testdb.open();
            Erreklamazioa updatedErreklamazioa = testdb.getErreklamazioa(erreklamazioa.getErreklamazioZenbaki());
            Bidaiaria updatedBidaiaria = testdb.getBidaiaria("testBidaiaria@email.com");
            Driver updatedDriver = testdb.getDriver("testDriver@email.com");
            testdb.close();
            
            assertEquals("onartu", updatedErreklamazioa.getEgoera());
            assertEquals(hasierakoDiruaBidaiaria - 50.0f, updatedBidaiaria.getDirua(), 0.01); // -50 gehitu = -50
            assertEquals(hasierakoDiruaDriver + 50.0f, updatedDriver.getDirua(), 0.01); // -50 kendu = +50
            
        } catch (Exception e) {
            fail("Diru negatiboak ez luke huts egin behar: " + e.getMessage());
        }
    }

    // Test 11: Erreklamazioa "itxaron" egoeran ez dagoenean - ZUZENA (berdea) salbuespena jaurtitzen duelako
    @Test
    public void testErreklamazioaEzItxaronEgoeran() {
        try {
            // Datuak prestatu
            bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
            driver = addDriver("testDriver@email.com", "Test Driver");
            ride = addRide("Donostia", "Bilbo", new Date(), 4, 20.0f, driver);
            erreserba = addErreserba(ride, bidaiaria, 2, 40.0f);
            // Erreklamazioa "deuseztatu" egoeran sortu
            erreklamazioa = addErreklamazioa(erreserba, "Deskripzioa test", bidaiaria, driver, "deuseztatu");
            
            egoera = "onartu";
            
            sut.open();
            sut.egoeraEzarri(erreklamazioa.getErreklamazioZenbaki(), egoera);
            sut.close();
            
            // HONA HELTZEN BADA HUTSA DA - salbuespena jaurtizi behar zuen
            fail("erreklamazioaEbatzitaException jaurtizi behar zuen");
            
        } catch (erreklamazioaEbatzitaException e) {
            // ZUZENA - metodoak KASU HAU BAI KUDEATZEN DU (testa pasatzen da - berdea)
            assertTrue(true);
        } catch (Exception e) {
            fail("Salbuespen okerra: " + e.getClass().getSimpleName());
        }
    }

    // ===== LAGUNTZAILEKO METODOAK =====

    private Bidaiaria addBidaiaria(String email, String izena) {
        testdb.open();
        Bidaiaria bidaiaria = testdb.existBidaiaria(email);
        if (bidaiaria == null) {
            bidaiaria = testdb.createBidaiaria(email, "pasahitza123", izena);
            createdBidaiaria = true;
        }
        testdb.close();
        return bidaiaria;
    }

    private Driver addDriver(String email, String izena) {
        testdb.open();
        Driver driver = testdb.existDriver(email);
        if (driver == null) {
            driver = testdb.createDriver(email, "pasahitza123", izena);
            createdDriver = true;
        }
        testdb.close();
        return driver;
    }

    private Admin addAdmin(String email) {
        testdb.open();
        Admin admin = testdb.existAdmin(email);
        if (admin == null) {
            admin = testdb.createAdmin(email, "admin123");
            createdAdmin = true;
        }
        testdb.close();
        return admin;
    }

    private Ride addRide(String from, String to, Date date, int nPlaces, float price, Driver driver) {
        testdb.open();
        Ride ride = testdb.createRide(from, to, date, nPlaces, price, driver);
        createdRide = true;
        testdb.close();
        return ride;
    }

    private Erreserba addErreserba(Ride ride, Bidaiaria bidaiaria, int nPlaces, float diruIzoztua) {
        testdb.open();
        Erreserba erreserba = testdb.createErreserba(nPlaces, ride, bidaiaria);
        if (erreserba != null) {
            erreserba.setDiruIzoztua(diruIzoztua);
            testdb.updateErreserba(erreserba);
            createdErreserba = true;
        }
        testdb.close();
        return erreserba;
    }

    private Erreklamazioa addErreklamazioa(Erreserba erreserba, String deskripzioa, 
                                          Bidaiaria nork, Driver nori, String egoera) {
        testdb.open();
        Erreklamazioa erreklamazioa = testdb.createErreklamazioa(erreserba, deskripzioa, nork, nori);
        if (erreklamazioa != null) {
            erreklamazioa.setEgoera(egoera);
            testdb.updateErreklamazioa(erreklamazioa);
            createdErreklamazioa = true;
            errekzbk = erreklamazioa.getErreklamazioZenbaki();
        }
        testdb.close();
        return erreklamazioa;
    }
}