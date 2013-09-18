<#macro plugins_header_ComputeHeader screen>
<div id="header" style="margin-top: 5px; margin-bottom: 10px;">
	<p>
		<font size="10" face="Verdana">&nbsp;&nbsp;&nbsp;<br>Compute 5
	</p>
	<div align="right" style="color: maroon; font: 12px Arial;margin-right: 10px;">
	<a href="/generated-doc/fileformat.html">Exchange format</a>
</div>
</div>
<div id="login-modal-container-header"></div>
<div class="login-header">
	<#assign login = screen.login/>
	<#if !login.authenticated>
		<div><a class="modal-href" href="/account/login" data-target="login-modal-container-header">login/register</a></div>
	<#else>
		<div><a href="/account/logout">logout</a></div>
	</#if>
</div>
</#macro>
