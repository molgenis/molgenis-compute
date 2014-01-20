package org.molgenis.compute.db;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 1/9/14
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */


import org.apache.log4j.Logger;
import org.molgenis.security.MolgenisWebAppSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.security.SecurityUtils.getPluginReadAuthority;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebAppSecurityConfig extends MolgenisWebAppSecurityConfig
{
	private static final Logger logger = Logger.getLogger(WebAppSecurityConfig.class);

	@Autowired
	private RoleVoter roleVoter;

	// TODO automate URL authorization configuration (ticket #2133)
	@Override
	protected void configureUrlAuthorization(
			ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry)
	{
		List<AccessDecisionVoter> listOfVoters = new ArrayList<AccessDecisionVoter>();
		listOfVoters.add(new WebExpressionVoter());
		listOfVoters.add(new MolgenisAccessDecisionVoter());
		expressionInterceptUrlRegistry.accessDecisionManager(new AffirmativeBased(listOfVoters));

		expressionInterceptUrlRegistry.antMatchers("/").permitAll();

	}

	@Override
	protected List<GrantedAuthority> createAnonymousUserAuthorities()
	{
		String s = getPluginReadAuthority("home");
		return AuthorityUtils.createAuthorityList(s);
	}

	// TODO automate role hierarchy configuration (ticket #2134)
	@Override
	public RoleHierarchy roleHierarchy()
	{
		RoleHierarchyImpl roleHierarchyImpl = new RoleHierarchyImpl();

		roleHierarchyImpl.setHierarchy("");
		return roleHierarchyImpl;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http.addFilter(basicAuthenticationFilter());
		http.addFilter(anonymousAuthFilter());
		http.authenticationProvider(anonymousAuthenticationProvider());

		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry = http
				.authorizeRequests();
		configureUrlAuthorization(expressionInterceptUrlRegistry);

		expressionInterceptUrlRegistry.antMatchers("/login").permitAll()

				.antMatchers("/account/**").permitAll()

				.antMatchers("/css/**").permitAll()

				.antMatchers("/img/**").permitAll()

				.antMatchers("/js/**").permitAll()

				.antMatchers("/html/**").permitAll()

				.antMatchers("/plugin/void/**").permitAll()

				.antMatchers("/api/**").permitAll()

				.antMatchers("/search").permitAll()

				.antMatchers("/captcha").permitAll()

				.antMatchers("/dataindexerstatus").authenticated()

				.anyRequest().denyAll().and()

				.formLogin().loginPage("/login").failureUrl("/login?error").and()

				.logout().logoutSuccessUrl("/").and()

				.csrf().disable();
	}

	@Bean
	public BasicAuthenticationFilter basicAuthenticationFilter() throws Exception {
		BasicAuthenticationEntryPoint authenticationEntryPoint = new BasicAuthenticationEntryPoint();
		authenticationEntryPoint.setRealmName("computedb-api");
		return new BasicAuthenticationFilter(authenticationManager(), authenticationEntryPoint);
	}

}