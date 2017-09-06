Important!!!

Before using the system for the first time:
==========================================

1. Make sure you have MySQL, Eclipse Neon and Java 8 version installed on your PC. 
2. Go to your MySQL Command Line in your pc.
3. It will probably ask for a password
   (type whatever password you chose when you installed MySQL on your PC
   and then press enter).
4. Type the following line:
   grant all privileges on *.* to 'scott'@'localhost' identified by 'tiger';
   (and press enter)
5. Now open the project on Eclipse, go to the storage package and run the java file named ImportMySQLDB
6. Your'e good to go! Everything is set! :)


Using the system:
================

1. Open the project on Eclipse, go to the iface package and run the java file named TraderApp
2. Enjoy!


Reseting the DB:
===============

Whenever you'll want to completely reset the storage regarding the system
(meaning, deletion of all saved data and recreation of the tables),
then you could do it by running the java file named ImportMySQLDB (located in the storage package).
