

package io.cloudsoft.mapr

import static brooklyn.event.basic.DependentConfiguration.*
import io.cloudsoft.mapr.m3.AbstractM3Node
import io.cloudsoft.mapr.m3.MasterNode
import io.cloudsoft.mapr.m3.MasterNodeImpl
import io.cloudsoft.mapr.m3.WorkerNode
import io.cloudsoft.mapr.m3.ZookeeperWorkerNode
import io.cloudsoft.mapr.m3.ZookeeperWorkerNodeImpl

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import brooklyn.enricher.basic.SensorPropagatingEnricher
import brooklyn.entity.Entity
import brooklyn.entity.basic.AbstractEntity
import brooklyn.entity.group.DynamicCluster
import brooklyn.entity.group.DynamicClusterImpl
import brooklyn.entity.proxying.BasicEntitySpec
import brooklyn.entity.trait.StartableMethods
import brooklyn.event.basic.DependentConfiguration
import brooklyn.location.Location

public class M3Impl extends AbstractEntity implements M3 {

    public static final Logger log = LoggerFactory.getLogger(M3.class);

    MasterNode master;
    ZookeeperWorkerNode zk1;
    ZookeeperWorkerNode zk2;
    DynamicCluster workers;

    public M3Impl() {
    }

    public M3Impl(Entity parent) {
        super(parent)
    }

    public M3Impl(Map flags) {
        super(flags)
    }

    public M3Impl(Map flags, Entity parent) {
        super(flags, parent)
    }

    @Override
    public void postConstruct() {
        master = addChild(getEntityManager().createEntity(MasterNode, name: "node1 (master)"))
        
        zk1 = addChild(getEntityManager().createEntity(ZookeeperWorkerNode, name: "node2 (zk+worker)"))

        zk2 = addChild(getEntityManager().createEntity(ZookeeperWorkerNode, name: "node3 (zk+worker)"))

        workers = addChild(getEntityManager().createEntity(DynamicCluster,
                initialSize: 2,
                memberSpec: BasicEntitySpec.newInstance(WorkerNode.class)));
        
        setConfig(MASTER_UP, DependentConfiguration.attributeWhenReady(master, MasterNode.SERVICE_UP));
        
        setConfig(MASTER_HOSTNAME, DependentConfiguration.attributeWhenReady(master, MasterNode.HOSTNAME));
        
        final def zookeeperNodes = children.findAll({ (it in AbstractM3Node) && (it.isZookeeper()) });
        setConfig(ZOOKEEPER_HOSTNAMES, DependentConfiguration.listAttributesWhenReady(AbstractM3Node.HOSTNAME, zookeeperNodes));
        setConfig(ZOOKEEPER_READY, DependentConfiguration.listAttributesWhenReady(AbstractM3Node.ZOOKEEPER_UP, zookeeperNodes));
        
        SensorPropagatingEnricher.newInstanceListeningTo(master, MasterNode.MAPR_URL).addToEntityAndEmitAll(this);
    }    

    @Override public void start(Collection<? extends Location> locations) { StartableMethods.start(this, locations); }
    @Override public void stop() { StartableMethods.stop(this); }
    @Override public void restart() { StartableMethods.restart(this); }
}
