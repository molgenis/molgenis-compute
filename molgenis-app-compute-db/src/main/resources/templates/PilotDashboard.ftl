<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["pilot-dashboard.css"]>
<#assign js=[]>
<@header css js/>


<script type="text/javascript">
    function updateRunStatus(run) {
        $.ajax({
            type: 'GET',
            url: '/plugin/dashboard/status?run=' + run,
            contentType: 'application/json',
            async: true,
            success: function (response) {
                console.log(JSON.stringify(response));
                updateHostStatusTable(run, response);
            }

        });
    }

    function updateHostStatusTable(run, response) {
        $('#' + run + ' td.generated').html(response.generated);
        $('#' + run + ' td.ready').html(response.ready);
        $('#' + run + ' td.running').html(response.running);
        $('#' + run + ' td.failed').html(response.failed);
        $('#' + run + ' td.done').html(response.done);
        $('#' + run + ' td.jobsubmitted').html(response.jobsubmitted);
        $('#' + run + ' td.submitted').html(response.submitted);
        $('#' + run + ' td.started').html(response.started);
        $('#' + run + ' td.cancelled').html(response.cancelled);

        if (response.failed > 0) {
            $('#resubmitFailedTasksForm_' + run).show();
        } else {
            $('#resubmitFailedTasksForm_' + run).hide();
        }
    }

    function updateStatus() {
        console.log('updateStatus');
    <#list runs as run>
        updateRunStatus('${run.name}');
    </#list>
        setTimeout(function () {
            updateStatus()
        }, 5000);
    }

    // on document ready
    $(function () {
        updateStatus();
    });

</script>

<#if error??>
<div class="alert alert-error">
    <button type="button" class="close" data-dismiss="alert">&times;</button>
${error}
</div>
</#if>

<#if message??>
<div class="alert alert-success">
    <button type="button" class="close" data-dismiss="alert">&times;</button>
${message}
</div>
</#if>

<#if runs?size == 0>
<h4>No Runs found.</h4>
</#if>

<#list runs as run>

    <#if errormessage??>
        <#if errortask==run.name>
        <div class="alert alert-error">
        ${errormessage}
        </div>
        </#if>
    </#if>

