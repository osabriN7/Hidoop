#! /usr/bin/python3
import sys
import os
import threading
import time

listeMachine = []
with open("hostname.txt","r") as f:
    for ligne in f:
        listeMachine.append(ligne.replace("\n",""))
port_hdfs = 4045
port_worker = 2015
nameNodeMachine = "azote.enseeiht.fr"
username = "mdahhoum"
nameNodeCmd = 'java -classpath "Bureau/hidoop.jar" hdfs.NameNodeImpl'


    print("Deployement en cours")


if  sys.argv[1] == "deploy":
    print("------ Deployement -------")
    time.sleep(1)
    os.system("ssh -f {0}@{1} nohup java -cp Bureau/hidoop.jar hdfs.NameNodeImpl".format(username,nameNodeMachine))
    time.sleep(6)
    print("NameNode is ready")
    time.sleep(1)
    print(".")
    time.sleep(1)
    print(".")
    time.sleep(1)
    print(".")
    
    for machine in listeMachine:
        os.system("ssh -f {0}@{1} nohup java -cp Bureau/hidoop.jar hdfs.DataNode {2}".format(username,machine,port_hdfs))
        #os.system("ssh -f {0}@{1} nohup java -cp Bureau/hidoop.jar ordo.WorkerImpl {2}".format(username,machine,port_worker))

        time.sleep(1)
        print(".")
        time.sleep(1)
        print(".")
        time.sleep(1)
        print(".")
        time.sleep(1)

    print("All deamons are ready")

elif sys.argv[1] == "kill":
    print("------ Stop -------")
    os.system("ssh -f {0}@{1} nohup killall java".format(username,nameNodeMachine)) 
    
    for machine in listeMachine:
        os.system("ssh -f {0}@{1} nohup killall java".format(username,machine)) 
    
    print("All deamons got killed stopped")

else:
    print("Mauvais usage")
    print("python ./deployPython.py <deploy|kill>")