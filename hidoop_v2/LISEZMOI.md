<div align="center">
    <img src="img/header.png" />
</div>

![Language Java](https://img.shields.io/badge/Language-Java-B07219 "Language") ![Status Under Development](https://img.shields.io/badge/Status-Under%20Development-brightgreen "Status")

**Hidoop est une plateforme développée en Java, permettant l'exécution d'applications basées sur le modèle de programmation MapReduce sur un cluster d'ordinateurs.**  

Ce projet est notre premier travail sur le thème de la programmation répartie  pour le calcul intensif et le traitement des données massives.
Il consiste en une version miniaturisée de [Hadoop](https://hadoop.apache.org/) (developed by Apache) et il est composé de deux grandes parties :
* :file_folder: **Un système Hdfs** **H**adoop **D**istributed **F**ile **S**ystem (HDFS)_
* :diamond_shape_with_a_dot_inside: **Un modèle de programmation MapReduce**.

## Table de matière
<details>
<summary>Click to expand</summary>


- [**Démarer Hidoop** :rocket:](#run-hidoop-rocket)
    - [Deployer](#deployment)
    - [Launcer](#launching)
- [**distribué les données dans le  cluster utilisant HDFS** :file_folder:](#spread-data-on-the-cluster-using-hdfs-file_folder)
    - [Data Format](#data-format)
    - [Écrire un fichier en HDFS](#write-a-file-on-hdfs)
    - [Lire un fcihier en HDFS](#read-a-file-from-hdfs)
    - [supprimer un fichier en HDFS](#delete-a-file-from-hdfs)
- [**Démarer l'opération de MapReduce dans le cluster** :diamond_shape_with_a_dot_inside:](#run-a-mapreduce-application-on-the-cluster-diamond_shape_with_a_dot_inside)
    - [WordCount application](#wordcount-application)
    - [QuasiMonteCarlo application](#quasimontecarlo-application)
    - [PageRank application](#pagerank-application)
- [**Comment hidoop marche ? :gear:**](#how-hidoop-works-gear)
    - [Details  HDFS](#details-on-hdfs)
    - [Details d'implementation du concept MapReduce ](#details-on-the-implementation-of-the-mapreduce-concept)
- [**seconde étape de developement :bulb:**](#next-development-stages-bulb)
- [**Contributions** :busts_in_silhouette:](#contributors-busts_in_silhouette)
</details>

## Introduction

Cette application permet à son utilisateur de **traiter de grands quantités de données sur plusieurs serveurs** à l'aide du modèle de programmation MapReduce.
Ce modèle de programmation est utilisé pour **paralléliser le traitement de données à grande échelle** au sein d'un cluster on parle de la programmation répartie.
Chaque serveur du cluster traite une petite partie des données qu'on appelle chunk.

Dans ce projet, le traitement des données se déroule en 3 étapes:
* Les données (fournies sous forme de fichier) sont coupées en petits morceaux (chunks) et réparties sur les serveurs (processus géré par __HDFS__)
* Chaque serveur traite une petite partie des données (__Map__)
* Les résultats du serveur sont collectés et agrégés (__Reduce__)

Les processus __Map__ et __Reduce__ dépendent de l'objectif de l'application MapReduce et peuvent être entièrement conçus par l'utilisateur.
La force de ce modèle réside dans la parallélisation des processus.
Par exemple, le modèle de programmation MapReduce peut être utilisé pour paralléliser le comptage du nombre d'occurrences d'un mot spécifique dans un grand ensemble de données.



## Pour débuter :pushpin:

### Preliminaire :

* La version actuelle doit être exécutée sur un système **Linux**.
En effet, le démarrage de la version actuelle entraîne l'ouverture de différents terminaux permettant le suivi de l'activité des différents services.
* Tous les serveurs (ou machine) doivent être dans un même **réseau**, **accessible par la machine exécutant le projet** (ex. vpn n7).
* Les serveurs doivent être accessibles via **SSH**.
* Tous les serveurs doivent avoir une **version récente de Java** (1.8 ou plus récente).

### Déploiment
Le script "deployPython" nous offre deux fonctionnalités:
    - Deploiement des demons Worker et Datanode (deploy).
    - Arret des demons (kill).
Cela est fait en executant le script avec la commande:

-------- python ./deployPyhton.py <deploy|kill> --------

Ce script utilise tout d'abord un fichier hostname.txt. Ce fichier contient les 
noms de quelques machines enseeiht. Ces dernieres sont mis dans une liste 
(listeMachine). On peut ajouter à ce fichier le nombre qu'on veut de machines.
Il faut aussi choisir la machine qui va host le NameNode. On a choisit par 
defaut la machine "azote". On a aussi choisit 4045 et 2015 comme port par defaut
 pour les NameNode et les Worker (tout cela reste modifiable dans le code)

Avant d'utiliser ce script, il faut tout d'abord generer et copier les cles rsa
entre les machines (ssh-keygen -t  rsa et ssh-copy-id user@machine), 

Il faut aussi modifier le classpath selon l'utilisation. Dans notre cas, on a 
mit nos classes dans un dossier hidoop_v2/src dans le bureau
 (-cp Bureau/hidoop_v2/src).



### Launcement de l'application
## Répartissez les données sur le cluster à l'aide de HDFS: dossier_fichier:
Dans ce projet, HDFS fournit 3 fonctionnalités principales:
* **Ecrire des données:** décomposer un fichier fourni en morceaux et les répartir sur les serveurs (DataNodes)
* **Lire les données:** récupérer les données qui ont été écrites sur les serveurs par le processus _write_ afin de reconstruire le fichier d'origine
* **Supprimer les données:** supprimer les données stockées par les serveurs concernant un fichier spécifié

### Data Format

Les données à traiter doivent être stockées sous forme de fichier. La version actuelle du projet prend en charge deux types de format: Ligne et KV.


#### Line format
Au format Ligne, le texte est écrit **ligne par ligne**. Une ligne de texte est considérée comme une unité de données, la longueur des lignes ne doit donc pas être trop disparate.

Voici un exemple de contenu de fichier au format Line:
> C'est le contenu du fichier.
> Il doit être écrit ligne par ligne,
> la longueur des lignes ne doit pas être trop disparate,
> le fichier peut être aussi volumineux que souhaité.



#### KV format
Au format KV, le fichier texte est composé de paires clé-valeur. Chaque paire est écrite **sur une ligne**, le séparateur est le symbole * \ <- \> *.

Voici un exemple de contenu de fichier au format KV:

>omar<->SABRI  
>inp<->ENSEEIHT  
>France<->Toulouse  
>color<->green  

### Write a file on HDFS

Pour écrire un fichier volumineux en HDFS (correspond à ** la répartition des données entre les serveurs **), ouvrez un terminal dans le ** dossier racine ** du projet et exécutez la commande suivante:
```
java hdfs.HdfsClient write <line|kv> <sourcefilename> [replicationfactor]
```
* *\<line|kv\>* correspond au format de fichier d'entrée, au format ligne ou KV.
* *\<sourcefilename\>* est le nom du fichier à poursuivre.
* *\[replicationfactor\]* est un argument **facultatif **. Il correspond au facteur de réplication du fichier, c'est-à-dire le nombre de fois que chaque morceau est dupliqué sur le cluster, afin d'anticiper les pannes du serveur. **La valeur par défaut est 1**.

### Read a file from HDFS
Pour lire un fichier depuis HDFS (correspond à la récupération de données depuis des serveurs), ouvrez un terminal dans le dossier racine du projet et exécutez la commande suivante:
```
java hdfs.HdfsClient read <filename> <destfilename>
```

### Delete a file from HDFS
Pour supprimer un fichier de HDFS (correspond à ** la suppression des données des serveurs **), ouvrez un terminal dans le ** dossier racine ** du projet et exécutez la commande suivante:


```
java hdfs.HdfsClient delete <sourcefilename>
```
* *\<sourcefilename\>* est le nom du fichier (précédemment écrit sur HDFS) à supprimer de HDFS.

## Exécuter une application MapReduce sur le cluster :diamond_shape_with_a_dot_inside:

Les modèles d'application MapReduce sont donnés dans le package _src / application_.

:warning: **Les instructions suivantes ne peuvent fonctionner que si la plateforme Hidoop s'exécute sur le cluster et que les données sont réparties dans le cluster.**


### WordCount application
 
**Wordcount** est une application MapReduce exécutable sur la plateforme Hidoop.
Cette application compte le nombre d'occurrences de chaque mot dans un grand fichier texte au format Ligne.
L'application se trouve dans le package _src / application / _.

_Note: Nous avons également implémenté une version itérative pour comparer les différences de performances sur de très gros fichiers (4,8 Go) ._

Assurez-vous d'avoir écrit le fichier en HDFS avant de lancer l'application. Ensuite, exécutez le code suivant à partir d'un terminal ouvert dans le projet's
 **root folder** :
```
java application.WordCount_MapReduce <filename>
```
* *\<filename\>* is the name of the file (written on HDFS previously) to process.

> Exemple d'utilisation:
>
> Supposons que vous souhaitiez compter le nombre d'occurrences de tous les mots d'un grand fichier texte de 50 Go, stocké sur votre système dans data / filesample.txt.
> La première étape consiste à écrire le fichier en HDFS:
> `java hdfs.HdfsClient write line data / filesample.txt 1`
> L'étape suivante consiste à exécuter l'application WordCount en spécifiant le nom du fichier (sans le chemin car HDFS est une hiérarchie plate):
> `java application.WordCount_MapReduce filesample.txt`
> Le résultat du processus est écrit dans le fichier _results / resf-filesample.txt_, au format KV.

_Note: Il est possible de comparer les performances des applications MapReduce avec leurs versions itératives, également présentes dans le package src / application  ._
_ Gardez à l'esprit que **le gain de temps ne sera perceptible que sur les fichiers très volumineux (> 10 Go)**, sans tenir compte du temps passé à écrire des fichiers sur HDFS._
_En effet, le processus MapReduce est assez coûteux et n'est utile que sur de grands ensembles de données._


## Comment Hidoop marche ?:

###  Détails sur HDFS
L'implémentation proposée du service HDFS est composée de 2 entités principales, **NameNode** et **DataNode**.
Il fournit également une classe avec des méthodes statiques pour effectuer toutes les opérations possibles sur le système de fichiers, **HdfsClient**.

Le ***NameNode*** est le processus Java exécuté sur le **serveur maître du cluster**.
Il supervise l'ensemble du réseau, stocke des informations sur les fichiers stockés sur le système de fichiers (métadonnées et emplacements des blocs de fichiers) et fournit une interface pour effectuer des requêtes sur les données.

Le ***DataNode*** est le processus Java exécuté sur chacun des **serveurs esclaves** du cluster.
Il supervise les données stockées sur le serveur (chunks), effectuant les opérations contrôlées par le NameNode (réception / envoi / suppression de chunks).

Les processus sont interconnectés par **RMI **, et échangent des données via **Socket en mode TCP**.
Les 3 opérations possibles sur le système de fichiers HDFS sont celles effectuées par les méthodes statiques de la classe **HdfsClient**, à savoir:

* **HdfsWrite**: écrit un fichier sur le système de fichiers. Le fichier est divisé en morceaux, et chaque bloc est envoyé à un ou plusieurs **DataNode** (selon le facteur de réplication).
Le choix du **DataNode** recevant le bloc est déterminé par le **NameNode**.

* **HdfsRead**: lit un fichier sur HDFS. Le NameNode fournit les emplacements des différents morceaux composant le fichier.
Pour chaque bloc, le client essaie de récupérer des données d'au moins un des serveurs contenant une copie.
Une connexion est établie avec le DataNode du serveur, qui fournit le contenu du bloc au client.

* **HdfsDelete**: supprime un fichier de HDFS. Le NameNode fournit les emplacements des différents morceaux composant le fichier.

### Détails sur la mise en œuvre du concept MapReduce


L'implémentation du concept MapReduce est composée de 2 entités majeures: **JobManager** et **Daemon**.

Le **JobManager** est le processus Java, qui a un rôle similaire au *NameNode* mais du côté de l'application.
Il supervise toutes les opérations en cours de *Daemons* Map and Reduce au sein du cluster.
Il assure également le suivi des * Jobs * lancés et permettrait, dans une future version, de gérer les pannes lors d'une opération Map.

Le **Daemon** est le processus Java qui exécute une action définie par l'opération **Map**.
Les résultats de chaque carte seront agrégés et renvoyés au client grâce à l'opération **Réduce**.

Un **Job** exécute les méthodes Map et Reduce d'une application MapReduce (*c'est-à-dire WordCount_MapReduce*) au sein du cluster.
Il récupérera la liste des **Daemon** disponibles grâce au **JobManager**.
Il récupérera également la liste des fragments si l'application nécessite un fichier d'entrée, écrit en **HDFS**.
Ensuite, le Job exécutera les opérations Map et demandera au **JobManager** la progression des opérations jusqu'à ce que toutes les maps soient terminées.
À la  fin, il lira le fichier résultat de la carte grâce à *HDFS* et lancera l'opération de réduction.


## Prochaines étapes de développement: bulb:

### Service HDFS
* **Parallélisation des opérations côté client* **(HdfsClient)*.
* **Surveillance du cluster** amélioration **(NameNode)**.
* **Implémentation d'un HDFS Shell**, facilitant l'utilisation du système de fichiers.
* **Amélioration de l'architecture globale**.
* **Implémentation de nouveaux formats de fichiers**.

### Implémentation du concept MapReduce
* **Parallélisation de l'opération Réduire**.
* **Gérer les pannes** au cours d'une application MapReduce.
* **Optimiser la répartition des ressources** en fonction du CPU utilisé par exemple (actuellement distribué en fonction du nombre de cartes par serveur).
* **Assurez-vous que le Job est exécuté sur un nœud de cluster** et non sur la machine client (pour se rapprocher de la vraie architecture **Hadoop**).
* **Améliorez l'interface Job** pour la rendre plus générique et accessible à d'autres types d'applications MapReduce.

### Applications MapReduce développées
* Dans l'application PageRank, **ajoutez une partie entière permettant d'analyser une page web avec [Jsoup] (https://jsoup.org/)** afin de retrouver tous les liens (pages web existantes et pages web qui n'existe plus).
* **Développer plus d'applications** basées sur le concept MapReduce (c'est-à-dire des algorithmes sur la couverture exacte comme **l'algorithme DLX** de Donald Knuth permettant de résoudre des sudokus ou d'autres puzzles de ce genre).

## Contributors :busts_in_silhouette:

[Omar SABRI](https://github.com/osabriN7) - [xx](xx) - [xx](xx)
