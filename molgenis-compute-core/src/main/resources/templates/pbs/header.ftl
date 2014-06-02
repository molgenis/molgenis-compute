'''__ login on VM (e.g. user@molgenis01.target.rug.nl)__'''

''sudo su - molgenis''

'''#make directory and create a molgenis-server.properties file[[BR]]'''''mkdir ~/.molgenis [[BR]]mkdir ~/.molgenis/omx [[BR]]mkdir ~/.molgenis/omx/data [[BR]]nano ~/.molgenis/omx/molgenis-server.properties''

'''#add the following to the file:[[BR]]'''admin.password=admin[[BR]]user.password=admin[[BR]]db_driver=com.mysql.jdbc.Driver [[BR]]db_uri=jdbc:mysql://localhost/<nameofthedatabase>?innodb_autoinc_lock_mode=2&rewriteBatchedStatements=true[[BR]]db_user=molgenis[[BR]]db_password=molgenis

'''# use wget to get the latest tomcat7 package from http://tomcat.apache.org/download-70.cgi (copy link address from the tar.gz in the binary->core)'''

''wget http://apache.hippo.nl/tomcat/tomcat-7/v7.0.54/bin/apache-tomcat-7.0.54.tar.gz''

'''#unpack the tar '''[[BR]]''tar xzfv apache-tomcat-7.0.54.tar.gz[[BR]][[BR]]'''#make symlink'''[[BR]]ln -s apache-tomcat-7.0.54 ''apache-tomcat

'''#set permissions'''[[BR]]''chmod  -R g+rX apache-tomcat''

'''#setting the environment variable '''[[BR]]''nano ~/apache-tomcat-7.0.54/bin/setenv.sh ''[[BR]]

{{{
CATALINA_OPTS="-Xmx2g -XX:MaxPermSize=256m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Dmolgenis.home=/srv/molgenis/.molgenis/omx -Dlog4j.configuration=log4j-molgenis.properties"
}}}
'''#set encoding and maxpostsize to server''' [[BR]]'' nano ~/apache-tomcat-7.0.54/conf/server.xml''

'''#add the following attributes to port 8080:''' [[BR]]

{{{
maxPostSize="33554432" 
URIEncoding="UTF-8" 
compression="2048"  
compressableMimeType="text/html,application/javascript,application/json"'' 
}}}
[[BR]][[BR]]'''#this is how it should look like  [[BR]]'''

{{{
<Connector port="8080" 
protocol="HTTP/1.1" 
connectionTimeout="20000" 
maxPostSize="33554432" 
URIEncoding="UTF-8" 
compression="2048"  
compressableMimeType="text/html,application/javascript,application/json" 
redirectPort="8443" />
}}}
'''# install tomcat user''' [[BR]]''nano ~/apache-tomcat-7.0.54/conf/tomcat-users.xml [[BR]]'''''#paste just before the closing tag from tomcat-users (</tomcat-users>)''' [[BR]]

{{{
<role rolename="manager-gui"/>
<user username="molgenis" password="password" roles="manager-gui,manager-script"/>
<role rolename="login"/>
<user username="molgenis_user" password="password" roles="login"/>
}}}
'''# get mysql connector''' [[BR]]''cd ~/apache-tomcat-7.0.54/lib/ ''[[BR]]''wget http://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.28/mysql-connector-java-5.1.28.jar''

'''#create database''' [[BR]]''mysql -u molgenis -p password; [[BR]]
create database omx;[[BR]]
exit''

'''__Make sure that relevant daemons startup on reboot.__'''

 * 
{{{
sudo chkconfig

}}}
   If httpd, tomcat7 or mysqld are ''off'', turn them on:
{{{
sudo chkconfig httpd on
sudo chkconfig tomcat7 on
sudo chkconfig mysqld on

}}}
 * Starting and stopping daemons
{{{
sudo service [daemon] [start|stop|status|reload|restart]

}}}
   For example to restart Tomcat:
{{{
sudo service tomcat7 restart
}}}