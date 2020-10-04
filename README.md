# Werewolfs
Online werewolfs game for children for quarantine's time.

Projekt powastaje, aby można było grać w ulubioną grę grupy wikingów z PR, czyli w wilkołaki, podczas kwarantanny i nie tylko bez wychodzenia z domu.

Architektura: klient - serwer

Serwer przeprowadza grę. Zawiera całą logikę gry itd.
Klient łączy się z serwerem i jedynie zapewnia odpowiednią interakcję z graczem podczas jego tury.

Główny wątek (serwer):
1. Serwer nasłuchuje klientów tworząc wektor klientów (id klientów od 100 w górę), w wybranym momencie kończy nasłuchiwać i zaczyna grę.
2. Wybieramy karty, które chcemy, żeby były w grze.
3. Serwer "rozdaje" karty (przypisuje losowo do karty id klienta, lub jedną z trzech pozycji na środku stołu (0, 1, 2).
4. Serwer wysyła do wszystkich klientów info z wektorem graczy (info o kartach tylko na serwerze) oraz karcie danego gracza.
5. Logika gry po kolei.
6. Po zakończeniu nocy wciskamy przycisk "Głosowanie". Serwer wysyła do klientów info, z prośbą o zagłosowanie na konkretnego gracza.
7. Serwer "liczy głosy" i wysyła info kto zginął oraz odsłania wszystkie karty (wysyła info o kartach).

Logika gry:
Serwer przechowuje 3 główne wektory: Wektor graczy (WG), wektor kart graczy na początku gry (WP) i wektor kart aktualnie (WA) i jeszcze może listę kart w grze (LK) (dla ułatwienia), jakąś tablicę kart na środku stołu (TC)

Główna funkcja:
Po kolei sprawdza czy jest dana funkcja, a potem robi odpowiednie rzeczy. Coś w stylu:

```
void play(){
	boolean insomniac = false;
	//Wywalamy insomniaca bo on na końcu
	if( LK.contains( "insomniac" ) ){
		insomniac = true;
		LK.remove( "insomniac" );
	}
	makeCopycat();
	makeWerewolfs();
	//Tu jakiś random
	Random rand = new Random();
	while( LK.length ){
		// Losujemy postać
		switch( LK[ rand.nextInt( LK.length ) ] ){
			case "witch" -> makeWitch();
			case "beholder" -> makeBeholder();
			case "Seer" -> makeSeer();
			//bez insomniaca, bo on na końcu
		}
	}
	if( insomniac )
		makeInsomniac();
}
```
Logika pojedynczej postaci:

COPYCAT:
- serwer wysyła do copycata info o jego turze, coś w stylu "COPYCAT".
- gdy klient odbierze "COPYCAT", to daje graczowi do wyboru jedną z trzech środkowych kart (UI - user interface)
- Klient wysyla do serwera wiadomośc zwrotną, którą kartę wybrał, coś w stylu "2".
- serwer wysyła do klienta info, jaką kartą się staje i podmienia u siebi odpowiednio w wektorze WP i WA.
Przykładowy kod poniżej:

```
void makeCopycat(){
	if( LK.contains( "copycat" )
		return;
	//tu logika copycata
	
	int idOfCopycat = WG[ WP.indexOf( "copycat" ) ].id;		// Uzyskujemy id gracza copycata
	
	send( idOfCopycat, "COPYCAT" );			// Wysyłamy do gracza o danym id wiadomość "COPYCAT"	(to send to ja już ogarnę)
	chosenCardId = receive( idOfCopycat );	// Odbieramy numer wybranej karty
	send( idOfCopycat, TC[ chosenCardId ];	// Wysyłamy nazwę karty którą gracz się staje
	WA[ WP.indexOf( "copycat" ) ] = TC[ chosenCardId;
	WP[ WP.indexOf( "copycat" ) ] = TC[ chosenCardId;		// Podmieniamy karty
	
	//na koniec
	LK.remove( "copycat" );
}
```
ITD.
