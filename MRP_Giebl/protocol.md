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

## Aufwandschätzung
### anfängliche Planung und setup
- erste Überlegungen der Herangehensweise ca. 3h
- Erstellung eines ersten UML-Entwurfs ca. 2h
- nähere Recherche und Anpassung der bissherigen Entwürfe ca. 3h
### Models erstellen
- erster Entwurf der Model-Klassen ca. 2h
- weiterer kontinuierlicher Ausbau ca. 2h
### Server und Services
- Erstaufsetzen des Servers ca. 3h
- grobe Planung von Services und Handlern 1h
- kontinuierliche Anpassung des Servers ca. 1h
- genauere Ausführung der Services ca. 2h
- Erstellung von Repositories ca. 1h
### Tests
- anfängliches vertrautmachen mit Postman ca. 1h
- Erstellung endgültiger Postman Requests ca. 2h
- schreiben endgültiger Postman-Tests ca. 2h


