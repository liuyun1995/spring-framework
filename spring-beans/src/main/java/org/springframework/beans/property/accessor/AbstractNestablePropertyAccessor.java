package org.springframework.beans.property.accessor;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.bean.BeanUtils;
import org.springframework.beans.TypeConverterDelegate;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.exception.ConversionNotSupportedException;
import org.springframework.beans.exception.InvalidPropertyException;
import org.springframework.beans.exception.MethodInvocationException;
import org.springframework.beans.exception.NotReadablePropertyException;
import org.springframework.beans.exception.NotWritablePropertyException;
import org.springframework.beans.exception.NullValueInNestedPathException;
import org.springframework.beans.exception.TypeMismatchException;
import org.springframework.beans.property.PropertyValue;
import org.springframework.core.CollectionFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.UsesJava8;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

//抽象嵌套属性获取器
public abstract class AbstractNestablePropertyAccessor extends AbstractPropertyAccessor {

    private static final Log logger = LogFactory.getLog(AbstractNestablePropertyAccessor.class);

    private static Class<?> javaUtilOptionalClass = null;

    static {
        try {
            javaUtilOptionalClass = ClassUtils.forName("java.util.Optional",
                    AbstractNestablePropertyAccessor.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // Java 8 not available - Optional references simply not supported then.
        }
    }

    private int autoGrowCollectionLimit = Integer.MAX_VALUE;

    Object wrappedObject;

    private String nestedPath = "";

    Object rootObject;

    private Map<String, AbstractNestablePropertyAccessor> nestedPropertyAccessors;

    protected AbstractNestablePropertyAccessor() {
        this(true);
    }

    protected AbstractNestablePropertyAccessor(boolean registerDefaultEditors) {
        if (registerDefaultEditors) {
            registerDefaultEditors();
        }
        this.typeConverterDelegate = new TypeConverterDelegate(this);
    }

    protected AbstractNestablePropertyAccessor(Object object) {
        registerDefaultEditors();
        setWrappedInstance(object);
    }

    protected AbstractNestablePropertyAccessor(Class<?> clazz) {
        registerDefaultEditors();
        setWrappedInstance(BeanUtils.instantiateClass(clazz));
    }

    protected AbstractNestablePropertyAccessor(Object object, String nestedPath, Object rootObject) {
        registerDefaultEditors();
        setWrappedInstance(object, nestedPath, rootObject);
    }

    protected AbstractNestablePropertyAccessor(Object object, String nestedPath,
                                               AbstractNestablePropertyAccessor parent) {
        setWrappedInstance(object, nestedPath, parent.getWrappedInstance());
        setExtractOldValueForEditor(parent.isExtractOldValueForEditor());
        setAutoGrowNestedPaths(parent.isAutoGrowNestedPaths());
        setAutoGrowCollectionLimit(parent.getAutoGrowCollectionLimit());
        setConversionService(parent.getConversionService());
    }

    public void setAutoGrowCollectionLimit(int autoGrowCollectionLimit) {
        this.autoGrowCollectionLimit = autoGrowCollectionLimit;
    }

    public int getAutoGrowCollectionLimit() {
        return this.autoGrowCollectionLimit;
    }

    //设置包装的实例
    public void setWrappedInstance(Object object) {
        setWrappedInstance(object, "", null);
    }

    //设置包装的实例
    public void setWrappedInstance(Object object, String nestedPath, Object rootObject) {
        Assert.notNull(object, "Target object must not be null");
        if (object.getClass() == javaUtilOptionalClass) {
            this.wrappedObject = OptionalUnwrapper.unwrap(object);
        } else {
            this.wrappedObject = object;
        }
        this.nestedPath = (nestedPath != null ? nestedPath : "");
        this.rootObject = (!"".equals(this.nestedPath) ? rootObject : this.wrappedObject);
        this.nestedPropertyAccessors = null;
        this.typeConverterDelegate = new TypeConverterDelegate(this, this.wrappedObject);
    }

    //获取包装实例
    public final Object getWrappedInstance() {
        return this.wrappedObject;
    }

    //获取包装类型
    public final Class<?> getWrappedClass() {
        return (this.wrappedObject != null ? this.wrappedObject.getClass() : null);
    }

    //获取嵌套路径
    public final String getNestedPath() {
        return this.nestedPath;
    }

    //获取根实例
    public final Object getRootInstance() {
        return this.rootObject;
    }

    //获取根类型
    public final Class<?> getRootClass() {
        return (this.rootObject != null ? this.rootObject.getClass() : null);
    }

    //设置属性值
    @Override
    public void setPropertyValue(String propertyName, Object value) throws org.springframework.beans.exception.BeansException {
        AbstractNestablePropertyAccessor nestedPa;
        try {
            nestedPa = getPropertyAccessorForPropertyPath(propertyName);
        } catch (org.springframework.beans.exception.NotReadablePropertyException ex) {
            throw new org.springframework.beans.exception.NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName,
                    "Nested property in path '" + propertyName + "' does not exist", ex);
        }
        PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedPa, propertyName));
        nestedPa.setPropertyValue(tokens, new org.springframework.beans.property.PropertyValue(propertyName, value));
    }

    //设置属性值
    @Override
    public void setPropertyValue(org.springframework.beans.property.PropertyValue pv) throws org.springframework.beans.exception.BeansException {
        PropertyTokenHolder tokens = (PropertyTokenHolder) pv.resolvedTokens;
        if (tokens == null) {
            String propertyName = pv.getName();
            AbstractNestablePropertyAccessor nestedPa;
            try {
                nestedPa = getPropertyAccessorForPropertyPath(propertyName);
            } catch (org.springframework.beans.exception.NotReadablePropertyException ex) {
                throw new org.springframework.beans.exception.NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName,
                        "Nested property in path '" + propertyName + "' does not exist", ex);
            }
            tokens = getPropertyNameTokens(getFinalPath(nestedPa, propertyName));
            if (nestedPa == this) {
                pv.getOriginalPropertyValue().resolvedTokens = tokens;
            }
            nestedPa.setPropertyValue(tokens, pv);
        } else {
            setPropertyValue(tokens, pv);
        }
    }

    //设置属性值
    protected void setPropertyValue(PropertyTokenHolder tokens, org.springframework.beans.property.PropertyValue pv) throws org.springframework.beans.exception.BeansException {
        if (tokens.keys != null) {
            processKeyedProperty(tokens, pv);
        } else {
            processLocalProperty(tokens, pv);
        }
    }

    @SuppressWarnings("unchecked")
    private void processKeyedProperty(PropertyTokenHolder tokens, org.springframework.beans.property.PropertyValue pv) {
        Object propValue = getPropertyHoldingValue(tokens);
        String lastKey = tokens.keys[tokens.keys.length - 1];

        if (propValue.getClass().isArray()) {
            PropertyHandler ph = getLocalPropertyHandler(tokens.actualName);
            Class<?> requiredType = propValue.getClass().getComponentType();
            int arrayIndex = Integer.parseInt(lastKey);
            Object oldValue = null;
            try {
                if (isExtractOldValueForEditor() && arrayIndex < Array.getLength(propValue)) {
                    oldValue = Array.get(propValue, arrayIndex);
                }
                Object convertedValue = convertIfNecessary(tokens.canonicalName, oldValue, pv.getValue(), requiredType,
                        ph.nested(tokens.keys.length));
                int length = Array.getLength(propValue);
                if (arrayIndex >= length && arrayIndex < this.autoGrowCollectionLimit) {
                    Class<?> componentType = propValue.getClass().getComponentType();
                    Object newArray = Array.newInstance(componentType, arrayIndex + 1);
                    System.arraycopy(propValue, 0, newArray, 0, length);
                    setPropertyValue(tokens.actualName, newArray);
                    propValue = getPropertyValue(tokens.actualName);
                }
                Array.set(propValue, arrayIndex, convertedValue);
            } catch (IndexOutOfBoundsException ex) {
                throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName,
                        "Invalid array index in property path '" + tokens.canonicalName + "'", ex);
            }
        } else if (propValue instanceof List) {
            PropertyHandler ph = getPropertyHandler(tokens.actualName);
            Class<?> requiredType = ph.getCollectionType(tokens.keys.length);
            List<Object> list = (List<Object>) propValue;
            int index = Integer.parseInt(lastKey);
            Object oldValue = null;
            if (isExtractOldValueForEditor() && index < list.size()) {
                oldValue = list.get(index);
            }
            Object convertedValue = convertIfNecessary(tokens.canonicalName, oldValue, pv.getValue(), requiredType,
                    ph.nested(tokens.keys.length));
            int size = list.size();
            if (index >= size && index < this.autoGrowCollectionLimit) {
                for (int i = size; i < index; i++) {
                    try {
                        list.add(null);
                    } catch (NullPointerException ex) {
                        throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName,
                                "Cannot set element with index " + index + " in List of size " + size
                                        + ", accessed using property path '" + tokens.canonicalName
                                        + "': List does not support filling up gaps with null elements");
                    }
                }
                list.add(convertedValue);
            } else {
                try {
                    list.set(index, convertedValue);
                } catch (IndexOutOfBoundsException ex) {
                    throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName,
                            "Invalid list index in property path '" + tokens.canonicalName + "'", ex);
                }
            }
        } else if (propValue instanceof Map) {
            PropertyHandler ph = getLocalPropertyHandler(tokens.actualName);
            Class<?> mapKeyType = ph.getMapKeyType(tokens.keys.length);
            Class<?> mapValueType = ph.getMapValueType(tokens.keys.length);
            Map<Object, Object> map = (Map<Object, Object>) propValue;
            // IMPORTANT: Do not pass full property name in here - property editors
            // must not kick in for map keys but rather only for map values.
            TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(mapKeyType);
            Object convertedMapKey = convertIfNecessary(null, null, lastKey, mapKeyType, typeDescriptor);
            Object oldValue = null;
            if (isExtractOldValueForEditor()) {
                oldValue = map.get(convertedMapKey);
            }
            // Pass full property name and old value in here, since we want full
            // conversion ability for map values.
            Object convertedMapValue = convertIfNecessary(tokens.canonicalName, oldValue, pv.getValue(), mapValueType,
                    ph.nested(tokens.keys.length));
            map.put(convertedMapKey, convertedMapValue);
        } else {
            throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName,
                    "Property referenced in indexed property path '" + tokens.canonicalName
                            + "' is neither an array nor a List nor a Map; returned value was [" + propValue + "]");
        }
    }

    private Object getPropertyHoldingValue(PropertyTokenHolder tokens) {
        // Apply indexes and map keys: fetch value for all keys but the last one.
        PropertyTokenHolder getterTokens = new PropertyTokenHolder();
        getterTokens.canonicalName = tokens.canonicalName;
        getterTokens.actualName = tokens.actualName;
        getterTokens.keys = new String[tokens.keys.length - 1];
        System.arraycopy(tokens.keys, 0, getterTokens.keys, 0, tokens.keys.length - 1);

        Object propValue;
        try {
            propValue = getPropertyValue(getterTokens);
        } catch (org.springframework.beans.exception.NotReadablePropertyException ex) {
            throw new org.springframework.beans.exception.NotWritablePropertyException(getRootClass(), this.nestedPath + tokens.canonicalName,
                    "Cannot access indexed value in property referenced " + "in indexed property path '"
                            + tokens.canonicalName + "'",
                    ex);
        }

        if (propValue == null) {
            // null map value case
            if (isAutoGrowNestedPaths()) {
                int lastKeyIndex = tokens.canonicalName.lastIndexOf('[');
                getterTokens.canonicalName = tokens.canonicalName.substring(0, lastKeyIndex);
                propValue = setDefaultValue(getterTokens);
            } else {
                throw new org.springframework.beans.exception.NullValueInNestedPathException(getRootClass(), this.nestedPath + tokens.canonicalName,
                        "Cannot access indexed value in property referenced " + "in indexed property path '"
                                + tokens.canonicalName + "': returned null");
            }
        }
        return propValue;
    }

    //处理本地属性
    private void processLocalProperty(PropertyTokenHolder tokens, org.springframework.beans.property.PropertyValue pv) {
        PropertyHandler ph = getLocalPropertyHandler(tokens.actualName);
        if (ph == null || !ph.isWritable()) {
            if (pv.isOptional()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring optional value for property '" + tokens.actualName
                            + "' - property not found on bean class [" + getRootClass().getName() + "]");
                }
                return;
            } else {
                throw createNotWritablePropertyException(tokens.canonicalName);
            }
        }

        Object oldValue = null;
        try {
            Object originalValue = pv.getValue();
            Object valueToApply = originalValue;
            if (!Boolean.FALSE.equals(pv.conversionNecessary)) {
                if (pv.isConverted()) {
                    valueToApply = pv.getConvertedValue();
                } else {
                    if (isExtractOldValueForEditor() && ph.isReadable()) {
                        try {
                            oldValue = ph.getValue();
                        } catch (Exception ex) {
                            if (ex instanceof PrivilegedActionException) {
                                ex = ((PrivilegedActionException) ex).getException();
                            }
                            if (logger.isDebugEnabled()) {
                                logger.debug("Could not read previous value of property '" + this.nestedPath
                                        + tokens.canonicalName + "'", ex);
                            }
                        }
                    }
                    valueToApply = convertForProperty(tokens.canonicalName, oldValue, originalValue,
                            ph.toTypeDescriptor());
                }
                pv.getOriginalPropertyValue().conversionNecessary = (valueToApply != originalValue);
            }
            ph.setValue(this.wrappedObject, valueToApply);
        } catch (org.springframework.beans.exception.TypeMismatchException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this.rootObject,
                    this.nestedPath + tokens.canonicalName, oldValue, pv.getValue());
            if (ex.getTargetException() instanceof ClassCastException) {
                throw new org.springframework.beans.exception.TypeMismatchException(propertyChangeEvent, ph.getPropertyType(), ex.getTargetException());
            } else {
                Throwable cause = ex.getTargetException();
                if (cause instanceof UndeclaredThrowableException) {
                    // May happen e.g. with Groovy-generated methods
                    cause = cause.getCause();
                }
                throw new org.springframework.beans.exception.MethodInvocationException(propertyChangeEvent, cause);
            }
        } catch (Exception ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestedPath + tokens.canonicalName,
                    oldValue, pv.getValue());
            throw new MethodInvocationException(pce, ex);
        }
    }

    //获取属性类型
    @Override
    public Class<?> getPropertyType(String propertyName) throws org.springframework.beans.exception.BeansException {
        try {
            PropertyHandler ph = getPropertyHandler(propertyName);
            if (ph != null) {
                return ph.getPropertyType();
            } else {
                // Maybe an indexed/mapped property...
                Object value = getPropertyValue(propertyName);
                if (value != null) {
                    return value.getClass();
                }
                // Check to see if there is a custom editor,
                // which might give an indication on the desired target type.
                Class<?> editorType = guessPropertyTypeFromEditors(propertyName);
                if (editorType != null) {
                    return editorType;
                }
            }
        } catch (org.springframework.beans.exception.InvalidPropertyException ex) {
            // Consider as not determinable.
        }
        return null;
    }

    //获取属性类型修饰符
    @Override
    public TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws org.springframework.beans.exception.BeansException {
        try {
            AbstractNestablePropertyAccessor nestedPa = getPropertyAccessorForPropertyPath(propertyName);
            String finalPath = getFinalPath(nestedPa, propertyName);
            PropertyTokenHolder tokens = getPropertyNameTokens(finalPath);
            PropertyHandler ph = nestedPa.getLocalPropertyHandler(tokens.actualName);
            if (ph != null) {
                if (tokens.keys != null) {
                    if (ph.isReadable() || ph.isWritable()) {
                        return ph.nested(tokens.keys.length);
                    }
                } else {
                    if (ph.isReadable() || ph.isWritable()) {
                        return ph.toTypeDescriptor();
                    }
                }
            }
        } catch (org.springframework.beans.exception.InvalidPropertyException ex) {
            // Consider as not determinable.
        }
        return null;
    }

    //是否是可读属性
    @Override
    public boolean isReadableProperty(String propertyName) {
        try {
            PropertyHandler ph = getPropertyHandler(propertyName);
            if (ph != null) {
                return ph.isReadable();
            } else {
                // Maybe an indexed/mapped property...
                getPropertyValue(propertyName);
                return true;
            }
        } catch (org.springframework.beans.exception.InvalidPropertyException ex) {
            // Cannot be evaluated, so can't be readable.
        }
        return false;
    }

    //是否是可写属性
    @Override
    public boolean isWritableProperty(String propertyName) {
        try {
            PropertyHandler ph = getPropertyHandler(propertyName);
            if (ph != null) {
                return ph.isWritable();
            } else {
                // Maybe an indexed/mapped property...
                getPropertyValue(propertyName);
                return true;
            }
        } catch (org.springframework.beans.exception.InvalidPropertyException ex) {
            // Cannot be evaluated, so can't be writable.
        }
        return false;
    }

    private Object convertIfNecessary(String propertyName, Object oldValue, Object newValue, Class<?> requiredType,
                                      TypeDescriptor td) throws org.springframework.beans.exception.TypeMismatchException {
        try {
            return this.typeConverterDelegate.convertIfNecessary(propertyName, oldValue, newValue, requiredType, td);
        } catch (ConverterNotFoundException ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue,
                    newValue);
            throw new org.springframework.beans.exception.ConversionNotSupportedException(pce, td.getType(), ex);
        } catch (ConversionException ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue,
                    newValue);
            throw new org.springframework.beans.exception.TypeMismatchException(pce, requiredType, ex);
        } catch (IllegalStateException ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue,
                    newValue);
            throw new ConversionNotSupportedException(pce, requiredType, ex);
        } catch (IllegalArgumentException ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue,
                    newValue);
            throw new org.springframework.beans.exception.TypeMismatchException(pce, requiredType, ex);
        }
    }

    protected Object convertForProperty(String propertyName, Object oldValue, Object newValue, TypeDescriptor td)
            throws org.springframework.beans.exception.TypeMismatchException {

        return convertIfNecessary(propertyName, oldValue, newValue, td.getType(), td);
    }

    //获取属性值
    @Override
    public Object getPropertyValue(String propertyName) throws org.springframework.beans.exception.BeansException {
        AbstractNestablePropertyAccessor nestedPa = getPropertyAccessorForPropertyPath(propertyName);
        PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedPa, propertyName));
        return nestedPa.getPropertyValue(tokens);
    }

    //获取属性值
    @SuppressWarnings("unchecked")
    protected Object getPropertyValue(PropertyTokenHolder tokens) throws org.springframework.beans.exception.BeansException {
        String propertyName = tokens.canonicalName;
        String actualName = tokens.actualName;
        PropertyHandler ph = getLocalPropertyHandler(actualName);
        if (ph == null || !ph.isReadable()) {
            throw new NotReadablePropertyException(getRootClass(), this.nestedPath + propertyName);
        }
        try {
            Object value = ph.getValue();
            if (tokens.keys != null) {
                if (value == null) {
                    if (isAutoGrowNestedPaths()) {
                        value = setDefaultValue(tokens.actualName);
                    } else {
                        throw new org.springframework.beans.exception.NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName,
                                "Cannot access indexed value of property referenced in indexed " + "property path '"
                                        + propertyName + "': returned null");
                    }
                }
                String indexedPropertyName = tokens.actualName;
                // apply indexes and map keys
                for (int i = 0; i < tokens.keys.length; i++) {
                    String key = tokens.keys[i];
                    if (value == null) {
                        throw new org.springframework.beans.exception.NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName,
                                "Cannot access indexed value of property referenced in indexed " + "property path '"
                                        + propertyName + "': returned null");
                    } else if (value.getClass().isArray()) {
                        int index = Integer.parseInt(key);
                        value = growArrayIfNecessary(value, index, indexedPropertyName);
                        value = Array.get(value, index);
                    } else if (value instanceof List) {
                        int index = Integer.parseInt(key);
                        List<Object> list = (List<Object>) value;
                        growCollectionIfNecessary(list, index, indexedPropertyName, ph, i + 1);
                        value = list.get(index);
                    } else if (value instanceof Set) {
                        // Apply index to Iterator in case of a Set.
                        Set<Object> set = (Set<Object>) value;
                        int index = Integer.parseInt(key);
                        if (index < 0 || index >= set.size()) {
                            throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
                                    "Cannot get element with index " + index + " from Set of size " + set.size()
                                            + ", accessed using property path '" + propertyName + "'");
                        }
                        Iterator<Object> it = set.iterator();
                        for (int j = 0; it.hasNext(); j++) {
                            Object elem = it.next();
                            if (j == index) {
                                value = elem;
                                break;
                            }
                        }
                    } else if (value instanceof Map) {
                        Map<Object, Object> map = (Map<Object, Object>) value;
                        Class<?> mapKeyType = ph.getResolvableType().getNested(i + 1).asMap().resolveGeneric(0);
                        // IMPORTANT: Do not pass full property name in here - property editors
                        // must not kick in for map keys but rather only for map values.
                        TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(mapKeyType);
                        Object convertedMapKey = convertIfNecessary(null, null, key, mapKeyType, typeDescriptor);
                        value = map.get(convertedMapKey);
                    } else {
                        throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
                                "Property referenced in indexed property path '" + propertyName
                                        + "' is neither an array nor a List nor a Set nor a Map; returned value was ["
                                        + value + "]");
                    }
                    indexedPropertyName += PROPERTY_KEY_PREFIX + key + PROPERTY_KEY_SUFFIX;
                }
            }
            return value;
        } catch (IndexOutOfBoundsException ex) {
            throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
                    "Index of out of bounds in property path '" + propertyName + "'", ex);
        } catch (NumberFormatException ex) {
            throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
                    "Invalid index in property path '" + propertyName + "'", ex);
        } catch (TypeMismatchException ex) {
            throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
                    "Invalid index in property path '" + propertyName + "'", ex);
        } catch (InvocationTargetException ex) {
            throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
                    "Getter for property '" + actualName + "' threw exception", ex);
        } catch (Exception ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
                    "Illegal attempt to get property '" + actualName + "' threw exception", ex);
        }
    }

    //获取属性处理器
    protected PropertyHandler getPropertyHandler(String propertyName) throws BeansException {
        Assert.notNull(propertyName, "Property name must not be null");
        AbstractNestablePropertyAccessor nestedPa = getPropertyAccessorForPropertyPath(propertyName);
        return nestedPa.getLocalPropertyHandler(getFinalPath(nestedPa, propertyName));
    }

    //获取本地属性处理器
    protected abstract PropertyHandler getLocalPropertyHandler(String propertyName);

    //新建嵌套属性获取器
    protected abstract AbstractNestablePropertyAccessor newNestedPropertyAccessor(Object object, String nestedPath);

    //新建不可写属性异常
    protected abstract NotWritablePropertyException createNotWritablePropertyException(String propertyName);

    //扩容数组
    private Object growArrayIfNecessary(Object array, int index, String name) {
        if (!isAutoGrowNestedPaths()) {
            return array;
        }
        int length = Array.getLength(array);
        if (index >= length && index < this.autoGrowCollectionLimit) {
            Class<?> componentType = array.getClass().getComponentType();
            Object newArray = Array.newInstance(componentType, index + 1);
            System.arraycopy(array, 0, newArray, 0, length);
            for (int i = length; i < Array.getLength(newArray); i++) {
                Array.set(newArray, i, newValue(componentType, null, name));
            }
            setPropertyValue(name, newArray);
            return getPropertyValue(name);
        } else {
            return array;
        }
    }

    //扩容集合
    private void growCollectionIfNecessary(Collection<Object> collection, int index, String name, PropertyHandler ph,
                                           int nestingLevel) {
        if (!isAutoGrowNestedPaths()) {
            return;
        }
        int size = collection.size();
        if (index >= size && index < this.autoGrowCollectionLimit) {
            Class<?> elementType = ph.getResolvableType().getNested(nestingLevel).asCollection().resolveGeneric();
            if (elementType != null) {
                for (int i = collection.size(); i < index + 1; i++) {
                    collection.add(newValue(elementType, null, name));
                }
            }
        }
    }


    protected String getFinalPath(AbstractNestablePropertyAccessor pa, String nestedPath) {
        if (pa == this) {
            return nestedPath;
        }
        return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath) + 1);
    }


    @SuppressWarnings("unchecked")
    protected AbstractNestablePropertyAccessor getPropertyAccessorForPropertyPath(String propertyPath) {
        int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);
        // Handle nested properties recursively.
        if (pos > -1) {
            String nestedProperty = propertyPath.substring(0, pos);
            String nestedPath = propertyPath.substring(pos + 1);
            AbstractNestablePropertyAccessor nestedPa = getNestedPropertyAccessor(nestedProperty);
            return nestedPa.getPropertyAccessorForPropertyPath(nestedPath);
        } else {
            return this;
        }
    }


    private AbstractNestablePropertyAccessor getNestedPropertyAccessor(String nestedProperty) {
        if (this.nestedPropertyAccessors == null) {
            this.nestedPropertyAccessors = new HashMap<String, AbstractNestablePropertyAccessor>();
        }
        // Get value of bean property.
        PropertyTokenHolder tokens = getPropertyNameTokens(nestedProperty);
        String canonicalName = tokens.canonicalName;
        Object value = getPropertyValue(tokens);
        if (value == null || (value.getClass() == javaUtilOptionalClass && OptionalUnwrapper.isEmpty(value))) {
            if (isAutoGrowNestedPaths()) {
                value = setDefaultValue(tokens);
            } else {
                throw new org.springframework.beans.exception.NullValueInNestedPathException(getRootClass(), this.nestedPath + canonicalName);
            }
        }

        // Lookup cached sub-PropertyAccessor, create new one if not found.
        AbstractNestablePropertyAccessor nestedPa = this.nestedPropertyAccessors.get(canonicalName);
        if (nestedPa == null || nestedPa
                .getWrappedInstance() != (value.getClass() == javaUtilOptionalClass ? OptionalUnwrapper.unwrap(value)
                : value)) {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Creating new nested " + getClass().getSimpleName() + " for property '" + canonicalName + "'");
            }
            nestedPa = newNestedPropertyAccessor(value, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR);
            // Inherit all type-specific PropertyEditors.
            copyDefaultEditorsTo(nestedPa);
            copyCustomEditorsTo(nestedPa, canonicalName);
            this.nestedPropertyAccessors.put(canonicalName, nestedPa);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Using cached nested property accessor for property '" + canonicalName + "'");
            }
        }
        return nestedPa;
    }

    private Object setDefaultValue(String propertyName) {
        PropertyTokenHolder tokens = new PropertyTokenHolder();
        tokens.actualName = propertyName;
        tokens.canonicalName = propertyName;
        return setDefaultValue(tokens);
    }

    //设置默认值
    private Object setDefaultValue(PropertyTokenHolder tokens) {
        org.springframework.beans.property.PropertyValue pv = createDefaultPropertyValue(tokens);
        setPropertyValue(tokens, pv);
        return getPropertyValue(tokens);
    }

    //创建默认属性值
    private org.springframework.beans.property.PropertyValue createDefaultPropertyValue(PropertyTokenHolder tokens) {
        TypeDescriptor desc = getPropertyTypeDescriptor(tokens.canonicalName);
        Class<?> type = desc.getType();
        if (type == null) {
            throw new org.springframework.beans.exception.NullValueInNestedPathException(getRootClass(), this.nestedPath + tokens.canonicalName,
                    "Could not determine property type for auto-growing a default value");
        }
        Object defaultValue = newValue(type, desc, tokens.canonicalName);
        return new PropertyValue(tokens.canonicalName, defaultValue);
    }

    private Object newValue(Class<?> type, TypeDescriptor desc, String name) {
        try {
            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                // TODO - only handles 2-dimensional arrays
                if (componentType.isArray()) {
                    Object array = Array.newInstance(componentType, 1);
                    Array.set(array, 0, Array.newInstance(componentType.getComponentType(), 0));
                    return array;
                } else {
                    return Array.newInstance(componentType, 0);
                }
            } else if (Collection.class.isAssignableFrom(type)) {
                TypeDescriptor elementDesc = (desc != null ? desc.getElementTypeDescriptor() : null);
                return CollectionFactory.createCollection(type, (elementDesc != null ? elementDesc.getType() : null),
                        16);
            } else if (Map.class.isAssignableFrom(type)) {
                TypeDescriptor keyDesc = (desc != null ? desc.getMapKeyTypeDescriptor() : null);
                return CollectionFactory.createMap(type, (keyDesc != null ? keyDesc.getType() : null), 16);
            } else {
                return BeanUtils.instantiate(type);
            }
        } catch (Throwable ex) {
            throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + name,
                    "Could not instantiate property type [" + type.getName() + "] to auto-grow nested property path",
                    ex);
        }
    }

    /**
     * Parse the given property name into the corresponding property name tokens.
     *
     * @param propertyName the property name to parse
     * @return representation of the parsed property tokens
     */
    private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
        PropertyTokenHolder tokens = new PropertyTokenHolder();
        String actualName = null;
        List<String> keys = new ArrayList<String>(2);
        int searchIndex = 0;
        while (searchIndex != -1) {
            int keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex);
            searchIndex = -1;
            if (keyStart != -1) {
                int keyEnd = propertyName.indexOf(PROPERTY_KEY_SUFFIX, keyStart + PROPERTY_KEY_PREFIX.length());
                if (keyEnd != -1) {
                    if (actualName == null) {
                        actualName = propertyName.substring(0, keyStart);
                    }
                    String key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length(), keyEnd);
                    if (key.length() > 1 && (key.startsWith("'") && key.endsWith("'"))
                            || (key.startsWith("\"") && key.endsWith("\""))) {
                        key = key.substring(1, key.length() - 1);
                    }
                    keys.add(key);
                    searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length();
                }
            }
        }
        tokens.actualName = (actualName != null ? actualName : propertyName);
        tokens.canonicalName = tokens.actualName;
        if (!keys.isEmpty()) {
            tokens.canonicalName += PROPERTY_KEY_PREFIX
                    + StringUtils.collectionToDelimitedString(keys, PROPERTY_KEY_SUFFIX + PROPERTY_KEY_PREFIX)
                    + PROPERTY_KEY_SUFFIX;
            tokens.keys = StringUtils.toStringArray(keys);
        }
        return tokens;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        if (this.wrappedObject != null) {
            sb.append(": wrapping object [").append(ObjectUtils.identityToString(this.wrappedObject)).append("]");
        } else {
            sb.append(": no wrapped object set");
        }
        return sb.toString();
    }

    protected abstract static class PropertyHandler {

        private final Class<?> propertyType;   //属性值类型
        private final boolean readable;        //是否可读
        private final boolean writable;        //是否可写

        public PropertyHandler(Class<?> propertyType, boolean readable, boolean writable) {
            this.propertyType = propertyType;
            this.readable = readable;
            this.writable = writable;
        }

        public Class<?> getPropertyType() {
            return this.propertyType;
        }

        public boolean isReadable() {
            return this.readable;
        }

        public boolean isWritable() {
            return this.writable;
        }

        public abstract TypeDescriptor toTypeDescriptor();

        public abstract ResolvableType getResolvableType();

        public Class<?> getMapKeyType(int nestingLevel) {
            return getResolvableType().getNested(nestingLevel).asMap().resolveGeneric(0);
        }

        public Class<?> getMapValueType(int nestingLevel) {
            return getResolvableType().getNested(nestingLevel).asMap().resolveGeneric(1);
        }

        public Class<?> getCollectionType(int nestingLevel) {
            return getResolvableType().getNested(nestingLevel).asCollection().resolveGeneric();
        }

        public abstract TypeDescriptor nested(int level);

        public abstract Object getValue() throws Exception;

        public abstract void setValue(Object object, Object value) throws Exception;
    }

    //属性标记持有器
    protected static class PropertyTokenHolder {

        public String canonicalName;   //规范名称
        public String actualName;      //实际名称
        public String[] keys;          //键集合

    }

    @UsesJava8
    private static class OptionalUnwrapper {

        public static Object unwrap(Object optionalObject) {
            Optional<?> optional = (Optional<?>) optionalObject;
            Assert.isTrue(optional.isPresent(), "Optional value must be present");
            Object result = optional.get();
            Assert.isTrue(!(result instanceof Optional), "Multi-level Optional usage not supported");
            return result;
        }

        public static boolean isEmpty(Object optionalObject) {
            return !((Optional<?>) optionalObject).isPresent();
        }

    }

}
