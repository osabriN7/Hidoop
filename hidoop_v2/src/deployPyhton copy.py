#! /usr/bin/python3
import sys
import os
import threading
import time

listeMachine = []
with open("hostname.txt","r") as f:
    for ligne in f:
        liste_machine.append(ligne.replace("\n",""))

nameNodeMachine = "azote"
username = "mdahhoum"
nameNodeCmd = 'java -classpath "Bureau/hidoop.jar" hdfs.NameNodeImpl'

# fonction pour lancer le NameNode
def runNameode():
    os.system(nameNodeCmd)

port = 4043
dataNodeCmd = 'java -classpath "Bureau/hidoop.jar" hdfs.DataNode' + str(4043)

def runDataNode(nomMachine):
    os.system(dataNodeCmd)

def runDataNode():
    os.system("ssh {0}{1} ./deployPython.py".format(username, nomMachine))

def undeploy(nomMachine):
    pass

if not len(sys.argv) == 3:
    print("Mauvais Usage")

if sys.argv[1] == "demon":
    runDataNode()

elif sys.argv[1] == "NameNode":
    runNameode()

elif sys.argv[1] == "stop":
    os.system("killall java") 

elif sys.argv[1] == "deploy":
    os.system("ssh -f {0}@{1} nohup java -cp Bureau/hidoop.jar hdfs.NameNodeImpl".format(username,nameNodeMachine))

