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
import java.util.Iterator;
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
        sql = "DELETE FROM " + tableName + " WHERE " + fieldName + " =?";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setObject(1, pkValue);
        int updateCount = stmt.executeUpdate(sql); //don't need the int variable, all you NEED is stmt.executeUpdate(sql)
    }
    
    public int insertRecord(String tableName, List colDescriptors, List colValues) throws Exception{
        
        int recordsUpdated;
        PreparedStatement pstmt = null;
        try{
        pstmt = buildInsertStmt(conn,tableName,colDescriptors);
        
        final Iterator i=colValues.iterator();
        int index = 1;
        while(i.hasNext()){
            Object obj=i.next();
            pstmt.setObject(index++, obj);
        }
        recordsUpdated = pstmt.executeUpdate();
        } catch (SQLException sqle) {
			throw sqle;
		} catch (Exception e) {
			throw e;
		}
        return recordsUpdated;
    }
    
    private PreparedStatement buildInsertStmt (Connection conn, String tableName, List colDescriptors) throws Exception{
        //INSERT INTO table_name (col1, col2) VALUES (val1, val2)
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        (sql.append(tableName)).append(" (");
        final Iterator i=colDescriptors.iterator();
        while(i.hasNext()){
            (sql.append((String)i.next())).append(", ");
        }
        sql = new StringBuffer( (sql.toString()).substring(0, (sql.toString()).lastIndexOf(", ") ) + ") VALUES (" );
        for(int x=0; x<colDescriptors.size(); x++){
            sql.append("?, ");
        }
        String sqlStmt = (sql.toString()).substring(0,(sql.toString()).lastIndexOf(", ")) + ")";
        return conn.prepareStatement(sqlStmt);
    }     
    //testing purposes only, normally do this in another class
    public static void main(String[] args) throws Exception{
        MySqlDb db = new MySqlDb();
        db.openConnection("com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/book",
                "root", "admin");
        //db.deleteSingleRecord("author", "author_id", 3);
        
        //INSERT INTO table_name (col1, col2) VALUES (val1, val2)
//        List colDesc = new ArrayList<>();
//        colDesc.add("author_id");
//        colDesc.add("author_name");
//        colDesc.add("date_created");
//        
//        List colValues = new ArrayList<>();
//        colValues.add(null);
//        colValues.add("JRR Tolkien");
//        colValues.add("2015-09-21");
//        
//        db.insertRecord("book.author", colDesc, colValues);
        List<Map<String,Object>> records = db.findAllRecords("author");
        for(Map record : records) {
            System.out.println(record);
        }
        db.closeConnection();
    }
}
