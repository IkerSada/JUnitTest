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
    public void hasieratu() {
        MockitoAnnotations.openMocks(this);
        persistenceMock = Mockito.mockStatic(Persistence.class);
        persistenceMock.when(() -> Persistence.createEntityManagerFactory(Mockito.any()))
            .thenReturn(entityManagerFactory);
        
        Mockito.doReturn(db).when(entityManagerFactory).createEntityManager();
        Mockito.doReturn(et).when(db).getTransaction();
        sut = new DataAccess(db);
    }
    
    @After
    public void amaitu() {
        persistenceMock.close();
    }

    @Test
    public void testEgoeraEzarriDeuseztatu() throws exceptions.erreklamazioaEbatzitaException {
        // ---------- Prestaketa ----------
        Erreserba erres = new Erreserba();
        Bidaiaria b = new Bidaiaria();
        Driver d = new Driver("driverUser", "pass");

        int erreklamazioZenbaki = 1; 
        String egoera = "deuseztatu";

        Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron");

        // Admin simulaturik
        Admin admin = new Admin();
        List<Admin> adminList = new ArrayList<>();
        adminList.add(admin);

        // ---------- Mock-ak ----------
        TypedQuery<Admin> queryMock = Mockito.mock(TypedQuery.class);
        Mockito.when(queryMock.getResultList()).thenReturn(adminList);
        Mockito.when(db.createQuery("SELECT a FROM Admin a", Admin.class)).thenReturn(queryMock);
        Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);

        // ---------- Exekuzioa ----------
        sut.open();
        sut.egoeraEzarri(erreklamazioZenbaki, egoera);
        sut.close();

        // ---------- Egiaztapenak ----------

        // 1️⃣ Egoera aldatzen da zuzenki
        assertEquals("deuseztatu", rr1.getEgoera());

        // 2️⃣ Admin-ak erreklamazioa jasotzen du
        assertTrue(admin.getJasotakoErreklamazioak().contains(rr1));

        // 3️⃣ Erreklamazioa bilatzen da datu-basean
        Mockito.verify(db).find(Erreklamazioa.class, erreklamazioZenbaki);

        // 4️⃣ Admin-ak lortzeko query-a sortzen da
        Mockito.verify(db).createQuery("SELECT a FROM Admin a", Admin.class);

        // 5️⃣ Admin-en zerrenda lortzen da query-tik
        Mockito.verify(queryMock).getResultList();

        // 6️⃣ Transakzio kontrola zuzena
        Mockito.verify(db.getTransaction()).begin();
        Mockito.verify(db.getTransaction()).commit();

        // ✅ Hona heltzen bada salbuespenik gabe, dena zuzen
    }

    @Test
    public void testEgoeraEzarriItxaronEgoeraBerriaEzOnartuEzDeuseztatu() {
        // ---------- Prestaketa ----------
        Erreserba erresMock = new Erreserba();
        Bidaiaria bMock = new Bidaiaria();
        Driver driverMock = new Driver("driverUser", "pass");

        int erreklamazioZenbaki = 1; 
        String egoeraBerria = "besteBat"; // ez "onartu" ez "deuseztatu"

        // Erreklamazio hasierakoa
        Erreklamazioa rr1 = new Erreklamazioa(erresMock, "deskribapena", bMock, driverMock);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron"); // egoera hasierako zuzena

        // ---------- Mock-ak ----------
        Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);

        // ---------- Exekuzioa ----------
        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoeraBerria);
            sut.close();

            // ---------- Egiaztapenak ----------

            // 1️⃣ Egoera aldatzen da zuzenki
            assertEquals("Egoerak 'besteBat'ra aldatu behar zuen", egoeraBerria, rr1.getEgoera());

            // 2️⃣ Erreklamazioa bilatzen da datu-basean
            Mockito.verify(db).find(Erreklamazioa.class, erreklamazioZenbaki);

            // 3️⃣ Transakzio kontrola zuzena
            Mockito.verify(db.getTransaction()).begin();
            Mockito.verify(db.getTransaction()).commit();

            // 4️⃣ Ez litzateke query gehigarririk sortu behar dirua aldatu gabe
            Mockito.verify(db, Mockito.never()).createQuery(Mockito.anyString(), Mockito.any());
            
        } catch (erreklamazioaEbatzitaException e) {
            sut.close();
            fail("Ez litzateke erreklamazioaEbatzitaException jaurti behar egoera hasierakoa 'itxaron' denean");
        }
    }

    @Test
    public void testErreklamazioaEbatziaDagoenean() {
        // Konfigurazioa ZUZENA
        Erreserba erresMock = new Erreserba();
        erresMock.setDiruIzoztua(100.0f);
        
        Bidaiaria bMock = new Bidaiaria();
        Driver driver = new Driver("driverUser","pass");
        
        int erreklamazioZenbaki = 1; 
        String egoera = "onartu";
        
        // Erreklamazioa EBATZITA dago (ez "itxaron" egoeran)
        Erreklamazioa rr1 = new Erreklamazioa(erresMock,"deskribapena",bMock,driver);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("onartu"); // ← Egoera "itxaron" ezberdina
        
        Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);
        
        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            fail("erreklamazioaEbatzitaException jaurti behar zuen");
        } catch (erreklamazioaEbatzitaException e) {
            // ✅ ZUZENA - Espero zen salbuespena jaurti da
            assertTrue("Salbuespena zuzenki jaurti da", true);
        } catch (Exception e) {
            fail("Salbuespen okerra: " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void testEgoeraEzarriOnartu() {
        // Konfigurazioa
        Erreserba erres = new Erreserba();
        erres.setDiruIzoztua(100.0f);

        Bidaiaria b = new Bidaiaria();
        Driver d = new Driver("driverUser", "pass");

        b.setDirua(50.0f);
        d.setDirua(200.0f);

        int erreklamazioZenbaki = 1;
        String egoera = "onartu";

        Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron"); // ← Egoera zuzena

        // Mock-ak
        Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);

        // Exekuzioa
        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera); // ← Metodo ZUZENA
            sut.close();

            // Egiaztapenak
            assertEquals("onartu", rr1.getEgoera());
            assertEquals(150.0f, b.getDirua(), 0.001); // 50 + 100
            assertEquals(100.0f, d.getDirua(), 0.001); // 200 - 100
            
            // Egiaztatu addMugimendua deitu dela
            // (spy beharko zenuke edo mezuak egiaztatu)

        } catch (Exception e) {
            fail("Ez luke salbuespenik jaurti behar: " + e.getMessage());
        }
    }
}