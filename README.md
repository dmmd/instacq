instacq
=======
a zero-frills instagram acquisition tool

prerequisites 
-------------

*postgresql database (9.1 or greater)
*Java (7 or greater)
*SBT (tested on v0.13.5)
*An instagram client_id


set up
------
1. cp /src/main/resources/application.conf_template /src/main/resources/application.conf.
2. edit /src/main/resources/application.conf, insert your client_id, data_dir(where you want images stored), and postgres connection info.
3. edit log4j.properties, insert the location where Instaq should write its log
4. initialize the database system
5. add an instagram user to the system
6. Build the application: $sbt assembly