<div class="well well-small">
<div class="row">
<div class="col-md-5">
    <div class="host-header">
        <span class="host-name">${run.name}@${run.backendUrl}</span>
        <span class="creation-time">(${run.creationTime?string("yyyy-MM-dd HH:mm:ss")})</span>
    </div>
    <#if (run.complete && run.hasFailed)>
        <div class="text-danger">Failed</div>
        <#if run.hasFailed>
            <form id="resubmitFailedTasksForm_${run.name}" action="/menu/Compute/dashboard/resubmit" class="form-inline"
                  method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn resubmit-btn">Resubmit failed</button>
            </form>
        </#if>
        <#if run.backendType == "CLOUD">
            <form action="/menu/Compute/dashboard/release" class="form-inline" method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn">Release VMs</button>
            </form>
            <form action="/menu/Compute/dashboard/terminate" class="form-inline" method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn">Terminate VMs</button>
            </form>
        </#if>
    <#elseif run.complete>
        <div class="text-success">Completed</div>
        <#if run.backendType == "CLOUD">
            <form action="/menu/Compute/dashboard/release" class="form-inline" method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn">Release VMs</button>
            </form>
            <form action="/menu/Compute/dashboard/terminate" class="form-inline" method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn">Terminate VMs</button>
            </form>
        </#if>
    <#elseif run.cancelled>
        <div class="text-danger">Cancelled</div>
        <#if run.backendType == "CLOUD">
            <form action="/menu/Compute/dashboard/release" class="form-inline" method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn">Release VMs</button>
            </form>
            <form action="/menu/Compute/dashboard/terminate" class="form-inline" method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn">Terminate VMs</button>
            </form>
        </#if>
        <#if run.backendType == "CLUSTER">
            <div>
                <form action="/menu/Compute/dashboard/resubmit" class="form-inline" method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn btn-default btn-sm">Resubmit failed and cancelled</button>
                </form>
            </div>
        </#if>
    <#elseif run.backendType == "CLOUD">
        <#if run.running>
            <div class="text-success">Running</div>
            <form action="/menu/Compute/dashboard/stop" class="form-inline" method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn">Stop</button>
            </form>
            <#if run.hasFailed>
                <form id="resubmitFailedTasksForm_${run.name}" action="/menu/Compute/dashboard/resubmit"
                      class="form-inline" method="post">
                    <input type="hidden" name="run" value="${run.name}"/>
                    <button type="submit" class="btn resubmit-btn">Resubmit failed</button>
                </form>
            </#if>
            <form action="/menu/Compute/dashboard/release" class="form-inline" method="post" style="margin-top:10px;">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn">Release VMs</button>
            </form>
            <form action="/menu/Compute/dashboard/terminate" class="form-inline" method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn">Terminate VMs</button>
            </form>
        <#else>
            <div class="text-info">Ready to run</div>
            <!--div class="container-fluid">
                <div class="row"-->
                    <form name="cloudsubmit" role="form" action="/menu/Compute/dashboard/start" method="post"
                          class="form-inline">
                        <input type="hidden" name="run" value="${run.name}"/>
                        <!--div class="row">
                         <div class="col-xs-3"-->
                            <input type="text" name="username" id="inputUsername"
                                   class="form-control input-sm" placeholder="Username"/>
                        <!--/div-->
                        <!--div class="form-group form-group-sm"-->
                        <!--div class="col-xs-2"-->
                            <input type="password" name="password" id="inputPassword"
                                   class="form-control input-sm" placeholder="Password"/>
                        <!--/div-->

                        <!--div class="col-xs-2"-->
                            <input type="text" name="Max#VMs" id="inputMaxVM"
                                   class="form-control input-sm" placeholder="Max#VMs" maxlength="4" size="4"/>
                        <!--/div-->
                        <button type="submit" class="btn btn-default btn-sm">Start</button>
                    </form>
                <!--/div>
            </div-->
            <#if run.hasFailed>
                <div>
                    <form action="/menu/Compute/dashboard/resubmit" class="form-inline" method="post">
                        <input type="hidden" name="run" value="${run.name}"/>
                        <button type="submit" class="btn btn-default btn-sm">Resubmit failed</button>
                    </form>
                </div>
            </#if>
            <div class="container-fluid">
                <div class="row">
                    <!--div class="form-group"-->

                    <!--/div>
                    <div class="form-group"-->
                        <!--form action="/menu/Compute/dashboard/terminate" class="form-inline" method="post"
                              style="margin-top:5px;>
                            <input type="hidden" name="run" value=""/>
                            <button type="submit" class="btn btn-default btn-sm">Terminate VMs</button>
                        </form-->
                    <!--/div-->
                </div>
            </div>
        </#if>
    <#elseif run.backendType == "CLUSTER">
        <#if run.running>
        <div class="text-success">Running</div>
        <form action="/menu/Compute/dashboard/stop" class="form-inline" method="post">
            <input type="hidden" name="run" value="${run.name}"/>
            <button type="submit" class="btn">Cancel</button>
        </form>
            <#if run.hasFailed || run.cancelled>
            <form id="resubmitFailedTasksForm_${run.name}" action="/menu/Compute/dashboard/resubmit"
                  class="form-inline" method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn resubmit-btn">Resubmit failed</button>
            </form>
            </#if>
        <#else>
        <div class="text-info">Ready to run</div>
         <form name="cloudsubmit" role="form" action="/menu/Compute/dashboard/start" method="post"
              class="form-inline">
            <input type="hidden" name="run" value="${run.name}"/>
            <input type="text" name="username" id="inputUsername"
                   class="form-control input-sm" placeholder="Username"/>
            <input type="password" name="password" id="inputPassword"
                   class="form-control input-sm" placeholder="Password"/>
            <button type="submit" class="btn btn-default btn-sm">Submit Run</button>
        </form>
            <#if run.hasFailed>
            <div>
                <form action="/menu/Compute/dashboard/resubmit" class="form-inline" method="post">
                    <input type="hidden" name="run" value="${run.name}"/>
                    <button type="submit" class="btn btn-default btn-sm">Resubmit failed</button>
                </form>
            </div>
            </#if>
        </#if>
    <#else>
        <#if run.owned>
            <#if run.running>
                <div class="text-info">Active</div>
                <form action="/menu/Compute/dashboard/inactivate" class="form-inline" method="post">
                    <input type="hidden" name="run" value="${run.name}"/>
                    <button type="submit" class="btn inactivate-btn">Inactivate</button>
                </form>
            <#else>
                <div class="text-danger">Not active</div>
                <form action="/menu/Compute/dashboard/activate" class="form-inline" method="post">
                    <input type="hidden" name="run" value="${run.name}"/>
                    <button type="submit" class="btn activate-btn">Activate</button>
                </form>
            </#if>
            <#if run.running>
                <#if run.submitting>
                    <form action="/menu/Compute/dashboard/stop" class="form-inline" method="post">
                        <input type="hidden" name="run" value="${run.name}"/>
                        <button type="submit" class="btn">Stop Submitting Pilots</button>
                    </form>
                <#else>
                    <form action="/menu/Compute/dashboard/start" class="form-inline" method="post">
                        <input type="hidden" name="run" value="${run.name}"/>
                        <input type="text" name="username" id="inputUsername" placeholder="Username"/>
                        <input type="password" name="password" id="inputPassword" placeholder="Password"/>
                        <button type="submit" class="btn">Submit Pilots</button>
                    </form>
                </#if>
            </#if>
            <form id="resubmitFailedTasksForm_${run.name}" action="/menu/Compute/dashboard/resubmit" class="form-inline"
                  method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn resubmit-btn">Resubmit failed</button>
            </form>
            <form id="cancel_${run.name}" action="/menu/Compute/dashboard/cancel" class="form-inline"
                  method="post">
                <input type="hidden" name="run" value="${run.name}"/>
                <button type="submit" class="btn cancel-btn">Cancel Run</button>
            </form>
        <#else>
            <div class="text-info">${run.owner}</div>
        </#if>
    </#if>
