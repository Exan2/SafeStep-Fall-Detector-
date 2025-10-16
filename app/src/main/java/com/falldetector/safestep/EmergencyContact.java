package com.falldetector.safestep;

import java.util.Objects;

public class EmergencyContact {
    private String name;
    private String phone;
    private String relationship;
    
    public EmergencyContact(String name, String phone, String relationship) {
        this.name = name;
        this.phone = phone;
        this.relationship = relationship;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    
    // Override equals and hashCode for proper comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EmergencyContact that = (EmergencyContact) obj;
        return Objects.equals(name, that.name) && 
               Objects.equals(phone, that.phone) && 
               Objects.equals(relationship, that.relationship);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, phone, relationship);
    }
    
    @Override
    public String toString() {
        return "EmergencyContact{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
    }
}
