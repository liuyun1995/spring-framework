package org.springframework.beans.factory.xml;

import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.springframework.util.Assert;

public class DelegatingEntityResolver implements EntityResolver {

    public static final String DTD_SUFFIX = ".dtd";  //dtd文件后缀
    public static final String XSD_SUFFIX = ".xsd";  //xsd文件后缀
    private final EntityResolver dtdResolver;        //dtd转换器
    private final EntityResolver schemaResolver;     //schema转换器

    //构造器
    public DelegatingEntityResolver(ClassLoader classLoader) {
        this.dtdResolver = new BeansDtdResolver();
        this.schemaResolver = new PluggableSchemaResolver(classLoader);
    }

    //构造器
    public DelegatingEntityResolver(EntityResolver dtdResolver, EntityResolver schemaResolver) {
        Assert.notNull(dtdResolver, "'dtdResolver' is required");
        Assert.notNull(schemaResolver, "'schemaResolver' is required");
        this.dtdResolver = dtdResolver;
        this.schemaResolver = schemaResolver;
    }

    //转换实体
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (systemId != null) {
            if (systemId.endsWith(DTD_SUFFIX)) {
                return this.dtdResolver.resolveEntity(publicId, systemId);
            } else if (systemId.endsWith(XSD_SUFFIX)) {
                return this.schemaResolver.resolveEntity(publicId, systemId);
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return "EntityResolver delegating " + XSD_SUFFIX + " to " + this.schemaResolver +
                " and " + DTD_SUFFIX + " to " + this.dtdResolver;
    }

}
