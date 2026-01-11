# MRP Protocol
## Architektur
Das Projekt ist in fünf packages (models, handlers, repositories, services und utils) gegliedert.

Die Models enthalten Model-Klassen für User, Media Entry und Rating. Sie bilden die Basis Klassen mit welchen im gesamten Projekt gearbeitet wird.

In die Handlers werden API-Anfragen, anhand ihrer Endpoints, weitergeleitet und näher spezifiziert.

Daraufhin wird sie zu dem entsprechenden Service weitergeleited, in welchem die eigentliche Bearbeitung stattfindet.

Repositories werden in Services aufgerufen um Daten zu überprüfen, auszulesen oder zu speichern.

Der JsonHelper in utils wird von mehreren Klassen in unterschiedlichen packages genutzt, um JSON korrekt zu parsen.
## Tests
### Postman
Integration Tests wurden mithilfe einer Postman Collection durchgeführt, welche in diesem Projekt unter [Postman Collection](MRP_Giebl_Collection.postman_collection.json) zu finden ist.

Getestet wurden mehrere möglichen Kombinationen des Request-body auf die zur Zeit bestehenden Endpoints login und register. Dabei wurde folgendes geprüft:

- entspricht der HTTP Status Code der Antwort dem erwarteten Wert
- Ist die Antwort in validem JSON Format
- beinhaltet die Antwort die erwarteten Felder
- haben diese Felder die richten Datentypen
- beinhaltet die Antwort bestimmte Schlüsselwörter (korrekte Fehler- oder Erfolgsnachricht)
### Unit Tests
Für Unit tests wurde JUnit und Mockito verwendet um einzelne Vorgänge gezielt testen zu können.
Um möglichst verschiedene Funktionen zu testen und Wiederholungen zu vermeiden, wurden sowohl von den Handlern, den Services, sowie den Repositories zu je einer Klasse Unit tests geschrieben.

Insgesamt gibt es 25 Tests:
- 5 zu AuthHandler, um das Aufrufen der richtigen Funktionen zu überprüfen
- 14 zu RatingService, um das korrekte Verarbeiten der Daten (Business Logic) zu überprüfen
- 6 zu MediaRepository, um das korrekte Speichern und Bearbeiten der Daten zu überprüfen
## SOLID Principles
### Single-Responsibility
Die Aufteilung in die packages Models, Handlers, Services und Repositories ermöglicht eine klare Zuteilung der Aufgaben auf die entsprechenden Klassen.
### Interface segregation
Das Repository Interface enthält nur jene Methoden, welche mit Sicherheit von allen Repositories benötigt werden. Auf diese Weise wird keine Klasse gezwungen Funktionen zu implementieren, welche sie nicht nutzt. 
## Aufwandschätzung
### anfängliche Planung und setup
- erste Überlegungen der Herangehensweise ca. 4h
- Erstellung eines ersten UML-Entwurfs ca. 2h
- nähere Recherche und Anpassung der bissherigen Entwürfe ca. 3h
### Models erstellen
- erster Entwurf der Model-Klassen ca. 2h
- weiterer kontinuierlicher Ausbau ca. 2h
- zusätzliche Models erstellen (Favourites, Leaderboard) ca. 3h
### Server und Services
- Erstaufsetzen des Servers ca. 3h
- grobe Planung von Services und Handlern 1h
- kontinuierliche Anpassung des Servers ca. 4h
- genauere Ausführung der Services ca. 5h
- Erstellung von Repositories ca. 1h
- Anpassung der Repositories ca. 2h
- Erweitern der Repositories für zusätzliche Use Cases ca. 30h
- Erweitern der Services für zusätzliche Use Cases ca. 35h
### Tests
- anfängliches vertrautmachen mit Postman ca. 1h
- Erstellung Postman Requests ca. 3h
- schreiben Postman-Tests ca. 5h
- überarbeiten von Postman-Tests ca. 5h
- Test Strategie überlegen, einbinden von JUnit und Mockite, ca. 2h
- schreiben Unit Tests ca. 12h
### Datenbank
- Docker installieren und Datenbank aufsetzen ca. 3h
- Repositories bearbeiten um Datenbank zu nutzen ca. 7h
- nötige Anpassungen an der Datenbank um Favourites und Likes zu integrieren ca. 3h

## Link zu Github Repository:
[https://github.com/if24b067/MRP\_Giebl.git](https://github.com/if24b067/MRP\_Giebl.git)


