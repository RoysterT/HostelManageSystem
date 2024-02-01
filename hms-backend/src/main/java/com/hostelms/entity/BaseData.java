package com.hostelms.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

/**
 * BaseData 接口定义了用于将数据对象转换为视图对象的方法。
 */
public interface BaseData {

    /**
     * 将数据对象转换为视图对象，并应用指定的 Consumer 对象。
     *
     * @param vClass    视图对象的类型
     * @param consumer  用于对视图对象进行进一步处理的 Consumer 函数
     * @param <V>       视图对象的类型
     * @return 转换后的视图对象
     */
    default <V> V asViewObject(Class<V> vClass, Consumer<V> consumer){
        V v = this.asViewObject(vClass);
        consumer.accept(v);
        return v;
    }

    /**
     * 将数据对象转换为视图对象。
     *
     * @param vClass 视图对象的类型
     * @param <V>    视图对象的类型
     * @return 转换后的视图对象
     */
    default <V> V asViewObject(Class<V> vClass){
        try {
            Field[] declaredFields = vClass.getDeclaredFields();
            Constructor<V> constructor = vClass.getConstructor();
            V v = constructor.newInstance();
            for (Field declaredField : declaredFields){
                convert(declaredField, v);
            }
            return v;
        } catch(ReflectiveOperationException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 将数据对象的字段值转换为视图对象的字段值。
     *
     * @param field  视图对象的字段
     * @param vo     视图对象
     */
    private void convert(Field field, Object vo){
        try {
            Field source = this.getClass().getDeclaredField(field.getName());
            field.setAccessible(true);
            source.setAccessible(true);
            field.set(vo, source.get(this));
        } catch(NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
