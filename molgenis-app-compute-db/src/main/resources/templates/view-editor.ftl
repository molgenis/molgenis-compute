<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["base.css", "codemirror.css"]>
<#assign js=["testpost.js", "codemirror.js", "shell.js"]>
<@header css js/>
<style type="text/css" media="screen">
    #editor {
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
    }
</style>

<div class="row-fluid" align="center">
    <h2>
        Editor view
    </h2>

    <div id="container">
        <div id="left-container">
        </div>
        <div id="center-container" align="left">
            <!--div id="editor-container" align="left"></div-->
            <textarea id="code" align="left">
            </textarea>
        </div>
        <div id="right-container">
            <br>
            <table>
                <tr>
                    <td>
                        <input type="button" id="save" value="Save It!" onclick="test_post();"/>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</div>

<script>
        var editor = CodeMirror.fromTextArea(document.getElementById('code'), {
        mode: 'shell',
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true
    });

        $.ajax({
            type : 'GET',
            dataType : 'json',
            url : '/plugin/editor/test',
            contentType : 'text/plain',
            success : function(data)
            {
                editor.doc.setValue(data);
                //$('#code').html(data);
                console.log("goodin");
            },
            error : function(data)
            {
                console.log("badin");
            }

        });

</script>

<!--script src="/js/ace-builds/src/ace.js" type="text/javascript" charset="utf-8"></script>
<--script src="/js/ace-builds/kitchen-sink/demo.js" type="text/javascript" charset="utf-8"></script>
<script type="text/javascript" charset="utf-8">
    require("kitchen-sink/demo");
</script>
<script>
    var editor = ace.edit("editor-container");

    $.ajax({
        type : 'GET',
        dataType : 'json',
        url : '/plugin/editor/test',
        contentType : 'text/plain',
        success : function(data)
        {
           //$('#editor-container').html(data);
           editor.setContent(data);
           console.log("goodin");
        },
        error : function(data)
        {
            console.log("badin");
        }

    });
    console.log("later1");

    editor.setTheme("ace/theme/solarized_light");
    editor.setReadOnly(false);
    editor.getSession().setMode("ace/mode/SH");
    console.log("later2");
</script-->
<@footer/>