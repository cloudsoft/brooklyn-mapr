package io.cloudsoft.mapr.m3

import java.util.List;

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import brooklyn.config.render.RendererHints;
import brooklyn.entity.Effector;
import brooklyn.entity.basic.Description
import brooklyn.entity.basic.MethodEffector
import brooklyn.entity.basic.NamedParameter;
import brooklyn.event.basic.BasicAttributeSensor
import brooklyn.event.basic.BasicConfigKey;
import brooklyn.event.basic.DependentConfiguration;
import brooklyn.location.Location;

import groovy.transform.InheritConstructors;

@InheritConstructors
class MasterNode extends AbstractM3Node {

    public static final Logger log = LoggerFactory.getLogger(MasterNode.class);
    
    public static final BasicAttributeSensor<String> MAPR_URL = [ String, "mapr.url", "URL where MapR can be accessed" ];
    static {
        RendererHints.register(MAPR_URL, new RendererHints.NamedActionWithUrl("Open"));
    }
    
    public static final BasicAttributeSensor<String> LICENSE_APPROVED = [ String, "mapr.master.license", "this attribute is set when the license is approved (manually)" ];
    public static final Effector<Void> SET_LICENSE_APPROVED = new MethodEffector(MasterNode.&setLicenseApproved);
        
    public boolean isZookeeper() { return true; }
    
    public List<String> getAptPackagesToInstall() {
        [ "mapr-cldb", "mapr-jobtracker", "mapr-nfs", "mapr-webserver" ] + super.getAptPackagesToInstall();
    }

    public void setupAdminUserMapr(String user, String password) {
        // TODO this should happen on all nodes
        // (but isn't needed except for metrics)
        exec([
            "sudo /opt/mapr/bin/maprcli acl edit -type cluster -user ${user}:fc" ]);
    }

    public void waitForLicense() {
        // MANUALLY: accept the license
        //    https://<node 1>:8443  -->  accept agreement, login, add license key
        log.info("${this} waiting for MapR LICENSE"+"""
**********************************************************************
* LICENSE must be accepted manually at:
*   MapR console -- https://${getAttribute(HOSTNAME)}:8443
* THEN invoke effector  setLicenseApproved true  at:
*   Brooklyn console -- e.g. http://localhost:8081
**********************************************************************""");
        getExecutionContext().submit(DependentConfiguration.attributeWhenReady(this, LICENSE_APPROVED)).get();
        log.info("MapR LICENSE accepted, proceeding");
    }

    public void startMasterServices() {
        // start the services
//        driver.exec([ "sudo /opt/mapr/bin/maprcli node services -nodes ${getAttribute(HOSTNAME)} -nfs start" ]);
    }    
    
    public void runMaprPhase2() {
        driver.startWarden();
        startMasterServices();
        setupAdminUserMapr(getUser(), getPassword());
        
        // not sure this sleep is necessary, but seems safer...
        Thread.sleep(10*1000);
        setAttribute(MAPR_URL, "https://${getAttribute(HOSTNAME)}:8443")
    }

    public void start(Collection<? extends Location> locations) {
        if (!getPassword())
            throw new IllegalArgumentException("configuration "+MAPR_PASSWORD.getName()+" must be specified");
        super.start(locations);
    }
    
    @Description("Sets an attribute on the entity to indicate that the license has been approved")
    public void setLicenseApproved(@NamedParameter("text") String text) {
        log.info("MapR master {} got license approved invoked with: {}", this, text);
        setAttribute(LICENSE_APPROVED, text);
    }
    
}
