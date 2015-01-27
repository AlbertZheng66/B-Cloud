/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.test;

import com.xt.core.db.conn.DatabaseContext;
import com.xt.core.db.meta.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

/**
 *
 * @author albert
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("uuid=" + UUID.randomUUID().toString());
        System.out.println("uuid=" + UUID.randomUUID().toString().length());
        System.getProperties().list(System.out);
//        try {
//            Class.forName("com.mysql.jdbc.Driver").newInstance();
//            Connection con = java.sql.DriverManager.getConnection("jdbc:mysql://localhost/b_cloud", "root", "cuican");
//            Statement stmt = con.createStatement();
//            ResultSet rst = stmt.executeQuery("select * from server_info");
//            while (rst.next()) {
//                System.out.println("2222221=" + rst.getString(1));
//            }
//
//            // 测试装载元数据
//            DatabaseContext dc = new DatabaseContext();
//            dc.setId("test");
//            dc.setUserName("b_cloud");
//            dc.setPassword("b_cloud");
//            dc.setDriverClass("com.mysql.jdbc.Driver");
//            dc.setUrl("jdbc:mysql://localhost/b_cloud");
//            dc.setType(DatabaseContext.JDBC);
//            Database.getInstance().load(dc);
//            System.out.println("db=" + Database.getInstance());
//            //关闭连接、释放资源
//            rst.close();
//            stmt.close();
//            con.close();
//        } catch (Throwable t) {
//            t.printStackTrace();
//        } finally {
//            System.out.println("finally");
//        }
    }
}
