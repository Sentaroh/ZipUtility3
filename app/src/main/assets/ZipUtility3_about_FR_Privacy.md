### 1.données collectées  
### 1.1.les données fournies à ZipUtility3 par l'utilisateur  

- Mot de passe pour créer et extraire le fichier ZIP crypté.  
Le mot de passe sera supprimé et ne sera pas enregistré lorsque l'application sera fermée.  
- Mot de passe pour protéger les données dans "1.4 Envoi ou écriture de données en dehors de ZipUtility3"  
Le mot de passe sera éliminé et non sauvegardé lorsque le processus sera terminé.  

### 1.2.ZipUtility3 Journal des activités  

Lorsque l'enregistrement est activé, les données d'activité seront stockées dans la zone de mémoire de l'application pour la vérification des résultats d'exécution de l'application et pour l'assistance technique. Si la journalisation est désactivée, l'enregistrement des données sera arrêté, mais les données déjà enregistrées ne seront pas supprimées.  
<span style="color : red ;"><u>Les données ne seront pas envoyées à l'extérieur, sauf si l'opération "1.3.Envoi ou écriture de données en dehors de ZipUtility3" est effectuée.</u></span>.  

- Informations sur le dispositif (nom du fabricant, nom du modèle, version du système d'exploitation, point de montage, répertoire spécifique à l'application, StorageAccessFramework, gestionnaire de stockage)  
- Version de ZipUtility3, options d'exécution de ZipUtility3  
- Nom du répertoire, nom du fichier, état d'exécution  
- Informations sur le débogage  
- Informations sur les erreurs  

### 1.3.Envoi ou écriture de données en dehors de ZipUtility3  

Les données détenues par ZipUtility3 ne peuvent pas être envoyées ou écrites vers l'extérieur, sauf si l'utilisateur l'actionne.  

- Cliquez sur le bouton "Envoyer au développeur" à partir des informations sur le système.  
- Cliquez sur le bouton "Partager" à partir de la gestion du journal.  
- Cliquez sur le bouton "Envoyer au développeur" à partir de la gestion du journal.  
Si vous spécifiez un mot de passe, le fichier joint sera protégé par un mot de passe.  
- Cliquez sur le bouton "Exportation des journaux" dans la gestion des journaux pour exporter le fichier journal vers un stockage externe.  

### 1.4.Suppression des données stockées dans ZipUtility3  

En désinstallant ZipUtility3, les données sauvegardées ("1.2.ZipUtility3 Journal des activités") seront supprimées de l'appareil.  
<span style="color : red ;"><u>Cependant, les données enregistrées sur un stockage externe par l'interaction de l'utilisateur ne seront pas supprimées. </u></span>  

## 2.les autorisations requises pour exécuter l'application.  

#### 2.1.Photos, médias, fichiers  
**<u>read the contents of your USB storage</u>**  
**<u>modify or delete the contents of your USB storage</u>**  
Utilisé pour les opérations sur les fichiers et les répertoires (création, suppression, renommage), les opérations sur les fichiers ZIP (création, suppression, renommage, mise à jour, extraction) et l'écriture des fichiers journaux.  

### 2.2.Autres  

### 2.2.1.Empêcher l'appareil de dormir  
Utilisé pour empêcher l'appareil de se mettre en veille pendant les opérations sur les fichiers/répertoires et les fichiers ZIP.  
