package com.sequenceiq.cloudbreak.domain.stack.cluster;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.sequenceiq.cloudbreak.common.json.Json;
import com.sequenceiq.cloudbreak.common.json.JsonToString;
import com.sequenceiq.cloudbreak.common.type.ComponentType;
import com.sequenceiq.cloudbreak.domain.ProvisionEntity;
import com.sequenceiq.cloudbreak.domain.converter.ComponentTypeConverter;

@Entity
@Table(name = "clustercomponent_history")
public class ClusterComponentHistory implements ProvisionEntity {

    @Id
    private Long id;

    @Column(name = "componenttype")
    @Convert(converter = ComponentTypeConverter.class)
    private ComponentType componentType;

    @Column(nullable = false)
    private String name;

    @Convert(converter = JsonToString.class)
    @Column(columnDefinition = "TEXT")
    private Json attributes;

    @ManyToOne
    private Cluster cluster;

    public ClusterComponentHistory() {
    }

    public ClusterComponentHistory(ComponentType componentType, Json attributes, Cluster cluster) {
        this(componentType, componentType.name(), attributes, cluster);
    }

    public ClusterComponentHistory(ComponentType componentType, String name, Json attributes, Cluster cluster) {
        this.componentType = componentType;
        this.name = name;
        this.attributes = attributes;
        this.cluster = cluster;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Json getAttributes() {
        return attributes;
    }

    public void setAttributes(Json attributes) {
        this.attributes = attributes;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster clusterId) {
        this.cluster = clusterId;
    }

    @Override
    public String toString() {
        return "Component{"
                + "id=" + id
                + ", componentType=" + componentType
                + ", name='" + name + '\''
                + '}';
    }

}
