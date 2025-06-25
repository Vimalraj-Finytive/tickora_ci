package com.uniq.tms.tms_microservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "location")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organizationEntity;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "latitude", nullable = false, length = 255)
    private String latitude;

    @Column(name = "longitude", nullable = false, length = 255)
    private String longitude;

    @Column(name = "radius", nullable = false, length = 255)
    private String radius;

    @OneToMany(mappedBy = "locationEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupEntity> groups = new ArrayList<>();

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    public LocationEntity() {}

    public LocationEntity(Long locationId) {
        this.locationId = locationId;
    }

    public List<GroupEntity> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupEntity> groups) {
        this.groups = groups;
    }

    public OrganizationEntity getOrganizationEntity() {
        return organizationEntity;
    }

    public void setOrganizationEntity(OrganizationEntity organizationEntity) {
        this.organizationEntity = organizationEntity;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public OrganizationEntity getOrganization() {
        return organizationEntity;
    }

    public void setOrganization(OrganizationEntity organizationEntity) {
        this.organizationEntity = organizationEntity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
