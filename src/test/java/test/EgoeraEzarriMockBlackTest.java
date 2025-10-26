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

public class EgoeraEzarriMockBlackTest {

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

    // ============================================================
    // TEST 1: Erreklamazioa ya resuelta → debe lanzar excepción
    // ============================================================
    @Test
    public void test_erreklamazioaEbatzitaException() {
        int erreklamazioZenbaki = 1;
        String egoera = "onartu";
        Erreklamazioa errek = new Erreklamazioa();
        errek.setEgoera("onartu"); // ya resuelta

        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(errek);

        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            fail("Debía lanzar erreklamazioaEbatzitaException");
        } catch (erreklamazioaEbatzitaException e) {
            assertTrue(true);
        }
    }

    // ============================================================
    // TEST 2: Cambiar estado a "onartu" desde "itxaron"
    // ============================================================
    @Test
    public void test_egoeraEzarri_onartu() {
        int erreklamazioZenbaki = 1;
        String egoera = "onartu";

        Erreserba erres = new Erreserba();
        Bidaiaria b = new Bidaiaria();
        Driver d = new Driver("driver", "pass");
        Erreklamazioa errek = new Erreklamazioa(erres, "deskribapena", b, d);
        errek.setEgoera("itxaron");

        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(errek);

        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            assertEquals("onartu", errek.getEgoera());
        } catch (Exception e) {
            fail("No debía lanzar excepción");
        }
    }

    // ============================================================
    // TEST 3: Cambiar estado a "deuseztatu" desde "itxaron"
    // ============================================================
    @Test
    public void test_egoeraEzarri_deuseztatu() {
        int erreklamazioZenbaki = 2;
        String egoera = "deuseztatu";

        Erreserba erres = new Erreserba();
        Bidaiaria b = new Bidaiaria();
        Driver d = new Driver("driver", "pass");
        Erreklamazioa errek = new Erreklamazioa(erres, "deskribapena", b, d);
        errek.setEgoera("itxaron");

        Admin admin = new Admin();
        List<Admin> admins = new ArrayList<>();
        admins.add(admin);

        TypedQuery<Admin> queryMock = mock(TypedQuery.class);
        when(queryMock.getResultList()).thenReturn(admins);
        when(db.createQuery("SELECT a FROM Admin a", Admin.class)).thenReturn(queryMock);

        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(errek);

        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            assertEquals("deuseztatu", errek.getEgoera());
        } catch (Exception e) {
            fail("No debía lanzar excepción");
        }
    }

    // ============================================================
    // TEST 4: Estado nuevo ≠ "onartu" ni "deuseztatu"
    // ============================================================
    @Test
    public void test_egoeraEzarri_egoeraDesberdina() {
        int erreklamazioZenbaki = 3;
        String egoeraBerria = "besteEgoera";

        Erreserba erres = new Erreserba();
        Bidaiaria b = new Bidaiaria();
        Driver d = new Driver("driver", "pass");
        Erreklamazioa errek = new Erreklamazioa(erres, "deskribapena", b, d);
        errek.setEgoera("itxaron");

        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(errek);

        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoeraBerria);
            sut.close();
            assertEquals("besteEgoera", errek.getEgoera());
        } catch (Exception e) {
            fail("No debía lanzar excepción");
        }
    }

    
    
    
    
    @Test
    public void test_ErreklamazioaNotFound() {
        int nonExistentId = 999;
        String egoera = "onartu";

        when(db.find(Erreklamazioa.class, nonExistentId)).thenReturn(null);

        sut.open();
        assertThrows(Exception.class, () -> sut.egoeraEzarri(nonExistentId, egoera));
        sut.close();
    }

    @Test
    public void test_NullEgoera() {
        int erreklamazioZenbaki = 1;
        Erreklamazioa errek = new Erreklamazioa();
        errek.setEgoera("itxaron");

        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(errek);

        sut.open();
        assertThrows(Exception.class, () -> sut.egoeraEzarri(erreklamazioZenbaki, null));
        sut.close();
    }
   
}
