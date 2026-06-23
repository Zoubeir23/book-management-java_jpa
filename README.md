# Book Management System (Java JPA)

Ce projet est une application Java utilisant JPA (Jakarta Persistence API) et Hibernate pour la gestion d'une bibliothèque de livres.

## Fonctionnalités

Le composant principal est `BookRepository` qui permet de :
- Créer de nouveaux livres (avec génération automatique de l'ISBN si manquant)
- Récupérer tous les livres ou chercher par ID, ISBN, Catégorie, Titre, Auteur ou Année
- Mettre à jour et supprimer des livres
- Compter le nombre de livres (au total ou par catégorie)

## Prérequis
- Java 17+
- Maven
- Base de données (MySQL par défaut, configurable dans `persistence.xml`)

## Configuration
Le fichier `src/main/resources/META-INF/persistence.xml` contient la configuration de la base de données. N'oubliez pas d'adapter l'URL JDBC, l'utilisateur et le mot de passe selon votre environnement.
