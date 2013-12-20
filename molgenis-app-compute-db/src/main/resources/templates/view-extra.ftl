<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["base.css", "Spacetree.css", "RGraph.css"]> <!-- "ForceDirected.css", -->
<#assign js=["molgenis_jit.js", "workflow.js"]>
<@header css js/>
<body onload="init_new();">
<div class="row-fluid" align="center">
    <!--${app_extra_html}-->
    <h2>
        Workflow view
    </h2>

    <div id="container">
        <div id="left-container">
        </div>
        <div id="center-container">
            <div id="infovis"></div>
        </div>
        <div id="right-container">
            <br>
            <table>
                <tr>
                    <td>
                        CheckBox:
                    </td>
                    <td>
                        <input type="checkbox" id="checkbox1" checked="checked"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <input type="button" id="operation1" value="Clear" onclick="remove_subtree();"/>
                    </td>
                    <td>
                        <input type="button" value="Refresh" onclick="window.location = window.location"/>
                    </td>
                </tr>
                <!--
                <tr>
                    <td>
                        <input type="button" id="operation1" value="Clear" onclick="remove_subtree_clean_canvas();"/>
                    </td>
                    <td>
                        <input type="button" value="Transform" onclick="transform();"/>
                    </td>
                </tr>
                -->
            </table>
            <div id="inner-details">

            </div>
        </div>
    </div>
</div>
<@footer/>