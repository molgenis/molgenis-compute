/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 12/11/13
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */

function test_post()
{
    //var editor = document.getElementById('editor');
    //var value = document.getElementById('#editor').getValue();
    //var v = editor.session.getValue();
    //var editor = ace.edit("editor-container");
    //var value = editor.getSession().getDocument().getValue();
    //var value = editor.getSession().getValue();

    //var editor = CodeMirror(document.body);
    //var editor = CodeMirror.fromTextArea(document.getElementById('code'));
    console.log("something");
    var value = editor.getValue();

    //var value = document.getElementById("code").doc.getValue();
    console.log(value);

    var toSend = "test=" + value;

    //var toSend = toSend.replace(/&/g, "__AND__");

    console.log(toSend);

    $.ajax({

        type : 'POST',
        url : '/plugin/editor/testpost',
        data : JSON.stringify({test:value}),
        async : false,
        contentType : "application/json",
        success : function()
        {
            console.log("nice");
        },
        error : function()
        {
            console.log("bad");
        }

    });

}
