This is team repository for team-207-F18.

### Team-number: 207

### Team members:
1. Prasad Madhale
2. Karan Tyagi
3. Mahima Singh
4. Yatish Kadam

### Link to live system (AWS instance): ec2-54-236-7-66.compute-1.amazonaws.com

### Videos:
1. System Setup [Youtube Link](https://youtu.be/oCAasm7kOOw)
2. Final Presentation [Youtube Link](https://youtu.be/1eL-v7ea_8o)

### Final Files:
1. [report](https://github.ccs.neu.edu/cs5500/team-207-F18/blob/final_submission/final/report.pdf)
2. [uml](https://github.ccs.neu.edu/cs5500/team-207-F18/blob/final_submission/final/uml.pdf)
3. [presentation](https://github.ccs.neu.edu/cs5500/team-207-F18/blob/final_submission/final/presentation.pdf)
4. [usecases](https://github.ccs.neu.edu/cs5500/team-207-F18/blob/final_submission/final/usecases.pdf)


# Project Description
Chatter-Prattle is a messaging service. This application can run on a command line client.

## Requirements
* Operating system with Java 8 and above.
* Port 8613 open for listening

## How to use
* You can run the server using edu.northeastern.ccs.im.server.Prattle.java class. It takes no arguments
* You can Run Client/User using edu.northeastern.ccs.client.chatter.CommandLineMain.Java using with hostname and port number of server machine.

## Operations

|            Command              |                     Operation                             |      Usage                |
| --------------------------      |:---------------------------------------------------------:|:-------------------------:|
| `/profile`                        | User can see his/her profile                              |                           |
| `/setfirstname <First-Name>`      | User can set first name                                   |                           |
| `/getfirstname`                   | User can see his/her firstname                            |                           |
| `/setlastname <Last-Name>`        | User can set last name                                    |                           |
| `/getlastname`                    | User can get his/her last name                            |                           |
| `@<receiver-username> <Message>`  | User can send a private message to another user           | `@Bob Hello Bob!`         |
| `/help`                           | User can get help from the system                         |                           |
| `/quit`                           | User logs out                                             |                           |
| `/deleteaccount`                  | Delete this user from the system and log out              |                           |
| `/newgroup`                       | User can create a group                                   |                           |
| `/mygroups`                       | User can read list of groups they are part of             |                           |
| `/joingroup`                      | User can add himself/herself to a group                   |                           |
| `/leavegroup <Group-Name>`        | User can leave a group                                    |                           |
| `/deletegroup <Group-Name>`       | Deletes the group                                         |                           |
| `/listmembers <Group-Name>`       | Lists all the members of a group                          |                           |
| `/listadmins <Group-Name>`        | Lists the admin users of a group                          |                           |
| `><group-name> <Message>`       | Send message to all users in the given group            |`>MSDgroup hello everyone`   |
| `/becomewiretapper`                | Makes a user as wiretapper                                |                          |
| `/displayunreadmsgs`               | Displays all unread messages                              |                          |
| `/turnonparentalcontrol`           | Turns on parental control                                 |                          |
| `/turnoffparentalcontrol`          | Turns off parental control                                |                          |
| `/addfilteredwords <words>`        | Adds words to the list of censored words                  | `/addfilteredwords wtf`  |
| `/removefilteredwords <word>`      | Removes the word from list of censored words              | `/removefilteredwords b` | 
| `/getsentmessages`                 | Gets all the sent messages by the user                    |                          |
| `/getreceivedmessages`             | Gets all the messages received by the user                |                          |
| `/getmessagebetween`               | Gets all the messages between two time |`/getmessagebetween <TimeStamp1> <TimeStamp2>`|
| `/recallmessage`                          | Recalls the last message sent by user                     |                          |
| `/recallmessagebyid <Message-id>`                          | Recalls a message by it's ID                     |                          |
| `/updatepassword <New-password>`                          | Updates user's exisiting password                     |                          |
## Operations for Wiretapper

|     Command        |      Operation      |      Usage    |
| --------------------------      |:---------------------------------------------------------:|:-------------------------:|
| `/wiretap <start-time> <last-time>` |  Wiretaps the target user with start and end time       | `/wiretap <TimeStamp1> <TimeStamp2>`|
|  `/help`        | get help for wiretapper   |     |
|  `/settarget <user-name>`        | sets the target user for wiretapping   |     |


### User Operations
* Login using username and password. If password is incorrect, user is logged out and asked to log again. If password is correct it works Fine. The passwords are stored as SHA256 hash in the files.
* Register using username and password. If system detects this is a new user, it registers the user with basic username and password

### Group Operations
* Group is monitored by an admin
* Group can be deleted by an admin using `/deletegroup <Group-name>`
* Group can be updated, and users can add themselves and leave the group using `/joingroup <Group-name>` and `/leavegroup <Group-name>` respectively.

## Data Persistence
The Data is hosted on a RDS db.t2.micro instance at [instance-endpoint](cs5500database.cvunqyiiwmc8.us-east-1.rds.amazonaws.com). This is using MySQL as relational database management system. It can be connected to a maximum of 66 connections. But since RDS is flexible we can always upgrade the instance flavor.
