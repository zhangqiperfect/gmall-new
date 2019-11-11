package com.atguigu.gmall.index.annotationl;

import java.lang.annotation.*;

/**
 * @author ZQ
 * @create 2019-11-09 16:45
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GmallCache {
    /**
     * 缓存key的前缀
     *
     * @return
     */
    String prefix() default "cache";

    /**
     * 默认过期时间
     *
     * @return
     */
    long timeout() default 300l;

    /**
     * 随机时间
     *
     * @return
     */
    long random() default 500l;
}
