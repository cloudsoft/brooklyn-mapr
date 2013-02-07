package io.cloudsoft.mapr.m3;

import brooklyn.config.ConfigKey;
import brooklyn.entity.Effector;
import brooklyn.entity.basic.Description;
import brooklyn.entity.basic.MethodEffector;
import brooklyn.entity.basic.NamedParameter;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.BasicAttributeSensor;
import brooklyn.event.basic.BasicConfigKey;

@ImplementedBy(MasterNodeImpl.class)
public interface MasterNode extends AbstractM3Node {

    public static final AttributeSensor<String> MAPR_URL = new BasicAttributeSensor<String>(String.class, "mapr.url", "URL where MapR can be accessed");
    
    public static final BasicAttributeSensor<String> LICENSE_APPROVED = new BasicAttributeSensor<String>(
    		String.class, "mapr.master.license", "this attribute is set when the license is approved (manually)");
    
    public static final Effector<Void> SET_LICENSE_APPROVED = new MethodEffector(MasterNode.class, "setLicenseApproved");
    
    public static ConfigKey<String> MAPR_USERNAME = new BasicConfigKey<String>(String.class, "mapr.username", "initial user to create for mapr", "mapr");
    public static ConfigKey<String> MAPR_PASSWORD = new BasicConfigKey<String>(String.class, "mapr.password", "initial password for initial user");

    // TODO config param?  note, if this is not 'ubuntu', we have to create the user; see jclouds AdminAccess
    public String getUser();
    public String getPassword();
    
    public void setupAdminUser(String user, String password);
    
    public void waitForLicense();

    public void startMasterServices();
    
    @Description("Sets an attribute on the entity to indicate that the license has been approved")
    public void setLicenseApproved(@NamedParameter("text") String text);
}