</div>
<div class="col-md-5 status">
    <div class="status-table">
        <table id="${run.name}" class="table table-condensed table-hover">
            <tr>
                <td>Jobs generated</td>
                <td class="generated"></td>
                <td class="text-success">Jobs done</td>
                <td class="done text-success"></td>
            </tr>
            <tr>
                <td class="text-info">Jobs ready</td>
                <td class="ready text-warning"></td>
                <td class="text-danger">Jobs failed</td>
                <td class="failed text-danger"></td>
            </tr>
            <tr>
                <#if run.backendType == "GRID">
                    <td class="text-warning">Jobs running</td>
                    <td class="running text-info"></td>
                <#else>
                    <td class="text-warning">Jobs submitted</td>
                    <td class="jobsubmitted text-warning"></td>
                </#if>
                <td class="text-danger">Jobs cancelled</td>
                <td class="cancelled text-danger"></td>
            </tr>
            <#if run.backendType == "GRID">
                <tr>
                    <td class="text-info">Pilots submitted</td>
                    <td class="submitted text-info"></td>
                    <td class="text-info">Pilots started</td>
                    <td class="started text-info"></td>
                </tr>
            <#else>
                <td class="text-info">Job running</td>
                <td class="running text-info"></td>
                <td></td>
                <td></td>
            </#if>
        </table>
    </div>
</div>
<div class="col-md-1"></div>
<div class="col-md-offset-1 col-md-1">
    <#if run.owned>
        <a href="/menu/Compute/dashboard/close?run=${run.name}" title="Remove from dashboard"
           class="close">&times;</a>
    </#if>
</div>
</div>
</div>
</#list>
<div>
    <form action="/menu/Compute/dashboard/enableLightpath" class="form-inline" method="post">
        <input type="hidden" name="enableLightpath" value="start"/>
        <button type="submit" class="btn">Enable Lightpath</button>
    </form>
</div>
<@footer/>