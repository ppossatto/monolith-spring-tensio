<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>
<#import "../_form.ftlh" as f>

<#assign editing = substationId??>
<#assign action = editing?then("/substations/" + substationId?c, "/substations")>
<#assign cancelUrl = editing?then("/substations/" + substationId?c, "/substations")>

<@layout.page title=editing?then("Edit substation", "New substation") active="substations">
    <form action="<@spring.url action/>" method="post" class="d-flex flex-column gap-4" novalidate>
        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Identification</h2></div>
            <div class="card-body row g-3">
                <@f.input path="form.acronym" label="Acronym" required=true attrs='maxlength="10" autocapitalize="characters"'/>
                <@f.input path="form.name" label="Name" col="col-md-6" attrs='maxlength="255"'/>
                <@f.checkbox path="form.active" label="Active" col="col-md-2"/>
            </div>
        </section>

        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Location</h2></div>
            <div class="card-body row g-3">
                <@f.input path="form.city" label="City" col="col-md-6" attrs='maxlength="255"'/>
                <@f.input path="form.state" label="State" col="col-md-2" attrs='maxlength="2" autocapitalize="characters"'/>
                <div class="col-md-4"></div>
                <@f.input path="form.latitude" label="Latitude" type="number" col="col-md-6"
                          attrs='step="0.0000001" min="-90" max="90"'/>
                <@f.input path="form.longitude" label="Longitude" type="number" col="col-md-6"
                          attrs='step="0.0000001" min="-180" max="180"'/>
            </div>
        </section>

        <@f.actions cancelUrl=cancelUrl/>
    </form>
</@layout.page>
