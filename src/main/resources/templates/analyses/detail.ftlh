<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#assign badge = {"OK": "success", "ALERT": "warning", "DANGER": "danger"}>
<#assign a = analysis>
<#assign t = transformer>

<#macro field id label value="" col="col-md-4">
    <div class="${col}">
        <label for="${id}" class="form-label small text-muted mb-1">${label}</label>
        <input id="${id}" class="form-control" value="${value}" readonly>
    </div>
</#macro>

<@layout.page title="Analysis ${t.tag()} · ${a.sampleDate()}" active="analyses">
    <a href="<@spring.url '/analyses'/>" class="d-inline-flex align-items-center gap-1 mb-3 small">
        <i class="bi bi-arrow-left" aria-hidden="true"></i> Back to analyses
    </a>

    <form class="d-flex flex-column gap-4" aria-label="Analysis of ${t.tag()} on ${a.sampleDate()}">
        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Sample</h2></div>
            <div class="card-body row g-3">
                <@field id="sample-date" label="Sample date" value=a.sampleDate()/>
                <div class="col-md-4">
                    <label for="transformer" class="form-label small text-muted mb-1">Transformer</label>
                    <div class="input-group">
                        <input id="transformer" class="form-control" value="${t.tag()}" readonly>
                        <a class="btn btn-outline-secondary" href="<@spring.url '/transformers/${t.id()?c}'/>"
                           aria-label="Open transformer ${t.tag()}"><i class="bi bi-box-arrow-up-right" aria-hidden="true"></i></a>
                    </div>
                </div>
                <@field id="substation" label="Substation" value=t.substation().acronym()/>
            </div>
        </section>

        <section class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h2 class="h6 mb-0">Diagnosis</h2>
                <#if diagnosis??>
                    <#assign hs = diagnosis.healthStatus().name()>
                    <span class="badge text-bg-${badge[hs]}">${hs}</span>
                <#else>
                    <span class="text-muted">—</span>
                </#if>
            </div>
            <div class="card-body row g-3">
                <#if diagnosis??>
                    <@field id="final-fault" label="Final fault (weighted vote)" value=diagnosis.finalFault()???then(diagnosis.finalFault().label, "Not applicable") col="col-12"/>
                    <@field id="duval" label="Duval triangle (weight 4)" value=diagnosis.duvalFault()???then(diagnosis.duvalFault().label, "Not applicable") col="col-md-6"/>
                    <@field id="iec" label="IEC ratios (weight 3)" value=diagnosis.iecFault()???then(diagnosis.iecFault().label, "Not applicable") col="col-md-6"/>
                    <@field id="rogers" label="Rogers ratios (weight 2)" value=diagnosis.rogersFault()???then(diagnosis.rogersFault().label, "Not applicable") col="col-md-6"/>
                    <@field id="doernenburg" label="Doernenburg ratios (weight 1)" value=diagnosis.doernenburgFault()???then(diagnosis.doernenburgFault().label, "Not applicable") col="col-md-6"/>
                <#else>
                    <#-- Only analyses inserted outside the application, until the startup backfill runs -->
                    <p class="text-muted mb-0">Diagnosis not available.</p>
                </#if>
            </div>
        </section>
    </form>

    <section class="card mt-4" aria-labelledby="gases-title">
        <div class="card-header"><h2 id="gases-title" class="h6 mb-0">Dissolved gases (ppm)</h2></div>
        <div class="table-responsive">
            <table class="table align-middle mb-0">
                <thead>
                <tr>
                    <th>Gas</th><th class="text-end">Value</th><th class="text-end">Alert limit</th>
                    <th class="text-end">Danger limit</th><th>Level</th>
                </tr>
                </thead>
                <tbody>
                <#list readings as r>
                    <tr>
                        <td>${r.gas().label}<#if !r.gas().combustible> <span class="small text-muted">(non-combustible)</span></#if></td>
                        <td class="text-end">${r.value()???then(r.value()?string["0.##"], "—")}</td>
                        <td class="text-end">${r.alert()???then(r.alert()?string["0.##"], "—")}</td>
                        <td class="text-end">${r.danger()???then(r.danger()?string["0.##"], "—")}</td>
                        <td>
                            <#if r.value()?? && r.alert()??>
                                <#assign lv = r.level().name()>
                                <span class="badge text-bg-${badge[lv]}">${lv}</span>
                            <#else>
                                <span class="text-muted">—</span>
                            </#if>
                        </td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </section>
</@layout.page>
