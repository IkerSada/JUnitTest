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

public class ErreserbaEginMockWhiteTest {

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
	
	//erreseba berria sortu
	@Test
	public void testErreserbaEgin_ErreserbaBerria() {
		int bidaiZenbaki = 1;
		int eserlekuKop = 2;
		
		if(bidaiZenbaki<2) {
			System.out.println("estaldura jaisteko proba");
		}
		else {
			System.out.println("esaldi hau ez da inoiz pantailaratuko");
			System.out.println("esaldi hau ez da inoiz pantailaratuko");
			System.out.println("esaldi hau ez da inoiz pantailaratuko");
			Driver driver2 = new Driver("driver2", "pass2");
		}
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

	
	//bilatu dagoeneko deuseztatuta dagoen erreseba eta eguneratu
	@Test
	public void testErreserbaEgin_deuseztatutakoErresebaEguneratu() {
		int bidaiZenbaki = 2;
		int eserlekuKop = 1;

		Driver driver = new Driver("driver2", "pass");
		Ride ride = new Ride("Bilbo", "Donostia", null, 4, 15, driver);
		ride.setRideNumber(bidaiZenbaki);

		Bidaiaria bidaiaria = new Bidaiaria("b2@ehu.eus", "pass", "B2");
		bidaiaria.setDirua(100);
		
	    Erreserba aktiboa = bidaiaria.addErreserba(ride, 0);
	    aktiboa.setEgoera("deuseztatu");
	    
		when(db.find(Ride.class, bidaiZenbaki)).thenReturn(ride);
		when(db.find(Bidaiaria.class, bidaiaria.getEmail())).thenReturn(bidaiaria);
		

		sut.erreserbaEgin(bidaiZenbaki, bidaiaria, eserlekuKop);

		Erreserba e = bidaiaria.erreserbaBilatu(bidaiZenbaki);
		assertNotNull("Erreserba existitu behar da", e);
		assertEquals("Egoera 'itxaron' eguneratu behar da", "itxaron", e.getEgoera());
		assertEquals("Eserleku kopurua eguneratu behar da", eserlekuKop, e.getnPlaces());
		assertSame("Erreserba berbera eguneratu behar da", aktiboa, e);
	}
	
	//bilatu dagoeneko erreseba eta eguneratu
	@Test
	public void testErreserbaEgin_ErresebaEguneratu() {
	    int bidaiZenbaki = 3;
	    int eserlekuKop = 2;

	    Driver driver = new Driver("driver3", "pass");
	    Ride ride = new Ride("Bilbo", "Gasteiz", null, 4, 12, driver);
	    ride.setRideNumber(bidaiZenbaki);

	    Bidaiaria bidaiaria = new Bidaiaria("b3@ehu.eus", "pass", "B3");
	    bidaiaria.setDirua(100);
	    
	    // Aurretik zegoen erreserba: 0 eserlekurekin (berria balitz bezala)
	    Erreserba aktiboa = bidaiaria.addErreserba(ride, 0);
	    aktiboa.setEgoera("itxaron");
	    
	    when(db.find(Bidaiaria.class, bidaiaria.getEmail())).thenReturn(bidaiaria);
	    when(db.find(Ride.class, bidaiZenbaki)).thenReturn(ride);

	    sut.erreserbaEgin(bidaiZenbaki, bidaiaria, eserlekuKop);
	    
	    Erreserba e = bidaiaria.erreserbaBilatu(bidaiZenbaki);
	    assertNotNull("Erreserba existitu behar da", e);
	    assertEquals("Egoera 'itxaron' eguneratu behar da", "itxaron", e.getEgoera());
	    assertEquals("Eserleku kopurua eguneratu behar da", eserlekuKop, e.getnPlaces());
	    assertSame("Erreserba berbera eguneratu behar da", aktiboa, e);
	}
}
