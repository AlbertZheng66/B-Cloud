/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import java.io.*;
import java.sql.*;

public class LobPros
{

    /**
     * ORACLE驱动程序
     */
    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";

    /**
     * ORACLE连接用URL
     */
    private static final String URL = "jdbc:oracle:thin:@test2000:1521:orac";

    /**
     * 用户名
     */
    private static final String USER = "user";

    /**
     * 密码
     */
    private static final String PASSWORD = "pswd";

    /**
     * 数据库连接
     */
    private static Connection conn = null;

    /**
     * SQL语句对象
     */
    private static Statement stmt = null;

    /**
     * @roseuid 3EDA089E02BC
     */
    public LobPros()
    {

    }

    /**
     * 往数据库中插入一个新的CLOB对象
     *
     * @param infile - 数据文件
     * @throws java.lang.Exception
     * @roseuid 3EDA04A902BC
     */
    public static void clobInsert(String infile) throws Exception
    {
        /* 设定不自动提交 */
        boolean defaultCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            /* 插入一个空的CLOB对象 */
            stmt.executeUpdate("INSERT INTO TEST_CLOB VALUES ('111', EMPTY_CLOB())");
            /* 查询此CLOB对象并锁定 */
            ResultSet rs = stmt.executeQuery("SELECT CLOBCOL FROM TEST_CLOB WHERE ID='111' FOR UPDATE");
            while (rs.next()) {
                /* 取出此CLOB对象 */
                oracle.sql.CLOB clob = (oracle.sql.CLOB)rs.getClob("CLOBCOL");
                /* 向CLOB对象中写入数据 */
                BufferedWriter out = new BufferedWriter(clob.getCharacterOutputStream());
                BufferedReader in = new BufferedReader(new FileReader(infile));
                int c;
                while ((c=in.read())!=-1) {
                    out.write(c);
                }
                in.close();
                out.close();
            }
            /* 正式提交 */
            conn.commit();
        } catch (Exception ex) {
            /* 出错回滚 */
            conn.rollback();
            throw ex;
        }

