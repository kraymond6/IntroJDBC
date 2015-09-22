/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package booksample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Kallie
 */
public class MySqlDb {
    private Connection conn;
    
    public void openConnection(String driverClass, String url, 
            String userName, String password) throws Exception{
        Class.forName (driverClass);
			  conn = DriverManager.getConnection(url, userName, password);
    }
    
    public void closeConnection() throws SQLException{
        conn.close();
    }
    
    //column names are strings, the values are a ? so we make them generic objects
    public List<Map<String,Object>> findAllRecords(String tableName) throws Exception{
        List<Map<String,Object>> records = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        ResultSetMetaData metaData= rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while(rs.next()){
            //goes through each record
            Map<String,Object> record = new HashMap<>();
            for(int i=1; i <= columnCount; i++){
                //goes through each column
                record.put(metaData.getColumnName(i), rs.getObject(i));
            }
            records.add(record);
        }
        return records;
    }
    
    public void deleteSingleRecord(String tableName, String fieldName, Object pkValue) throws Exception{
        //check for string, use proper where statement
        String sql = "";
        if (pkValue instanceof String){
           sql = "DELETE FROM " + tableName + " WHERE " +  fieldName + " = '" + pkValue.toString() +"'"; 
        }else {
        sql = "DELETE FROM " + tableName + " WHERE " +  fieldName + " = " + pkValue.toString();
        }
        Statement stmt = conn.createStatement();
        int updateCount = stmt.executeUpdate(sql);
    }
    
    public void deleteSingleRecordPS(String tableName, String fieldName, Object pkValue) throws Exception{
        String sql = "";
        sql = "DELETE FROM ? WHERE ? =?";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, tableName);
        stmt.setString(2, fieldName);
        stmt.setString(3, pkValue.toString());
        int updateCount = stmt.executeUpdate(sql); //don't need the int variable, all you NEED is stmt.executeUpdate(sql)
    }
    //testing purposes only, normally do this in another class
    public static void main(String[] args) throws Exception{
        MySqlDb db = new MySqlDb();
        db.openConnection("com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/book",
                "root", "admin");
        //db.deleteSingleRecord("author", "author_id", 3);
        List<Map<String,Object>> records = db.findAllRecords("author");
        for(Map record : records) {
            System.out.println(record);
        }
        db.closeConnection();
    }
}
