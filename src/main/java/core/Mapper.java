package core;


import java.util.HashMap;
import java.util.Map;

//该类用来分装和存储映射信息
public class Mapper {

    private String className;//类名

    private String tableName;//表名

    private Map<String, String> idMapper = new HashMap<>();//主键映射

    private Map<String, String> propMapper = new HashMap<>();//非主键映射

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, String> getIdMapper() {
        return idMapper;
    }

    public void setIdMapper(Map<String, String> idMapper) {
        this.idMapper = idMapper;
    }

    public Map<String, String> getPropMapper() {
        return propMapper;
    }

    public void setPropMapper(Map<String, String> propMapper) {
        this.propMapper = propMapper;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Mapper{");
        sb.append("className='").append(className).append('\'');
        sb.append(", tableName='").append(tableName).append('\'');
        sb.append(", idMapper=").append(idMapper);
        sb.append(", propMapper=").append(propMapper);
        sb.append('}');
        return sb.toString();
    }
}
