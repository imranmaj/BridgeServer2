package org.sagebionetworks.bridge.hibernate;

import javax.annotation.Resource;

import org.sagebionetworks.bridge.dao.DemographicDao;

public class HibernateDemographicDao implements DemographicDao {
    private HibernateHelper hibernateHelper;
    
    @Resource(name = "mysqlHibernateHelper")
    final void setHibernateHelper(HibernateHelper hibernateHelper) {
        this.hibernateHelper = hibernateHelper;
    }


}
