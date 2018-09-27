package org.aopalliance.intercept;

import java.lang.reflect.Constructor;

//有构造器的调用接口
public interface ConstructorInvocation extends Invocation {

    //获取构造器
    Constructor<?> getConstructor();

}
