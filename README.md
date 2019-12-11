# elaastic-questions-server
Server component of the elaastic-questions application

# Installation
These instructions are for Ubuntu/Debian, but you can also adapt them to use another system.

## Java 11
### Amazon Correto
You can follow [this guide](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/generic-linux-install.html) to install Java.


## MySQL 8
### Install MySQL
You can follow [this guide](https://dev.mysql.com/doc/mysql-apt-repo-quick-guide/en/#apt-repo-fresh-install) to install MySQL.  
The version 8 is needed. Versions 5.6 and 5.7 will not work.

### Change the port to 6603
Edit the `/etc/mysql/mysql.conf.d/mysqld.cnf` file and add (or update) the following line after `[mysqld]`
```
port = 6603
```

### Create the database
Use `mysql` command with sudo, and execute the following lines
```MySQL
create database `elaastic-questions`; 
create user elaastic@localhost` identified by 'elaastic';
grant all on `elaastic-questions`.* to elaastic@localhost;
```
