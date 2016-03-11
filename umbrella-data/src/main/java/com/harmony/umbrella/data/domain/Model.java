/*
 * Copyright 2013-2015 wuxii@foxmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.harmony.umbrella.data.domain;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.harmony.umbrella.data.Persistable;
import com.harmony.umbrella.util.ReflectionUtils;
import com.harmony.umbrella.util.ReflectionUtils.FieldFilter;
import com.harmony.umbrella.util.ReflectionUtils.MethodFilter;

/**
 * @author wuxii@foxmail.com
 */
@MappedSuperclass
public abstract class Model<ID extends Serializable> implements Persistable<ID> {

    private static final long serialVersionUID = -9098668260590791573L;

    @Column(updatable = false)
    protected Long creatorId;

    @Column(updatable = false)
    protected String creatorName;

    @Column(updatable = false)
    protected String creatorCode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    protected Calendar createdTime;

    protected Long modifierId;

    protected String modifierName;

    protected String modifierCode;

    @Temporal(TemporalType.TIMESTAMP)
    protected Calendar modifiedTime;

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorCode() {
        return creatorCode;
    }

    public void setCreatorCode(String creatorCode) {
        this.creatorCode = creatorCode;
    }

    public Calendar getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Calendar createdTime) {
        this.createdTime = createdTime;
    }

    public Long getModifierId() {
        return modifierId;
    }

    public void setModifierId(Long modifierId) {
        this.modifierId = modifierId;
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    public String getModifierCode() {
        return modifierCode;
    }

    public void setModifierCode(String modifierCode) {
        this.modifierCode = modifierCode;
    }

    public Calendar getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Calendar modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ID getId() {
        Method method = getIdMethod();
        if (method != null) {
            try {
                return (ID) method.invoke(this);
            } catch (Exception e) {
            }
        }
        Field idField = getIdField();
        if (idField != null) {
            try {
                method = ReflectionUtils.findReadMethod(getClass(), idField);
                return (ID) ReflectionUtils.invokeMethod(method, this);
            } catch (Exception e) {
                try {
                    ReflectionUtils.makeAccessible(idField);
                    return (ID) idField.get(this);
                } catch (Exception e1) {
                }
            }
        }
        throw new IllegalStateException("entity " + getClass() + " not mapped @Id @EmbeddedId annotation on field and method");
    }

    @Override
    public boolean isNew() {
        return getId() == null;
    }

    @Transient
    private static final Class<?>[] idClasses = new Class[] { Id.class, EmbeddedId.class };

    /**
     * 获取主键的方法, 所有声明了的public方法中查找. 标记有{@linkplain #idClasses}其中之一的方法
     * 
     * @return 主键的获取方法
     */
    private Method getIdMethod() {
        return ReflectionUtils.findMethod(getClass(), new MethodFilter() {
            @Override
            @SuppressWarnings({ "rawtypes", "unchecked" })
            public boolean matches(Method method) {
                for (Class ann : idClasses) {
                    if (method.getAnnotation(ann) != null) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * 获取主键字段, 所有声明的字段中查找. 标记有{@linkplain #idClasses}其中之一的方法
     * 
     * @return
     */
    private Field getIdField() {
        return ReflectionUtils.findField(getClass(), new FieldFilter() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public boolean matches(Field field) {
                for (Class ann : idClasses) {
                    if (field.getAnnotation(ann) != null) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public String toString() {
        return "{\"" + getClass().getSimpleName() + "\":" + "{\"id\":\"" + getId() + "\"}}";
    }

}
