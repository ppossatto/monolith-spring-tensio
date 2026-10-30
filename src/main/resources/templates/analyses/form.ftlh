<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>
<#import "../_form.ftlh" as f>

<#assign choices = []>
<#list transformerChoices as t>
    <#assign choices = choices + [{"value": t.id()?c, "label": t.tag() + " · " + t.substation()}]>
</#list>

<#macro gas path label required=true>
    <@f.input path=path label=label + " (ppm)" type="number" col="col-sm-6 col-lg-3" required=required
              attrs='step="0.01" min="0"'/>
</#macro>

<@layout.page title="New analysis" active="analyses">
    <form action="<@spring.url '/analyses'/>" method="post" class="d-flex flex-column gap-4" novalidate>
        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Sample</h2></div>
            <div class="card-body row g-3">
                <@f.select path="form.transformerId" label="Transformer" options=choices col="col-md-6" required=true
                           placeholder="Select a transformer"/>
                <@f.input path="form.sampleDate" label="Sample date" type="date" required=true/>
            </div>
        </section>

        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Dissolved gases</h2></div>
            <div class="card-body row g-3">
                <@gas path="form.h2" label="H₂"/>
                <@gas path="form.ch4" label="CH₄"/>
                <@gas path="form.c2h2" label="C₂H₂"/>
                <@gas path="form.c2h4" label="C₂H₄"/>
                <@gas path="form.c2h6" label="C₂H₆"/>
                <@gas path="form.co" label="CO"/>
                <@gas path="form.co2" label="CO₂"/>
                <@gas path="form.o2" label="O₂" required=false/>
                <@gas path="form.n2" label="N₂" required=false/>
            </div>
            <div class="card-footer small text-muted">
                The diagnosis (Duval, IEC, Rogers and Doernenburg methods, weighted vote) is computed and stored
                when the analysis is saved, and a summary is sent to your e-mail.
            </div>
        </section>

        <@f.actions cancelUrl="/analyses"/>
    </form>
</@layout.page>
