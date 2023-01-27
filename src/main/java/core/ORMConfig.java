package core;


import org.dom4j.Document;
import utils.AnnotationUtil;
import utils.Dom4jUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//该类用来解析并封装框架的核心配置文件
/*
* 核心配置类内容
*   数据库url
*   数据库驱动
*   数据库用户名
*   数据库密码
*   采用xml配置映射数据
*   采用注解配置映射数据
* */
public class ORMConfig {

    private static String classPath;//核心配置文件路径

    private static File cfgFile;//核心配置文件

    private static Map<String, String> propConfig;//<property>标签中的数据

    private static Set<String> mappingSet;//映射配置文件路径

    private static Set<String> entitySet;//实体类

    public static List<Mapper> mapperList;//映射信息

    static {
        //得到的classpath路径
        classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        //针对中文路径进行转码
        try {
            classPath = URLDecoder.decode(classPath, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //得到核心配置文件
        cfgFile = new File(classPath + "miniORM.cfg.xml");
        if(cfgFile.exists()){
            //解析核心配置文件中的数据类
            Document document = Dom4jUtil.getXMLByFilePath(cfgFile.getPath());
            /*
            * <orm-factory>
                <property name = "connection.url">jdbc:mysql://......</property>
                <property name = "connection.driverClass">com.mysql.jdbc.Driver</property>
                <property name = .........>
            * */
            //property标签中的数据
            propConfig = Dom4jUtil.Elements2Map(document, "property", "name");
            //映射配置文件路径
            mappingSet = Dom4jUtil.Elements2Set(document, "mapping", "resource");
            //实体类文件路径
            entitySet = Dom4jUtil.Elements2Set(document, "entity", "package");
        }else{
            cfgFile = null;
            System.out.println("未找到核心配置文件miniORM.cfg.xml");
        }

    }

    //从propConfig集合中获取数据并连接数据库
    private Connection getConnection() throws ClassNotFoundException, SQLException {
        String url = propConfig.get("connection.url");
        String driverClass = propConfig.get("connection.driverClass");
        String username = propConfig.get("connection.username");
        String password = propConfig.get("connection.password");

        Class.forName(driverClass);
        Connection connection = DriverManager.getConnection(url, username, password);
        connection.setAutoCommit(true);
        return connection;
    }

    //
    /*
    * <orm-mapping>
        <class name = "cn.itcast.orm.test.entity.Book" table="t_book">
            <id name = "id", column = "bid/>
            <property name = "name" column = "bname"/>
            <property name = "author" column = "author"/>
            <property name = "price" column = "price"/>
        </class>
      <orm-mapping>
    *
    * */
    public void getMapping() throws ClassNotFoundException {
        mapperList = new ArrayList<>();
        //映射数据来自映射配置文件
        for(String xmlPath : mappingSet){
            Document document = Dom4jUtil.getXMLByFilePath(classPath + xmlPath);
            String className = Dom4jUtil.getPropValue(document, "class", "name");
            String tableName = Dom4jUtil.getPropValue(document, "class", "table");
            Map<String, String> id_id = Dom4jUtil.ElementsID2Map(document);
            Map<String, String> mapping = Dom4jUtil.Elements2Map(document);
            Mapper mapper = new Mapper();
            mapper.setTableName(tableName);
            mapper.setClassName(className);
            mapper.setIdMapper(id_id);
            mapper.setPropMapper(mapping);
            mapperList.add(mapper);
        }


        //解析实体类中的注解拿到映射数据
        for(String packagePath : entitySet){
            //获得包下的所有文件
            Set<String> nameSet = AnnotationUtil.getClassNameByPackage(packagePath);
            for(String name : nameSet){
                Class clz = Class.forName(name);

                String className = AnnotationUtil.getClassName(clz);

                String tableName = AnnotationUtil.getTableName(clz);

                Map<String, String> id_id = AnnotationUtil.getIdMapper(clz);

                Map<String, String> mapping = AnnotationUtil.getPropMapping(clz);

                Mapper mapper = new Mapper();
                mapper.setTableName(tableName);
                mapper.setClassName(className);
                mapper.setIdMapper(id_id);
                mapper.setPropMapper(mapping);
                mapperList.add(mapper);
            }
        }
    }
    //创建ORMSession对象
    public ORMSession buildORMSession() throws SQLException, ClassNotFoundException {

        //连接数据库
        Connection connection = getConnection();
        //得到映射数据
        getMapping();
        //创建ORMSession
        return new ORMSession(connection);
    }
}
