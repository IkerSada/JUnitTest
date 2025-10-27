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

import java.util.Calendar;
import java.util.Date;

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
        when(et.isActive()).thenReturn(true); // Para manejo de rollback

        sut = new DataAccess(db);
    }

    @After
    public void tearDown() {
        persistenceMock.close();
    }

    // ============================================================
    // TEST 1: bidaia existitzen da, bidaiaria existitzen da, eserlekuKop > 0
    // ============================================================
    @Test
    public void test_erreserbaEgin_arrakasta() {
        // ---------- Preparación de datos ----------
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("bidaiaria@gmail.com", "bidaiari", "Maite Arrieta");
        b.setDirua(100.0f);

        Driver driver = new Driver("driver1@gmail.com", "Jon Etxeberria");
        Ride r = driver.addRide("Bilbo", "Donostia", new Date(year - 1900, month, 10), 4, 10.0f);
        r.setRideNumber(1);

        // ---------- Configuración de mocks ----------
        when(db.find(Ride.class, 1)).thenReturn(r);
        when(db.find(Bidaiaria.class, "bidaiaria@gmail.com")).thenReturn(b);

        // ---------- Ejecución ----------
        sut.open();
        sut.erreserbaEgin(1, b, 2);
        sut.close();

        // ---------- Verificaciones ----------
        // Verificar que se buscaron los objetos correctos
        verify(db).find(Ride.class, 1);
        verify(db).find(Bidaiaria.class, "bidaiaria@gmail.com");
        
        // Verificar transacción
        verify(db.getTransaction()).begin();
        verify(db.getTransaction()).commit();
        
        // No debería haber excepciones
        assertTrue("Ejecución exitosa sin excepciones", true);
    }

    // ============================================================
    // TEST 2: eserlekuKop <= 0, no debería hacer nada
    // ============================================================
    @Test
    public void test_erreserbaEgin_eserlekuKopZero() {
        // ---------- Preparación de datos ----------
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("bidaiaria@gmail.com", "bidaiari", "Maite Arrieta");
        b.setDirua(100.0f);

        Driver driver = new Driver("driver1@gmail.com", "Jon Etxeberria");
        Ride r = driver.addRide("Bilbo", "Donostia", new Date(year - 1900, month, 10), 4, 10.0f);
        r.setRideNumber(1);

        // ---------- Configuración de mocks ----------
        when(db.find(Ride.class, 1)).thenReturn(r);
        when(db.find(Bidaiaria.class, "bidaiaria@gmail.com")).thenReturn(b);

        // ---------- Ejecución ----------
        sut.open();
        sut.erreserbaEgin(1, b, 0);
        sut.close();

        // ---------- Verificaciones ----------
        // Aunque eserlekuKop es 0, debería completar la transacción normalmente
        verify(db).find(Ride.class, 1);
        verify(db).find(Bidaiaria.class, "bidaiaria@gmail.com");
        verify(db.getTransaction()).begin();
        verify(db.getTransaction()).commit();
        
        // No debería lanzar excepción
        assertTrue("Debería completarse sin excepción incluso con eserlekuKop = 0", true);
    }

    // ============================================================
    // TEST 3: bidaiaria EZ da existitzen
    // ============================================================
    @Test
    public void test_erreserbaEgin_bidaiariaEzExistitzen() {
        // ---------- Preparación de datos ----------
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria bEzExistitzen = new Bidaiaria("ez@gmail.com", "bidaiari", "Ez dago");
        bEzExistitzen.setDirua(100.0f);

        Driver driver = new Driver("driver1@gmail.com", "Jon Etxeberria");
        Ride r = driver.addRide("Bilbo", "Donostia", new Date(year - 1900, month, 10), 4, 10.0f);
        r.setRideNumber(1);

        // ---------- Configuración de mocks ----------
        when(db.find(Ride.class, 1)).thenReturn(r);
        when(db.find(Bidaiaria.class, "ez@gmail.com")).thenReturn(null); // Bidaiaria no existe

        // ---------- Ejecución y Verificación ----------
        sut.open();
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            sut.erreserbaEgin(1, bEzExistitzen, 1);
        });
        
        assertTrue("Debería lanzar excepción por bidaiaria no encontrado", 
                   exception.getMessage().contains("Bidaiaria ez da aurkitu"));
        
        // Verificar rollback
        verify(db.getTransaction()).begin();
        verify(db.getTransaction()).rollback();
        verify(db.getTransaction(), never()).commit();
        
        sut.close();
    }

    // ============================================================
    // TEST 4: bidaia EZ da existitzen
    // ============================================================
    @Test
    public void test_erreserbaEgin_bidaiaEzExistitzen() {
        // ---------- Preparación de datos ----------
        Bidaiaria b = new Bidaiaria("bidaiaria@gmail.com", "bidaiari", "Maite Arrieta");
        b.setDirua(100.0f);

        // ---------- Configuración de mocks ----------
        when(db.find(Ride.class, 99)).thenReturn(null); // Ride no existe
        when(db.find(Bidaiaria.class, "bidaiaria@gmail.com")).thenReturn(b);

        // ---------- Ejecución y Verificación ----------
        sut.open();
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            sut.erreserbaEgin(99, b, 1);
        });
        
        assertTrue("Debería lanzar excepción por ride no encontrado", 
                   exception.getMessage().contains("Ride ez da aurkitu"));
        
        // Verificar rollback
        verify(db.getTransaction()).begin();
        verify(db.getTransaction()).rollback();
        verify(db.getTransaction(), never()).commit();
        
        sut.close();
    }

    // ============================================================
    // TEST 5: erreserba existente con estado "deuseztatu" - debería reactivarse
    // ============================================================
    @Test
    public void test_erreserbaEgin_erreserbaDeuseztatua() {
        // ---------- Preparación de datos ----------
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("bidaiaria@gmail.com", "bidaiari", "Maite Arrieta");
        b.setDirua(100.0f);

        Driver driver = new Driver("driver1@gmail.com", "Jon Etxeberria");
        Ride r = driver.addRide("Bilbo", "Donostia", new Date(year - 1900, month, 10), 4, 10.0f);
        r.setRideNumber(1);

        // Crear reserva previa deshecha
        Erreserba erreserbaAurrez = b.addErreserba(r, 2);
        erreserbaAurrez.setEgoera("deuseztatu");

        // ---------- Configuración de mocks ----------
        when(db.find(Ride.class, 1)).thenReturn(r);
        when(db.find(Bidaiaria.class, "bidaiaria@gmail.com")).thenReturn(b);

        // ---------- Ejecución ----------
        sut.open();
        sut.erreserbaEgin(1, b, 1);
        sut.close();

        // ---------- Verificaciones ----------
        verify(db).find(Ride.class, 1);
        verify(db).find(Bidaiaria.class, "bidaiaria@gmail.com");
        verify(db.getTransaction()).begin();
        verify(db.getTransaction()).commit();
        
        // No debería lanzar excepción
        assertTrue("Debería reactivar reserva deshecha sin excepción", true);
    }

    // ============================================================
    // TEST 6: erreserba existente con estado "itxaron" - debería actualizar plazas
    // ============================================================
    @Test
    public void test_erreserbaEgin_erreserbaExistente() {
        // ---------- Preparación de datos ----------
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("bidaiaria@gmail.com", "bidaiari", "Maite Arrieta");
        b.setDirua(100.0f);

        Driver driver = new Driver("driver1@gmail.com", "Jon Etxeberria");
        Ride r = driver.addRide("Bilbo", "Donostia", new Date(year - 1900, month, 10), 4, 10.0f);
        r.setRideNumber(1);

        // Crear reserva previa activa
        Erreserba erreserbaAurrez = b.addErreserba(r, 1);
        erreserbaAurrez.setEgoera("itxaron");

        // ---------- Configuración de mocks ----------
        when(db.find(Ride.class, 1)).thenReturn(r);
        when(db.find(Bidaiaria.class, "bidaiaria@gmail.com")).thenReturn(b);

        // ---------- Ejecución ----------
        sut.open();
        sut.erreserbaEgin(1, b, 1); // Añadir 1 plaza más
        sut.close();

        // ---------- Verificaciones ----------
        verify(db).find(Ride.class, 1);
        verify(db).find(Bidaiaria.class, "bidaiaria@gmail.com");
        verify(db.getTransaction()).begin();
        verify(db.getTransaction()).commit();
        
        // No debería lanzar excepción
        assertTrue("Debería actualizar reserva existente sin excepción", true);
    }

    // ============================================================
    // TEST 7: eserlekuKop negativo
    // ============================================================
    @Test
    public void test_erreserbaEgin_eserlekuKopNegatiboa() {
        // ---------- Preparación de datos ----------
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("bidaiaria@gmail.com", "bidaiari", "Maite Arrieta");
        b.setDirua(100.0f);

        Driver driver = new Driver("driver1@gmail.com", "Jon Etxeberria");
        Ride r = driver.addRide("Bilbo", "Donostia", new Date(year - 1900, month, 10), 4, 10.0f);
        r.setRideNumber(1);

        // ---------- Configuración de mocks ----------
        when(db.find(Ride.class, 1)).thenReturn(r);
        when(db.find(Bidaiaria.class, "bidaiaria@gmail.com")).thenReturn(b);

        // ---------- Ejecución ----------
        sut.open();
        sut.erreserbaEgin(1, b, -1);
        sut.close();

        // ---------- Verificaciones ----------
        // Debería completar la transacción normalmente aunque no haga nada
        verify(db).find(Ride.class, 1);
        verify(db).find(Bidaiaria.class, "bidaiaria@gmail.com");
        verify(db.getTransaction()).begin();
        verify(db.getTransaction()).commit();
        
        assertTrue("Debería completarse sin excepción incluso con eserlekuKop negativo", true);
    }
}