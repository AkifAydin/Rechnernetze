# CMTP - Chat Message Transport Protocol

## Abstract

In diesem Dokument wird ein Peer-to-Peer teilvermaschtes Chatprotokoll spezifiziert. Bei dem Protokoll handelt es sich um ein auf TCP aufbauendes Application-Layer-Protokoll,  das verbindungs- und paketorientiert arbeitet. 

## Inhaltsverzeichnis
[[_TOC_]]

## 1. Einführung (Hussein & Zohal)

### 1.1 Anforderungen
* Teilvermaschung  \
Hier sind im Gegenteil zur Vollvermaschung die Clients nicht alle direkt miteinander verbunden, sondern über einen Rechner/Router. Dieser Router wird genutzt, um die Kommunikation zwischen den Clients zu ermöglichen. Die Teilvermaschung ist am optimalsten, da der Aufwand mittel ist, Ausfallsicherheit gut, Datensicherheit gut, Broadcast gut und Erweiterbarkeit gut ist.
* Bestehende Netze miteinander verbinden\
Das Ziel des Chats soll es sein, die verschiedenen Netzteilnehmer miteinander zu verbinden und über diesen Weg Nachrichten auszutauschen. Jeder Netzteilnehmer hat eine Liste in dem er andere Netzteilnehmer speichert, die er kennt und ggf. durch diese Liste neue Teilnehmer über bestehende Teilnehmer zu informieren. 
* Teilnehmer kann zusätzlich zur ID einen Anzeigenamen haben\
Ein Teilnehmer wird anhand einer ID erreicht. Allerdings können die anderen Teilnehmer mit dieser ID nichts anfangen, da Sie diese ID zu keiner Person zuordnen können. Damit Teilnehmer auch wissen mit wem sie schreiben und von wem eine bestimmte Nachricht kommt, hat jeder Teilnehmer einen Namen, der im Chat angezeigt wird.

### 1.2 Charakterisierung 
* Application-Layer-Protocol\
Für unser Projekt ist die oberste Schicht des ISO/OSI-Schichtenmodells,  also die Schicht 7, eine besonders wichtige Schicht, denn sie bildet die Schnittstelle zwischen der eigentlichen Anwendung und dem Kommunikationssystem. Die Schicht 7 stellt der Benutzeranwendung direkt verwendbare Daten der Kommunikationsverbindung zur Verfügung. Dafür wird die Socket API verwendet.
* Verwendetes Transport-Layer-Protocol: TCP\
Im Sinne des OSI-Modells ist TCP ein Protokoll der Transportschicht. Es bietet eine zuverlässige Virtual-Circuit-Verbindung zwischen Anwendungen; Das heißt, eine Verbindung wird hergestellt, bevor die Datenübertragung beginnt. Daten werden ohne Fehler oder Duplizierung gesendet und in der gleichen Reihenfolge empfangen, in der sie gesendet wurden. Den Daten sind keine Grenzen gesetzt; TCP behandelt die Daten als einen Strom von Bytes.
* verbindungsorientiert (Aufbau->Übertragung->Abbau)\
Die verbindungsorientierte Kommunikation hat drei Phasen. Als erstes gibt es ein Verbindungsaufbau, also der Client meldet sich an und gibt ein „BinDa“ an. Als nächstes werden die Daten, in der Austausch-Phase gesendet und empfangen. Wenn man fertig ist und seine Verbindung trennen will, dann findet die letzte Phase nämlich der Verbindungsabbau statt. Hier gibt der Client dem Server ein „BinWeg“ an und anschließend schließt der Server alle Schnittstellen zu diesen Client. 
* paketorientiert (Jedes Paket wird an jedem Knoten einzeln geroutet)\
Dadurch, dass wir eine Chat-Anwendung entwickeln wollen, haben wir für jedes Paket einen bestimmten Knoten an dem das ankommen soll. Deshalb ist die Paketvermittlung eine optimale Lösung für uns, da sie das beschriebene umsetzt.

