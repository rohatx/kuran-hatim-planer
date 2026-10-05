# Kuran Hatim Planner

Eine einfache Android-App zur Organisation eines Kuran-Hatims mit mehreren Teilnehmenden. Die App verteilt jeden Monat Cüz pro Person, sodass die Cüz 1–30 jeden aktiven Monat genau einmal abgedeckt sind.

## Funktionen

- Mehrere unabhängige Gruppen, z. B. `Kuran Hatim Planner 1`, `Kuran Hatim Planner 2` usw.
- Startmonat über eine Kalenderauswahl
- Beliebig viele Pausenmonate
- Automatische Vergabe von zwei Cüz pro Person und aktivem Monat
- Jeder Cüz wird pro aktivem Monat genau einmal vergeben
- Nach so vielen aktiven Monaten wie Personen in der Gruppe hat jede Person alle Cüz 1–30 gelesen
- Deutsch, Englisch, Türkisch und Arabisch verfügbar
- Persönliche Tabellenübersicht pro Person
- Gesamtübersicht als Tabellenbild
- Export und Teilen von Bildern
- Export der Gesamtübersicht als DIN-A4-PDF zum Drucken
- Automatische lokale Speicherung der Gruppen und Eingaben
- Einstellbare Teilnehmerzahl: 5, 6, 10, 15 oder 30

## Vergabelogik

Eine Gruppe kann 5, 6, 10, 15 oder 30 Personen enthalten. In jedem aktiven Monat werden die 30 Cüz gleichmäßig auf die Personen verteilt. Die Laufzeit entspricht der Personenzahl: Bei 10 Personen sind es 10 aktive Monate mit jeweils 3 Cüz pro Person. Pausenmonate werden in der Zeitachse angezeigt, zählen aber nicht als aktiver Vergabemonat.

Beispiel:

```text
Person 1: 1 ve 2
Person 2: 3 ve 4
Person 3: 5 ve 6
...
Person 15: 29 ve 30
```

Im folgenden aktiven Monat wird die Verteilung um zwei Cüz weitergeschoben.

## Datenschutz

Die App benötigt für die Kernfunktionen keine Internetverbindung, kein Benutzerkonto und keinen eigenen Server. Namen, Gruppen, Startmonate und Pausenmonate werden lokal auf dem Android-Gerät gespeichert.

Beim Export werden Bilder im Gerätespeicher abgelegt. Beim Teilen oder Drucken entscheidet der Nutzer selbst, an welche App oder welchen Dienst die Datei übergeben wird.

## Projekt öffnen und bauen

1. Dieses Repository in Android Studio öffnen.
2. Gradle-Synchronisierung abwarten.
3. Ein Gerät oder einen Emulator auswählen.
4. Die App mit `Run` starten.

Debug-APK erstellen:

```text
gradlew.bat assembleDebug
```

Die APK liegt danach unter:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Lizenz

Dieses Projekt steht unter der MIT-Lizenz. Siehe [LICENSE](LICENSE).

Die MIT-Lizenz erlaubt Nutzung, Veränderung, Weitergabe und kommerzielle Verwendung, solange der Copyright- und Lizenzhinweis erhalten bleibt.

## Haftungsausschluss

Die App ist ein Organisationswerkzeug. Die automatisch erzeugten Verteilungen sollten vor der Weitergabe geprüft werden. Für verlorene Daten, fehlerhafte Eingaben oder Folgen der Nutzung wird keine Gewähr übernommen.
