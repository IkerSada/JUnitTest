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
    public void test_egoeraEzarriAdmin_onartu() {
        // ---------- Preparación de datos ----------
        Erreserba erres = new Erreserba();
        erres.setDiruIzoztua(100.0f);

        Bidaiaria b = new Bidaiaria();
        Driver d = new Driver("driverUser", "pass");
        Admin admin = new Admin("admin@test.com", "pass");

        b.setDirua(50.0f);
        d.setDirua(200.0f);

        int erreklamazioZenbaki = 1;
        String egoera = "onartu";
        String adminEmail = "admin@test.com";

        Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron");

        // ---------- Configuración de mocks ----------
        // Simula que el EntityManager devuelve las instancias deseadas
        Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);
        Mockito.when(db.find(Admin.class, adminEmail)).thenReturn(admin);

        // ---------- Ejecución ----------
        sut.open();
        sut.egoeraEzarriAdmin(erreklamazioZenbaki, egoera, adminEmail);
        sut.close();

        // ---------- Verificaciones ----------

        // 1️⃣ Se ha cambiado la situación de la reclamación
        assertEquals("onartu", rr1.getEgoera());

        // 2️⃣ Efectos sobre los balances
        assertEquals(150.0f, b.getDirua(), 0.001); // 50 + 100
        assertEquals(100.0f, d.getDirua(), 0.001); // 200 - 100

        // 3️⃣ Se buscaron los objetos esperados en la base de datos
        Mockito.verify(db).find(Erreklamazioa.class, erreklamazioZenbaki);
        Mockito.verify(db).find(Admin.class, adminEmail);

        // 4️⃣ Se controló la transacción correctamente
        Mockito.verify(db.getTransaction()).begin();
        Mockito.verify(db.getTransaction()).commit();

        // 5️⃣ Comprobamos que el admin eliminó la reclamación (efecto directo)
        assertFalse(admin.getJasotakoErreklamazioak().contains(rr1));

        // ✅ Si no lanza excepciones ni fallan los asserts, todo fue bien
    }



    @Test
    public void test_egoeraEzarri_deuseztatu() throws exceptions.erreklamazioaEbatzitaException {
        // ---------- Preparación ----------
        Erreserba erres = new Erreserba();
        Bidaiaria b = new Bidaiaria();
        Driver d = new Driver("driverUser", "pass");

        int erreklamazioZenbaki = 1; 
        String egoera = "deuseztatu";

        Erreklamazioa rr1 = new Erreklamazioa(erres, "deskribapena", b, d);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron");

        // Admin simulado
        Admin admin = new Admin();
        List<Admin> adminList = new ArrayList<>();
        adminList.add(admin);

        // ---------- Mocks ----------
        TypedQuery<Admin> queryMock = Mockito.mock(TypedQuery.class);
        Mockito.when(queryMock.getResultList()).thenReturn(adminList);
        Mockito.when(db.createQuery("SELECT a FROM Admin a", Admin.class)).thenReturn(queryMock);
        Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);

        // ---------- Ejecución ----------
        sut.open();
        sut.egoeraEzarri(erreklamazioZenbaki, egoera);
        sut.close();

        // ---------- Verificaciones ----------

        // 1️⃣ Se cambia la situación correctamente
        assertEquals("deuseztatu", rr1.getEgoera());

        // 2️⃣ El admin recibe la reclamación
        assertTrue(admin.getJasotakoErreklamazioak().contains(rr1));

        // 3️⃣ Se busca la reclamación en la BD
        Mockito.verify(db).find(Erreklamazioa.class, erreklamazioZenbaki);

        // 4️⃣ Se crea la query para obtener los admins
        Mockito.verify(db).createQuery("SELECT a FROM Admin a", Admin.class);

        // 5️⃣ Se obtiene la lista de admins del query
        Mockito.verify(queryMock).getResultList();

        // 6️⃣ Control transaccional correcto
        Mockito.verify(db.getTransaction()).begin();
        Mockito.verify(db.getTransaction()).commit();

        // ✅ Si llega aquí sin excepciones, todo correcto
    }

    
    @Test
    public void test_egoeraEzarri_itxaronEgoeraBerriaEzOnartuEzDeuseztatu() {
        // ---------- Preparación ----------
        Erreserba erresMock = new Erreserba();
        Bidaiaria bMock = new Bidaiaria();
        Driver driverMock = new Driver("driverUser", "pass");

        int erreklamazioZenbaki = 1; 
        String egoeraBerria = "besteBat"; // ni "onartu" ni "deuseztatu"

        // Reclamación inicial
        Erreklamazioa rr1 = new Erreklamazioa(erresMock, "deskribapena", bMock, driverMock);
        rr1.setErreklamazioZenbaki(erreklamazioZenbaki);
        rr1.setEgoera("itxaron"); // estado inicial correcto

        // ---------- Mocks ----------
        Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(rr1);

        // ---------- Ejecución ----------
        try {
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoeraBerria);
            sut.close();

            // ---------- Verificaciones ----------

            // 1️⃣ Se cambia el estado correctamente
            assertEquals("El estado debería haber cambiado a 'besteBat'", egoeraBerria, rr1.getEgoera());

            // 2️⃣ Se busca la reclamación en la BD
            Mockito.verify(db).find(Erreklamazioa.class, erreklamazioZenbaki);

            // 3️⃣ Se controla correctamente la transacción
            Mockito.verify(db.getTransaction()).begin();
            Mockito.verify(db.getTransaction()).commit();

            // 4️⃣ No debería haberse creado ninguna query adicional ni modificado dinero
            Mockito.verify(db, Mockito.never()).createQuery(Mockito.anyString(), Mockito.any());
            
        } catch (erreklamazioaEbatzitaException e) {
            sut.close();
            fail("No debería lanzarse erreklamazioaEbatzitaException cuando el estado inicial es 'itxaron'");
        }
    }


    
    
    
    
    
    
    
    


}