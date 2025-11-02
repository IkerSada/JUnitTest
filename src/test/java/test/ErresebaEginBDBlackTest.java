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

public class ErresebaEginBDBlackTest {

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



	//Bidaia eta bidaiaria existitzen da, eserlekukop>0 ---> erreserba ondo egin
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

	//bidaia ez da existitzen ---> errorea saltatu (metodoaren akatsa)
	@Test
	public void  testErreserbaEgin_BidaiaEzExistitzen() {

		// Datuak prestatu
		bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
		int existitzenEzDenBidaiZenbakia = 9999;

		// Konprobaketak
		sut.erreserbaEgin(existitzenEzDenBidaiZenbakia, bidaiaria, 2);
		fail();


	}

	//bidaiaria ez da existitzen ---> errorea saltatu (metodoaren akatsa)
	@Test
	public void testErreserbaEgin_BidaiariaEzExistitzen() {

		// Datuak prestatu
		driver = addDriver("testDriver@email.com", "Test Driver");
		ride = addRide("Donostia", "Bilbo", new Date(), 5, 15.0f, driver);
		Bidaiaria existitzenEzDenBidaiaria = new Bidaiaria("ezexistitzen@email.com", "password", "Ez Existitzen");

		// Konprobaketak         
		sut.erreserbaEgin(ride.getRideNumber(), existitzenEzDenBidaiaria, 2);
		fail();


	}

	//eserlekukop<=0 ---> metodoa ondo funtzionatuko da, zeren ez dugu sortu eserlekua negatiboa denean agertu behar den salbuespena
	@Test
	public void testErreserbaEgin_EserlekuKopNegatiboa() {

		// Datuak prestatu
		bidaiaria = addBidaiaria("testBidaiaria@email.com", "Test Bidaiaria");
		driver = addDriver("testDriver@email.com", "Test Driver");
		ride = addRide("Donostia", "Bilbo", new Date(), 5, 15.0f, driver);

		// Konprobaketak

		sut.erreserbaEgin(ride.getRideNumber(), bidaiaria, -1);



		// Bidaiaria berriro kargatu datu-base-tik erreserbak eguneratzeko
		testdb.open();
		Bidaiaria updatedBidaiaria = testdb.getBidaiaria("testBidaiaria@email.com");
		testdb.close();

		// Erreserba ondo egingo du zeren gure metodoan ez da salbuespenak altxatzen eserlekukop negatiboa denean
		Erreserba e = updatedBidaiaria.erreserbaBilatu(ride.getRideNumber());

		assertNotNull("Erreserba existitu behar da", e);
		//eserlekukop negatiboa da
		assertEquals(-1, e.getnPlaces());
		assertEquals("itxaron", e.getEgoera());

		// Gorde erreserba cleanup-erako
		erreserba = e;
		createdErreserba = true;

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