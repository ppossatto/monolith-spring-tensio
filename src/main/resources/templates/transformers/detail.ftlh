<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#assign badge = {"OK": "success", "ALERT": "warning", "DANGER": "danger"}>
<#assign t = transformer>
<#assign s = t.substation()>

<#macro field id label value="" col="col-md-4">
    <div class="${col}">
        <label for="${id}" class="form-label small text-muted mb-1">${label}</label>
        <input id="${id}" class="form-control" value="${value}" readonly>
    </div>
</#macro>

<#macro gasChart id title chart>
    <div class="col-md-6">
        <h3 id="${id}-title" class="h6 text-muted text-uppercase small mb-2">${title}</h3>
        <canvas data-gas-chart aria-labelledby="${id}-title" role="img"
                data-labels="<#list chart.labels() as l>${l}<#sep>,</#list>"
                data-latest="<#list chart.latest() as v><#if v??>${v?c}<#else>null</#if><#sep>,</#list>"
                data-average="<#list chart.average() as v><#if v??>${v?c}<#else>null</#if><#sep>,</#list>">
        </canvas>
    </div>
</#macro>

<#assign finalFault = (diagnosis?? && diagnosis.finalFault()??)?then(diagnosis.finalFault().label, "")>
<#assign lastSample = diagnosis???then(diagnosis.sampleDate(), "")>
<#assign year = t.manufactureYear()???then(t.manufactureYear()?c, "")>
<#assign ratedPower = t.ratedPowerMva()???then(t.ratedPowerMva()?string["0.##"] + " MVA", "")>
<#assign primaryVoltage = t.primaryVoltageKv()???then(t.primaryVoltageKv()?string["0.##"] + " kV", "")>
<#assign secondaryVoltage = t.secondaryVoltageKv()???then(t.secondaryVoltageKv()?string["0.##"] + " kV", "")>
<#assign location = (s.city()!"") + s.state()???then(" / " + s.state(), "")>

<@layout.page title="Transformer ${t.tag()}" active="transformers">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <a href="<@spring.url '/transformers'/>" class="d-inline-flex align-items-center gap-1 small">
            <i class="bi bi-arrow-left" aria-hidden="true"></i> Back to transformers
        </a>
        <a href="<@spring.url '/transformers/${t.id()?c}/pdf'/>" class="btn btn-sm btn-outline-secondary"
           download>
            <i class="bi bi-file-earmark-pdf" aria-hidden="true"></i> Export PDF
        </a>
    </div>

    <form class="d-flex flex-column gap-4" aria-label="Transformer ${t.tag()}">
        <section class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h2 class="h6 mb-0">Current condition</h2>
                <#if diagnosis??>
                    <#assign hs = diagnosis.healthStatus().name()>
                    <span class="badge text-bg-${badge[hs]}">${hs}</span>
                <#else>
                    <span class="badge text-bg-secondary">No data</span>
                </#if>
            </div>
            <div class="card-body row g-3">
                <@field id="final-fault" label="Final fault" value=finalFault col="col-md-8"/>
                <@field id="last-sample" label="Last sample" value=lastSample/>
            </div>
        </section>

        <section class="card" aria-labelledby="gas-profile-title">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h2 id="gas-profile-title" class="h6 mb-0">Gas profile</h2>
                <#if gasProfile.hasData()>
                    <span class="small text-muted">Latest sample: ${gasProfile.latestSampleDate()}</span>
                </#if>
            </div>
            <div class="card-body">
                <#if gasProfile.hasData()>
                    <div class="row g-4">
                        <@gasChart id="combustible-gases" title="Combustible gases" chart=gasProfile.combustible()/>
                        <@gasChart id="non-combustible-gases" title="Non-combustible gases" chart=gasProfile.nonCombustible()/>
                    </div>
                    <p class="small text-muted mt-3 mb-0">
                        Values as % of each gas reference · latest analysis vs 12-month average.
                    </p>
                <#else>
                    <p class="text-muted mb-0">No analyses registered yet.</p>
                </#if>
            </div>
        </section>

        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Identification</h2></div>
            <div class="card-body row g-3">
                <@field id="tag" label="Tag" value=t.tag()/>
                <@field id="serial-number" label="Serial number" value=t.serialNumber()/>
                <@field id="status" label="Status" value=t.status().name()/>
                <@field id="manufacturer" label="Manufacturer" value=t.manufacturer()!""/>
                <@field id="manufacture-year" label="Manufacture year" value=year/>
                <@field id="commissioning-date" label="Commissioning date" value=t.commissioningDate()!""/>
                <@field id="criticality" label="Criticality" value=t.criticality()?c/>
            </div>
        </section>

        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Electrical data</h2></div>
            <div class="card-body row g-3">
                <@field id="rated-power" label="Rated power" value=ratedPower/>
                <@field id="primary-voltage" label="Primary voltage" value=primaryVoltage/>
                <@field id="secondary-voltage" label="Secondary voltage" value=secondaryVoltage/>
                <@field id="oil-type" label="Oil type" value=t.oilType().name()/>
            </div>
        </section>

        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Substation</h2></div>
            <div class="card-body row g-3">
                <@field id="substation-acronym" label="Acronym" value=s.acronym()/>
                <@field id="substation-name" label="Name" value=s.name()!""/>
                <@field id="substation-location" label="City / State" value=location/>
            </div>
        </section>
    </form>
</@layout.page>
