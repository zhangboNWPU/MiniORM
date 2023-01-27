package annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//该注解用于设置列名
//设置注解保留策略
@Retention(RetentionPolicy.RUNTIME)
//注解用在哪
@Target(ElementType.FIELD)
public @interface ORMColumn {
    public String name() default "";
}
