package org.springframework.dao.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

//DAO支持助手
public abstract class DaoSupport implements InitializingBean {

    protected final Log logger = LogFactory.getLog(getClass());

    @Override
    public final void afterPropertiesSet() throws IllegalArgumentException, BeanInitializationException {
        checkDaoConfig();
        try {
            initDao();
        } catch (Exception ex) {
            throw new BeanInitializationException("Initialization of DAO failed", ex);
        }
    }

    //检查DAO配置
    protected abstract void checkDaoConfig() throws IllegalArgumentException;

    //初始化DAO
    protected void initDao() throws Exception {}

}
