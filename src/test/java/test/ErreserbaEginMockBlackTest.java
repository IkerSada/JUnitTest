package test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import dataAccess.DataAccess;
import domain.Bidaiaria;
import domain.Driver;
import domain.Erreserba;
import domain.Ride;

public class ErreserbaEginMockBlackTest {

	static DataAccess sut;

	protected MockedStatic<Persistence> persistenceMock;

	@Mock
	protected EntityManagerFactory entityManagerFactory;
	@Mock
	protected EntityManager db;
	@Mock
	protected EntityTransaction et;

	@Before
	public void init() {
		MockitoAnnotations.openMocks(this);
		persistenceMock = Mockito.mockStatic(Persistence.class);
		persistenceMock.when(() -> Persistence.createEntityManagerFactory(Mockito.any()))
		.thenReturn(entityManagerFactory);

		when(entityManagerFactory.createEntityManager()).thenReturn(db);
		when(db.getTransaction()).thenReturn(et);

		sut = new DataAccess(db);
	}

	@After
	public void tearDown() {
		persistenceMock.close();
	}

	//Bidaia eta bidaiaria existitzen da, eserlekukop>0 ---> erreserba ondo egin
	@Test
	public void testErreserbaEgin_ErreserbaBerria() {
		int bidaiZenbaki = 1;
		int eserlekuKop = 2;

		Driver driver = new Driver("driver1", "pass");
		Ride ride = new Ride("Bilbo", "Donostia", null, 4, 15, driver);
		ride.setRideNumber(bidaiZenbaki);

		Bidaiaria bidaiaria = new Bidaiaria("test@ehu.eus", "pass", "B1");
		bidaiaria.setDirua(100);

		when(db.find(Ride.class, bidaiZenbaki)).thenReturn(ride);
		when(db.find(Bidaiaria.class, bidaiaria.getEmail())).thenReturn(bidaiaria);

		Erreserba e = bidaiaria.erreserbaBilatu(bidaiZenbaki);
		assertNull(e);

		sut.erreserbaEgin(bidaiZenbaki, bidaiaria, eserlekuKop);

		e = bidaiaria.erreserbaBilatu(bidaiZenbaki);
		assertNotNull(e);
		assertEquals("itxaron", e.getEgoera());
		assertEquals(eserlekuKop, e.getnPlaces());

		float prezioa = ride.getBidaiarenPrezioa(eserlekuKop);
		assertEquals(100 - prezioa, bidaiaria.getDirua(), 0.001);
		assertEquals(prezioa, e.getDiruIzoztua(), 0.001);
	}

	//bidaia ez da existitzen ---> errorea saltatu (metodoaren akatsa)
	@Test
	public void testErreserbaEgin_BidaiaEzExistitzen() {
		int bidaiZenbaki = 4;
		int eserlekuKop = 1;

		Bidaiaria bidaiaria = new Bidaiaria("b4@ehu.eus", "pass", "B4");
		bidaiaria.setDirua(100);

		when(db.find(Bidaiaria.class, bidaiaria.getEmail())).thenReturn(bidaiaria); // Bidaia ez existitzen
		when(db.find(Ride.class, bidaiZenbaki)).thenReturn(null); // Bidaia ez existitzen

		sut.erreserbaEgin(bidaiZenbaki, bidaiaria, eserlekuKop);
		
		fail();

	}



	//bidaiaria ez da existitzen ---> errorea saltatu (metodoaren akatsa)
	@Test
	public void testErreserbaEgin_BidaiariaEzExistitzen() {
		int bidaiZenbaki = 3;
		int eserlekuKop = 2;

		Driver driver = new Driver("driver3", "pass");
		Ride ride = new Ride("Bilbo", "Gasteiz", null, 4, 12, driver);
		ride.setRideNumber(bidaiZenbaki);

		Bidaiaria bidaiaria = new Bidaiaria("b3@ehu.eus", "pass", "B3");
		bidaiaria.setDirua(100);

		when(db.find(Ride.class, bidaiZenbaki)).thenReturn(ride);
		when(db.find(Bidaiaria.class, bidaiaria.getEmail())).thenReturn(null); // Bidaiaria ez existitzen
		sut.erreserbaEgin(bidaiZenbaki, bidaiaria, eserlekuKop);

		fail();

	}



	//eserlekukop<=0 ---> metodoa ondo funtzionatuko da, zeren ez dugu sortu eserlekua negatiboa denean agertu behar den salbuespena
	@Test
	public void testErreserbaEgin_EserlekuKopNegatiboa() {
		int bidaiZenbaki = 2;
		int eserlekuKop = -1;

		Driver driver = new Driver("driver2", "pass");
		Ride ride = new Ride("Bilbo", "Donostia", null, 4, 15, driver);
		ride.setRideNumber(bidaiZenbaki);

		Bidaiaria bidaiaria = new Bidaiaria("b2@ehu.eus", "pass", "B2");
		bidaiaria.setDirua(100);


		when(db.find(Ride.class, bidaiZenbaki)).thenReturn(ride);
		when(db.find(Bidaiaria.class, bidaiaria.getEmail())).thenReturn(bidaiaria);

		sut.erreserbaEgin(bidaiZenbaki, bidaiaria, eserlekuKop);

	}
}
