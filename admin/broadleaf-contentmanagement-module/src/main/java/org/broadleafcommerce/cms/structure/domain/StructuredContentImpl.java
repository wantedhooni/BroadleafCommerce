/*
 * #%L
 * BroadleafCommerce CMS Module
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.cms.structure.domain;

import org.broadleafcommerce.common.admin.domain.AdminMainEntity;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransform;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransformMember;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransformTypes;
import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.common.locale.domain.LocaleImpl;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.AdminPresentationMapField;
import org.broadleafcommerce.common.presentation.AdminPresentationMapFields;
import org.broadleafcommerce.common.presentation.AdminPresentationToOneLookup;
import org.broadleafcommerce.common.presentation.PopulateToOneFieldsEnum;
import org.broadleafcommerce.common.presentation.RequiredOverride;
import org.broadleafcommerce.common.presentation.RuleIdentifier;
import org.broadleafcommerce.common.presentation.client.LookupType;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.common.presentation.override.AdminPresentationOverride;
import org.broadleafcommerce.common.presentation.override.AdminPresentationOverrides;
import org.broadleafcommerce.openadmin.audit.AdminAuditable;
import org.broadleafcommerce.openadmin.audit.AdminAuditableListener;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Created by bpolster.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_SC")
@EntityListeners(value = { AdminAuditableListener.class })
@AdminPresentationOverrides(
    {
        @AdminPresentationOverride(name = "auditable.createdBy.id", value = @AdminPresentation(readOnly = true, visibility = VisibilityEnum.HIDDEN_ALL)),
        @AdminPresentationOverride(name = "auditable.updatedBy.id", value = @AdminPresentation(readOnly = true, visibility = VisibilityEnum.HIDDEN_ALL)),
        @AdminPresentationOverride(name = "auditable.createdBy.name", value = @AdminPresentation(readOnly = true, visibility = VisibilityEnum.HIDDEN_ALL)),
        @AdminPresentationOverride(name = "auditable.updatedBy.name", value = @AdminPresentation(readOnly = true, visibility = VisibilityEnum.HIDDEN_ALL)),
        @AdminPresentationOverride(name = "auditable.dateCreated", value = @AdminPresentation(readOnly = true, visibility = VisibilityEnum.HIDDEN_ALL)),
        @AdminPresentationOverride(name = "auditable.dateUpdated", value = @AdminPresentation(readOnly = true, visibility = VisibilityEnum.HIDDEN_ALL)),
        @AdminPresentationOverride(name = "structuredContentType.name", value = @AdminPresentation(readOnly = true, visibility = VisibilityEnum.HIDDEN_ALL)),
        @AdminPresentationOverride(name = "structuredContentType.structuredContentFieldTemplate.name", value = @AdminPresentation(readOnly = true, visibility = VisibilityEnum.HIDDEN_ALL))
    }
)
@AdminPresentationClass(populateToOneFields = PopulateToOneFieldsEnum.TRUE, friendlyName = "StructuredContentImpl_baseStructuredContent")
@DirectCopyTransform({
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.SANDBOX, skipOverlaps=true),
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.MULTITENANT_SITE)
})
@Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
public class StructuredContentImpl implements StructuredContent, AdminMainEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "StructuredContentId")
    @GenericGenerator(
        name="StructuredContentId",
        strategy="org.broadleafcommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name="segment_value", value="StructuredContentImpl"),
            @Parameter(name="entity_name", value="org.broadleafcommerce.cms.structure.domain.StructuredContentImpl")
        }
    )
    @Column(name = "SC_ID")
    protected Long id;

    @Embedded
    @AdminPresentation(excluded = true)
    protected AdminAuditable auditable = new AdminAuditable();

    @AdminPresentation(friendlyName = "StructuredContentImpl_Content_Name", order = 1, 
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description,
        prominent = true, gridOrder = 1)
    @Column(name = "CONTENT_NAME", nullable = false)
    @Index(name="CONTENT_NAME_INDEX", columnNames={"CONTENT_NAME", "ARCHIVED_FLAG", "SC_TYPE_ID"})
    protected String contentName;

    @ManyToOne(targetEntity = LocaleImpl.class, optional = false)
    @JoinColumn(name = "LOCALE_CODE")
    @AdminPresentation(friendlyName = "StructuredContentImpl_Locale", order = 2,
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description,
        prominent = true, gridOrder = 2)
    @AdminPresentationToOneLookup(lookupDisplayProperty = "friendlyName", lookupType = LookupType.DROPDOWN)
    protected Locale locale;

    @Column(name = "PRIORITY", nullable = false)
    @AdminPresentation(friendlyName = "StructuredContentImpl_Priority", order = 3,
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description)
    @Index(name="CONTENT_PRIORITY_INDEX", columnNames={"PRIORITY"})
    protected Integer priority;

    @OneToMany(targetEntity = StructuredContentStructuredContentRuleXrefImpl.class, cascade = { CascadeType.ALL }, orphanRemoval = true)
    @MapKey(name = "key")
    @AdminPresentationMapFields(
        mapDisplayFields = {
            @AdminPresentationMapField(
                fieldName = RuleIdentifier.CUSTOMER_FIELD_KEY,
                fieldPresentation = @AdminPresentation(fieldType = SupportedFieldType.RULE_SIMPLE, order = 1,
                    tab = Presentation.Tab.Name.Rules, tabOrder = Presentation.Tab.Order.Rules,
                    group = Presentation.Group.Name.Rules, groupOrder = Presentation.Group.Order.Rules,
                    ruleIdentifier = RuleIdentifier.CUSTOMER, friendlyName = "Generic_Customer_Rule")
            ),
            @AdminPresentationMapField(
                fieldName = RuleIdentifier.TIME_FIELD_KEY,
                fieldPresentation = @AdminPresentation(fieldType = SupportedFieldType.RULE_SIMPLE, order = 2,
                    tab = Presentation.Tab.Name.Rules, tabOrder = Presentation.Tab.Order.Rules,
                    group = Presentation.Group.Name.Rules, groupOrder = Presentation.Group.Order.Rules,
                    ruleIdentifier = RuleIdentifier.TIME, friendlyName = "Generic_Time_Rule")
            ),
            @AdminPresentationMapField(
                fieldName = RuleIdentifier.REQUEST_FIELD_KEY,
                fieldPresentation = @AdminPresentation(fieldType = SupportedFieldType.RULE_SIMPLE, order = 3,
                    tab = Presentation.Tab.Name.Rules, tabOrder = Presentation.Tab.Order.Rules,
                    group = Presentation.Group.Name.Rules, groupOrder = Presentation.Group.Order.Rules,
                    ruleIdentifier = RuleIdentifier.REQUEST, friendlyName = "Generic_Request_Rule")
            ),
            @AdminPresentationMapField(
                fieldName = RuleIdentifier.PRODUCT_FIELD_KEY,
                fieldPresentation = @AdminPresentation(fieldType = SupportedFieldType.RULE_SIMPLE, order = 4,
                    tab = Presentation.Tab.Name.Rules, tabOrder = Presentation.Tab.Order.Rules,
                    group = Presentation.Group.Name.Rules, groupOrder = Presentation.Group.Order.Rules,
                    ruleIdentifier = RuleIdentifier.PRODUCT, friendlyName = "Generic_Product_Rule")
                    ),
            @AdminPresentationMapField(
                fieldName = RuleIdentifier.ORDER_FIELD_KEY,
                fieldPresentation = @AdminPresentation(fieldType = SupportedFieldType.RULE_SIMPLE, order = 5,
                    tab = Presentation.Tab.Name.Rules, tabOrder = Presentation.Tab.Order.Rules,
                    group = Presentation.Group.Name.Rules, groupOrder = Presentation.Group.Order.Rules,
                    ruleIdentifier = RuleIdentifier.ORDER, friendlyName = "Generic_Order_Rule")
                    ),
            @AdminPresentationMapField(
                fieldName = RuleIdentifier.CATEGORY,
                fieldPresentation = @AdminPresentation(fieldType = SupportedFieldType.RULE_SIMPLE, order = 6,
                    tab = Presentation.Tab.Name.Rules, tabOrder = Presentation.Tab.Order.Rules,
                    group = Presentation.Group.Name.Rules, groupOrder = Presentation.Group.Order.Rules,
                    ruleIdentifier = RuleIdentifier.CATEGORY, friendlyName = "Generic_Category_Rule")
                    )
        }
    )
    @Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
    protected Map<String, StructuredContentStructuredContentRuleXref> structuredContentMatchRules = new HashMap<String, StructuredContentStructuredContentRuleXref>();

    @Transient
    protected Map<String, StructuredContentRule> legacyStructuredContentMatchRules = new HashMap<String, StructuredContentRule>();

    @OneToMany(mappedBy = "structuredContent", fetch = FetchType.LAZY, targetEntity = StructuredContentItemCriteriaImpl.class, cascade = { CascadeType.ALL }, orphanRemoval = true)
    @AdminPresentation(friendlyName = "Generic_Item_Rule", order = 5,
        tab = Presentation.Tab.Name.Rules, tabOrder = Presentation.Tab.Order.Rules,
        group = Presentation.Group.Name.Rules, groupOrder = Presentation.Group.Order.Rules,
        fieldType = SupportedFieldType.RULE_WITH_QUANTITY, 
        ruleIdentifier = RuleIdentifier.ORDERITEM)
    @Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
    protected Set<StructuredContentItemCriteria> qualifyingItemCriteria = new HashSet<StructuredContentItemCriteria>();

    @ManyToOne(targetEntity = StructuredContentTypeImpl.class)
    @JoinColumn(name="SC_TYPE_ID")
    @AdminPresentation(friendlyName = "StructuredContentImpl_Content_Type", order = 2, prominent = true,
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description,
        requiredOverride = RequiredOverride.REQUIRED)
    @AdminPresentationToOneLookup(lookupDisplayProperty = "name", forcePopulateChildProperties = true)
    protected StructuredContentType structuredContentType;

    @OneToMany(targetEntity = StructuredContentStructuredContentFieldXrefImpl.class, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "MAP_KEY")
    @BatchSize(size = 20)
    @Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
    protected Map<String, StructuredContentStructuredContentFieldXref> structuredContentFields = new HashMap<String, StructuredContentStructuredContentFieldXref>();

    @Transient
    protected Map<String, StructuredContentField> legacyStructuredContentFields = new HashMap<String, StructuredContentField>();

    @AdminPresentation(friendlyName = "StructuredContentImpl_Offline", order = 4, 
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description)
    @Column(name = "OFFLINE_FLAG")
    @Index(name="SC_OFFLN_FLG_INDX", columnNames={"OFFLINE_FLAG"})
    protected Boolean offlineFlag = false;
    
    @Transient
    protected Map<String, String> fieldValuesMap = null;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getContentName() {
        return contentName;
    }

    @Override
    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public StructuredContentType getStructuredContentType() {
        return structuredContentType;
    }

    @Override
    public void setStructuredContentType(StructuredContentType structuredContentType) {
        this.structuredContentType = structuredContentType;
    }

    @Override
    @Deprecated
    public Map<String, StructuredContentField> getStructuredContentFields() {
        if (legacyStructuredContentFields.isEmpty()) {
            for (Map.Entry<String, StructuredContentStructuredContentFieldXref> entry : getStructuredContentFieldXrefs().entrySet()) {
                legacyStructuredContentFields.put(entry.getKey(), entry.getValue().getStructuredContentField());
            }
        }
        return Collections.unmodifiableMap(legacyStructuredContentFields);
    }
    
    @Override
    @Deprecated
    public void setStructuredContentFields(Map<String, StructuredContentField> structuredContentFields) {
        this.structuredContentFields.clear();
        this.legacyStructuredContentFields.clear();
        for (Map.Entry<String, StructuredContentField> entry : structuredContentFields.entrySet()) {
            this.structuredContentFields.put(entry.getKey(), new StructuredContentStructuredContentFieldXrefImpl(this, entry.getValue(), entry.getKey()));
        }
    }
    
    @Override
    public Map<String, StructuredContentStructuredContentFieldXref> getStructuredContentFieldXrefs() {
        return structuredContentFields;
    }

    @Override
    public void setStructuredContentFieldXrefs(@Nullable Map<String, StructuredContentStructuredContentFieldXref> structuredContentFields) {
        this.structuredContentFields = structuredContentFields;
    }

    @Override
    public String getFieldValue(String fieldName) {
        if (structuredContentFields.containsKey(fieldName)) {
            return getStructuredContentFieldXrefs().get(fieldName).getStructuredContentField().getValue();
        }
        return null;
    }
    
    @Override
    public void setFieldValues(Map<String, String> fieldValuesMap) {
        this.fieldValuesMap = fieldValuesMap;
    }

    @Override
    public Map<String, String> getFieldValues() {
        if (fieldValuesMap == null) {
            fieldValuesMap = new HashMap<String, String>();
            for (Entry<String, StructuredContentStructuredContentFieldXref> entry : getStructuredContentFieldXrefs().entrySet()) {
                fieldValuesMap.put(entry.getKey(), entry.getValue().getStructuredContentField().getValue());
            }
        }
        return fieldValuesMap;
    }

    @Override
    public Boolean getOfflineFlag() {
        if (offlineFlag == null) {
            return Boolean.FALSE;
        } else {
            return offlineFlag;
        }
    }

    @Override
    public void setOfflineFlag(Boolean offlineFlag) {
        this.offlineFlag = offlineFlag;
    }

    @Override
    public Integer getPriority() {
        return priority;
    }

    @Override
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public AdminAuditable getAuditable() {
        return auditable;
    }

    @Override
    public void setAuditable(AdminAuditable auditable) {
        this.auditable = auditable;
    }

    @Override
    @Deprecated
    public Map<String, StructuredContentRule> getStructuredContentMatchRules() {
        if (legacyStructuredContentMatchRules.size() == 0) {
            for (Map.Entry<String, StructuredContentStructuredContentRuleXref> entry : getStructuredContentMatchRuleXref().entrySet()) {
                legacyStructuredContentMatchRules.put(entry.getKey(), entry.getValue().getStructuredContentRule());
            }
        }
        return Collections.unmodifiableMap(legacyStructuredContentMatchRules);
    }

    @Override
    @Deprecated
    public void setStructuredContentMatchRules(Map<String, StructuredContentRule> structuredContentMatchRules) {
        this.structuredContentMatchRules.clear();
        this.legacyStructuredContentMatchRules.clear();
        for (Map.Entry<String, StructuredContentRule> entry : structuredContentMatchRules.entrySet()) {
            this.structuredContentMatchRules.put(entry.getKey(), new StructuredContentStructuredContentRuleXrefImpl(this, entry.getValue(), entry.getKey()));
        }
    }

    @Override
    public Map<String, StructuredContentStructuredContentRuleXref> getStructuredContentMatchRuleXref() {
        return structuredContentMatchRules;
    }

    @Override
    public void setStructuredContentMatchRuleXrefs(Map<String, StructuredContentStructuredContentRuleXref> structuredContentMatchRules) {
        this.structuredContentMatchRules = structuredContentMatchRules;
    }

    @Override
    public Set<StructuredContentItemCriteria> getQualifyingItemCriteria() {
        return qualifyingItemCriteria;
    }

    @Override
    public void setQualifyingItemCriteria(Set<StructuredContentItemCriteria> qualifyingItemCriteria) {
        this.qualifyingItemCriteria = qualifyingItemCriteria;
    }
    
    @Override
    public String getMainEntityName() {
        return getContentName();
    }
    
    public static class Presentation {
        public static class Tab {
            public static class Name {
                public static final String Rules = "StructuredContentImpl_Rules_Tab";
            }
            
            public static class Order {
                public static final int Rules = 1000;
            }
        }
            
        public static class Group {
            public static class Name {
                public static final String Description = "StructuredContentImpl_Description";
                public static final String Internal = "StructuredContentImpl_Internal";
                public static final String Rules = "StructuredContentImpl_Rules";
            }
            
            public static class Order {
                public static final int Description = 1000;
                public static final int Internal = 2000;
                public static final int Rules = 1000;
            }
        }
    }

}