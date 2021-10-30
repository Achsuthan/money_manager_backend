## Money Manger backend system 

This project is developed by using
- Servlet 
- MySql

The JAR file required to run this project 
- Java JSON: Is used to convert the data in to JSON format when returning API response and Getting the body object [JAR](http://www.java2s.com/Code/Jar/j/Downloadjavajsonjar.htm)
- MySQL Connector: Is required to connect the MySql with Java [JAR](https://dev.mysql.com/downloads/connector/j)
- Mail: This Is used to send the email to the users for the specific activites such as (Successfull registration, Friend invitation, Friend request accept, Invitation to join to the system and be as a friend to invitee) [JAR](https://static.javatpoint.com/src/mail/mailactivation.zip)

To run this project given list are requied

- [Eclipse](https://www.eclipse.org)/[NetBeans](https://netbeans.apache.org)
- [Java JDK](https://www.oracle.com/java/technologies/downloads/)
- [MySql](https://dev.mysql.com/downloads/installer/)
- [MySql Workbench](https://dev.mysql.com/downloads/workbench/)

After install all the required list and downloaded the jar files, import WAR file from this [link](https://drive.google.com/drive/folders/1VfbqS6EqD-igvIjagYqZ5q3H_nqUwy_v?usp=sharing) to Eclipse or NetBeans

After the WAR file imported to the Eclipse or Netbease add the ```Server Runtime``` as ```apache-tomcat-x.x.x```

In the Msql Workbench, 
1. Create the schema called ```money_manager```
2. Import the database files from this [link](https://drive.google.com/drive/folders/1ZDBvBMOvUQlhkXcjURqeiXEhwlBRnbo6?usp=sharing)

All the API postman collection can be found from this [link](https://github.com/Achsuthan/money_manager_backend/blob/master/Money%20Manager.postman_collection.json). ```{{baseUrl}}``` should be set in the environment variable in the postman collection.

This project's frontend is build in with Vue.js,  the srouce code can be found [here](https://github.com/Achsuthan/money_manager_frontend)