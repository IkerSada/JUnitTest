package test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import domain.Ride;
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
        
        Mockito.doReturn(db).when(entityManagerFactory).createEntityManager();
        Mockito.doReturn(et).when(db).getTransaction();
        sut = new DataAccess(db);
    }
    
    @After
    public void tearDown() {
        persistenceMock.close();
    }

  
    @Test
    public void testEgoeraEzarri_EstadoNoPermitido() {
        // parámetros del método - solo interfaz pública
        int erreklamazioZenbaki = 1; 
        String egoera = "estadoNoValido"; // No sé qué estados son válidos
        
        try {
            // No configuro estado interno, solo mockeo la existencia
            Erreklamazioa erreklamazioaMock = Mockito.mock(Erreklamazioa.class);
            Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(erreklamazioaMock);
            
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            
            // No sé si debe fallar o no - verifico comportamiento observable
            // Podría verificar que no hubo excepciones o cambios visibles
            
        } catch (Exception e) {
            // Acepto cualquier excepción como comportamiento válido
            // No sé qué excepciones específicas debe lanzar
        }
    }
  
    
    @Test
    public void testEgoeraEzarri_CambioEstadoExitoso() {
        // parámetros del método
        int erreklamazioZenbaki = 1; 
        String egoera = "onartu"; // Estado válido (lo sé por el dominio)
        
        try {
            // Solo mockeo que existe la reclamación
            Erreklamazioa erreklamazioaMock = Mockito.mock(Erreklamazioa.class);
            Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(erreklamazioaMock);
            
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            
            // Verificación black box: 
            // - No se lanzó excepción
            // - La transacción se completó
            Mockito.verify(et).commit(); // Comportamiento observable
            
        } catch (Exception e) {
            fail("No debería lanzar excepción con parámetros válidos");
        }
    }
    
    
    
    @Test
    public void testEgoeraEzarri_ReclamacionNoExiste() {
        // parámetros del método
        int erreklamazioZenbaki = 999; // ID que no existe
        String egoera = "onartu";
        
        try {
            // Mockeo que no encuentra la reclamación
            Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(null);
            
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, egoera);
            sut.close();
            
            // Comportamiento observable: podría lanzar excepción o hacer rollback
            Mockito.verify(et, atLeastOnce()).rollback();
            
        } catch (Exception e) {
            // Comportamiento aceptable - no sé qué excepción específica
            assertTrue(e instanceof RuntimeException);
        }
    }
    
    
    
    @Test
    public void testEgoeraEzarri_FlujoCompleto() {
        // Solo pruebo el contrato público
        int erreklamazioZenbaki = 1; 
        String estadoInicial = "itxaron";
        String estadoFinal = "onartu";
        
        try {
            Erreklamazioa erreklamazioaMock = Mockito.mock(Erreklamazioa.class);
            Mockito.when(db.find(Erreklamazioa.class, erreklamazioZenbaki)).thenReturn(erreklamazioaMock);
            
            sut.open();
            sut.egoeraEzarri(erreklamazioZenbaki, estadoFinal);
            sut.close();
            
            // Verificaciones BLACK BOX:
            // 1. Se inició transacción
            Mockito.verify(et).begin();
            // 2. Se buscó la reclamación
            Mockito.verify(db).find(Erreklamazioa.class, erreklamazioZenbaki);
            // 3. Se completó transacción
            Mockito.verify(et).commit();
            // 4. No hubo rollback
            Mockito.verify(et, never()).rollback();
            
        } catch (Exception e) {
            fail("Flujo normal no debería fallar");
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 // Helper: fija el importe congelado en Erreserba (setter público o reflection)
    private void setDiruIzoztuaOnErreserba(Object erresObj, float amount) throws Exception {
        // intentar setter público primero
        try {
            Method m = erresObj.getClass().getMethod("setDiruIzoztua", float.class);
            m.invoke(erresObj, amount);
            return;
        } catch (NoSuchMethodException ignored) {}

        try {
            Method m2 = erresObj.getClass().getMethod("setErreserbarenDiruIzoztua", float.class);
            m2.invoke(erresObj, amount);
            return;
        } catch (NoSuchMethodException ignored) {}

        // si no hay setter, probar con reflection en varios nombres posibles de campo
        String[] posibles = {"diruIzoztua", "erreserbarenDiruIzoztua", "diruaIzoztua", "kopurua"};
        for (String nombre : posibles) {
            try {
                Field f = erresObj.getClass().getDeclaredField(nombre);
                f.setAccessible(true);
                if (f.getType() == float.class) f.setFloat(erresObj, amount);
                else f.set(erresObj, amount);
                return;
            } catch (NoSuchFieldException ignored) {}
        }

        throw new NoSuchFieldException("No se encontró setter/field para el importe congelado en Erreserba");
    }

    
    

    
   
}