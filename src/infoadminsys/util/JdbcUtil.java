/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infoadminsys.util;

import infoadminsys.cls.UserPass;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/**
 *
 * @author hed
 */
public class JdbcUtil {
    private Connection connection = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    private static final String DB_URL = "jdbc:mysql://localhost/InfoAdminSys?useSSL=false&characterEncoding=utf8";
    
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "hedong";
    
    public JdbcUtil(){
        try {
            Class.forName(JDBC_DRIVER);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public Connection getConnection(){
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
    public <T> T getSingleResult(String sql,List<Object> param,Class<T> cls)throws Exception {
        T resultObject = null;
        int index = 1;
        preparedStatement = connection.prepareStatement(sql);
        if(param != null && !param.isEmpty()){
            for(int i = 0; i<param.size(); i++){
                preparedStatement.setObject(index++, param.get(i));
            }
        }
        resultSet = preparedStatement.executeQuery();
        ResultSetMetaData metaData  = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        if (resultSet.next()) {
            resultObject = cls.newInstance();
            for (int i = 0; i < cols_len; i++) {
                String cols_name = metaData.getColumnLabel(i + 1);
                Object cols_value = resultSet.getObject(cols_name);
                if (cols_value == null) {
                    cols_value = "";
                }
                Field field = cls.getDeclaredField(cols_name);
                if (field.getType() == int.class) {
                    if (cols_value == "") {
                        cols_value = 0;
                    } else {
                        cols_value = Integer.parseInt(cols_value.toString());
                    }
                }  else
                if (field.getType() == double.class) {
                    if (cols_value == "") {
                        cols_value = 0.0;
                    } else {
                        cols_value = Double.parseDouble(cols_value.toString());
                    }
                }
                field.setAccessible(true);
                field.set(resultObject, cols_value);
            }
        }
        return resultObject;

    }

    public <T> List<T> getMultipleResult(String sql,List<Object> param,Class<T> cls )throws Exception {
        List<T> list = new ArrayList<T>();
        int index = 1;
        preparedStatement = connection.prepareStatement(sql);
        if(param != null && !param.isEmpty()){
            for(int i = 0; i<param.size(); i++){
                preparedStatement.setObject(index++, param.get(i));
            }
        }
        resultSet = preparedStatement.executeQuery();
        ResultSetMetaData metaData  = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        while(resultSet.next()) {
            T resultObject = cls.newInstance();
            for(int i = 0; i<cols_len; i++){
                String cols_name = metaData.getColumnLabel(i+1);
                Object cols_value = resultSet.getObject(cols_name);
                if(cols_value == null){
                    cols_value = "";
                }
                Field field = cls.getDeclaredField(cols_name);
                if (field.getType() == int.class) {
                    if (cols_value == "") {
                        cols_value = 0;
                    } else {
                        cols_value = Integer.parseInt(cols_value.toString());
                    }

                } else
                if (field.getType() == double.class) {
                    if (cols_value == "") {
                        cols_value = 0.0;
                    } else {
                        cols_value = Double.parseDouble(cols_value.toString());
                    }
                }
                field.setAccessible(true);
                field.set(resultObject,cols_value);
            }
            list.add(resultObject);
        }
        return list;
    }
    
    public boolean updateByPreparedStatement(String sql,List<Object> param) throws SQLException {
        boolean flag = false;
        int result = -1;
        preparedStatement = connection.prepareStatement(sql);
        int index = 1;
        if (param != null && !param.isEmpty()) {
            for (int i = 0; i < param.size(); i++) {
                preparedStatement.setObject(index++, param.get(i));
            }
        }
        result = preparedStatement.executeUpdate();
        flag = result > 0 ? true : false;
        return flag;
    }
    
    public Map<String,Object> getCertainResult(String sql,List<Object> param) throws SQLException {
        Map<String,Object> map = new HashMap<String,Object>();
        int index = 1;
        preparedStatement = connection.prepareStatement(sql);
        if (param != null && !param.isEmpty()) {
            for (int i = 0; i < param.size(); i++) {
                preparedStatement.setObject(index++, param.get(i));
            }
        }
        resultSet = preparedStatement.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int col_len = metaData.getColumnCount();
        if (resultSet.next()){
            for(int i=0; i<col_len; i++ ){
                String cols_name = metaData.getColumnName(i+1);
                Object cols_value = resultSet.getObject(cols_name);
                if(cols_value == null){
                    cols_value = "";
                }
                map.put(cols_name, cols_value);
            }
        }
        return map;
    }
    
    public List<Map<String,Object>> getCertainResultList(String sql,List<Object> param) throws SQLException{
        List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
        int index = 1;
        preparedStatement = connection.prepareStatement(sql);
        if(param != null && !param.isEmpty()){
            for(int i = 0; i<param.size(); i++){
                preparedStatement.setObject(index++, param.get(i));
            }
        }
        resultSet = preparedStatement.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        while(resultSet.next()){
            Map<String, Object> map = new HashMap<String,Object>();
            for(int i=0; i<cols_len; i++){
                String cols_name = metaData.getColumnName(i+1);
                Object cols_value = resultSet.getObject(cols_name);
                if(cols_value == null){
                    cols_value = "";
                }
                map.put(cols_name, cols_value);
            }
            list.add(map);
        }

        return list;
    }
    
    public void releaseConn() {
        if (resultSet != null)
        {
            try {
                resultSet.close();
            } catch(SQLException e)
            {
                e.printStackTrace();
            } finally {
                resultSet = null;
            }
        }

        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                preparedStatement = null;
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                connection = null;
            }
        }
    }
}
