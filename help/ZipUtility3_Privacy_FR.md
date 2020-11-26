# 1. Envoi de données à partir de l'application

Les données enregistrées par l'application peuvent être envoyées par courrier électronique en utilisant les opérations suivantes de l'application. **Les données enregistrées ne seront pas envoyées au monde extérieur par l'application sans que l'utilisateur n'ait à intervenir.**

- Appuyez sur le bouton "Envoyer au développeur" à partir des informations du système.

- Appuyez sur le bouton "Partager" ou "Envoyer au développeur" à partir de la gestion du journal.

# 2. Données enregistrées par l'application

Les données enregistrées ne seront pas envoyées au monde extérieur par l'application sans intervention de l'utilisateur.

## 2.1fiche d'activité de la demande

Les données suivantes sont enregistrées afin de vérifier que l'application a été traitée avec succès.

- Version d'Android, fabricant de l'appareil, nom de l'appareil, modèle de l'appareil et version de l'application

- Nom du répertoire, nom du fichier, taille du fichier et date de la dernière modification du fichier

- Informations sur les erreurs d'application

- valeur fixée par le système（Battery optimization, Storage information） 

## 2.2 Utilisation des données envoyées au développeur de l'application

Les données envoyées au développeur de l'application seront utilisées pour résoudre les problèmes de l'application. **Les données ne seront divulguées à personne d'autre que le développeur.**  

# 3. Autorité requise

L'application utilise les autorisations suivantes

3.1. Photos, médias et fichiers

-”read the contents of your USB storage”, “modify or delete the contents of your USB storage”

Il est utilisé pour créer, renommer et supprimer des fichiers et des répertoires pour le stockage interne et externe.

3.2. stockage

-”read the contents of your USB storage”, “modify or delete the contents of your USB storage”

Il est utilisé pour créer, renommer et supprimer des fichiers et des répertoires pour le stockage interne et externe.

3.3. les autres

-prevent device from sleeping

Utilisé pour s'assurer que le processus n'est pas interrompu lorsque l'application passe en arrière-plan pendant le traitement du fichier.

 