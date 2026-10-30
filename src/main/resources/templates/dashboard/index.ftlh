<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#assign badge = {"OK": "success", "ALERT": "warning", "DANGER": "danger"}>

<#macro card title value color health>
    <div class="col-md-4">
        <div class="card text-bg-${color} h-100">
            <div class="card-body">
                <h3 class="card-title h6 text-uppercase mb-1">
                    <a href="<@spring.url '/transformers?health=${health}'/>"
                       class="stretched-link text-reset text-decoration-none">${title}</a>
                </h3>
                <p class="display-6 mb-0">${value}</p>
            </div>
        </div>
    </div>
</#macro>

<#macro transformerCard t>
    <div class="col-md-4">
        <div class="card h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <div>
                        <h4 class="h6 mb-0">
                            <a href="<@spring.url '/transformers/${t.id()?c}'/>"
                               class="stretched-link text-reset text-decoration-none">${t.tag()}</a>
                        </h4>
                        <span class="small text-muted">${t.substation()}</span>
                    </div>
                    <#if t.healthStatus()??>
                        <#assign hs = t.healthStatus().name()>
                        <span class="badge text-bg-${badge[hs]}">${hs}</span>
                    <#else>
                        <span class="badge text-bg-secondary">No data</span>
                    </#if>
                </div>
                <dl class="row small mb-0">
                    <dt class="col-5 fw-normal text-muted">Fault</dt>
                    <dd class="col-7 mb-1"><#if t.finalFault()??>${t.finalFault().label}<#else>—</#if></dd>
                    <dt class="col-5 fw-normal text-muted">Last sample</dt>
                    <dd class="col-7 mb-1">${t.lastSampleDate()!'—'}</dd>
                    <dt class="col-5 fw-normal text-muted">Criticality</dt>
                    <dd class="col-7 mb-1">${t.criticality()}</dd>
                    <dt class="col-5 fw-normal text-muted">Rated power</dt>
                    <dd class="col-7 mb-0"><#if t.ratedPowerMva()??>${t.ratedPowerMva()?string["0.##"]} MVA<#else>—</#if></dd>
                </dl>
            </div>
        </div>
    </div>
</#macro>

