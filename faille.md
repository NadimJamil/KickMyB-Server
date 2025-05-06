
1.  Installer Postman 
2.  Aller sur Postman

3.  Créer un compte avec la méthode POST : http://10.10.47.137:8080/api/id/signup, dans le corps, il faut mettre un "username" et un "password" dans un fichier JSON. 
Par exemple, {
  "username" : "MrPipo",
  "password" : "pipo"
}

4.  Pour trouver la tâche qu'on veut modifier, il faut utiliser la requête GET : http://10.10.47.137:8080/api/detail/{id}
On fait une requête avec l'id 1 si ce n'est pas la bonne, on passe à l'id suivant -> 2, jusqu'a ce qu'on tombe sur la tâche qu'on veut modifier, par exemple http://10.10.47.137:8080/api/detail/1
La bonne tâche serait donc celle qui a le "name" qu'on recherche, par exemple, je recherche une tâche avec le nom suivant : Tache pour mélanger 2
Cela devrait être le corps qu'on reçoit après avoir fait la reqûete : 
{
    "id": 2,
    "name": "Tache pour mélanger 2",
    "deadline": "2025-06-30T12:15:58",
    "events": [],
    "percentageDone": 0,
    "percentageTimeSpent": 0.0
}

5.  Une fois qu'on trouve la bonne tâche, il faut executer la requête GET : http://10.10.47.137:8080/api/progress/{id}/{Progression}
Par exemple, je veux que la tâche avec l'id 20 ai 89% de progression je fais la requête suivante : http://10.10.47.137:8080/api/progress/20/89

Problème du code :
N'importe quel utilisateur peut regarder les détails de la tâche d'une personne
N'importe quel utilisteur peut modifier le pourcentage de progression d'un autre utilisateur