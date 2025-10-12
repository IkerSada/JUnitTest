



package dataAccess;

import java.util.Date;

public class UserData {
	private String izena;
	private String abizena;
	private Date jaiotzeData;
	private String sexua;
	private String erabiltzaileMota; 
	private String email;
	private String pasahitza;



	public UserData(String izena, String abizena, Date jaiotzeData, String sexua,
			String erabiltzaileMota, String email, String pasahitza) {
		this.izena = izena;
		this.abizena = abizena;
		this.jaiotzeData = jaiotzeData;
		this.sexua = sexua;
		this.erabiltzaileMota = erabiltzaileMota;
		this.email = email;
		this.pasahitza = pasahitza;
	}
	// Getters
	public String getIzena() { return izena; }
	public String getAbizena() { return abizena; }
	public Date getJaiotzeData() { return jaiotzeData; }
	public String getSexua() { return sexua; }
	public String getErabiltzaileMota() { return erabiltzaileMota; }
	public String getEmail() { return email; }
	public String getPasahitza() { return pasahitza; }
}

