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
import domain.Mugimendua;
import exceptions.erreklamazioaEbatzitaException;

public class EgoeraEzarriBDWhiteTest {
    
    static DataAccess sut;
    
    protected MockedStatic<Persistence> persistenceMock;

    @Mock
    protected EntityManagerFactory entityManagerFactory;
    @Mock
    protected EntityManager db;
    @Mock
    protected EntityTransaction et;
    @Mock
    protected TypedQuery<Admin> adminQuery;

    @Before
    public void hasieratu() {
        MockitoAnnotations.openMocks(this);
        persistenceMock = Mockito.mockStatic(Persistence.class);
        persistenceMock.when(() -> Persistence.createEntityManagerFactory(Mockito.any()))
            .thenReturn(entityManagerFactory);
        
        Mockito.doReturn(db).when(entityManagerFactory).createEntityManager();
        Mockito.doReturn(et).when(db).getTransaction();
        
        // Mock para las operaciones de transacción
        Mockito.doNothing().when(et).begin();
        Mockito.doNothing().when(et).commit();
        Mockito.doNothing().when(et).rollback();
        
        sut = new DataAccess(db);
    }
    
    @After
    public void amaitu() {
        persistenceMock.close();
    }

    @Test
    public void testEgoeraEzarriDeuseztatu() throws erreklamazioaEbatzitaException {
        // ========== PRESTAKETA ==========
        // Crear objetos de dominio reales
        Erreserba erres = new Erreserba();
        Bidaiaria b = new Bidaiaria("bidaiaria@test.com", "pass", "Bidaiaria");
        Driver d = new Driver("driver@test.com", "pass", "Driver");

        int erreklamazioZenbaki = 1; 
        String egoera = "deuseztatu";

        Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron");

        // Admin simulado
        Admin admin = new Admin("admin@test.com", "admin");
        List<Admin> adminList = new ArrayList<>();
        adminList.add(admin);

        // ========== MOCK CONFIGURATION ==========
        when(db.createQuery("SELECT a FROM Admin a", Admin.class)).thenReturn(adminQuery);
        when(adminQuery.getResultList()).thenReturn(adminList);
        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);

        // ========== EXEKUZIOA ==========
        sut.open();
        sut.egoeraEzarri(erreklamazioZenbaki, egoera);
        sut.close();

        // ========== EGIAZTAPENAK ==========
        // 1. Verificar que el estado cambió correctamente
        assertEquals("deuseztatu", rr1.getEgoera());

        // 2. Verificar que el admin recibió la reclamación
        assertTrue("Admin-ak erreklamazioa jaso behar zuen", 
                   admin.getJasotakoErreklamazioak().contains(rr1));

        // 3. Verificar interacciones con la base de datos
        verify(db).find(Erreklamazioa.class, erreklamazioZenbaki);
        verify(db).createQuery("SELECT a FROM Admin a", Admin.class);
        verify(adminQuery).getResultList();
        verify(et).begin();
        verify(et).commit();
        
