# Roadmap — Dragon Block: Tenkaichi

Stato aggiornato all'11 ottobre 2026. "Fatto" significa: codice presente, integrato, compilato su
entrambi i loader e verificato nei limiti indicati in `TESTING.md`.

## Fase 1 — Fondamenta (completata)
- [x] Repository multi-loader (common / fabric / neoforge / build-logic)
- [x] Toolchain verificata (Java 25, Gradle 9.7.1, Loom 1.18.4, MDG 2.0.148)
- [x] Registrazioni (entità, oggetti, suoni, tab creativa) su entrambi i loader
- [x] Networking astratto con validazione e rate limit
- [x] Persistenza con allegati e schema versionato
- [x] Configurazioni versionate con migrazioni e backup
- [x] Test automatici (JUnit) + smoke test su server dedicato per entrambi i loader

## Fase 2 — Personaggio (vertical slice)
- [x] Character creator con anteprima 3D ruotabile/zoomabile
- [x] Tutte le 10 razze giocabili con passive reali, aspetto, modelli 3D e skin HD dedicati
- [x] 12 statistiche persistenti, TP, limiti da Potenziale, pratica per statistica
- [x] Battle Power con condizione, forma, soppressione
- [ ] Altezza: applicata via attributo `scale`; editor espressioni/voce minimale

## Fase 3 — Combattimento (vertical slice)
- [x] Combo leggere (5 colpi), pesanti contestuali (launcher, smash, guard break, rush), counter
- [x] Parata direzionale, perfect block con contrattacco, guard break per stamina
- [x] Schivata con i-frame, vanish reattivo, teleport alle spalle con lock-on, dash
- [x] Clash corpo a corpo, knockback direzionale, knockdown, recupero aereo
- [x] Lock-on con cambio bersaglio, perdita di linea di vista, reticolo HUD
- [x] Volo (hover, normale, supersonico) con predizione client e validazione server
- [ ] Inseguimento automatico dopo lancio, attacchi sincronizzati contro boss

## Fase 4 — Ki (vertical slice)
- [x] Carica, rigenerazione, esaurimento, costi ridotti dal Controllo Ki
- [x] Aura 3D procedurale con qualità grafica
- [x] Ki Blast, Kamehameha (carica, sterzata, penetrazione, trincee), Raider Cannon (NPC)
- [x] Beam Clash autoritativo con battiti a tempo, stagger, intervento esterno
- [x] Distruzione ambientale configurabile
- [ ] Altre tecniche (Galick Gun, Masenko, Destructo Disc, Special Beam Cannon, Spirit Bomb…)

## Fase 5 — Trasformazioni
- [x] Architettura data-driven via datapack, sincronizzata ai client
- [x] Super Saiyan con sblocco per rabbia, cinematica alla prima attivazione, padronanza
- [ ] Gradi SS, SS2/3/4, God, Blue, UI/UE e percorsi razziali

## Fase 6 — NPC e Boss
- [x] Maestro di arti marziali (allenatore) e Predone Saiyan con FSM gerarchica (10 stati, seconda fase)
- [ ] Boss multi-fase canonici, spawn naturale, scaling avanzato

## Fasi 7–10
Non iniziate: dimensioni e strutture, Sfere del Drago, story mode, fusioni, tornei, ottimizzazione
avanzata e benchmark. Vedi `FEATURES.md` per l'elenco completo dei requisiti.
