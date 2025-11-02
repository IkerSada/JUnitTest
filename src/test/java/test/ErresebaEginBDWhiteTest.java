package test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dataAccess.DataAccess;

import domain.Bidaiaria;
import domain.Driver;

import domain.Erreserba;
import domain.Ride;
import testOperations.TestDataAccess;

public class ErresebaEginBDWhiteTest {

	DataAccess sut = new DataAccess();
	TestDataAccess testdb = new TestDataAccess();

	private boolean createdBidaiaria = false;
	private boolean createdDriver = false;

	private boolean createdErreserba = false;
	private boolean createdRide = false;
	private Erreserba erreserba;
	private Bidaiaria bidaiaria;
	private Driver driver;

	private Ride ride;

	@Before
	public void initialize() {
		System.out.println("Initialize and check ...");
		createdBidaiaria = false;
		createdDriver = false;
		createdErreserba = false;
		createdRide = false;
	}

	@After
	public void bukatu() {
		try {
			testdb.open();

			if (createdBidaiaria) {
				testdb.removeBidaiaria("testBidaiaria@email.com");
			}
			if (createdDriver) {
				testdb.removeDriver("testDriver@email.com");
			}
			if (createdErreserba && erreserba != null) {
				testdb.removeErreserba(erreserba.getBookNumber());
			}
			if (createdRide && ride != null) {
				testdb.removeRide(ride.getRideNumber());
			}
			testdb.close();
		} catch (Exception e) {
			fail("Ezinezkoa da datu basea garbitzea " + e.getMessage());
		}
	}



	//erreserbaEgin metodoa erreseba berria sortzen du, zeren ez dago aurretik sortutako erreserbarik
	@Test
	public void testErreserbaEgin_ErreserbaBerria() {

		// Datuak prestatu
		bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
		driver = addDriver("testDriver@email.com", "Test Driver");
		ride = addRide("Donostia", "Bilbo", new Date(), 5, 15.0f, driver);

		// Konprobaketak        
		sut.erreserbaEgin(ride.getRideNumber(), bidaiaria, 2);

		// Bidaiaria berriro kargatu datu-base-tik erreserbak eguneratzeko
		testdb.open();
		Bidaiaria updatedBidaiaria = testdb.getBidaiaria("testBidaiaria@email.com");
		testdb.close();

		// Erreserba ondo egin dela egiaztatu
		Erreserba e = updatedBidaiaria.erreserbaBilatu(ride.getRideNumber());

		assertNotNull("Erreserba existitu behar da", e);
		assertEquals(2, e.getnPlaces());
		assertEquals("itxaron", e.getEgoera());

		// Gorde erreserba cleanup-erako
		erreserba = e;
		createdErreserba = true;
	}

	//bilatu dagoeneko deuseztatuta dagoen erreseba eta eguneratu
	@Test
	public void testErreserbaEgin_deuseztatutakoErresebaEguneratu() {

		// Datuak prestatu
		bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
		driver = addDriver("testDriver@email.com", "Test Driver");
		ride = addRide("Donostia", "Bilbo", new Date(), 5, 15.0f, driver);
		
		// Sortu existitzen den deuseztatutako erreserba bat
		erreserba =bidaiaria.addErreserba(ride, 2);
		erreserba.setEgoera("deuseztatu");
		
		testdb.open();
		testdb.updateBidaiaria(bidaiaria);
		testdb.updateErreserba(erreserba);
		testdb.close();
		
		
		// Konprobaketak
		sut.erreserbaEgin(ride.getRideNumber(), bidaiaria, 3);
		
		// Egiaztatu erreserba eguneratu dela
		testdb.open();
		Bidaiaria updatedBidaiaria = testdb.getBidaiaria("testBidaiaria@email.com");
		testdb.close();
		
		Erreserba updatedErreserba = updatedBidaiaria.erreserbaBilatu(ride.getRideNumber());
		assertNotNull("Erreserba existitu behar da", updatedErreserba);
		assertEquals(3, updatedErreserba.getnPlaces());
		assertEquals("itxaron", updatedErreserba.getEgoera());
		assertTrue("Diru izoztua eguneratu behar da", updatedErreserba.getDiruIzoztua() > 0);
	}

	//bilatu dagoeneko erreseba eta eguneratu
	@Test
	public void testErreserbaEgin_ErresebaEguneratu() {

		// Datuak prestatu
		bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
		driver = addDriver("testDriver@email.com", "Test Driver");
		ride = addRide("Donostia", "Bilbo", new Date(), 10, 15.0f, driver);
		
		// Sortu existitzen den erreserba bat
		erreserba =bidaiaria.addErreserba(ride, 2);
		erreserba.setEgoera("itxaron");
		
		testdb.open();
		testdb.updateBidaiaria(bidaiaria);
		testdb.updateErreserba(erreserba);
		testdb.close();

		// Konprobaketak
		sut.erreserbaEgin(ride.getRideNumber(), bidaiaria, 4); // Gehitu 2 eserleku gehiago
		
		// Egiaztatu erreserba eguneratu dela
		testdb.open();
		Bidaiaria updatedBidaiaria = testdb.getBidaiaria("testBidaiaria@email.com");
		testdb.close();
		
		Erreserba updatedErreserba = updatedBidaiaria.erreserbaBilatu(ride.getRideNumber());
		assertNotNull("Erreserba existitu behar da", updatedErreserba);
		assertEquals(6, updatedErreserba.getnPlaces()); // 2 + 4 = 6
		assertEquals("itxaron", updatedErreserba.getEgoera());
		assertTrue("Diru izoztua eguneratu behar da", updatedErreserba.getDiruIzoztua() > 30.0f);
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

	private Ride addRide(String from, String to, Date date, int nPlaces, float price, Driver driver) {
		testdb.open();
		Ride ride = testdb.createRide(from, to, date, nPlaces, price, driver);
		createdRide = true;
		testdb.close();
		return ride;
	}


}