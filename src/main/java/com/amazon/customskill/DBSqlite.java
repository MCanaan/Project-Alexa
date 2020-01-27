package com.amazon.customskill;

import java.sql.Connection;

import java.sql.*;

public class DBSqlite {
	
	public static Connection  createConnection(){
		
		Connection con = null;
		
		// Loading driver (can be skipped since java 1.5)
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

		
		//create connection
        try {
        	con = DriverManager.getConnection("jdbc:sqlite:C:/sqlite/db/alexa.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
		
		return con;
	
		
	}
	
	public static void main(String[] args) {

		Connection con = DBSqlite.createConnection();
		createTableDeutsch(con);
		createTableEnglisch(con);
		//create(con);
		delete(con,2);
		delete(con,3);
		
		selectTest(con);
    }
	//Tabelle für wörter auf von Englisch nach deutsch erstellen
    public static void createTableEnglisch(Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS Words (id INTEGER PRIMARY KEY, name string);")) {
            pstmt.executeUpdate();
           
           
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  
	/*
	 * public static void COUNTEN(Connection con) { try ( Statement stat =
	 * con.createStatement(); ResultSet rs =
	 * stat.executeQuery("SELECT COUNT(*) FROM Words ");){ int tableCount =
	 * rs.getInt(1);
	 * 
	 * } catch (SQLException e) { e.printStackTrace(); } }
	 */
   
    
    //Tabelle für wörter auf von Deutsch nach Englisch erstellen
    public static void createTableDeutsch(Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS Wort (id INTEGER PRIMARY KEY, name string);")) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Wörtet in der Tabelle für wörter auf von Englisch nach deutsch eintragen.
    public static boolean insertEnglisch(Connection con, String name) {
    	
        try (PreparedStatement pstmt = con.prepareStatement(
                "INSERT INTO Words (name) VALUES (?);")) {
            pstmt.setString(1,name);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
   
    
    //Wörtet in der Tabelle für wörter auf von Deutsch nach Englisch eintragen.
    public static boolean insertDeutsch(Connection con, String name) {
    	
        try (PreparedStatement pstmt = con.prepareStatement(
                "INSERT INTO Wort (name) VALUES (?);")) {
            pstmt.setString(1,name);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void delete(Connection con, int id) {
        try (PreparedStatement pstmt = con.prepareStatement(
                "DELETE FROM Words WHERE id = ?;")) {
            pstmt.setInt(1,id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //Wörter von der Tabelle mit Englischen wörtern nehmen
    public static String selectEnglisch(Connection con,int id) {
    	String name = "";
        try (PreparedStatement pstmt = con.prepareStatement("SELECT * FROM Words where id = "+ id+";");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
              name = rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }
    
    
    //Wörter von der Tabelle mit Deutschen wörtern nehmen
    public static String selectDeutsch(Connection con,int id) {
    	String name = "";
        try (PreparedStatement pstmt = con.prepareStatement("SELECT * FROM Wort where id = "+ id+";");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
              name = rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }
    
    
    private static void selectTest(Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("SELECT * FROM Words;");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ") " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
