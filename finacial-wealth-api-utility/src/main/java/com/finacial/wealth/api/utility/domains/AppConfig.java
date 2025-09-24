/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.domains;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author OSHIN
 */
@Data
@Entity
//@Builder
@Table(name = "app_config")
public class AppConfig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String configName;
    private String configDescription;
    private String configValue;

    public AppConfig() {
    }

   

    public AppConfig(Long id, String configName, String configDescription, String configValue) {
        this.id = id;
        this.configName = configName;
        this.configDescription= configDescription;
        this.configValue= configValue;

    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AppConfig)) {
            return false;
        }
        AppConfig other = (AppConfig) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AppConfig{" + "id=" + id + ", configName=" + configName + ", configValue=" + configValue + '}';
    }
}
