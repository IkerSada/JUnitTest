package testOperations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import configuration.ConfigXML;
import domain.Admin;
import domain.Bidaiaria;
import domain.Driver;
import domain.Erreklamazioa;
import domain.Erreserba;
import domain.Mugimendua;
import domain.Ride;
import domain.User;


public class TestDataAccess {
	protected  EntityManager  db;
	protected  EntityManagerFactory emf;

	ConfigXML  c=ConfigXML.getInstance();


	public TestDataAccess()  {
		
		System.out.println("TestDataAccess created");

		//open();
		
	}

	
	public void open(){
		

		String fileName=c.getDbFilename();
		
		if (c.isDatabaseLocal()) {
			  emf = Persistence.createEntityManagerFactory("objectdb:"+fileName);
			  db = emf.createEntityManager();
		} else {
			Map<String, String> properties = new HashMap<String, String>();
			  properties.put("javax.persistence.jdbc.user", c.getUser());
			  properties.put("javax.persistence.jdbc.password", c.getPassword());

			  emf = Persistence.createEntityManagerFactory("objectdb://"+c.getDatabaseNode()+":"+c.getDatabasePort()+"/"+fileName, properties);

			  db = emf.createEntityManager();
    	   }
		System.out.println("TestDataAccess opened");

		
	}
	public void close(){
		db.close();
		System.out.println("TestDataAccess closed");
	}