        /* 恢复原提交状态 */
        conn.setAutoCommit(defaultCommit);
    }

    /**
     * 修改CLOB对象（是在原CLOB对象基础上进行覆盖式的修改）
     *
     * @param infile - 数据文件
     * @throws java.lang.Exception
     * @roseuid 3EDA04B60367
     */
    public static void clobModify(String infile) throws Exception
    {
        /* 设定不自动提交 */
        boolean defaultCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            /* 查询CLOB对象并锁定 */
            ResultSet rs = stmt.executeQuery("SELECT CLOBCOL FROM TEST_CLOB WHERE ID='111' FOR UPDATE");
            while (rs.next()) {
                /* 获取此CLOB对象 */
                oracle.sql.CLOB clob = (oracle.sql.CLOB)rs.getClob("CLOBCOL");
                /* 进行覆盖式修改 */
                BufferedWriter out = new BufferedWriter(clob.getCharacterOutputStream());
                BufferedReader in = new BufferedReader(new FileReader(infile));
                int c;
                while ((c=in.read())!=-1) {
                    out.write(c);
                }
                in.close();
                out.close();
            }
            /* 正式提交 */
            conn.commit();
        } catch (Exception ex) {
            /* 出错回滚 */
            conn.rollback();
            throw ex;
        }

        /* 恢复原提交状态 */
        conn.setAutoCommit(defaultCommit);
    }

    /**
     * 替换CLOB对象（将原CLOB对象清除，换成一个全新的CLOB对象）
     *
     * @param infile - 数据文件
     * @throws java.lang.Exception
     * @roseuid 3EDA04BF01E1
     */
    public static void clobReplace(String infile) throws Exception
    {
        /* 设定不自动提交 */
        boolean defaultCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            /* 清空原CLOB对象 */
            stmt.executeUpdate("UPDATE TEST_CLOB SET CLOBCOL=EMPTY_CLOB() WHERE ID='111'");
            /* 查询CLOB对象并锁定 */
            ResultSet rs = stmt.executeQuery("SELECT CLOBCOL FROM TEST_CLOB WHERE ID='111' FOR UPDATE");
            while (rs.next()) {
                /* 获取此CLOB对象 */
                oracle.sql.CLOB clob = (oracle.sql.CLOB)rs.getClob("CLOBCOL");
                /* 更新数据 */
                BufferedWriter out = new BufferedWriter(clob.getCharacterOutputStream());
                BufferedReader in = new BufferedReader(new FileReader(infile));
                int c;
                while ((c=in.read())!=-1) {
                    out.write(c);
                }
                in.close();
                out.close();
            }
            /* 正式提交 */
            conn.commit();
        } catch (Exception ex) {
            /* 出错回滚 */
            conn.rollback();
            throw ex;
        }

        /* 恢复原提交状态 */
        conn.setAutoCommit(defaultCommit);
    }

    /**
     * CLOB对象读取
     *
     * @param outfile - 输出文件名
     * @throws java.lang.Exception
     * @roseuid 3EDA04D80116
     */
    public static void clobRead(String outfile) throws Exception
    {
        /* 设定不自动提交 */
        boolean defaultCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            /* 查询CLOB对象 */
            ResultSet rs = stmt.executeQuery("SELECT * FROM TEST_CLOB WHERE ID='111'");
            while (rs.next()) {
                /* 获取CLOB对象 */
                oracle.sql.CLOB clob = (oracle.sql.CLOB)rs.getClob("CLOBCOL");
                /* 以字符形式输出 */
                BufferedReader in = new BufferedReader(clob.getCharacterStream());
                BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
                int c;
                while ((c=in.read())!=-1) {
                    out.write(c);
                }
                out.close();
                in.close();
            }
        } catch (Exception ex) {
            conn.rollback();
            throw ex;
        }

        /* 恢复原提交状态 */
        conn.setAutoCommit(defaultCommit);
    }

    /**
     * 向数据库中插入一个新的BLOB对象
     *
     * @param infile - 数据文件
     * @throws java.lang.Exception
     * @roseuid 3EDA04E300F6
     */
    public static void blobInsert(String infile) throws Exception
    {
        /* 设定不自动提交 */
        boolean defaultCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            /* 插入一个空的BLOB对象 */
            stmt.executeUpdate("INSERT INTO TEST_BLOB VALUES ('222', EMPTY_BLOB())");
            /* 查询此BLOB对象并锁定 */
            ResultSet rs = stmt.executeQuery("SELECT BLOBCOL FROM TEST_BLOB WHERE ID='222' FOR UPDATE");
            while (rs.next()) {
                /* 取出此BLOB对象 */
                oracle.sql.BLOB blob = (oracle.sql.BLOB)rs.getBlob("BLOBCOL");
                /* 向BLOB对象中写入数据 */
                BufferedOutputStream out = new BufferedOutputStream(blob.getBinaryOutputStream());
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(infile));
                int c;
                while ((c=in.read())!=-1) {
                    out.write(c);
                }
                in.close();
                out.close();
            }
            /* 正式提交 */
            conn.commit();
        } catch (Exception ex) {
            /* 出错回滚 */
            conn.rollback();
            throw ex;
        }

        /* 恢复原提交状态 */
        conn.setAutoCommit(defaultCommit);
    }

    /**
     * 修改BLOB对象（是在原BLOB对象基础上进行覆盖式的修改）
     *
     * @param infile - 数据文件
     * @throws java.lang.Exception
     * @roseuid 3EDA04E90106
     */
    public static void blobModify(String infile) throws Exception
    {
        /* 设定不自动提交 */
        boolean defaultCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            /* 查询BLOB对象并锁定 */
            ResultSet rs = stmt.executeQuery("SELECT BLOBCOL FROM TEST_BLOB WHERE ID='222' FOR UPDATE");
            while (rs.next()) {
                /* 取出此BLOB对象 */
                oracle.sql.BLOB blob = (oracle.sql.BLOB)rs.getBlob("BLOBCOL");
                /* 向BLOB对象中写入数据 */
                BufferedOutputStream out = new BufferedOutputStream(blob.getBinaryOutputStream());
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(infile));
                int c;
                while ((c=in.read())!=-1) {
                    out.write(c);
                }
                in.close();
                out.close();
            }
            /* 正式提交 */
            conn.commit();
        } catch (Exception ex) {
            /* 出错回滚 */
            conn.rollback();
            throw ex;
        }

        /* 恢复原提交状态 */
        conn.setAutoCommit(defaultCommit);
    }

    /**
     * 替换BLOB对象（将原BLOB对象清除，换成一个全新的BLOB对象）
     *
     * @param infile - 数据文件
     * @throws java.lang.Exception
     * @roseuid 3EDA0505000C
     */
    public static void blobReplace(String infile) throws Exception
    {
        /* 设定不自动提交 */
        boolean defaultCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            /* 清空原BLOB对象 */
            stmt.executeUpdate("UPDATE TEST_BLOB SET BLOBCOL=EMPTY_BLOB() WHERE ID='222'");
            /* 查询此BLOB对象并锁定 */
            ResultSet rs = stmt.executeQuery("SELECT BLOBCOL FROM TEST_BLOB WHERE ID='222' FOR UPDATE");
            while (rs.next()) {
                /* 取出此BLOB对象 */
                oracle.sql.BLOB blob = (oracle.sql.BLOB)rs.getBlob("BLOBCOL");
                /* 向BLOB对象中写入数据 */
                BufferedOutputStream out = new BufferedOutputStream(blob.getBinaryOutputStream());
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(infile));
                int c;
                while ((c=in.read())!=-1) {
                    out.write(c);
                }
                in.close();
                out.close();
            }
            /* 正式提交 */
            conn.commit();
        } catch (Exception ex) {
            /* 出错回滚 */
            conn.rollback();
            throw ex;
        }

        /* 恢复原提交状态 */
        conn.setAutoCommit(defaultCommit);
    }

    /**
     * BLOB对象读取
     *
     * @param outfile - 输出文件名
     * @throws java.lang.Exception
     * @roseuid 3EDA050B003B
     */
    public static void blobRead(String outfile) throws Exception
    {
        /* 设定不自动提交 */
        boolean defaultCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            /* 查询BLOB对象 */
            ResultSet rs = stmt.executeQuery("SELECT BLOBCOL FROM TEST_BLOB WHERE ID='222'");
            while (rs.next()) {
                /* 取出此BLOB对象 */
                oracle.sql.BLOB blob = (oracle.sql.BLOB)rs.getBlob("BLOBCOL");
                /* 以二进制形式输出 */
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfile));
                BufferedInputStream in = new BufferedInputStream(blob.getBinaryStream());
                int c;
                while ((c=in.read())!=-1) {
                    out.write(c);
                }
                in.close();
                out.close();
            }
            /* 正式提交 */
            conn.commit();
        } catch (Exception ex) {
            /* 出错回滚 */
            conn.rollback();
            throw ex;
        }

        /* 恢复原提交状态 */
        conn.setAutoCommit(defaultCommit);
    }

    /**
     * 建立测试用表格
     * @throws Exception
     */
    public static void createTables() throws Exception {
        try {
            stmt.executeUpdate("CREATE TABLE TEST_CLOB ( ID NUMBER(3), CLOBCOL CLOB)");
            stmt.executeUpdate("CREATE TABLE TEST_BLOB ( ID NUMBER(3), BLOBCOL BLOB)");
        } catch (Exception ex) {

        }
    }

    /**
     * @param args - 命令行参数
     * @throws java.lang.Exception
     * @roseuid 3EDA052002AC
     */
    public static void main(String[] args) throws Exception
    {
        /* 装载驱动,建立数据库连接 */
        Class.forName(DRIVER);
        conn = DriverManager.getConnection(URL,USER,PASSWORD);
        stmt = conn.createStatement();

        /* 建立测试表格 */
        createTables();

        /* CLOB对象插入测试 */
        clobInsert("c:/clobInsert.txt");
        clobRead("c:/clobInsert.out");

        /* CLOB对象修改测试 */
        clobModify("c:/clobModify.txt");
        clobRead("c:/clobModify.out");

        /* CLOB对象替换测试 */
        clobReplace("c:/clobReplace.txt");
        clobRead("c:/clobReplace.out");

        /* BLOB对象插入测试 */
        blobInsert("c:/blobInsert.doc");
        blobRead("c:/blobInsert.out");

        /* BLOB对象修改测试 */
        blobModify("c:/blobModify.doc");
        blobRead("c:/blobModify.out");

        /* BLOB对象替换测试 */
        blobReplace("c:/blobReplace.doc");
        blobRead("c:/bolbReplace.out");

        /* 关闭资源退出 */
        conn.close();
        System.exit(0);
    }
} 
