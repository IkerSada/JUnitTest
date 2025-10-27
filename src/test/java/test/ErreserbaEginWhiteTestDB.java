package test;

import domain.Bidaiaria;
import domain.Driver;
import domain.Ride;
import domain.Erreserba;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import java.util.*;

public class ErreserbaEginWhiteTestDB {

    private LocalDataAccess da;

    @Before
    public void setup() { // CORREGIDO: Cambiado a public
        da = new LocalDataAccess();
    }

    @Test
    public void testErreserbaEginErreserbaBerria() { // CORREGIDO: Cambiado a public
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("TEST@gmail.com", "bidaiari", "Magdalena Sevillano");
        b.setDirua(100);

        Driver driver1 = new Driver("driver1@gmail.com", "Aitor Fernandez");
        Ride r = driver1.addRide("Bilbo", "Donostia", new Date(year-1900, month, 15), 4, 7); // CORREGIDO: java.util.Date
        r.setRideNumber(1);

        da.addRide(r);
        da.addBidaiaria(b);

        da.erreserbaEgin(1, b, 1);

        Erreserba e = b.erreserbaBilatu(1);
        assertEquals("itxaron", e.getEgoera());
        assertEquals(1, e.getnPlaces());
    }

    @Test
    public void testErreserbaDeuseztatua() { // CORREGIDO: Cambiado a public
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("TEST2@gmail.com", "bidaiari", "Maria Perez");
        b.setDirua(100);

        Driver driver1 = new Driver("driver2@gmail.com", "Jon Martinez");
        Ride r = driver1.addRide("Gasteiz", "Bilbo", new Date(year-1900, month, 20), 4, 7); // CORREGIDO: java.util.Date
        r.setRideNumber(2);

        da.addRide(r);
        da.addBidaiaria(b);

        Erreserba eAurrez = b.addErreserba(r, 2);
        eAurrez.setEgoera("deuseztatu");

        da.erreserbaEgin(2, b, 1);

        Erreserba e = b.erreserbaBilatu(2);
        assertEquals("itxaron", e.getEgoera());
        assertEquals(1, e.getnPlaces()); 
    }

    @Test
    public void testErreserbaExistitzenEgoeraEzDeuseztatu() { // CORREGIDO: Cambiado a public
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("TEST3@gmail.com", "bidaiari", "Jon Lopez");
        b.setDirua(100);

        Driver driver1 = new Driver("driver3@gmail.com", "Laura Garcia");
        Ride r = driver1.addRide("Donostia", "Bilbo", new Date(year-1900, month, 25), 4, 7); // CORREGIDO: java.util.Date
        r.setRideNumber(3);

        da.addRide(r);
        da.addBidaiaria(b);

        Erreserba eAurrez = b.addErreserba(r, 1);
        eAurrez.setEgoera("itxaron");

        da.erreserbaEgin(3, b, 1);

        Erreserba e = b.erreserbaBilatu(3);
        assertEquals("itxaron", e.getEgoera());
        assertEquals(2, e.getnPlaces());
    }

    // ============================================================
    // TESTS ADICIONALES PARA MAYOR COBERTURA
    // ============================================================

    @Test
    public void testErreserbaEginRideNotFound() {
        Bidaiaria b = new Bidaiaria("TEST4@gmail.com", "bidaiari", "Pedro Sanchez");
        b.setDirua(100);

        da.addBidaiaria(b);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            da.erreserbaEgin(999, b, 1); // Ride que no existe
        });

        assertEquals("Ride ez da aurkitu", exception.getMessage());
    }

    @Test
    public void testErreserbaEginUpdatePlaces() {
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("TEST5@gmail.com", "bidaiari", "Ana Garcia");
        b.setDirua(100);

        Driver driver1 = new Driver("driver5@gmail.com", "Mikel Otero");
        Ride r = driver1.addRide("Bilbo", "Gasteiz", new Date(), 4, 10);
        r.setRideNumber(5);

        da.addRide(r);
        da.addBidaiaria(b);

        // Primera reserva
        da.erreserbaEgin(5, b, 2);
        Erreserba e1 = b.erreserbaBilatu(5);
        assertEquals(2, e1.getnPlaces());

        // Actualizar reserva existente
        da.erreserbaEgin(5, b, 1);
        Erreserba e2 = b.erreserbaBilatu(5);
        assertEquals(3, e2.getnPlaces()); // 2 + 1 = 3
    }

    @Test
    public void testErreserbaEginDiruaKendu() {
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("TEST6@gmail.com", "bidaiari", "Laura Martinez");
        b.setDirua(100);

        Driver driver1 = new Driver("driver6@gmail.com", "David Lopez");
        Ride r = driver1.addRide("Donostia", "Gasteiz", new Date(), 4, 15);
        r.setRideNumber(6);

        da.addRide(r);
        da.addBidaiaria(b);

        float diruHasiera = b.getDirua();
        da.erreserbaEgin(6, b, 2);

        // Verificar que se descontó dinero
        float prezioa = r.getBidaiarenPrezioa(2);
        assertEquals(diruHasiera - prezioa, b.getDirua(), 0.001);
    }

    @Test
    public void testErreserbaEginMugimenduaGehitu() {
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);

        Bidaiaria b = new Bidaiaria("TEST7@gmail.com", "bidaiari", "Iker Fernandez");
        b.setDirua(100);

        Driver driver1 = new Driver("driver7@gmail.com", "Sara Garcia");
        Ride r = driver1.addRide("Bilbo", "Donostia", new Date(), 4, 12);
        r.setRideNumber(7);

        da.addRide(r);
        da.addBidaiaria(b);

        int mugimenduKopHasiera = b.getMugimenduak().size();
        da.erreserbaEgin(7, b, 1);

        // Verificar que se añadió un movimiento
        assertEquals(mugimenduKopHasiera + 1, b.getMugimenduak());
    }

    static class LocalDataAccess {
        private List<Ride> rides = new ArrayList<>();
        private List<Bidaiaria> bidaiariak = new ArrayList<>();

        public void addRide(Ride r) {
            rides.add(r);
        }

        public void addBidaiaria(Bidaiaria b) {
            bidaiariak.add(b);
        }

        public Ride findRide(int rideNumber) {
            return rides.stream()
                        .filter(r -> r.getRideNumber() != null && r.getRideNumber() == rideNumber)
                        .findFirst()
                        .orElse(null);
        }

        public void erreserbaEgin(int bidaiZenbaki, Bidaiaria b, int eserlekuKop) {
            Ride r = findRide(bidaiZenbaki);
            if (r == null) throw new RuntimeException("Ride ez da aurkitu");

            Erreserba e = b.erreserbaBilatu(bidaiZenbaki);
            if (e == null) {
                e = b.addErreserba(r, eserlekuKop);
            } else {
                if ("deuseztatu".equals(e.getEgoera())) {
                    e.setnPlaces(0);
                }
                e.setEgoera("itxaron");
                e.updatePlaces(eserlekuKop);
            }

            r.updateSeat(eserlekuKop);

            float prezioa = r.getBidaiarenPrezioa(eserlekuKop);
            b.diruaKendu(prezioa);
            e.eguneratuDiruIzoztua(prezioa);
            b.addMugimendua("Diruzorrotik erreserbaren prezioa kobratu da (diru hori izoztuta)");
        }
    }
}