### 1.3 Schlüsselbegriffe (Sandra)
* **Netzteilnehmer**: Teilnehmer, die sich verbunden haben. 
* **Netzteilnehmerliste**: Eine Liste an allen bekannten Netzteilnehmern. 
* **Netzteilnehmereintrag**: Ein Eintrag in der Netzteilnehmerliste, mit der Form: {IP}{Serverport}{Name}{0}
* **ID**: Die ID identifiziert einen Netzteilnehmer eindeutig. Sie besteht aus der IP und dem Serverport des Netzteilnehmers und ist unveränderlich. 
* **Name**: Ein frei wählbarer Anzeigename. 


## 2. Codierung (Julius & Marlon)


### Aufbau der ID eines Netzteilnehmers
Die **ID** eines Netzteilnehmers setzt sich zusammen aus seiner **IP**-Adresse und dem **Serverport** auf welchem sein Chatprogram läuft. 
Durch diese Kombination wird mit wenig Aufwand eine eindeutige ID für jeden Netzsteilnehmer garantiert.

Im **Payload** wird die Teilnehmer-ID direkt als Bitfolge übergeben.
Das Schema ist: {**IP**}{**Port**}.  
Für die ID `127.0.0.1:1138` würde also wie folgt codiert werden:  
IP: 7F 00 00 01  
Port: 04 72  
Komplette ID: 7F 00 00 01 04 72

Im **Header** werden IP und Port bitcodiert.

### Aufbau der Nachrichtenpakete
Jedes Paket besteht aus einem **Header** fester Größe gefolgt von einem **Payload** dessen Länge sich aus dem Nachrichtentyp und dem **PayloadLength**-Feld im Header ergibt (der Playload kann je nach Nachrichtentyp auch die Länge 0 haben).

#### Header

![](./Header.png)

Jedes Paket hat einen 20 Byte großen Header.
Alle Felder des Headers sind positive Ganzzahlen.  

| Offset | Länge *in Bytes*| Feldname | Beschreibung|
|--|--|--|--|
| 0x00 | 4 | IpEmpfänger | Die IP-Adresse des Empfängers |
| 0x04 | 2 | PortEmpfänger | Der Port des Empfängers |
| 0x06 | 2 | PortSender| Der Port des Senders |
| 0x08 | 4 | IpSender| Die IP-Adresse des Senders |
| 0x12 | 2 | NachrichtenTyp | Der Typ der Nachricht |
| 0x14 | 2 | Checksumme | Bis auf weiteres immer `0` |
| 0x16 | 4 | PayloadLength| Hat je nach Nachrichtentyp eine andere Bedeutung |

**Empfänger**: Der Teilnehmer an welchen diese Nachricht gehen soll
**Sender**: Der Teilnehmer von welchem diese Nachricht Ursprünglich kommt

|Nachrichten-Typ|Codierung|
|--|--|
|BinDa|0x0001|
|BinWeg|0x0002|
|Nachricht|0x0003|
|Bestätigung|0x0004|

#### Payload
Jedes Paket hat einen Payload dessen Länge sich aus dem **Typ** der Nachricht und dem **PayloadLength**-Feld im Header ergibt (kann auch 0 sein).  
Der Inhalt des Payloads variiert je nach Nachrichtentyp:
* **BinDa**: Liste an Enträgen der Form {IP}{Port}{Name} 
  * IP und Port jedes Eintrags sind jeweils bitcodiert, also die Zahl steht als solche direkt im Stream
  * Der Name des Teilnehmers ist in Textform mit Utf-8 Encoding codiert
  * Enträge sind jeweils durch 0-Byte voneinander getrennt
  * Anzahl an Einträgen steht im **PayloadLength**-Feld des Headers
* **BinWeg**: Kein Payload
* **Nachricht**: 
  * Payload ist der tatsächliche Inhalt der Nachricht in UTF-8 codiert
  * Anzahl an Bytes steht im **PayloadLength**-Feld des Headers
* **Bestätigung**: Kein Payload