<@layout.page title="Dashboard" active="dashboard">
    <section class="card mb-4" aria-labelledby="transformers-overview-title">
        <div class="card-header d-flex align-items-center gap-2">
            <i class="bi bi-lightning-charge" aria-hidden="true"></i>
            <h2 id="transformers-overview-title" class="h6 mb-0">Transformers</h2>
        </div>
        <div class="card-body">
            <div class="row g-3">
                <@card "Healthy" summary.ok() "success" "OK"/>
                <@card "Alert" summary.alert() "warning" "ALERT"/>
                <@card "Danger" summary.danger() "danger" "DANGER"/>
            </div>

            <#assign slides = highlights?chunk(3)>
            <div class="d-flex justify-content-between align-items-center mt-4 mb-3">
                <h3 class="h6 text-muted text-uppercase small mb-0">Most important</h3>
                <#if slides?size gt 1>
                    <div class="btn-group btn-group-sm">
                        <button type="button" class="btn btn-outline-secondary" data-bs-target="#transformer-highlights"
                                data-bs-slide="prev" aria-label="Previous transformers">
                            <i class="bi bi-chevron-left" aria-hidden="true"></i>
                        </button>
                        <button type="button" class="btn btn-outline-secondary" data-bs-target="#transformer-highlights"
                                data-bs-slide="next" aria-label="Next transformers">
                            <i class="bi bi-chevron-right" aria-hidden="true"></i>
                        </button>
                    </div>
                </#if>
            </div>
            <#if highlights?has_content>
                <div id="transformer-highlights" class="carousel slide transformer-highlights">
                    <div class="carousel-inner">
                        <#list slides as slide>
                            <div class="carousel-item<#if slide?is_first> active</#if>">
                                <div class="row g-3">
                                    <#list slide as t><@transformerCard t/></#list>
                                </div>
                            </div>
                        </#list>
                    </div>
                    <#if slides?size gt 1>
                        <div class="carousel-indicators">
                            <#list slides as slide>
                                <button type="button" data-bs-target="#transformer-highlights"
                                        data-bs-slide-to="${slide?index}"<#if slide?is_first> class="active" aria-current="true"</#if>
                                        aria-label="Slide ${slide?counter} of ${slides?size}"></button>
                            </#list>
                        </div>
                    </#if>
                </div>
            <#else>
                <p class="text-muted mb-0">No transformers registered.</p>
            </#if>
        </div>
    </section>

    <div class="row g-4">
        <div class="col-lg-6">
            <section class="card h-100" aria-labelledby="substations-title">
                <div class="card-header d-flex align-items-center gap-2">
                    <i class="bi bi-building" aria-hidden="true"></i>
                    <h2 id="substations-title" class="h6 mb-0">Substations</h2>
                </div>
                <#if substations?has_content>
                    <ul class="list-group list-group-flush">
                        <#list substations as s>
                            <li class="list-group-item position-relative">
                                <div class="d-flex justify-content-between align-items-center gap-2">
                                    <div>
                                        <a href="<@spring.url '/substations/${s.id()?c}'/>"
                                           class="stretched-link text-reset text-decoration-none fw-semibold">${s.acronym()}</a>
                                        <#if s.name()??><span class="text-muted">· ${s.name()}</span></#if>
                                        <#if s.city()??>
                                            <div class="small text-muted">${s.city()}<#if s.state()??> / ${s.state()}</#if></div>
                                        </#if>
                                    </div>
                                    <div class="d-flex flex-wrap justify-content-end gap-1">
                                        <#if s.dangerCount() gt 0>
                                            <span class="badge rounded-pill bg-danger-subtle text-danger-emphasis border border-danger-subtle text-nowrap"
                                                  title="Needs attention">
                                                <i class="bi bi-exclamation-circle" aria-hidden="true"></i>
                                                ${s.dangerCount()} in danger
                                            </span>
                                        </#if>
                                        <span class="badge rounded-pill text-bg-light border text-nowrap">
                                            ${s.transformerCount()} transformer<#if s.transformerCount() != 1>s</#if>
                                        </span>
                                    </div>
                                </div>
                            </li>
                        </#list>
                    </ul>
                <#else>
                    <p class="card-body text-muted mb-0">No substations registered.</p>
                </#if>
            </section>
        </div>

        <div class="col-lg-6">
            <section class="card h-100" aria-labelledby="latest-analyses-title">
                <div class="card-header d-flex align-items-center gap-2">
                    <i class="bi bi-clipboard-data" aria-hidden="true"></i>
                    <h2 id="latest-analyses-title" class="h6 mb-0">Latest analyses</h2>
                </div>
                <#if latestAnalyses?has_content>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead>
                            <tr><th>Date</th><th>Transformer</th><th>Fault</th><th>Health</th></tr>
                            </thead>
                            <tbody>
                            <#list latestAnalyses as a>
                                <tr data-href="<@spring.url '/analyses/${a.id()?c}'/>">
                                    <td class="text-nowrap"><a href="<@spring.url '/analyses/${a.id()?c}'/>">${a.sampleDate()}</a></td>
                                    <td><a href="<@spring.url '/transformers/${a.transformerId()?c}'/>">${a.transformerTag()}</a></td>
                                    <td><#if a.finalFault()??>${a.finalFault().label}<#else>—</#if></td>
                                    <td>
                                        <#if a.healthStatus()??>
                                            <#assign hs = a.healthStatus().name()>
                                            <span class="badge text-bg-${badge[hs]}">${hs}</span>
                                        <#else>
                                            <span class="text-muted">Pending</span>
                                        </#if>
                                    </td>
                                </tr>
                            </#list>
                            </tbody>
                        </table>
                    </div>
                <#else>
                    <p class="card-body text-muted mb-0">No analyses registered yet.</p>
                </#if>
            </section>
        </div>
    </div>

</@layout.page>