package io.cloudsoft.mapr.m3

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import brooklyn.config.render.RendererHints
import brooklyn.entity.Entity
import brooklyn.event.basic.DependentConfiguration
import brooklyn.location.Location

public class MasterNodeImpl extends AbstractM3NodeImpl implements MasterNode {

    // Note not using @InheritConstructors because of:
    // http://stackoverflow.com/questions/9741133/classformaterror-when-using-secured-and-requestmapping-on-same-method/14748739#14748739
    
    public static final Logger log = LoggerFactory.getLogger(MasterNodeImpl.class);
    
    static {
        RendererHints.register(MasterNode.MAPR_URL, new RendererHints.NamedActionWithUrl("Open"));
    }

    public MasterNodeImpl() {
    }

    public MasterNodeImpl(Entity parent) {
        super(parent)
    }

    public MasterNodeImpl(Map flags) {
        super(flags)
    }

    public MasterNodeImpl(Map flags, Entity parent) {
        super(flags, parent)
    }
    
    @Override
    public boolean isZookeeper() { return true; }
    
    @Override
    public List<String> getAptPackagesToInstall() {
        [ "mapr-cldb", "mapr-jobtracker", "mapr-nfs", "mapr-webserver" ] + super.getAptPackagesToInstall();
    }

    // TODO config param?  note, if this is not 'ubuntu', we have to create the user; see jclouds AdminAccess
    @Override
    public String getUser() { getConfig(MAPR_USERNAME) }
    
    @Override
    public String getPassword() { getConfig(MAPR_PASSWORD) }
    
    @Override
    public void setupAdminUser(String user, String password) {
        //    On node 1, give full permission to the chosen administrative user using the following command:
        //    (and set a passwd)
        driver.exec([
            "sudo adduser ${user} < /dev/null || true",
            "echo \"${password}\n${password}\" | sudo passwd ${user}",
            "sudo /opt/mapr/bin/maprcli acl edit -type cluster -user ${user}:fc" ]);
    }
    
    @Override
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

    @Override
    public void startMasterServices() {
        // start the services
        driver.exec([ "sudo /opt/mapr/bin/maprcli node services -nodes ${getAttribute(HOSTNAME)} -nfs start" ]);
    }    
    
    @Override
    public void runMaprPhase2() {
        driver.startWarden();
        setupAdminUser(user, password);
        startMasterServices();
        
        // not sure this sleep is necessary, but seems safer...
        Thread.sleep(10*1000);
        setAttribute(MAPR_URL, "https://${getAttribute(HOSTNAME)}:8443")
    }

    @Override
    public void start(Collection<? extends Location> locations) {
        if (!getPassword())
            throw new IllegalArgumentException("configuration "+MAPR_PASSWORD.getName()+" must be specified");
        super.start(locations);
    }
    
    @Override
    public void setLicenseApproved(String text) {
        log.info("MapR master {} got license approved invoked with: {}", this, text);
        setAttribute(LICENSE_APPROVED, text);
    }
    
}
