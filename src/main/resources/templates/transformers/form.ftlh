<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>
<#import "../_form.ftlh" as f>

<#assign editing = transformerId??>
<#assign action = editing?then("/transformers/" + transformerId?c, "/transformers")>
<#assign cancelUrl = editing?then("/transformers/" + transformerId?c, "/transformers")>

<#assign substationChoices = []>
<#list substationOptions as s>
    <#assign substationChoices = substationChoices + [{
        "value": s.id()?c,
        "label": s.acronym() + s.name()???then(" · " + s.name(), "") + s.active()?then("", " (inactive)")
    }]>
</#list>
<#assign oilChoices = []>
<#list oilTypes as o><#assign oilChoices = oilChoices + [{"value": o.name(), "label": o.name()?capitalize}]></#list>
<#assign statusChoices = []>
<#list statuses as st><#assign statusChoices = statusChoices + [{"value": st.name(), "label": st.name()?capitalize}]></#list>
<#assign criticalityChoices = []>
<#list 1..5 as c><#assign criticalityChoices = criticalityChoices + [{"value": c?c, "label": c?c}]></#list>

<@layout.page title=editing?then("Edit transformer", "New transformer") active="transformers">
    <form action="<@spring.url action/>" method="post" class="d-flex flex-column gap-4" novalidate>
        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Identification</h2></div>
            <div class="card-body row g-3">
                <@f.input path="form.tag" label="Tag" required=true attrs='maxlength="50" autocapitalize="characters"'/>
                <@f.input path="form.serialNumber" label="Serial number" required=true attrs='maxlength="255"'/>
                <@f.select path="form.substationId" label="Substation" options=substationChoices required=true
                           placeholder="Select a substation"/>
                <@f.input path="form.manufacturer" label="Manufacturer" attrs='maxlength="255"'/>
                <@f.input path="form.manufactureYear" label="Manufacture year" type="number"
                          attrs='min="1900" max="2100" step="1"'/>
                <@f.input path="form.commissioningDate" label="Commissioning date" type="date"/>
                <@f.select path="form.status" label="Status" options=statusChoices required=true/>
                <@f.select path="form.criticality" label="Criticality (1 low – 5 high)" options=criticalityChoices
                           required=true/>
            </div>
        </section>

        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Electrical data</h2></div>
            <div class="card-body row g-3">
                <@f.input path="form.ratedPowerMva" label="Rated power (MVA)" type="number" attrs='step="0.01" min="0"'/>
                <@f.input path="form.primaryVoltageKv" label="Primary voltage (kV)" type="number" attrs='step="0.01" min="0"'/>
                <@f.input path="form.secondaryVoltageKv" label="Secondary voltage (kV)" type="number"
                          attrs='step="0.01" min="0"'/>
                <@f.select path="form.oilType" label="Oil type" options=oilChoices required=true/>
            </div>
        </section>

        <@f.actions cancelUrl=cancelUrl/>
    </form>
</@layout.page>
