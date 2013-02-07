package io.cloudsoft.mapr.m3;

import java.util.List;

import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.trait.Startable;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.BasicAttributeSensor;
import brooklyn.event.basic.BasicConfigKey;

public interface AbstractM3Node extends SoftwareProcess, Startable {

    public static BasicConfigKey<DiskSetupSpec> DISK_SETUP_SPEC = new BasicConfigKey<DiskSetupSpec>(DiskSetupSpec.class, "mapr.node.disk.setup", "");
    public static final AttributeSensor<Boolean> ZOOKEEPER_UP = new BasicAttributeSensor<Boolean>(Boolean.class, "mapr.zookeeper.serviceUp", "whether zookeeper has been started");
    
    public boolean isZookeeper();
    
    public List<String> getAptPackagesToInstall();

    public void runMaprPhase2();
}
