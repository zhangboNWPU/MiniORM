package core;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ORMSession {

    private Connection connection;

    public  ORMSession(Connection connection){
        this.connection = connection;
    }

    //更新insert into 表名 (列名) values (列值)
    public void save(Object entity) throws IllegalAccessException, SQLException {
        String insertSQL = "";
        //1.从ORMConfig中获取映射信息
        List<Mapper> mapperList = ORMConfig.mapperList;

        //2.遍历集合，从集合中找到和entity参数对应的mapper对象
        for(Mapper mapper : mapperList){
            if(mapper.getClassName().equals(entity.getClass().getName())){
                String tableName = mapper.getTableName();
                String insertSQL1 = "insert into " + tableName + "(";
                String insertSQL2 = ") values (";

                //3.得到当前对象所属类中的所有信息
                Field[] fields = entity.getClass().getDeclaredFields();
                for(Field field : fields){
                    //突破private
                    field.setAccessible(true);
                    //4.遍历过程中根据属性得到字段名
                    String columnName = mapper.getPropMapper().get(field.getName());

                    //5.遍历过程中根据属性得到他的值
                    String columnValue = field.get(entity).toString();

                    //6.拼接SQL语句
                    insertSQL1 += columnName + ",";
                    insertSQL2 += "'" + columnValue + "',";
                }
                insertSQL = insertSQL1.substring(0, insertSQL1.length() - 1) + insertSQL2.substring(0, insertSQL2.length() - 1) + ")";
                break;
            }

        }

        System.out.println("MiniORM-save: " + insertSQL);

        //7.通过jdbc发送并执行SQL
        PreparedStatement statement = connection.prepareStatement(insertSQL);
        statement.executeUpdate();
        statement.close();

    }

    //根据主键删除 delete from 表名 where 主键 = 值
    public void delete(Object entity) throws NoSuchFieldException, IllegalAccessException, SQLException {
        String delSQL = "delete from";
        //1.从ORMConfig中获得保存有映射信息的集合
        List<Mapper> mapperList = ORMConfig.mapperList;

        //2.遍历集合，从集合中找到和entity参数对应的mapper对象
        for(Mapper mapper : mapperList){
            if(mapper.getClassName().equals(entity.getClass().getName())){
                //3.得到我们想要得mapper对象，并得到表名
                String tableName = mapper.getTableName();
                delSQL += tableName + "where";
                //4.得到主键属性名和字段名
                Object[] idProp = mapper.getIdMapper().keySet().toArray();
                Object[] idColumn = mapper.getIdMapper().values().toArray();
                //5.得到主键的值
                Field field = entity.getClass().getDeclaredField(idProp[0].toString());
                field.setAccessible(true);
                String idVal = field.get(entity).toString();
                //6.凭借SQL字符串
                delSQL += idColumn[0].toString() + "=" + idVal;


                break;

            }

        }
        //7.通过jdbc发送并执行SQL
        PreparedStatement statement = connection.prepareStatement(delSQL);
        statement.executeUpdate();
        statement.close();
    }

    //根据主键进行查询 select * from 表名 where 主键字段 = 值
    public Object findOne(Class clz, Object id) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        //clz用于对查询结果进行封装,id主键值
        String querySQL = "select * from";
        //1.从ORMConfig中获得存有映射信息的集合
        List<Mapper> mapperList = ORMConfig.mapperList;

        //2.遍历集合获得想要的mapper对象
        for(Mapper mapper : mapperList){
            if(mapper.getClassName().equals(clz.getName())){
                //3.获得表名
                String tableName = mapper.getTableName();
                //4.获得主键字段名
                Object[] idColumn = mapper.getIdMapper().values().toArray();
                //5.拼接SQL语句
                querySQL += tableName + "where" + idColumn[0].toString() + "=" + id;
                break;
            }
        }
        //6.通过jdbc发送并执行SQL
        PreparedStatement statement = connection.prepareStatement(querySQL);
        ResultSet rs = statement.executeQuery();

        //7.封装结果集返回对象
        if(rs.next()){
            //8.查询到遗憾数据
            Object object = clz.newInstance();
            //9.得到存有映射信息集合
            for(Mapper mapper : mapperList){
                if(mapper.getClassName().equals(clz.getName())){
                    //10.得到存有属性和字段的信息
                    Map<String, String> propMap = mapper.getPropMapper();
                    Set<String> keySet = propMap.keySet();
                    //11.分别拿到属性名和字段名
                    for(String prop : keySet){
                        String column = propMap.get(prop);
                        Field field = clz.getDeclaredField(prop);
                        field.setAccessible(true);
                        field.set(object, rs.getObject(column));
                    }
                    break;
                }
            }
            //12.释放资源
            statement.close();
            rs.close();
            //13.返回
            return object;
        }else{
            return null;
        }
    }

    public void close() throws SQLException {
        if(connection != null){
            connection.close();
            connection = null;
        }
    }
}
