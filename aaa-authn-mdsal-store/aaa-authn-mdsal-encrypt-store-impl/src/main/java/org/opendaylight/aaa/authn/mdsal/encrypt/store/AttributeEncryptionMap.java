/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 */

package org.opendaylight.aaa.authn.mdsal.encrypt.store;

import com.google.common.base.Preconditions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunikulk on 4/11/2016.
 */
public class AttributeEncryptionMap {

    private final DataEncrypter dataEncrypter;

    private final List<ClassData> classDataList;

    public AttributeEncryptionMap(final DataEncrypter dataEncrypter) {
        this.dataEncrypter = dataEncrypter;
        this.classDataList = new ArrayList<>();
    }

    public ClassData getClassData(Class<?> cls) {
        Preconditions.checkNotNull(cls);
        for (ClassData data : this.classDataList) {
            if (data.getCanonicalName().equals(cls.getCanonicalName())) {
                return data;
            }
        }
        return null;
    }

    public void add(Class<?> cls, List<String> attributeNames) throws ClassNotFoundException, NoSuchMethodException {
        ClassData classData = new ClassData(cls);
        for (String attribute : attributeNames) {
            classData.addAttribute(attribute);
        }
        this.classDataList.add(classData);
    }

    protected class ClassData {
        private final Class<?> cls;
        private final Class<?> builderClass;
        private final List<AttributeData> attributes;
        private final Method buildMethod;

        public ClassData(final Class<?> cls) throws ClassNotFoundException, NoSuchMethodException {
            this.cls = Preconditions.checkNotNull(cls);
            this.builderClass = cls.getClassLoader().loadClass(cls.getCanonicalName() + "Builder");
            this.buildMethod = this.builderClass.getMethod("build");
            attributes = new ArrayList<>();
        }

        public Object build(Object builderClassInstance) throws InvocationTargetException, IllegalAccessException {
            return this.buildMethod.invoke(builderClassInstance);
        }

        public void addAttribute(String attribute) throws NoSuchMethodException {
            this.attributes.add(new AttributeData(attribute, this.builderClass));
        }

        public String getCanonicalName() {
            return this.cls.getCanonicalName();
        }

        public List<AttributeData> getAttributes() {
            return this.attributes;
        }

        public Class<?> getBuilder() {
            return this.builderClass;
        }
    }

    protected class AttributeData {
        private final Method getMethod;
        private final Method setMethod;

        public AttributeData(final String attributeName, final Class<?> builderClassInstance) throws NoSuchMethodException {
            Preconditions.checkNotNull(attributeName);
            Preconditions.checkNotNull(builderClassInstance);
            this.getMethod = builderClassInstance.getMethod("get" + attributeName);
            this.setMethod = builderClassInstance.getMethod("set" + attributeName, String.class);
        }

        public String encrypt(String attributeName) throws InvocationTargetException, IllegalAccessException {
            return dataEncrypter.encrypt(attributeName);
        }

        public String decrypt(String encryptedValue) {
            return dataEncrypter.decrypt(encryptedValue);
        }

        public String invokeGetMethod(Object builderClassInstance) throws InvocationTargetException, IllegalAccessException {
            return (String) this.getMethod.invoke(builderClassInstance);
        }

        public void invokeSetMethod(Object builderClassInstance, String value) throws InvocationTargetException, IllegalAccessException {
            this.setMethod.invoke(builderClassInstance, value);
        }
    }
}
