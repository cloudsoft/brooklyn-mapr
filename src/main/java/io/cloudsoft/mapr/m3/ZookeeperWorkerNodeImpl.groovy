package io.cloudsoft.mapr.m3

import brooklyn.entity.Entity

public class ZookeeperWorkerNodeImpl extends WorkerNodeImpl implements ZookeeperWorkerNode {

    public ZookeeperWorkerNodeImpl() {
    }

    public ZookeeperWorkerNodeImpl(Entity parent) {
        super(parent)
    }

    public ZookeeperWorkerNodeImpl(Map flags) {
        super(flags)
    }

    public ZookeeperWorkerNodeImpl(Map flags, Entity parent) {
        super(flags, parent)
    }

    public boolean isZookeeper() { return true; }
    
}
