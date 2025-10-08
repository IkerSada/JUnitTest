package test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import dataAccess.DataAccess;
import domain.Admin;
import domain.Bidaiaria;
import domain.Driver;
import domain.Erreklamazioa;
import domain.Erreserba;
import exceptions.erreklamazioaEbatzitaException;

public class EgoeraEzarriMockWhiteTest {
    
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
        
        Mockito.doReturn(db).when(entityManagerFactory).createEntityManager();
        Mockito.doReturn(et).when(db).getTransaction();
        sut = new DataAccess(db);
    }
    
    @After
    public void tearDown() {
        persistenceMock.close();
    }

    @Test
    public void erreklamazioaEbatzitaException() {
    	
    	//parametros de la reclamacion
    	 Erreserba erresMock = new Erreserba();
    	 Bidaiaria bMock = new Bidaiaria();
    	 Driver driver = new Driver("driverUser","pass");
    	
		//parametros del metodo
    	int erreklamazioZenbaki = 1; 
    	String egoera="onartu";
    	
    	
    	try {   	       	 
    		Erreklamazioa rr1 = new Erreklamazioa(erresMock,"deskribapena",bMock,driver);
    		rr1.setErreklamazioZenbaki(erreklamazioZenbaki); // si existe el setter
    	    rr1.setEgoera(egoera); 
    	      		
    	    Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);
    	    
    	    sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
         
			fail();
    	}
    	catch (erreklamazioaEbatzitaException e) {
			sut.close();
			assertTrue(true);
        }	
    }
    
  
    @Test
    public void test_egoeraEzarri_onartu() {
        
        // parámetros de la reclamación
        Erreserba erres = new Erreserba();
        erres.setDiruIzoztua(100.0f); // dinero bloqueado en la reserva
        
        Bidaiaria b = new Bidaiaria();
        Driver d = new Driver("driverUser", "pass");
        
        // saldo inicial (si tu clase tiene este atributo)
        b.setDirua(50.0f);
        d.setDirua(200.0f);
        
        // parámetros del método
        int erreklamazioZenbaki = 1; 
        String egoera = "onartu";
        
        try {
            // crear reclamación con estado inicial correcto
            Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
            rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
            rr1.setEgoera("itxaron"); // solo así entra al bloque principal
            
            // simular que el find devuelve esta reclamación
            Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);
            
            // ejecutar
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            
            // si llega aquí, todo fue bien
            assertEquals("onartu", rr1.getEgoera());
            assertEquals(150.0f, b.getDirua(), 0.001); // 50 + 100
            assertEquals(100.0f, d.getDirua(), 0.001); // 200 - 100
            assertTrue(true); // confirma que no ha habido excepciones
            
        } catch (erreklamazioaEbatzitaException e) {
            // si llega aquí, algo fue mal (no debería lanzar)
            sut.close();
            fail("No debería lanzarse erreklamazioaEbatzitaException");
        }
    }



    @Test
    public void test_egoeraEzarri_deuseztatu() {
        // parámetros de la reclamación
        Erreserba erres = new Erreserba();
        Bidaiaria b = new Bidaiaria();
        Driver d = new Driver("driverUser", "pass");

        // parámetros del método
        int erreklamazioZenbaki = 1; 
        String egoera = "deuseztatu";
        
        try {
            // reclamación inicial
            Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
            rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
            rr1.setEgoera("itxaron");
            
            // Admin simulado (lista con un admin)
            Admin admin = new Admin();
            List<Admin> adminList = new ArrayList<>();
            adminList.add(admin);
            
            // mock del query
            TypedQuery<Admin> queryMock = Mockito.mock(TypedQuery.class);
            Mockito.when(queryMock.getResultList()).thenReturn(adminList);
            Mockito.when(db.createQuery("SELECT a FROM Admin a", Admin.class)).thenReturn(queryMock);
            
            // el find devuelve la reclamación
            Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);
            
            // ejecutar
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            
            // verificar
            assertEquals("deuseztatu", rr1.getEgoera());
            assertTrue(admin.getJasotakoErreklamazioak().contains(rr1));
            
        } catch (erreklamazioaEbatzitaException e) {
            sut.close();
            fail("No debería lanzarse erreklamazioaEbatzitaException");
        }
    }

    
    @Test
    public void test_egoeraEzarri_itxaronEgoeraBerriaEzOnartuEzDeuseztatu() {
        // parámetros de la reclamación
        Erreserba erresMock = new Erreserba();
        Bidaiaria bMock = new Bidaiaria();
        Driver driverMock = new Driver("driverUser", "pass");
        
        // parámetros del método
        int erreklamazioZenbaki = 1; 
        String egoeraBerria = "besteBat"; // Estado diferente a "onartu" o "deuseztatu"
        
        try {
            // crear reclamación con estado inicial "itxaron"
            Erreklamazioa rr1 = new Erreklamazioa(erresMock, "deskribapena", bMock, driverMock);
            rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
            rr1.setEgoera("itxaron"); // Estado inicial correcto para entrar en el if
            
            // configure the state through mocks
            Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);
            
            // invoke System Under Test (sut)
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoeraBerria);
            sut.close();
            
            // verify the results
            assertEquals("El estado debería haber cambiado a 'besteBat'", egoeraBerria, rr1.getEgoera());
            
        } catch (erreklamazioaEbatzitaException e) {
            sut.close();
            fail("No debería lanzarse erreklamazioaEbatzitaException cuando el estado inicial es 'itxaron'");
        }
    }

    
    
    
    
    
    
    
    


}