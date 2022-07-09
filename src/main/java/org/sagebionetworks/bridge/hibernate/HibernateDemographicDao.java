package org.sagebionetworks.bridge.hibernate;

import javax.annotation.Resource;

public class HibernateDemographicDao {
    private HibernateHelper hibernateHelper;
    
    @Resource(name = "mysqlHibernateHelper")
    final void setHibernateHelper(HibernateHelper hibernateHelper) {
        this.hibernateHelper = hibernateHelper;
    }
}
