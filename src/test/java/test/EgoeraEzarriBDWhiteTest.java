package test;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sun.xml.messaging.saaj.packaging.mime.internet.ParseException;

import dataAccess.DataAccess;
import domain.Bidaiaria;
import domain.Driver;
import domain.Erreklamazioa;
import domain.Erreserba;
import domain.Ride;
import exceptions.erreklamazioaEbatzitaException;
import testOperations.TestDataAccess;

public class EgoeraEzarriBDWhiteTest {

    // sut: system under test
    static DataAccess sut = new DataAccess();
    
	 static TestDataAccess testDA=new TestDataAccess();

	 

	 @Test
	 public void testErreklamazioaEbatzitaException() {
	     String bidaiariaEmail = "bidaiaria@test.com";
	     String driverEmail = "driver@test.com";
	     
	     try {
	         // Setup mínimo - solo crear la reclamación directamente
	         testDA.open();
	         
	         // Crear solo los objetos absolutamente necesarios
	         testDA.createBidaiaria(bidaiariaEmail, "Bidaiaria", "pass", 100f);
	         testDA.createDriver(driverEmail, "Driver");
	         
	         Bidaiaria bidaiaria = testDA.getBidaiaria(bidaiariaEmail);
	         Driver driver = testDA.getDriver(driverEmail);
	         
	         // Crear reclamación directamente sin Ride ni Erreserba
	         // (asumiendo que tienes un constructor alternativo)
	         int erreklamazioaId = testDA.createSimpleErreklamazioa("onartu", bidaiaria, driver);
	         testDA.close();

	         // Execute
	         sut.open();
	         sut.egoeraEzarri(erreklamazioaId, "deuseztatu");
	         fail("Expected exception not thrown");
	         
	     } catch (erreklamazioaEbatzitaException e) {
	         try { sut.close(); } catch (Exception ex) { }
	         assertTrue(true);
	     } catch (Exception e) {
	         try { sut.close(); } catch (Exception ex) { }
	         fail("Unexpected exception: " + e.getMessage());
	     } finally {
	         try {
	             testDA.open();
	             testDA.removeBidaiaria(bidaiariaEmail);
	             testDA.removeDriver(driverEmail);
	             testDA.close();
	         } catch (Exception e) { }
	     }
	 }

	 @Test
	 public void test_egoeraEzarri_itxaron() {
	     String bidaiariaEmail = "bidaiaria@test.com";
	     String driverEmail = "driver@test.com";
	     int erreklamazioaId = -1;

	     try {
	         // Setup mínimo - solo crear la reclamación directamente
	         testDA.open();
	         
	         // Crear solo los objetos absolutamente necesarios
	         testDA.createBidaiaria(bidaiariaEmail, "Bidaiaria", "pass", 100f);
	         testDA.createDriver(driverEmail, "Driver");
	         
	         Bidaiaria bidaiaria = testDA.getBidaiaria(bidaiariaEmail);
	         Driver driver = testDA.getDriver(driverEmail);
	         // Crear reclamación directamente sin Ride ni Erreserba
	         // (asumiendo que tienes un constructor alternativo)
	          erreklamazioaId = testDA.createSimpleErreklamazioa("itxaron", bidaiaria, driver);
	         testDA.close();

	         // Execute
	         sut.open();
	         sut.egoeraEzarri(erreklamazioaId, "itxaron");
	        
	       
	         
	     } catch (erreklamazioaEbatzitaException e) {
	         try { sut.close(); } catch (Exception ex) { }
	        fail("erreklamazioaEbatzitaException: ");
	     } catch (Exception e) {
	         try { sut.close(); } catch (Exception ex) { }
	         fail("Unexpected exception: " + e.getMessage());
	     } finally {
	             testDA.open();
	             // IMPORTANTE: Eliminar también la reclamación
	  
	             if (erreklamazioaId != -1) {
	                 testDA.removeErreklamazioa(erreklamazioaId);
	             }
	     
	             testDA.removeBidaiaria(bidaiariaEmail);
	             testDA.removeDriver(driverEmail);
	             testDA.close();
	         }
	         try {
	             testDA.open();
	             testDA.removeBidaiaria(bidaiariaEmail);
	             testDA.removeDriver(driverEmail);
	             testDA.close();
	             assertTrue(true);
	         } catch (Exception e) { }
	     }

	 
	 
	 
	 
	 
	 
	 