	public boolean removeDriver(String driverEmail) {
		System.out.println(">> TestDataAccess: removeRide");
		Driver d = db.find(Driver.class, driverEmail);
		if (d!=null) {
			db.getTransaction().begin();
			db.remove(d);
			db.getTransaction().commit();
			return true;
		} else 
		return false;
    }
	public Driver createDriver(String email, String name) {
		System.out.println(">> TestDataAccess: addDriver");
		Driver driver=null;
			db.getTransaction().begin();
			try {
			    driver=new Driver(name,email);
				db.persist(driver);
				db.getTransaction().commit();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return driver;
    }
	public boolean existDriver(String email) {
		 return  db.find(Driver.class, email)!=null;
		 

	}


	    // Método para crear Ride
	    public Ride createRide(String from, String to, Date date, int maxPlaces, float price, Driver driver) {
	        EntityTransaction tx = db.getTransaction();
	        try {
	            tx.begin();
	            Ride ride = new Ride(from, to, date, maxPlaces, price, driver);
	            db.persist(ride);
	            tx.commit();
	            return ride;
	        } catch (Exception ex) {
	            if (tx.isActive()) tx.rollback();
	            throw ex;
	        }
	    }
	    


	    // Método para obtener Bidaiaria por email
	    public Bidaiaria getBidaiaria(String email) {
	        return db.find(Bidaiaria.class, email);
	    }

	    // Método para obtener Driver por email
	    public Driver getDriver(String email) {
	        return db.find(Driver.class, email);
	    }

	    // Método para crear Erreserba

	    // Método para crear reclamación con estado específico
	    public int addErreklamazioaWithEgoera(Erreserba erreserba, String deskribapena, Bidaiaria bidaiaria, Driver driver, String egoera) {
	        EntityTransaction tx = db.getTransaction();
	        try {
	            tx.begin();
	            Erreklamazioa e = new Erreklamazioa(erreserba, deskribapena, bidaiaria, driver);
	            e.setEgoera(egoera);
	            db.persist(e);
	            tx.commit();
	            return e.getErreklamazioZenbaki();
	        } catch (Exception ex) {
	            if (tx.isActive()) tx.rollback();
	            throw ex;
	        }
	    }
	
	        // Método para eliminar Bidaiaria
	        public void removeBidaiaria(String email) {
	            EntityTransaction tx = db.getTransaction();
	            try {
	                tx.begin();
	                Bidaiaria b = db.find(Bidaiaria.class, email);
	                if (b != null) {
	                    db.remove(b);
	                }
	                tx.commit();
	            } catch (Exception ex) {
	                if (tx.isActive()) tx.rollback();
	                throw ex;
	            }
	        }


	        // Método para eliminar Ride (por si acaso)
	        public void removeRide(int rideId) {
	            EntityTransaction tx = db.getTransaction();
	            try {
	                tx.begin();
	                Ride ride = db.find(Ride.class, rideId);
	                if (ride != null) {
	                    db.remove(ride);
	                }
	                tx.commit();
	            } catch (Exception ex) {
	                if (tx.isActive()) tx.rollback();
	                throw ex;
	            }
	        }

	        // Método para eliminar Erreserba
	        public void removeErreserba(int erreserbaId) {
	            EntityTransaction tx = db.getTransaction();
	            try {
	                tx.begin();
	                Erreserba erres = db.find(Erreserba.class, erreserbaId);
	                if (erres != null) {
	                    db.remove(erres);
	                }
	                tx.commit();
	            } catch (Exception ex) {
	                if (tx.isActive()) tx.rollback();
	                throw ex;
	            }
	        }
	        
	     // En TestDataAccess - métodos específicos para tests
	        public int addErreklamazioaWithState(String egoera, String bidaiariaEmail, String driverEmail, float diruIzoztua) {
	            EntityTransaction tx = db.getTransaction();
	            try {
	                tx.begin();
	                
	                // Crear Bidaiaria si no existe
	                Bidaiaria bidaiaria = db.find(Bidaiaria.class, bidaiariaEmail);
	                if (bidaiaria == null) {
	                    bidaiaria = new Bidaiaria(bidaiariaEmail, "pass", "Bidaiaria");
	                    bidaiaria.setDirua(100f);
	                    db.persist(bidaiaria);
	                }
	                
	                // Crear Driver si no existe  
	                Driver driver = db.find(Driver.class, driverEmail);
	                if (driver == null) {
	                    driver = new Driver(driverEmail, "pass", "Driver");
	                    driver.setDirua(200f);
	                    db.persist(driver);
	                }
	                 
	                // Crear objetos mínimos
	                Ride ride = new Ride("From", "To", new Date(), 2, 10f, driver);
	                db.persist(ride);
	                
	                Erreserba erreserba = new Erreserba(1, ride, bidaiaria);
	                erreserba.setDiruIzoztua(diruIzoztua);
	                db.persist(erreserba);
	                
	                Erreklamazioa e = new Erreklamazioa(erreserba, "Test", bidaiaria, driver);
	                e.setEgoera(egoera);
	                db.persist(e);
	                
	                tx.commit();
	                return e.getErreklamazioZenbaki();
	                
	            } catch (Exception ex) {
	                if (tx.isActive()) tx.rollback();
	                throw ex;
	            }
	        }
	     // en TestDataAccess

	        public Erreklamazioa getErreklamazioa(int id) {
	            open();
	            Erreklamazioa e = db.find(Erreklamazioa.class, id);
	            close();
	            return e;
	        }
	        
	        
	     // En TestDataAccess.java
	        public boolean existAdmin(String email) {
	            return db.find(Admin.class, email) != null;
	        }

	        public Admin createAdmin(String email, String password) {
	            db.getTransaction().begin();
	            try {
	                Admin admin = new Admin(email, password);
	                db.persist(admin);
	                db.getTransaction().commit();
	                return admin;
	            } catch (Exception e) {
	                db.getTransaction().rollback();
	                throw e;
	            }
	        }

	        public int createErreklamizioaWithState(String bidaiariaEmail, String driverEmail, String estado, float diruIzoztua) {
	            db.getTransaction().begin();
	            try {
	                // Crear Bidaiaria si no existe
	                Bidaiaria b = db.find(Bidaiaria.class, bidaiariaEmail);
	                if (b == null) {
	                    b = new Bidaiaria(bidaiariaEmail, "password", "Test Bidaiaria");
	                    b.setDirua(100.0f); // Dinero inicial para pruebas
	                    db.persist(b);
	                }
	                
	                // Crear Driver si no existe
	                Driver d = db.find(Driver.class, driverEmail);
	                if (d == null) {
	                    d = new Driver(driverEmail, "password", "Test Driver");
	                    d.setDirua(100.0f); // Dinero inicial para pruebas
	                    db.persist(d);
	                }
	                
	                // Crear Ride para la Erreserba
	                Date date = new Date(System.currentTimeMillis() + 86400000); // Mañana
	                Ride ride = d.addRide("Donostia", "Bilbo", date, 4, 10.0f);
	                db.persist(ride);
	                
	                // Crear Erreserba
	                Erreserba erreserba = b.addErreserba(ride, 2);
	                erreserba.setDiruIzoztua(diruIzoztua);
	                db.persist(erreserba);
	                
	                // Crear Erreklamizioa
	                Erreklamazioa erreklamizioa = new Erreklamazioa(erreserba, "Test deskripzioa", b, d);
	                erreklamizioa.setEgoera(estado);
	                db.persist(erreklamizioa);
	                
	                db.getTransaction().commit();
	                return erreklamizioa.getErreklamazioZenbaki();
	            } catch (Exception e) {
	                db.getTransaction().rollback();
	                throw e;
	            }
	        }

	        public String getErreklamizioaState(int errekzbk) {
	        	Erreklamazioa e = db.find(Erreklamazioa.class, errekzbk);
	            return e != null ? e.getEgoera() : null;
	        }

	        public float getBidaiariaMoney(String email) {
	            Bidaiaria b = db.find(Bidaiaria.class, email);
	            return b != null ? b.getDirua() : 0;
	        }

	        public float getDriverMoney(String email) {
	            Driver d = db.find(Driver.class, email);
	            return d != null ? d.getDirua() : 0;
	        }

	        public boolean hasMovementWithDescription(String userEmail, String description) {
	            User user = db.find(User.class, userEmail);
	            if (user != null) {
	                for (Mugimendua m : user.getMugimenduak()) {
	                    if (m.getDeskripzioa().contains(description)) {
	                        return true;
	                    }
	                }
	            }
	            return false;
	        }

	        public int getAdminReclamacionesCount(String adminEmail) {
	            Admin admin = db.find(Admin.class, adminEmail);
	            return admin != null ? admin.getJasotakoErreklamazioak().size() : 0;
	        }

	        public boolean isReclamacionInAdmin(int errekzbk, String adminEmail) {
	            Admin admin = db.find(Admin.class, adminEmail);
	            if (admin != null) {
	                for (Erreklamazioa e : admin.getJasotakoErreklamazioak()) {
	                    if (e.getErreklamazioZenbaki().equals(errekzbk)) {
	                        return true;
	                    }
	                }
	            }
	            return false;
	        }

	        public boolean removeErreklamizioa(int errekzbk) {
	        	Erreklamazioa e = db.find(Erreklamazioa.class, errekzbk);
	            if (e != null) {
	                db.getTransaction().begin();
	                db.remove(e);
	                db.getTransaction().commit();
	                return true;
	            }
	            return false;
	        }



	    
	        public int createSimpleErreklamazioa(String egoera, Bidaiaria bidaiaria, Driver driver, float diruIzoztua) {
	            EntityTransaction tx = db.getTransaction();
	            try {
	                tx.begin();
	                
	                // Crear objetos mínimos necesarios
	                Ride ride = new Ride("TestFrom", "TestTo", new Date(), 2, 10f, driver);
	                db.persist(ride);
	                
	                Erreserba erreserba = new Erreserba(1, ride, bidaiaria);
	                erreserba.setDiruIzoztua(diruIzoztua);
	                db.persist(erreserba);
	                
	                Erreklamazioa e = new Erreklamazioa(erreserba, "Test", bidaiaria, driver);
	                e.setEgoera(egoera);
	                db.persist(e);
	                
	                tx.commit();
	                return e.getErreklamazioZenbaki();
	            } catch (Exception ex) {
	                if (tx.isActive()) tx.rollback();
	                throw ex;
	            }
	        }
	    
	    
	        public int createSimpleErreklamazioa(String egoera, Bidaiaria bidaiaria, Driver driver) {
	            EntityTransaction tx = db.getTransaction();
	            try {
	                tx.begin();
	                
	                // Crear objetos mínimos necesarios
	                Ride ride = new Ride("TestFrom", "TestTo", new Date(), 2, 10f, driver);
	                db.persist(ride);
	                
	                Erreserba erreserba = new Erreserba(1, ride, bidaiaria);
	                erreserba.setDiruIzoztua(10f);
	                db.persist(erreserba);
	                
	                Erreklamazioa e = new Erreklamazioa(erreserba, "Test", bidaiaria, driver);
	                e.setEgoera(egoera);
	                db.persist(e);
	                
	                tx.commit();
	                return e.getErreklamazioZenbaki();
	            } catch (Exception ex) {
	                if (tx.isActive()) tx.rollback();
	                throw ex;
	            }
	        }
	    
	
	
	// En TestDataAccess - versión que permite especificar el estado inicial
	public int addErreklamazioa(Erreserba erreserba, String deskribapena, Bidaiaria bidaiaria, Driver driver, String egoera) {
	    EntityTransaction tx = db.getTransaction();
	    try {
	        tx.begin();
	        Erreklamazioa e = new Erreklamazioa(erreserba, deskribapena, bidaiaria, driver);
	        e.setEgoera(egoera); // Estado específico en lugar de siempre "itxaron"
	        db.persist(e);
	        tx.commit();
	        return e.getErreklamazioZenbaki();
	    } catch (Exception ex) {
	        if (tx.isActive()) tx.rollback();
	        throw ex;
	    }
	}
		
		public Driver addDriverWithRide(String email, String name, String from, String to,  Date date, int nPlaces, float price) {
			System.out.println(">> TestDataAccess: addDriverWithRide");
				Driver driver=null;
				db.getTransaction().begin();
				try {
					 driver = db.find(Driver.class, email);
					if (driver==null)
						driver=new Driver(name,email);
				    driver.addRide(from, to, date, nPlaces, price);
					db.getTransaction().commit();
					return driver;
					
				}
				catch (Exception e){
					e.printStackTrace();
				}
				return null;
	    }
		
		
		public boolean existRide(String email, String from, String to, Date date) {
			System.out.println(">> TestDataAccess: existRide");
			Driver d = db.find(Driver.class, email);
			if (d!=null) {
				return d.doesRideExists(from, to, date);
			} else 
			return false;
		}
		
		


		    // 1. Crear una reclamación con estado específico
		public void addErreklamazioa(int errekzbk, String egoera, Erreserba erreserba, String deskribapena, 
				Bidaiaria bidaiaria, Driver driver) {
			EntityTransaction tx = db.getTransaction();
			try {
				tx.begin();
				Erreklamazioa e = new Erreklamazioa(erreserba, deskribapena, bidaiaria, driver);
				e.setEgoera(egoera);
				db.persist(e);
				tx.commit();
			} catch (Exception ex) {
				if (tx.isActive()) tx.rollback();
				throw ex;
			}
		}

	    // 3. Crear Reserva (necesaria para la reclamación)
		public Erreserba createErreserba(int nPlaces, Ride bidaia, Bidaiaria traveler, float diruIzoztua) {
	        EntityTransaction tx = db.getTransaction();
	        try {
	            tx.begin();
	            Erreserba erres = new Erreserba(nPlaces, bidaia, traveler);
	            erres.setDiruIzoztua(diruIzoztua);
	            db.persist(erres);
	            tx.commit();
	            return erres;
	        } catch (Exception ex) {
	            if (tx.isActive()) tx.rollback();
	            throw ex;
	        }
	    }
		
		public boolean existeErreklamazioaEnAdmin(int erreklamazioZbk) {
	        TypedQuery<Admin> query = db.createQuery("SELECT a FROM Admin a", Admin.class);
	        List<Admin> admins = query.getResultList();
	        if (admins.isEmpty()) return false;
	        
	        Admin admin = admins.get(0);
	        for (Erreklamazioa e : admin.getJasotakoErreklamazioak()) {
	            if (e.getErreklamazioZenbaki() == erreklamazioZbk) {
	                return true;
	            }
	        }
	        return false;
	    }
		
		
	    public boolean existErreklamazioa(Integer erreklamazioZbk) {
	        Erreklamazioa e = db.find(Erreklamazioa.class, erreklamazioZbk);
	        return e != null;
	    }
		
		    // 2. Verificar el estado de una reclamación
		    public String getErreklamazioaEgoera(int errekzbk) {
		        Erreklamazioa e = db.find(Erreklamazioa.class, errekzbk);
		        return e != null ? e.getEgoera() : null;
		    }

		    // 3. Verificar saldo de Bidaiaria
		    public float getBidaiariaDirua(String bidaiariaUsername) {
		        Bidaiaria b = db.find(Bidaiaria.class, bidaiariaUsername);
		        return b != null ? b.getDirua() : -1;
		    }

		    // 4. Verificar saldo de Driver
		    public float getDriverDirua(String driverUsername) {
		        Driver d = db.find(Driver.class, driverUsername);
		        return d != null ? d.getDirua() : -1;
		    }

		    // 5. Verificar movimientos de Bidaiaria
		    public List<Mugimendua> getBidaiariaMugimenduak(String bidaiariaUsername) {
		        Bidaiaria b = db.find(Bidaiaria.class, bidaiariaUsername);
		        return b != null ? b.getMugimenduak() : new ArrayList<>();
		    }

		    // 6. Verificar movimientos de Driver
		    public List<Mugimendua> getDriverMugimenduak(String driverUsername) {
		        Driver d = db.find(Driver.class, driverUsername);
		        return d != null ? d.getMugimenduak() : new ArrayList<>();
		    }

		    // 7. Verificar si reclamación está en Admin
		    public boolean isErreklamazioaInAdmin(int errekzbk) {
		        TypedQuery<Admin> query = db.createQuery("SELECT a FROM Admin a", Admin.class);
		        List<Admin> admins = query.getResultList();
		        if (admins.isEmpty()) return false;
		        
		        Admin admin = admins.get(0);
		        return admin.getJasotakoErreklamazioak().stream()
		                   .anyMatch(e -> e.getErreklamazioZenbaki() == errekzbk);
		    }

		    // 8. Crear Bidaiaria con saldo inicial
		    public void createBidaiaria(String email, String izena, String pasahitza, float dirua) {
		        EntityTransaction tx = db.getTransaction();
		        try {
		            tx.begin();
		            Bidaiaria b = new Bidaiaria(email, pasahitza, izena);
		            b.setDirua(dirua);
		            db.persist(b);
		            tx.commit();
		        } catch (Exception ex) {
		            if (tx.isActive()) tx.rollback();
		            throw ex;
		        }
		    }



		    // 10. Limpiar datos de test
		    public void removeErreklamazioa(int errekzbk) {
		        EntityTransaction tx = db.getTransaction();
		        try {
		            tx.begin();
		            Erreklamazioa e = db.find(Erreklamazioa.class, errekzbk);
		            if (e != null) {
		                db.remove(e);
		            }
		            tx.commit();
		        } catch (Exception ex) {
		            if (tx.isActive()) tx.rollback();
		            throw ex;
		        }
		    }
		 // En TestDataAccess
		    public int getLastErreklamazioaId() {
		        TypedQuery<Erreklamazioa> query = db.createQuery("SELECT e FROM Erreklamazioa e ORDER BY e.errekzbk DESC", Erreklamazioa.class);
		        query.setMaxResults(1);
		        List<Erreklamazioa> result = query.getResultList();
		        return result.isEmpty() ? 0 : result.get(0).getErreklamazioZenbaki();
		    }
		
		
		public Ride removeRide(String email, String from, String to, Date date ) {
			System.out.println(">> TestDataAccess: removeRide");
			Driver d = db.find(Driver.class, email);
			if (d!=null) {
				db.getTransaction().begin();
				Ride r= d.removeRide(from, to, date);
				db.getTransaction().commit();
				return r;

			} else 
			return null;

		}


		
}


