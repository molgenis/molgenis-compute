<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["pilot-dashboard.css"]>
<#assign js=[]>
<@header css js/>
<form method="post" enctype="multipart/form-data" action="${context_url}/upload">
    File to upload: <input type="file" name="file" accept=".csv" required><br />
    Name: <input type="text" name="name" required><br /><br />
    <input type="submit" value="Upload">
</form>   
<@footer/>