<#import "/spring.ftl" as spring>

<#--
  Bootstrap form fields bound with the Spring "bind" macro: they show the submitted value back and the
  validation errors of the field. `path` is "<model attribute>.<property>", e.g. "form.acronym".
-->

<#macro input path label type="text" col="col-md-4" required=false attrs="">
    <@spring.bind path/>
    <div class="${col}">
        <label for="${spring.status.expression}" class="form-label">${label}<#if required> *</#if></label>
        <input type="${type}" id="${spring.status.expression}" name="${spring.status.expression}"
               value="${spring.status.displayValue!""}"
               class="form-control<#if spring.status.error> is-invalid</#if>"<#if required> required</#if> ${attrs?no_esc}>
        <#list spring.status.errorMessages as error>
            <div class="invalid-feedback">${error}</div>
        </#list>
    </div>
</#macro>

<#-- options: sequence of hashes {"value": ..., "label": ...} -->
<#macro select path label options col="col-md-4" required=false placeholder="">
    <@spring.bind path/>
    <#assign current = spring.status.displayValue!"">
    <div class="${col}">
        <label for="${spring.status.expression}" class="form-label">${label}<#if required> *</#if></label>
        <select id="${spring.status.expression}" name="${spring.status.expression}"
                class="form-select<#if spring.status.error> is-invalid</#if>"<#if required> required</#if>>
            <#if placeholder?has_content><option value="">${placeholder}</option></#if>
            <#list options as option>
                <option value="${option.value}"<#if option.value == current> selected</#if>>${option.label}</option>
            </#list>
        </select>
        <#list spring.status.errorMessages as error>
            <div class="invalid-feedback">${error}</div>
        </#list>
    </div>
</#macro>

<#macro checkbox path label col="col-md-4">
    <@spring.bind path/>
    <div class="${col} d-flex align-items-end">
        <div class="form-check mb-2">
            <input type="hidden" name="_${spring.status.expression}" value="on">
            <input type="checkbox" id="${spring.status.expression}" name="${spring.status.expression}" value="true"
                   class="form-check-input"<#if (spring.status.value!false)?is_boolean && spring.status.value> checked</#if>>
            <label for="${spring.status.expression}" class="form-check-label">${label}</label>
        </div>
    </div>
</#macro>

<#-- Submit and cancel buttons, plus the CSRF token -->
<#macro actions cancelUrl>
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <div class="d-flex justify-content-end gap-2">
        <a href="<@spring.url cancelUrl/>" class="btn btn-outline-secondary">Cancel</a>
        <button type="submit" class="btn btn-primary">Save</button>
    </div>
</#macro>
