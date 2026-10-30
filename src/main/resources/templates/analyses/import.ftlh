<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<@layout.page title="Import analyses" active="analyses">
    <a href="<@spring.url '/analyses'/>" class="d-inline-flex align-items-center gap-1 mb-3 small">
        <i class="bi bi-arrow-left" aria-hidden="true"></i> Back to analyses
    </a>

    <div class="row g-4">
        <div class="col-lg-5">
            <section class="card h-100">
                <div class="card-header"><h2 class="h6 mb-0">Upload a CSV file</h2></div>
                <div class="card-body">
                    <#if error??>
                        <div class="alert alert-danger" role="alert">${error}</div>
                    </#if>
                    <#-- Multipart body: the CSRF token also goes in the URL, as Spring Security reads it before the
                         multipart request is parsed -->
                    <form action="<@spring.url '/analyses/import?${_csrf.parameterName}=${_csrf.token}'/>" method="post"
                          enctype="multipart/form-data" class="d-flex flex-column gap-3">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <div>
                            <label for="file" class="form-label">CSV file (max. 10 MB)</label>
                            <input type="file" id="file" name="file" accept=".csv,text/csv" class="form-control" required>
                        </div>
                        <div class="d-flex justify-content-between align-items-center">
                            <a href="<@spring.url '/files/analyses-template.csv'/>" download class="small">
                                <i class="bi bi-download" aria-hidden="true"></i> Download a template
                            </a>
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-upload" aria-hidden="true"></i> Import
                            </button>
                        </div>
                    </form>
                </div>
                <div class="card-footer small text-muted">
                    The import runs in the background. Every valid line is stored with its computed diagnosis;
                    invalid lines are skipped. The summary, with the rejected lines, is sent to your e-mail.
                </div>
            </section>
        </div>

        <div class="col-lg-7">
            <section class="card h-100">
                <div class="card-header"><h2 class="h6 mb-0">Format</h2></div>
                <div class="card-body small">
                    <ul class="mb-3">
                        <li>First line: column names, in any order.</li>
                        <li>Required: <#list requiredColumns as c><code>${c}</code><#sep>, </#list>.</li>
                        <li>Optional: <#list optionalColumns as c><code>${c}</code><#sep>, </#list>.</li>
                        <li>Separator <code>,</code> or <code>;</code> (with <code>;</code>, a decimal comma is accepted).</li>
                        <li>Dates as <code>yyyy-MM-dd</code> or <code>dd/MM/yyyy</code>; gases in ppm.</li>
                        <li>One analysis per transformer and date; lines for unknown or decommissioned transformers,
                            or dates that already have an analysis, are rejected.</li>
                        <li>Order the lines by date: generation rates use the previous stored analysis.</li>
                    </ul>
                    <pre class="bg-body-tertiary border rounded p-2 mb-0"><code>transformer_tag,sample_date,h2,ch4,c2h2,c2h4,c2h6,co,co2,o2,n2
TR-101,2026-10-01,15.2,8.1,0,4.3,7.9,210,1850,3100,45200
TR-102,2026-10-01,380,150,290,210,22,420,3100,,</code></pre>
                </div>
            </section>
        </div>
    </div>
</@layout.page>
