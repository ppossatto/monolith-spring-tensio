<#import "/spring.ftl" as spring>
<#import "/_pagination.ftlh" as pagination>
<#assign filterQuery = "">
<#if role??><#assign filterQuery = filterQuery + "&role=" + role.name()></#if>
<#if q?has_content><#assign filterQuery = filterQuery + "&q=" + q?url("UTF-8")></#if>

<div id="users">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
            <tr><th>E-mail</th><th>Role</th><th>Status</th><th>Created</th></tr>
            </thead>
            <tbody>
            <#list users.content as u>
                <tr data-href="<@spring.url '/users/${u.id()?c}'/>">
                    <td><a href="<@spring.url '/users/${u.id()?c}'/>">${u.username()}</a></td>
                    <td>
                        <#if u.role().name() == "ADMIN">
                            <span class="badge text-bg-primary">ADMIN</span>
                        <#else>
                            <span class="badge text-bg-light border">USER</span>
                        </#if>
                    </td>
                    <td>
                        <#if u.active()>
                            <span class="badge text-bg-light border">Active</span>
                        <#else>
                            <span class="badge text-bg-secondary">Inactive</span>
                        </#if>
                    </td>
                    <td>${u.createdAt()!'—'}</td>
                </tr>
            <#else>
                <tr><td colspan="4" class="text-center text-muted">No users found.</td></tr>
            </#list>
            </tbody>
        </table>
    </div>

    <@pagination.pager page=users path="/users" query=filterQuery target="users"/>
</div>
