/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import com.xt.core.utils.IOHelper;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class Blob {

    public static void main (String[] argv) {
        Blob blob = new Blob();
        blob.testOraInsert();
    }

    public void testOraInsert() {
        try {
            String sql = "INSERT INTO TEST_BLOB(COL1, BLOB1) VALUES(?, empty_blob())";
            Connection conn = getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "0");
            ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void testHsqlBlobUpdateXml() {
		
		FileInputStream fileOut;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			fileOut = new FileInputStream("e:\\cover.jpg");
			IOHelper.i2o(fileOut, baos);
			fileOut.close();
            String sql = "update Book set cover=? where oid=?";
            Connection conn = getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            // Object[] params = new Object[] {"01", new org.hsqldb.jdbc.jdbcBlob(baos.toByteArray())};
            ps.setString(2, "0");
            ps.setBlob(1, new org.hsqldb.jdbc.jdbcBlob(baos.toByteArray()));
            // int[] types = new int[] { Types.VARCHAR, Types.BLOB };
            ps.executeUpdate();            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private Connection getConn() {
		Connection con = null;
		try {
			Class.forName("org.hsqldb.jdbcDriver").newInstance();
			con = DriverManager.getConnection(
					"jdbc:hsqldb:file:E:/work/xthinker/gt_html_demo/web/WEB-INF/db/gt_demo",
					"sa", "");
//            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
//			con = DriverManager.getConnection(
//					"jdbc:oracle:thin:@192.168.21.226:1521:test",
//					"rcp", "rcp");
            System.out.println("con.getMetaData().getDriverName()=" + con.getMetaData().getDriverName());
            System.out.println("con.getMetaData().getDriverVersion()=" + con.getMetaData().getDriverVersion());
            System.out.println("con.getMetaData().getDriverVersion()=" + con.getMetaData().getDatabaseProductName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}
}