        // 4. Verificar que NO se realizaron operaciones de dinero
        assertEquals(0.0f, b.getDirua(), 0.001);
        assertEquals(0.0f, d.getDirua(), 0.001);
    }

    @Test
    public void testEgoeraEzarriOnartu() throws erreklamazioaEbatzitaException {
        // ========== PRESTAKETA ==========
        Erreserba erres = new Erreserba();
        erres.setDiruIzoztua(100.0f);

        Bidaiaria b = new Bidaiaria("bidaiaria@test.com", "pass", "Bidaiaria");
        Driver d = new Driver("driver@test.com", "pass", "Driver");

        // Establecer dinero inicial
        b.setDirua(50.0f);
        d.setDirua(200.0f);

        int erreklamazioZenbaki = 1;
        String egoera = "onartu";

        Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron");

        // ========== MOCK CONFIGURATION ==========
        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);

        // ========== EXEKUZIOA ==========
        sut.open();
        sut.egoeraEzarri(erreklamazioZenbaki, egoera);
        sut.close();

        // ========== EGIAZTAPENAK ==========
        // 1. Verificar cambio de estado
        assertEquals("onartu", rr1.getEgoera());

        // 2. Verificar transferencia de dinero
        assertEquals("Bidaiariaren dirua eguneratuta egon behar da", 
                     150.0f, b.getDirua(), 0.001); // 50 + 100
        assertEquals("Gidariaren dirua eguneratuta egon behar da", 
                     100.0f, d.getDirua(), 0.001); // 200 - 100

        // 3. Verificar que se crearon movimientos
        assertFalse("Bidaiariak mugimenduak izan behar ditu", 
                   b.getMugimenduak().isEmpty());
        assertFalse("Gidariak mugimenduak izan behar ditu", 
                   d.getMugimenduak().isEmpty());

        // 4. Verificar interacciones con BD
        verify(db).find(Erreklamazioa.class, erreklamazioZenbaki);
        verify(et).begin();
        verify(et).commit();
        
        // 5. Verificar que NO se consultó a Admin
        verify(db, never()).createQuery("SELECT a FROM Admin a", Admin.class);
    }

    @Test
    public void testEgoeraEzarriItxaronEgoeraBerriaEzOnartuEzDeuseztatu() throws erreklamazioaEbatzitaException {
        // ========== PRESTAKETA ==========
        Erreserba erres = new Erreserba();
        Bidaiaria b = new Bidaiaria("bidaiaria@test.com", "pass", "Bidaiaria");
        Driver d = new Driver("driver@test.com", "pass", "Driver");

        int erreklamazioZenbaki = 1; 
        String egoeraBerria = "besteEgoeraBat"; // No es "onartu" ni "deuseztatu"

        Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron");

        // ========== MOCK CONFIGURATION ==========
        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);

        // ========== EXEKUZIOA ==========
        sut.open();
        sut.egoeraEzarri(erreklamazioZenbaki, egoeraBerria);
        sut.close();

        // ========== EGIAZTAPENAK ==========
        // 1. Verificar cambio de estado
        assertEquals(egoeraBerria, rr1.getEgoera());

        // 2. Verificar que NO hubo transferencia de dinero
        assertEquals(0.0f, b.getDirua(), 0.001);
        assertEquals(0.0f, d.getDirua(), 0.001);

        // 3. Verificar que NO se consultó a Admin
        verify(db, never()).createQuery("SELECT a FROM Admin a", Admin.class);

        // 4. Verificar interacciones básicas
        verify(db).find(Erreklamazioa.class, erreklamazioZenbaki);
        verify(et).begin();
        verify(et).commit();
    }

    @Test
    public void testErreklamazioaEbatziaDagoenean() {
        // ========== PRESTAKETA ==========
        Erreserba erres = new Erreserba();
        erres.setDiruIzoztua(100.0f);
        
        Bidaiaria b = new Bidaiaria("bidaiaria@test.com", "pass", "Bidaiaria");
        Driver d = new Driver("driver@test.com", "pass", "Driver");
        
        int erreklamazioZenbaki = 1; 
        String egoera = "onartu";
        
        // Reclamación ya RESUELTA (no en estado "itxaron")
        Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("ebatzia"); // ← Estado diferente de "itxaron"
        
        // ========== MOCK CONFIGURATION ==========
        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);
        
        // ========== EXEKUZIOA ==========
        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            fail("erreklamazioaEbatzitaException jaurti behar zuen");
        } catch (erreklamazioaEbatzitaException e) {
            // ✅ ÉXITO - Se lanzó la excepción esperada
            assertTrue("Salbuespena zuzenki jaurti da", true);
        } catch (Exception e) {
            fail("Salbuespen okerra: " + e.getClass().getSimpleName());
        }
        
        // ========== EGIAZTAPENAK ADICIONALES ==========
        // Verificar que NO se modificó el dinero
        assertEquals(0.0f, b.getDirua(), 0.001);
        assertEquals(0.0f, d.getDirua(), 0.001);
        
        // Verificar que la transacción se hizo rollback o no se completó
        verify(et, never()).commit();
    }
/*
    @Test
    public void testEgoeraEzarriEgoeraNull() {
        // ========== PRESTAKETA ==========
        Erreserba erres = new Erreserba();
        Bidaiaria b = new Bidaiaria("bidaiaria@test.com", "pass", "Bidaiaria");
        Driver d = new Driver("driver@test.com", "pass", "Driver");

        int erreklamazioZenbaki = 1; 
        String egoeraBerria = null;

        Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron");

        // ========== MOCK CONFIGURATION ==========
        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);

        // ========== EXEKUZIOA ==========
        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoeraBerria);
            sut.close();
            
            // ========== EGIAZTAPENAK ==========
            // Verificar que se estableció el estado null
            assertNull(rr1.getEgoera());
            
            // Verificar interacciones básicas
            verify(db).find(Erreklamazioa.class, erreklamazioZenbaki);
            verify(et).begin();
            verify(et).commit();
            
        } catch (Exception e) {
            fail("Ez luke salbuespenik jaurti behar egoera null denean: " + e.getMessage());
        }
    }*/
/*
    @Test
    public void testEgoeraEzarriTransakzioErrorea() {
        // ========== PRESTAKETA ==========
        Erreserba erres = new Erreserba();
        Bidaiaria b = new Bidaiaria("bidaiaria@test.com", "pass", "Bidaiaria");
        Driver d = new Driver("driver@test.com", "pass", "Driver");

        int erreklamazioZenbaki = 1; 
        String egoera = "onartu";

        Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron");

        // ========== MOCK CONFIGURATION ==========
        when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);
        
        // Simular error en la transacción
        doThrow(new RuntimeException("Database error")).when(et).commit();

        // ========== EXEKUZIOA ==========
        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            fail("Salbuespena jaurti behar zuen");
        } catch (RuntimeException e) {
            // ✅ ÉXITO - Se propagó la excepción
            assertEquals("Database error", e.getMessage());
        } catch (Exception e) {
            fail("Salbuespen okerra: " + e.getClass().getSimpleName());
        }

        // ========== EGIAZTAPENAK ==========
        // Verificar que se intentó hacer rollback
        verify(et).rollback();
    }*/
}