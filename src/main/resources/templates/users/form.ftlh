<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>
<#import "../_form.ftlh" as f>

<#-- Model: form (UserForm), user (UserAccount being edited), roles -->
<#assign u = user>
<#assign roleOptions = []>
<#list roles as r>
    <#assign roleOptions = roleOptions + [{"value": r.name(), "label": r.name()?capitalize}]>
</#list>
<#assign self = (userLogged!"")?lower_case == u.username()?lower_case>

<@layout.page title="Edit user" active="users">
    <form action="<@spring.url '/users/${u.id()?c}'/>" method="post" class="d-flex flex-column gap-4" novalidate>
        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Access</h2></div>
            <div class="card-body row g-3">
                <div class="col-md-6">
                    <label for="username" class="form-label">E-mail</label>
                    <input id="username" class="form-control" value="${u.username()}" readonly>
                </div>
                <@f.select path="form.role" label="Role" options=roleOptions col="col-md-3" required=true/>
                <@f.checkbox path="form.active" label="Active" col="col-md-3"/>
            </div>
            <div class="card-footer small text-muted">
                <#if self>
                    This is your account: you cannot remove your own administrator role or deactivate it.
                <#else>
                    Changes apply at the user's next sign-in. Inactive users cannot sign in.
                </#if>
            </div>
        </section>

        <@f.actions cancelUrl="/users/${u.id()?c}"/>
    </form>
</@layout.page>