	 @Test
	 public void test_egoeraEzarri_deuseztatu() {
	     String bidaiariaEmail = "bidaiaria@test.com";
	     String driverEmail = "driver@test.com";
	     int erreklamazioaId = -1;
	     
	     try {
	         // Setup mínimo - solo crear la reclamación directamente
	         testDA.open();
	         
	         // Crear solo los objetos absolutamente necesarios
	         testDA.createBidaiaria(bidaiariaEmail, "Bidaiaria", "pass", 100f);
	         testDA.createDriver(driverEmail, "Driver");
	         
	         Bidaiaria bidaiaria = testDA.getBidaiaria(bidaiariaEmail);
	         Driver driver = testDA.getDriver(driverEmail);
	         // Crear reclamación directamente sin Ride ni Erreserba
	         // (asumiendo que tienes un constructor alternativo)
	          erreklamazioaId = testDA.createSimpleErreklamazioa("itxaron", bidaiaria, driver);
	         testDA.close();

	         // Execute
	         sut.open();
	         sut.egoeraEzarri(erreklamazioaId, "itxaron");
	         sut.egoeraEzarri(erreklamazioaId, "deuzestatu");
	       
	         
	     } catch (erreklamazioaEbatzitaException e) {
	         try { sut.close(); } catch (Exception ex) { }
	        fail("erreklamazioaEbatzitaException: ");
	     } catch (Exception e) {
	         try { sut.close(); } catch (Exception ex) { }
	         fail("Unexpected exception: " + e.getMessage());
	     } finally {
             testDA.open();
             // IMPORTANTE: Eliminar también la reclamación
  
             if (erreklamazioaId != -1) {
                 testDA.removeErreklamazioa(erreklamazioaId);
             }
     
             testDA.removeBidaiaria(bidaiariaEmail);
             testDA.removeDriver(driverEmail);
             testDA.close();
         }
	         try {
	             testDA.open();
	             testDA.removeBidaiaria(bidaiariaEmail);
	             testDA.removeDriver(driverEmail);
	             testDA.close();
	             assertTrue(true);
	         } catch (Exception e) { }
	     }
	 
	 

	 @Test
	 public void test_egoeraEzarri_besteBat() {
	     String bidaiariaEmail = "bidaiaria@test.com";
	     String driverEmail = "driver@test.com";
	     int erreklamazioaId = -1;
	     
	     try {
	         // Setup mínimo - solo crear la reclamación directamente
	         testDA.open();
	         
	         // Crear solo los objetos absolutamente necesarios
	         testDA.createBidaiaria(bidaiariaEmail, "Bidaiaria", "pass", 100f);
	         testDA.createDriver(driverEmail, "Driver");
	         
	         Bidaiaria bidaiaria = testDA.getBidaiaria(bidaiariaEmail);
	         Driver driver = testDA.getDriver(driverEmail);
	         // Crear reclamación directamente sin Ride ni Erreserba
	         // (asumiendo que tienes un constructor alternativo)
	          erreklamazioaId = testDA.createSimpleErreklamazioa("itxaron", bidaiaria, driver);
	         testDA.close();

	         // Execute
	         sut.open();
	         sut.egoeraEzarri(erreklamazioaId, "itxaron");
	         sut.egoeraEzarri(erreklamazioaId, "besteBat");
	       
	         
	     } catch (erreklamazioaEbatzitaException e) {
	         try { sut.close(); } catch (Exception ex) { }
	        fail("erreklamazioaEbatzitaException: ");
	     } catch (Exception e) {
	         try { sut.close(); } catch (Exception ex) { }
	         fail("Unexpected exception: " + e.getMessage());
	     } finally {
             testDA.open();
             // IMPORTANTE: Eliminar también la reclamación
  
             if (erreklamazioaId != -1) {
                 testDA.removeErreklamazioa(erreklamazioaId);
             }
     
             testDA.removeBidaiaria(bidaiariaEmail);
             testDA.removeDriver(driverEmail);
             testDA.close();
         }
	         try {
	             testDA.open();
	             testDA.removeBidaiaria(bidaiariaEmail);
	             testDA.removeDriver(driverEmail);
	             testDA.close();
	             assertTrue(true);
	         } catch (Exception e) { }
	     }
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 }

	 
	 
	 
	 
	 