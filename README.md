# Wilkołaki

## Opis gry
Wilkołaki to wieloosobowa gra podobna do popularnej "Mafii", z tym że każdy ma swoją własną rolę, a cała akcja dzieje się podczas jednej doby. Niniejszy projekt jest przełożeniem tejże gry na wersję online.

## Instalacja
Instalator na Windows'a lub spakowane pliki gry (jeśli mamy Mac'a albo Linux'a) można pobrać z [zakładki *releases*](https://github.com/mmakos/Werewolves/releases). Następnie należy zainstalować grę podążając za wskazówkami instalatora. Dodatkowo konieczne jest posiadanie JRE (Java Runtine Environment) w wersji 8 - można je pobrać z <https://java.com/pl/download>.

## Uruchomienie

### Klient (gracz)
Aby dołączyć do gry jako klient, należy uruchomić aplikację **Werewolves.exe**. W pole *ip* należy wpisać adres IP serwera do którego chcemy dołączyć (podany przez administratora gry). W pole *port* należy wpisać numer portu, na którym serwer udostępnia usługę, czyli domyślnie **23000**. Po wciśnięciu przycisku *login*, allikacja wygląda jakby się zawiesiła, ale po prostu oczekuje na rozpoczęcie gry przez administratora.

### Serwer (administrator gry)
Tu sprawa jest bardziej skomplikowana. Ja nie udostępniam żadnego serwera, więc aby zorganizować grę, należy na swoim komputerze postawić serwer. Jeśli stawialiscie kiedyś serwery w Minecrafcie to można skorzystać z jakichś darmowych VPN-ów typu Hamachi. Najlepiej jednak po prostu zalogować się do routera, wpisując w przeglądarkę adres routera, czyli swoje IP lub lokalny adres IP z końcówką *1*, czyli albo sprawdzamy swoje IP np. na stronie <https://www.moje-ip.eu/> i wpisujemy go jako adres internetowy w przeglądarce albo sprawdzamy lokalny adres komputera wpisując w *cmd* (command line) plecenie *ipconfig* i jeżeli adres komputera jest np. *192.168.100.11* to wpisujemy w przeglądarce *192.168.100.**1*** - adres braby domyślnej. Następnie należy na routerze ustawić przekierowanie portu (będzie to pocja *Port Forwarding Rules*, *forwarding* lub coś z serwerem). Na kżdym routerze wugląda to inaczej ale ważne żeby porty usawić na *23000* a adres IP na nasze IP (jak na poniższym screenie). A w ogóle najlepiej to znaleźć jakiś tutorial na YT jak postawić serwer na routerze.<br><br>
Następnie należy uruchomić aplikację *SerWerewolves.exe* oraz wcisnąć *Run Server*. Kiedy połączy się zadowalająca nas liczba graczy (gracze pojawiają nam się w okienku), wciskamy *Start game*, wybieramy karty i rozpoczynamy grę.

![Server instruction](https://lh3.googleusercontent.com/fife/ABSRlIoENTgQX3nmef-2z-g4rvtSfd1RXM6gb59A_8QcYkkE-DjHtih8teSC-TzmEApKg689wyxtInuwMUPCfK6a8LzXwt_EKq2RzjEFdbsRP-KH-iqyliEVM70zr8hndDKz-oYXTr3ajcrDKveG1YLvZwdd0JlTh3nCHOqYZHM1EmVVlY289Di0fdyjI3p1AQGqFCPFyILbw4wAf1rhFAWPjjQo6YN_Q1Cpgr53ahAZOLrJaj1CiOhFXY3K_yPzuEBReFKDSoAmqbNoa9EA4HbhxaHohd4yuIOO-kyROnurJLrm7f6xqI32IicixtYHz-tS796ZBofM5rn8UkGhcMCrRY3AiOMHwBZYQkexYUkxe-qY2u6J5Wl2ClBFLDUpfDxW4dr-F-nuqUzGvN7dvg_-1iZ8W2F2BNGNPP-_m1kCs4pqzqmuinV00cnTtPuZuKc537889eeIaj_y5yYFqKctHie-N0i6coUb29TEgt8cJo28GnESaRTIi-Kp7tnp2SakCTHp2XjgrwN8dzE-CvfAouoeK5VKBI8jUqoRGR6WtcoTrXN_CrCAvb1umkGhPig79oiiHBOeuQ1Ty6lznLUsWkVLuJ6p2Wsk4HPIl5uwhyrsWd15gFgvRhL-qWQM53UXhP3Y5GpU1FPKLTPMyTPAJAqtNqoTc8mMrIoNoyQYWcKTlSIrkngYznA8eH0_I0KjopEI6k3L7mh43kAK0ExLSx9_spjeEMEMBg=w1600-h828-ft)

## Rozgrywka
W trakcie nocy czekaj na swoją kolej oraz wykonuj polecenia. Kiedy noc się skończy w celu ustalenia kto jest kim, trzeba połączyć się z graczami przez jakiegoś zooma czy coś. Następnie administrator włącza głosowanie i możemy głosować.