## 3. Ablaufsemantik

### 3.1 Verbindungsaufbau (Inken & Sandra)

##### Vorbedingungen
* Die ID von mindestens einem Netzteilnehmer muss bekannt sein.
* Für den Aufbau einer Direktverbindung mit einem Netzteilnehmer muss dessen ID bekannt sein. 

##### Nachbedingungen
* Alle Netzteilnehmer haben die gleichen Netzteilnehmer in ihrer Netzteilnehmerliste.

##### Ablauf
1. Um eine direkte Verbindung zu einem Netzteilnehmer aufzubauen, wird eine Nachricht vom Typ "BinDa" an ihn geschickt.
2. Im Payload der BinDa-Nachricht wird eine Netzteilnehmerliste geschickt, die folgende Einträge enthält:
      - Der Sender steht an erster Stelle.
      - Alle Einträge der eigenen Netzteilnehmerliste, die nicht durch den Empfänger bekannt geworden sind. 
3. Der Empfänger aktualisiert beim Empfang einer "BinDa"-Nachricht seine Netzteilnehmerliste. 
      - Er vergleicht jeden Eintrag der gesendeten Netzteilnehmerliste mit seiner eigenen. 
      - Einträge, deren ID dem Empänger bekannt sind, werden ignoriert, esseidenn der übertragene Name unterscheidet sich von dem Namen in der eigenen Netzteilnehmerliste. Dann wird der Name aktualisiert. 
      - Einträge von Netzteilnehmern, die er noch nicht kennt, werden in der eigenen Netzteilnehmerliste ergänzt und der Sender der Nachricht als Vermittlungsknoten eingetragen.
      - War der Sender vorher über einen Vermittlungsknoten bekannt, wird der Netzteilnehmereintrag aktualisiert (Direktverbindung statt über den vorherigen Vermittlungsknoten).
4. Jeder Netzteilnehmer verschickt in einem Abstand von 1 Sekunde eine BinDa-Nachricht an alle Netzteilnehmer, die er über Direktverbindung kennt, woraufhin diese ihre Netzteilnehmerlisten aktualisieren. 


### 3.2 Nachrichtenaustausch (Majid & Manu)

##### Einführung
Das Versenden der Nachricht, erfolgt nach dem Prinzip „At-most-once“.
Was bedeutet, dass der Empfang der Nachricht beim Empfänger nicht gewährleistet werden kann. Wenn eine Bestätigung auf eine Nachricht folgt, ist davon auszugehen, dass die Nachricht erfolgreich übermittelt wurde. Der Sender wartet nicht auf eine Bestätigung. Es bleibt dem Nutzer  selbst überlassen, wie er mit der Bestätigungsinformation umgeht. 

##### Vorbedingungen
* Der zu erreichende Teilnehmer, muss in der Netzteilnehmerliste vorhanden sein. 
* Der zu versendende Text muss eingegeben sein. 


##### Nachbedingungen
* Auf eine erfolgreich übermittelte Nachricht erfolgt eine erfolgreich übermittelte Bestätigung. 
* Im Falle keiner Bestätigung weiß man nicht, ob die Nachricht erfolgreich übermittelt wurde.

##### Ablauf
Sobald alle Punkte der Vorbedingung erfüllt sind, wird der Header mit den zur ID passenden Empfänger-Daten (laut Netzteilnehmerliste) und Sender-Daten gefüllt. Der Nachrichtentyp wird auf (HEX: 03)‘Nachricht’ gesetzt. Die PayloadLength und Checksumme werden entsprechend des zu versendenden Payloads berechnet, wobei die PayloadLength = Byte Count des Payloads ist. Der Payload selbst, ist die in UTF-8 codierte Nachricht.


