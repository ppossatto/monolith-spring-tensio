<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#assign badge = {"OK": "success", "ALERT": "warning", "DANGER": "danger"}>
<#assign s = substation>

<#macro field id label value="" col="col-md-4">
    <div class="${col}">
        <label for="${id}" class="form-label small text-muted mb-1">${label}</label>
        <input id="${id}" class="form-control" value="${value}" readonly>
    </div>
</#macro>

<#assign latitude = s.latitude()???then(s.latitude()?string["0.0000000"], "")>
<#assign longitude = s.longitude()???then(s.longitude()?string["0.0000000"], "")>

<@layout.page title="Substation ${s.acronym()}" active="substations">
    <a href="<@spring.url '/substations'/>" class="d-inline-flex align-items-center gap-1 mb-3 small">
        <i class="bi bi-arrow-left" aria-hidden="true"></i> Back to substations
    </a>

    <form class="d-flex flex-column gap-4" aria-label="Substation ${s.acronym()}">
        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Identification</h2></div>
            <div class="card-body row g-3">
                <@field id="acronym" label="Acronym" value=s.acronym()/>
                <@field id="name" label="Name" value=s.name()!""/>
                <@field id="status" label="Status" value=s.active()?then("Active", "Inactive")/>
            </div>
        </section>

        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Location</h2></div>
            <div class="card-body row g-3">
                <@field id="city" label="City" value=s.city()!"" col="col-md-6"/>
                <@field id="state" label="State" value=s.state()!"" col="col-md-6"/>
                <@field id="latitude" label="Latitude" value=latitude col="col-md-6"/>
                <@field id="longitude" label="Longitude" value=longitude col="col-md-6"/>
            </div>
        </section>
    </form>

    <section class="card mt-4" aria-labelledby="substation-transformers-title">
        <div class="card-header d-flex align-items-center gap-2">
            <i class="bi bi-lightning-charge" aria-hidden="true"></i>
            <h2 id="substation-transformers-title" class="h6 mb-0">Transformers</h2>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead>
                <tr>
                    <th>Tag</th><th class="text-end">MVA</th><th class="text-center">Criticality</th>
                    <th>Health</th><th>Fault</th><th>Last sample</th>
                </tr>
                </thead>
                <tbody>
                <#list transformers as t>
                    <tr>
                        <td><a href="<@spring.url '/transformers/${t.id()?c}'/>">${t.tag()}</a></td>
                        <td class="text-end">${(t.ratedPowerMva()?string["0.##"])!'—'}</td>
                        <td class="text-center">${t.criticality()}</td>
                        <td>
                            <#if t.healthStatus()??>
                                <#assign hs = t.healthStatus().name()>
                                <span class="badge text-bg-${badge[hs]}">${hs}</span>
                            <#else>
                                <span class="text-muted">No data</span>
                            </#if>
                        </td>
                        <td><#if t.finalFault()??>${t.finalFault().label}<#else>—</#if></td>
                        <td>${t.lastSampleDate()!'—'}</td>
                    </tr>
                <#else>
                    <tr><td colspan="6" class="text-center text-muted">No transformers in this substation.</td></tr>
                </#list>
                </tbody>
            </table>
        </div>
    </section>
</@layout.page>
