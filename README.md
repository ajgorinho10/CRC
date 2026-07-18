# Opis
Symulacja pracy 10 komputerów połączonych w sieć o topologii grafu. Pomiędzy wybranymi przez użytkownika węzłami można przesyłać informacje zabezpieczone kodem CRC (dowolny wielomian wprowadzany przez użytkownika).

# Założenia funkcjonalności
* Implementacja mechanizmu przesyłania wiadomości pomiędzy węzłami
* Implementacja weryfikacji poprawności wiadomości przy użyciu CRC z dowolnym wielomianem
* Umożliwienie zbierania statystyk transmisji (liczba przesłanych wiadomości,  ilość błędnych przesyłów, czas przesłania wiadomości)
* Stworzenie interfejsu graficznego
* Implementacja wstrzykiwania 3 różnych rodzajów błędów

# Narzędzia użyte do realizacji
* Język programowania – Java
* Biblioteki:
  * Lombok: adnotacje
  * JavaFX : GUI
  * ControlsFX: UI
  * FormsFX: zarządzanie formularzami
  * BootstrapFX: stylowanie CSS

# Diagram symulowanej sieci
<img width="755" height="594" alt="obraz" src="https://github.com/user-attachments/assets/c5c86d35-0bf4-48f4-b8b1-39d13aa24f88" />

# Algorytm szukania ścieżki
W celu znalezienia najkrótszej ścieżki pomiędzy dwoma komputerami
zaimplementowano algorytm “Przeszukiwania wszerz” (Breadth-first search). Algorytm ten
używany jest do znalezienia najkrótszej drogi pomiędzy danymi punktami w grafie. Jego
działanie opiera się na strategii odwiedzania wszystkich sąsiadów bieżącego węzła przed
przejściem do kolejnego poziomu zagłębienia.

# Wysyłanie wiadomości
<img width="716" height="628" alt="obraz" src="https://github.com/user-attachments/assets/4ab60d91-7a54-4d64-8ee8-8f7f3b43f78b" />

# Ustawienia programu
Karta “Ustawienia” pozwala na ręczne ustawienie parametrów używanych do
obliczania kodu CRC weryfikującego poprawność wiadomości. Dodatkowo umożliwia
wstrzykiwanie różnych błędów i badanie ich wpływu na zachowanie symulacji.

Modyfikowalne parametry CRC to:
* Polly: Dzielnik określający sposób generowania reszty.
* Szerokość: Liczba bitów sumy kontrolnej
* Init: Stan początkowy rejestru przed przetwarzaniem danych.
* Refin: Czy włączyć odbicie wejścia
* Refout: Czy odwrócić wynik przed końcowym XOR.
* XorOut: Wartość, która jest XOR-owana z wynikiem końcowym.
<img width="755" height="594" alt="obraz" src="https://github.com/user-attachments/assets/9675b181-109e-427b-97bf-bbe2eed4aefe" />

# Rodzaje błędów
Możliwe do wstrzyknięcia błędy to:
* Błąd połączenia między węzłami - wywołana metoda usuwa wskazany komputer z
mapy “topologia”. Skutkuje to zablokowaniem trasy przez dany węzeł. W przypadku,
gdy zablokowany węzeł jest jedyną drogą do celu przesłanie danych kończy się
niepowodzeniem (wyjątek NoWay)
* Błąd weryfikacji CRC między węzłami - wybrany węzeł celowo zmienia sumę CRC.
Powoduje to niezgodność z przesłaną sumą kontrolną (wyjątek BadCrc) i inicjuje
retransmisję
* Błąd przesyłania wiadomości - wybrany węzeł celowo zmienia zawartość
wiadomości. Odbiorca po obliczeniu sumy kontrolnej wykrywa błąd (wyjątek
BadMessage) i inicjuje retransmisję