Wird eine Nachricht vom Nachrichtentyp ‘Nachricht’ oder 'Bestätigung' empfangen, überprüft der Empfänger, ob die Nachricht für ihn bestimmt ist (Ist Destination-ID der Nachricht gleich meiner ID?).
* Wenn ja und es handelt sich um den Typ 'Nachricht', wird die Source-IP und der Source-Port (ID des Senders) der empfangenen Nachricht, als Destination-IP und Destination-Port in den Header der neuen (HEX: 04) ‘Bestätigung’-Nachricht gesetzt. Als PayloadLength der ‘Bestätigung’-Nachricht wird die PayloadLength der empfangenen Nachricht gesetzt. Die eigene ID wird für Source-IP und Source-Port der Nachricht verwendet. Die Checksumme ist bis auf weiteres 0. Anhand der Netzteilnehmerliste wird die 'Bestätigung' Nachricht verschickt.
* Wenn ja und es handelt sich um den Typ 'Bestätigung' wird die dazugehörige Nachricht gesucht und auf 'bestätigt' gesetzt. 
* Wenn nein, wird die Destination-ID, welche sich aus der Destination-IP-Adresse und dem Destination-Port der empfangenen Nachricht zusammensetzt, gebildet und mit der Netzteilnehmerliste verglichen und direkt oder über einen Gateway-Socket weitergeleitet. Die Nachricht bleibt dabei unverändert.

### 3.3 Verbindungsabbau (Yasemin & Denise)

##### Vorbedingungen
* Man muss als Netztteilnehmer angemeldet sein.
* Die ID von mindestens einem direkt benachbarten Netzteilnehmer muss bekannt sein.
* Mindestens ein direkt benachbarter Netzteilnehmer muss verbunden sein.

##### Nachbedingungen
* Die Verbindung zum Netzwerk ist getrennt.
* Der eigene Eintrag wurde erfolgreich in der Netzteilnehmerliste aller Netzteilnehmer gelöscht.

##### Ablauf

**Anmerkung: Dieser Abschnitt stimmt nicht ganz mit dem abgesprochenen Verhalten überein - BINWEG Nachrichten haben keinen Payload (es wird keine Liste mitgeschickt).**

Bei Abmeldung aus dem Netzwerk wird eine 'BinWeg'-Nachricht an alle direkt benachbarten Netzteilnehmer gesendet.\
Beim Empfang der 'BinWeg'-Nachricht aktualisieren die Empfänger ihre Netzteilnehmerliste:
- Jeder Eintrag der empfangenen Liste wird mit jedem Eintrag der vorhandenen Liste verglichen.
- Sollte ein Eintrag in der eigenen Liste vorhanden sein, der in der empfangenen Liste fehlt,  
wird dieser Eintrag gelöscht.

Die Empfänger schicken dann im Zyklus ihr BinDa mit der aktualisierten Netzteilnehmerliste an ihre eigenen direkt benachbarten Netzteilnehmer.\
Alle Netzteilnehmer aktualisieren ihre Netzteilnehmerliste, in der der 'BinWeg'-Sender gestrichen ist.




## 4. Fehlersemantik

### 4.1 Wegfall eines Knotens (Marlon)

Bei Ausfall oder Abschaltung eines Knotens wird nicht automatisch die Verbindung ersetzt, falls durch den Wegfall dieses Knotens keine Verbindung mehr zu einem oder mehreren anderen Knoten besteht.  
Durch einen solchen Wegfall vom Netzwerk abgeschnittene Knoten werden im Zuge dieses Praktikums nicht behandelt, müssen also jeweils selbst eine neue Verbindung zum Netzwerk aufbauen.

### 4.2 Fehlerhafte Checksumme (Sandra)

Empfangene Pakete mit fehlerhafter Checksumme werden ignoriert. 

### 4.3 Nachrichtenbestätigung (Manuel, Sandra)

Die Payload-Länge einer Nachricht wird als Kriterium für die Bestätigung verwendet. Da verschiedene Nachrichten zufällig die gleiche Länge haben können, kann es dazu kommen, dass die falschen Nachrichten als bestätigt verstanden oder Nachrichten bestätigt werden, die nicht angekommen sind. Dieses mögliche Fehlverhalten wird von diesem Protokoll so hingenommen. 


