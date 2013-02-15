package io.cloudsoft.mapr.m3

import io.cloudsoft.mapr.M3

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import brooklyn.entity.Entity

public class WorkerNodeImpl extends AbstractM3NodeImpl implements WorkerNode {

    public static final Logger log = LoggerFactory.getLogger(WorkerNodeImpl.class);

    public WorkerNodeImpl() {
    }

    public WorkerNodeImpl(Entity parent) {
        super(parent)
    }

    public WorkerNodeImpl(Map flags) {
        super(flags)
    }

    public WorkerNodeImpl(Map flags, Entity parent) {
        super(flags, parent)
    }

    public void runMaprPhase2() {
        log.info("MapR node {} waiting for master", this);
        getConfig(M3.MASTER_UP);
        log.info("MapR node {} detected master up, proceeding to start warden", this);
        driver.startWarden();
    }
    
}
